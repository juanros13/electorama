-- =====================================================================
-- Pruebas de V3: niveles de estructura por campaña. Transacción con ROLLBACK: no deja datos.
--   psql -d electorama -v ON_ERROR_STOP=1 -f infra/db/tests/T8__campaign_levels.sql
-- Cada caso imprime OK o aborta con FALLO.
-- =====================================================================
BEGIN;
SET LOCAL search_path TO structure, public;

\echo '=== CASO 1: las campañas existentes quedaron con sus tres niveles ==='
DO $$ BEGIN
  IF (SELECT count(*) FROM campaign c
       WHERE (SELECT count(*) FROM campaign_level l WHERE l.campaign_id = c.id) <> 3) > 0 THEN
    RAISE EXCEPTION 'FALLO: hay campañas sin sus tres niveles';
  END IF;
  IF EXISTS (SELECT 1 FROM position WHERE level_id IS NULL) THEN
    RAISE EXCEPTION 'FALLO: hay puestos sin nivel';
  END IF;
  IF EXISTS (SELECT 1 FROM position p JOIN campaign_level l ON l.id = p.level_id
              WHERE l.level_function IS DISTINCT FROM p.level) THEN
    RAISE EXCEPTION 'FALLO: hay puestos cuyo nivel no coincide con su función';
  END IF;
  RAISE NOTICE 'OK: niveles heredados completos';
END $$;

\echo '=== CASO 2: una campaña de cuatro niveles (estatal > municipal > promotor > movilizador) ==='
INSERT INTO campaign (name, type, year, territory_id, status, start_date, end_date)
SELECT 'T8 Prueba', 'STATE', 2030, t.id, 'PLANNING', DATE '2029-09-01', DATE '2030-06-30'
  FROM territory t WHERE t.type = 'STATE' AND t.code = '27';
INSERT INTO structure (campaign_id, name, type)
SELECT id, 'T8 Principal', 'MAIN' FROM campaign WHERE name = 'T8 Prueba';
INSERT INTO campaign_level (campaign_id, rank, code, name_singular, name_plural, territory_type,
                            default_goal, level_function, role_name)
SELECT c.id, v.* FROM campaign c CROSS JOIN (VALUES
  (1::smallint, 'ESTATAL', 'Coordinador estatal', 'Coordinadores estatales', 'STATE', NULL::int, NULL, 'T8_ESTATAL'),
  (2::smallint, 'MUNICIPAL', 'Coordinador municipal', 'Coordinadores municipales', 'MUNICIPALITY', NULL, 'REFERENT', 'T8_MUNICIPAL'),
  (3::smallint, 'PROMOTOR', 'Promotor', 'Promotores', 'SECTION', 200, 'ZONE_COORDINATOR', 'T8_PROMOTOR'),
  (4::smallint, 'MOVILIZADOR', 'Movilizador', 'Movilizadores', 'SECTION', 20, 'PROMOTER', 'T8_MOVILIZADOR')
) AS v WHERE c.name = 'T8 Prueba';

INSERT INTO position (structure_id, level_id, territory_id, code)
SELECT s.id, l.id, c.territory_id, 'T8-EST'
  FROM structure s JOIN campaign c ON c.id = s.campaign_id
  JOIN campaign_level l ON l.campaign_id = c.id AND l.rank = 1
 WHERE s.name = 'T8 Principal';
INSERT INTO position (structure_id, level_id, parent_position_id, territory_id, code)
SELECT e.structure_id, l.id, e.id, m.id, 'T8-MUN'
  FROM position e
  JOIN structure s ON s.id = e.structure_id
  JOIN campaign_level l ON l.campaign_id = s.campaign_id AND l.rank = 2
  JOIN territory m ON m.type = 'MUNICIPALITY' AND m.code = '27-004'
 WHERE e.code = 'T8-EST';
