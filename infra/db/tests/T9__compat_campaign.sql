-- =====================================================================
-- Pruebas de V7: las vistas compat escriben en la campaña activa de la sección.
-- Transacción con ROLLBACK: no deja datos.
--   psql -d electorama -v ON_ERROR_STOP=1 -f infra/db/tests/T9__compat_campaign.sql
-- Supone Tabasco 2024 cerrada, Tabasco 2027 activa y Veracruz 2027 sin activar.
-- Cada caso imprime OK o aborta con FALLO.
-- =====================================================================
BEGIN;
SET LOCAL search_path TO structure, public;

-- una sección de Tabasco, su municipio y las estructuras de la campaña activa
CREATE TEMP TABLE t9 AS
SELECT sec.id AS sec_terr, sec.section_geo_id AS section, mun.id AS mun_terr,
       mun.municipality_geo_id AS municipality,
       (SELECT s.id FROM structure s JOIN campaign c ON c.id = s.campaign_id
         WHERE c.status = 'ACTIVE' AND c.territory_id = st.id AND s.type = 'MAIN') AS main,
       (SELECT s.id FROM structure s JOIN campaign c ON c.id = s.campaign_id
         WHERE c.status = 'ACTIVE' AND c.territory_id = st.id AND s.type = 'ALTERNATE') AS alternate
  FROM territory st
  JOIN territory mun ON mun.parent_territory_id = st.id AND mun.type = 'MUNICIPALITY'
  JOIN territory sec ON sec.parent_territory_id = mun.id AND sec.type = 'SECTION' AND sec.valid_to IS NULL
 WHERE st.type = 'STATE' AND st.state_geo_id = 27
 ORDER BY sec.id LIMIT 1;

\echo '=== CASO 1: la estructura es la de la campaña activa, no la primera ==='
DO $$ BEGIN
  IF (SELECT main FROM t9) IS NULL OR (SELECT alternate FROM t9) IS NULL THEN
    RAISE EXCEPTION 'FALLO: Tabasco no tiene campaña activa con las dos ramas';
  END IF;
  IF active_structure((SELECT sec_terr FROM t9), false) <> (SELECT main FROM t9)
     OR active_structure((SELECT sec_terr FROM t9), true) <> (SELECT alternate FROM t9) THEN
    RAISE EXCEPTION 'FALLO: active_structure no da la campaña activa';
  END IF;
  RAISE NOTICE 'OK: estructuras % / %', (SELECT main FROM t9), (SELECT alternate FROM t9);
END $$;

\echo '=== CASO 2: sin referente conocido, el coordinador cuelga del referente del municipio ==='
INSERT INTO position (structure_id, level, territory_id, code)
SELECT main, 'REFERENT', mun_terr, 'T9|REF' FROM t9;
INSERT INTO users.users_seccions (user_auth_id, seccion_geo_id, municipality_id)
SELECT 't9-cz', section, municipality FROM t9;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM position p JOIN assignment a ON a.position_id = p.id
                  WHERE a.auth_id = 't9-cz' AND p.level = 'ZONE_COORDINATOR'
                    AND p.structure_id = (SELECT main FROM t9)
                    AND p.parent_position_id = (SELECT id FROM position WHERE code = 'T9|REF')) THEN
    RAISE EXCEPTION 'FALLO: el coordinador no quedó en la campaña activa bajo el referente';
  END IF;
  RAISE NOTICE 'OK: alta por users_seccions en la campaña activa';
END $$;

\echo '=== CASO 3: la misma alta otra vez no duplica ==='
INSERT INTO users.users_seccions (user_auth_id, seccion_geo_id, municipality_id)
SELECT 't9-cz', section, municipality FROM t9;
DO $$ BEGIN
  IF (SELECT count(*) FROM assignment a JOIN position p ON p.id = a.position_id
       WHERE a.auth_id = 't9-cz' AND p.level = 'ZONE_COORDINATOR') <> 1 THEN
    RAISE EXCEPTION 'FALLO: se duplicó el puesto del coordinador';
  END IF;
  RAISE NOTICE 'OK: sin duplicado';
