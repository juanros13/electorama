-- =====================================================================
-- Pruebas de V7/V8: lo que queda de la capa compat del flujo 2024 (la marca de
-- voto del día de la elección) apunta a la campaña activa, y lo demás ya no existe.
-- Transacción con ROLLBACK: no deja datos.
--   psql -d electorama -v ON_ERROR_STOP=1 -f infra/db/tests/T9__compat_campaign.sql
-- Supone Tabasco 2024 cerrada y Tabasco 2027 activa.
-- Cada caso imprime OK o aborta con FALLO.
-- =====================================================================
BEGIN;
SET LOCAL search_path TO structure, public;

-- una sección de Tabasco, su municipio y las estructuras de la campaña activa
CREATE TEMP TABLE t9 AS
SELECT sec.id AS sec_terr, sec.section_geo_id AS section, mun.id AS mun_terr,
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

\echo '=== CASO 2: position_for_promoter encuentra al promotor de la campaña activa ==='
DO $$
DECLARE
  v_ref  bigint;
  v_cz   bigint;
  v_prom bigint;
BEGIN
  INSERT INTO position (structure_id, level, territory_id, code)
  SELECT main, 'REFERENT', mun_terr, 'T9|REF' FROM t9 RETURNING id INTO v_ref;
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, code)
  SELECT main, 'ZONE_COORDINATOR', v_ref, sec_terr, 'T9|CZ' FROM t9 RETURNING id INTO v_cz;
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, code)
  SELECT main, 'PROMOTER', v_cz, sec_terr, 'T9|PROM' FROM t9 RETURNING id INTO v_prom;
  INSERT INTO person (auth_id, legacy_source, created) VALUES ('t9-prom', 'keycloak', now());
  INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
  SELECT v_prom, pe.id, 'HOLDER', 't9-prom', tstzrange(now(), NULL), 'T9'
    FROM person pe WHERE pe.auth_id = 't9-prom';

  IF followers_compat.position_for_promoter('t9-prom', false, (SELECT section FROM t9)) IS DISTINCT FROM v_prom THEN
    RAISE EXCEPTION 'FALLO: position_for_promoter no encuentra al promotor de la campaña activa';
  END IF;
  RAISE NOTICE 'OK: promotor % en la campaña activa', v_prom;
END $$;

\echo '=== CASO 3: lo del flujo 2024 que ya no se usa no existe (V8) ==='
DO $$ BEGIN
  IF to_regclass('users.users_seccions') IS NOT NULL
     OR to_regclass('followers.followers_legacy') IS NOT NULL
     OR to_regclass('followers.followers_compat') IS NOT NULL
     OR to_regnamespace('users_compat') IS NOT NULL
     OR to_regprocedure('structure_compat.user_section_add(varchar,integer,integer,boolean,integer,integer)') IS NOT NULL
     OR to_regprocedure('followers_compat.followers_insert()') IS NOT NULL
     OR to_regprocedure('followers_compat.promoters_insert()') IS NOT NULL THEN
    RAISE EXCEPTION 'FALLO: quedan objetos de la capa compat que V8 debía borrar';
  END IF;
  IF to_regclass('followers.followers') IS NULL OR to_regclass('followers.promoters') IS NULL
     OR to_regprocedure('followers_compat.followers_update()') IS NULL THEN
    RAISE EXCEPTION 'FALLO: falta lo que usa el día de la elección';
  END IF;
  RAISE NOTICE 'OK: sólo queda la lectura y la marca de voto';
END $$;

ROLLBACK;