INSERT INTO position (structure_id, level_id, parent_position_id, territory_id, code, goal)
SELECT r.structure_id, l.id, r.id, sec.id, 'T8-CZ', 200
  FROM position r
  JOIN structure s ON s.id = r.structure_id
  JOIN campaign_level l ON l.campaign_id = s.campaign_id AND l.rank = 3
  JOIN LATERAL (SELECT id FROM territory WHERE parent_territory_id = r.territory_id
                 AND type = 'SECTION' ORDER BY id LIMIT 1) sec ON true
 WHERE r.code = 'T8-MUN';
-- éste entra sólo con la función, como lo hacen las funciones compat
INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal)
SELECT cz.structure_id, 'PROMOTER', cz.id, cz.territory_id, 'T8-MOV', 20
  FROM position cz WHERE cz.code = 'T8-CZ';

DO $$
DECLARE n int; v_key text; v_level text; v_rank int;
BEGIN
  SELECT count(*) INTO n FROM position WHERE code LIKE 'T8-%';
  IF n <> 4 THEN RAISE EXCEPTION 'FALLO: se esperaban 4 puestos, hay %', n; END IF;
  SELECT p.level INTO v_level FROM position p WHERE p.code = 'T8-MUN';
  IF v_level <> 'REFERENT' THEN RAISE EXCEPTION 'FALLO: el municipal no tomó la función REFERENT'; END IF;
  SELECT p.level INTO v_level FROM position p WHERE p.code = 'T8-EST';
  IF v_level IS NOT NULL THEN RAISE EXCEPTION 'FALLO: el estatal no debía tener función'; END IF;
  SELECT l.rank INTO v_rank FROM position p JOIN campaign_level l ON l.id = p.level_id WHERE p.code = 'T8-MOV';
  IF v_rank <> 4 THEN RAISE EXCEPTION 'FALLO: el movilizador no resolvió su nivel por función'; END IF;
  SELECT section_key INTO v_key FROM position WHERE code = 'T8-CZ';
  IF v_key IS NULL OR v_key !~ 'A1$' THEN RAISE EXCEPTION 'FALLO: el promotor no recibió clave de sección (%)', v_key; END IF;
  SELECT section_key INTO v_key FROM position WHERE code = 'T8-MOV';
  IF v_key IS NULL OR v_key !~ '-01$' THEN RAISE EXCEPTION 'FALLO: el movilizador no recibió clave (%)', v_key; END IF;
  RAISE NOTICE 'OK: cuatro niveles, funciones y claves de sección';
END $$;

\echo '=== CASO 3: lo que se debe rechazar ==='
DO $$ BEGIN
  BEGIN
    -- un municipal sin padre: sólo la cúspide va suelta
    INSERT INTO position (structure_id, level_id, territory_id, code)
    SELECT s.id, l.id, m.id, 'T8-X1' FROM structure s
      JOIN campaign_level l ON l.campaign_id = s.campaign_id AND l.rank = 2
      JOIN territory m ON m.type = 'MUNICIPALITY' AND m.code = '27-009'
     WHERE s.name = 'T8 Principal';
    RAISE EXCEPTION 'FALLO: permitió un municipal sin padre';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: municipal sin padre (%)', SQLERRM;
  END;

  BEGIN
    -- un promotor colgando del movilizador (padre de rango mayor)
    INSERT INTO position (structure_id, level_id, parent_position_id, territory_id, code)
    SELECT mov.structure_id, l.id, mov.id, mov.territory_id, 'T8-X2'
      FROM position mov JOIN structure s ON s.id = mov.structure_id
      JOIN campaign_level l ON l.campaign_id = s.campaign_id AND l.rank = 3
     WHERE mov.code = 'T8-MOV';
    RAISE EXCEPTION 'FALLO: permitió colgar de un nivel inferior';
  EXCEPTION WHEN raise_exception THEN
    IF SQLERRM LIKE 'FALLO%' THEN RAISE; END IF;
    RAISE NOTICE 'OK: padre de nivel inferior (%)', SQLERRM;
  END;

  BEGIN
    -- un promotor sobre municipio cuando su nivel dice sección
    INSERT INTO position (structure_id, level_id, parent_position_id, territory_id, code)
    SELECT r.structure_id, l.id, r.id, r.territory_id, 'T8-X3'
      FROM position r JOIN structure s ON s.id = r.structure_id
      JOIN campaign_level l ON l.campaign_id = s.campaign_id AND l.rank = 3
     WHERE r.code = 'T8-MUN';
    RAISE EXCEPTION 'FALLO: permitió un territorio de otro tipo';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: territorio equivocado (%)', SQLERRM;
  END;

  BEGIN
    -- un nivel de otra campaña
    INSERT INTO position (structure_id, level_id, parent_position_id, territory_id, code)
    SELECT r.structure_id, (SELECT id FROM campaign_level WHERE campaign_id <> s.campaign_id LIMIT 1),
           r.id, r.territory_id, 'T8-X4'
      FROM position r JOIN structure s ON s.id = r.structure_id WHERE r.code = 'T8-MUN';
    RAISE EXCEPTION 'FALLO: permitió un nivel de otra campaña';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: nivel de otra campaña (%)', SQLERRM;
  END;