END $$;

\echo '=== CASO 4: alterna sin referente en su rama es un error claro; con él, va a la alterna ==='
DO $$ BEGIN
  BEGIN
    PERFORM structure_compat.user_section_add('t9-alt', (SELECT section FROM t9),
                                              (SELECT municipality FROM t9), true);
    RAISE EXCEPTION 'FALLO: aceptó un coordinador alterno sin puesto superior';
  EXCEPTION WHEN check_violation THEN
    RAISE NOTICE 'OK: %', SQLERRM;
  END;
END $$;
INSERT INTO position (structure_id, level, territory_id, code)
SELECT alternate, 'REFERENT', mun_terr, 'T9|REF-ALT' FROM t9;
SELECT structure_compat.user_section_add('t9-alt', section, municipality, true) IS NOT NULL AS alta FROM t9;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM position p JOIN assignment a ON a.position_id = p.id
                  WHERE a.auth_id = 't9-alt' AND p.structure_id = (SELECT alternate FROM t9)) THEN
    RAISE EXCEPTION 'FALLO: el alterno no quedó en la estructura alterna activa';
  END IF;
  RAISE NOTICE 'OK: alterna en la campaña activa';
END $$;

\echo '=== CASO 5: followers_compat encuentra coordinador y promotor en la campaña activa ==='
-- (el INSERT por followers.promoters ya no se usa: escribe en promoters_legacy, que se tiró en la migración)
DO $$
DECLARE
  v_cz   bigint;
  v_prom bigint;
BEGIN
  v_cz := followers_compat.position_for_coordinator('t9-cz', NULL, (SELECT section FROM t9),
                                                    (SELECT municipality FROM t9), false);
  IF v_cz IS DISTINCT FROM (SELECT p.id FROM position p JOIN assignment a ON a.position_id = p.id
                             WHERE a.auth_id = 't9-cz' AND p.level = 'ZONE_COORDINATOR') THEN
    RAISE EXCEPTION 'FALLO: position_for_coordinator no reusa el puesto de la campaña activa';
  END IF;

  INSERT INTO position (structure_id, level, parent_position_id, territory_id, code)
  SELECT main, 'PROMOTER', v_cz, sec_terr, 'T9|PROM' FROM t9 RETURNING id INTO v_prom;
  INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
  SELECT v_prom, pe.id, 'HOLDER', 't9-prom', tstzrange(now(), NULL), 'T9'
    FROM person pe WHERE pe.auth_id = 't9-cz';
  IF followers_compat.position_for_promoter('t9-prom', false, (SELECT section FROM t9)) IS DISTINCT FROM v_prom THEN
    RAISE EXCEPTION 'FALLO: position_for_promoter no encuentra al promotor de la campaña activa';
  END IF;
  RAISE NOTICE 'OK: coordinador % y promotor % en la campaña activa', v_cz, v_prom;
END $$;

\echo '=== CASO 6: una sección sin campaña activa (Veracruz en PLANNING) se rechaza ==='
DO $$
DECLARE v_section integer;
BEGIN
  SELECT t.section_geo_id INTO v_section FROM territory t
   WHERE t.type = 'SECTION' AND t.state_geo_id = 30 AND t.valid_to IS NULL
     AND NOT EXISTS (SELECT 1 FROM campaign c
                      WHERE c.status = 'ACTIVE' AND territory_contained_in(t.id, c.territory_id))
   LIMIT 1;
  IF v_section IS NULL THEN
    RAISE NOTICE 'OK (omitido): todas las secciones de Veracruz tienen campaña activa';
    RETURN;
  END IF;
  BEGIN
    INSERT INTO users.users_seccions (user_auth_id, seccion_geo_id) VALUES ('t9-vz', v_section);
    RAISE EXCEPTION 'FALLO: aceptó una sección sin campaña activa';
  EXCEPTION WHEN check_violation THEN
    RAISE NOTICE 'OK: %', SQLERRM;
  END;
END $$;

ROLLBACK;
