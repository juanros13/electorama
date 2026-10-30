-- =====================================================================
-- Pruebas de armado de la estructura 2027 (lo que hace BuildService),
-- contra la base real. Transacción con ROLLBACK: no deja datos.
--   psql -U postgres -d msvc_dash_structure -f T6__build_tests.sql
-- =====================================================================
BEGIN;

\echo '=== CASO 1: referente → promotor → movilizador en la Principal 2027 ==='
INSERT INTO position (structure_id, level, territory_id, code, goal)
SELECT s.id, 'REFERENT', m.id, 'T6-REF', 5000
  FROM structure s
  JOIN campaign c ON c.id = s.campaign_id AND c.status = 'ACTIVE'
  JOIN territory m ON m.type = 'MUNICIPALITY' AND m.code = '27-004' AND m.valid_to IS NULL
 WHERE s.type = 'MAIN';

INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal)
SELECT r.structure_id, 'ZONE_COORDINATOR', r.id, sec.id, 'T6-CZ', 200
  FROM position r
  JOIN territory sec ON sec.parent_territory_id = r.territory_id AND sec.type = 'SECTION'
                    AND sec.valid_to IS NULL
 WHERE r.code = 'T6-REF'
 ORDER BY sec.id LIMIT 1;

INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal)
SELECT cz.structure_id, 'PROMOTER', cz.id, cz.territory_id, 'T6-MOV', 20
  FROM position cz WHERE cz.code = 'T6-CZ';

SELECT p.code, p.level, t.code AS territorio, par.code AS cuelga_de
  FROM position p
  JOIN territory t ON t.id = p.territory_id
  LEFT JOIN position par ON par.id = p.parent_position_id
 WHERE p.code LIKE 'T6-%'
 ORDER BY p.id;

\echo '-- 1a. un promotor fuera del municipio de su referente:'
SAVEPOINT s1;
DO $$ BEGIN
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal)
  SELECT r.structure_id, 'ZONE_COORDINATOR', r.id, sec.id, 'T6-CZ-FUERA', 200
    FROM position r
    JOIN territory otro ON otro.type = 'MUNICIPALITY' AND otro.code = '27-009'
    JOIN territory sec ON sec.parent_territory_id = otro.id AND sec.type = 'SECTION'
   WHERE r.code = 'T6-REF'
   LIMIT 1;
  RAISE EXCEPTION 'FALLO: permitió un promotor fuera del municipio';
EXCEPTION WHEN raise_exception THEN
  IF SQLERRM LIKE 'FALLO%' THEN RAISE; END IF;
  RAISE NOTICE 'OK: rechazado (%)', SQLERRM;
END $$;
ROLLBACK TO s1;

\echo '-- 1b. un titular sin cuenta (NONE) se puede nombrar:'
INSERT INTO assignment (position_id, person_id, role)
SELECT p.id, (SELECT id FROM person ORDER BY id LIMIT 1), 'HOLDER' FROM position p WHERE p.code = 'T6-CZ';
SELECT count(*) AS titulares_t6 FROM assignment a JOIN position p ON p.id = a.position_id
 WHERE p.code LIKE 'T6-%' AND upper_inf(a.validity);

\echo ''
\echo '=== CASO 2: en la estructura 2024 no se crean puestos ==='
SAVEPOINT s2;
DO $$ BEGIN
  INSERT INTO position (structure_id, level, territory_id, code)
  SELECT s.id, 'REFERENT', m.id, 'T6-REF-2024'
    FROM structure s
    JOIN campaign c ON c.id = s.campaign_id AND c.status = 'CLOSED'
    JOIN territory m ON m.type = 'MUNICIPALITY' AND m.code = '27-004'
   WHERE s.type = 'MAIN';
  RAISE EXCEPTION 'FALLO: permitió crear un puesto en 2024';
EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: rechazado (check_violation)';
END $$;
ROLLBACK TO s2;

ROLLBACK;