END $$;

\echo '=== CASO 4: un nivel con puestos no se borra ni cambia de forma ==='
DO $$ BEGIN
  BEGIN
    DELETE FROM campaign_level l USING campaign c
     WHERE c.id = l.campaign_id AND c.name = 'T8 Prueba' AND l.rank = 4;
    RAISE EXCEPTION 'FALLO: borró un nivel con puestos';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: no se borra (%)', SQLERRM;
  END;

  BEGIN
    UPDATE campaign_level l SET territory_type = 'MUNICIPALITY' FROM campaign c
     WHERE c.id = l.campaign_id AND c.name = 'T8 Prueba' AND l.rank = 4;
    RAISE EXCEPTION 'FALLO: cambió el territorio de un nivel con puestos';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: no cambia de forma (%)', SQLERRM;
  END;

  UPDATE campaign_level l SET name_singular = 'Activista', name_plural = 'Activistas', default_goal = 25
    FROM campaign c WHERE c.id = l.campaign_id AND c.name = 'T8 Prueba' AND l.rank = 4;
  RAISE NOTICE 'OK: el nombre y la meta sí se editan';
END $$;

\echo '=== CASO 5: un nivel sin puestos se reordena en una transacción ==='
DO $$
DECLARE v_campaign bigint;
BEGIN
  INSERT INTO campaign (name, type, status) VALUES ('T8 Vacía', 'INTERNAL', 'PLANNING') RETURNING id INTO v_campaign;
  INSERT INTO campaign_level (campaign_id, rank, code, name_singular, name_plural, role_name)
  VALUES (v_campaign, 1, 'A', 'A', 'As', 'T8_A'), (v_campaign, 2, 'B', 'B', 'Bs', 'T8_B');
  SET CONSTRAINTS campaign_level_rank_uq DEFERRED;
  UPDATE campaign_level SET rank = 3 - rank WHERE campaign_id = v_campaign;
  SET CONSTRAINTS campaign_level_rank_uq IMMEDIATE;
  IF (SELECT code FROM campaign_level WHERE campaign_id = v_campaign AND rank = 1) <> 'B' THEN
    RAISE EXCEPTION 'FALLO: no se reordenó';
  END IF;
  RAISE NOTICE 'OK: intercambio de rangos';
END $$;

\echo '=== CASO 6: registro de roles y menú ==='
DO $$ BEGIN
  IF (SELECT count(*) FROM users.structure_role) < 5 THEN
    RAISE EXCEPTION 'FALLO: faltan los roles heredados en users.structure_role';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM users.menus_groups g JOIN users.menus_roles r ON r.menu_group_id = g.pk
                  WHERE g.link = '/admin/structure/campaigns' AND r.role = 'ADMIN') THEN
    RAISE EXCEPTION 'FALLO: falta el menú de campañas para ADMIN';
  END IF;
  RAISE NOTICE 'OK: roles y menú';
END $$;

ROLLBACK;
