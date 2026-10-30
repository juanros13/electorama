-- =====================================================================
-- Pruebas de las reglas duras del modelo. Se ejecuta dentro de una
-- transacción y termina con ROLLBACK: no deja datos.
--   psql -U postgres -d msvc_dash_structure -f T1__rule_tests.sql
-- =====================================================================
BEGIN;

-- --- montaje mínimo -------------------------------------------------
INSERT INTO territory (type, code, name) VALUES ('STATE','TEST-27','Tabasco (prueba)');
INSERT INTO territory (type, parent_territory_id, code, name)
  SELECT 'MUNICIPALITY', id, 'TEST-27-004', 'Centro' FROM territory WHERE code='TEST-27';
INSERT INTO territory (type, parent_territory_id, code, name)
  SELECT 'SECTION', id, 'TEST-30301', 'Sección 30301' FROM territory WHERE code='TEST-27-004';
INSERT INTO territory (type, parent_territory_id, code, name)
  SELECT 'FRAGMENT', id, 'TEST-30301-a', 'Sección 30301 parte A' FROM territory WHERE code='TEST-30301';
INSERT INTO territory (type, parent_territory_id, code, name)
  SELECT 'FRAGMENT', id, 'TEST-30301-b', 'Sección 30301 parte B' FROM territory WHERE code='TEST-30301';
-- un municipio ajeno, para probar el aislamiento territorial
INSERT INTO territory (type, parent_territory_id, code, name)
  SELECT 'MUNICIPALITY', id, 'TEST-27-009', 'Jonuta' FROM territory WHERE code='TEST-27';

INSERT INTO person (names, last_name) VALUES
  ('T_ALEJANDRA','M'), ('T_COORD_A','X'), ('T_COORD_B','Y'), ('T_JUAN','P'), ('T_PEDRO','Q'),
  ('T_ANDY','R'), ('T_MEMBER_ONE','S'), ('T_MEMBER_TWO','T');

INSERT INTO campaign (name, type, year, status, territory_id)
  SELECT 'Prueba 2027','STATE',2027,'ACTIVE', id FROM territory WHERE code='TEST-27';
INSERT INTO structure (campaign_id, name, type, owner_person_id)
  SELECT c.id,'Principal (prueba)','MAIN', p.id FROM campaign c, person p
  WHERE c.name='Prueba 2027' AND p.names='T_ALEJANDRA';
INSERT INTO structure (campaign_id, name, type)
  SELECT id,'Aliada CNC (prueba)','ALLIED' FROM campaign WHERE name='Prueba 2027';

-- referente sobre el municipio
INSERT INTO position (structure_id, level, territory_id, code, goal)
  SELECT s.id,'REFERENT', t.id,'T-REF-27-004', 5000
  FROM structure s, territory t WHERE s.name='Principal (prueba)' AND t.code='TEST-27-004';
-- dos coordinadores de zona sobre la MISMA sección
INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal, child_limit)
  SELECT s.id,'ZONE_COORDINATOR', ref.id, t.id,'T-CZ-A', 250, 10
  FROM structure s, position ref, territory t
  WHERE s.name='Principal (prueba)' AND ref.code='T-REF-27-004' AND t.code='TEST-30301';
INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal, child_limit)
  SELECT s.id,'ZONE_COORDINATOR', ref.id, t.id,'T-CZ-B', 250, 10
  FROM structure s, position ref, territory t
  WHERE s.name='Principal (prueba)' AND ref.code='T-REF-27-004' AND t.code='TEST-30301';

\echo ''
\echo '=== CASO 1: JUAN en 30301-a y PEDRO en 30301-b, con coordinadores DISTINTOS ==='
INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal)
  SELECT s.id,'PROMOTER', cz.id, t.id,'T-PROM-a', 35
  FROM structure s, position cz, territory t
  WHERE s.name='Principal (prueba)' AND cz.code='T-CZ-A' AND t.code='TEST-30301-a';
INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal)
  SELECT s.id,'PROMOTER', cz.id, t.id,'T-PROM-b', 35
  FROM structure s, position cz, territory t
  WHERE s.name='Principal (prueba)' AND cz.code='T-CZ-B' AND t.code='TEST-30301-b';

SELECT pr.code AS promoter, cz.code AS its_coordinator, te.code AS territory
FROM position pr JOIN position cz ON cz.id = pr.parent_position_id
JOIN territory te ON te.id = pr.territory_id
WHERE pr.level='PROMOTER' AND pr.code LIKE 'T-PROM%' ORDER BY 1;
\echo '--> OK: misma sección, dos coordinadores distintos.'

-- asignar titulares
INSERT INTO assignment (position_id, person_id, role, validity)
  SELECT p.id, pe.id, 'HOLDER', tstzrange(now() - interval '1 year', NULL)
  FROM position p, person pe WHERE p.code='T-PROM-a' AND pe.names='T_JUAN';
INSERT INTO assignment (position_id, person_id, role, validity)
  SELECT p.id, pe.id, 'HOLDER', tstzrange(now() - interval '1 year', NULL)
  FROM position p, person pe WHERE p.code='T-PROM-b' AND pe.names='T_PEDRO';

-- convencidos colgando del PUESTO de JUAN
INSERT INTO membership (person_id, structure_id, position_id, captured_by_assignment_id, territory_id, validity)
  SELECT pe.id, s.id, p.id, a.id, t.id, tstzrange(now() - interval '6 months', NULL)
  FROM person pe, structure s, position p, assignment a, territory t
  WHERE pe.names LIKE 'T_MEMBER%' AND s.name='Principal (prueba)'
    AND p.code='T-PROM-a' AND a.position_id = p.id AND t.code='TEST-30301-a';

\echo ''
\echo '=== CASO 2: rotar el titular NO debe mover a los convencidos ==='
CREATE TEMP TABLE before_rotation AS
  SELECT id, position_id, captured_by_assignment_id FROM membership
  WHERE legacy_id IS NULL ORDER BY id;

UPDATE assignment SET validity = tstzrange(lower(validity), now()), end_reason='prueba de rotacion'
  WHERE position_id = (SELECT id FROM position WHERE code='T-PROM-a') AND role='HOLDER';
INSERT INTO assignment (position_id, person_id, role, validity, start_reason)
  SELECT p.id, pe.id, 'HOLDER', tstzrange(now(), NULL), 'releva a JUAN'
  FROM position p, person pe WHERE p.code='T-PROM-a' AND pe.names='T_ANDY';

SELECT CASE WHEN count(*) = 0 THEN 'OK: 0 membresias modificadas'
            ELSE 'FALLO: ' || count(*) || ' membresias cambiaron' END AS resultado
FROM (SELECT id, position_id, captured_by_assignment_id FROM membership WHERE legacy_id IS NULL
      EXCEPT SELECT id, position_id, captured_by_assignment_id FROM before_rotation) d;

SELECT p.names AS current_holder, lower(a.validity)::date AS since
FROM assignment a JOIN person p ON p.id=a.person_id
WHERE a.position_id=(SELECT id FROM position WHERE code='T-PROM-a')
  AND a.role='HOLDER' AND upper(a.validity) IS NULL;

\echo ''
\echo '=== CASO 3: consulta histórica — quién era titular hace 1 mes ==='
SELECT p.names AS holder_one_month_ago
FROM assignment a JOIN person p ON p.id=a.person_id
WHERE a.position_id=(SELECT id FROM position WHERE code='T-PROM-a')
  AND a.role='HOLDER' AND a.validity @> (now() - interval '1 month');

\echo ''
\echo '=== CASO 4: reglas que DEBEN fallar ==='

\echo '-- 4a. dos titulares simultáneos en el mismo puesto:'
SAVEPOINT s1;
DO $$ BEGIN
  INSERT INTO assignment (position_id, person_id, role, validity)
    SELECT p.id, pe.id, 'HOLDER', tstzrange(now(), NULL)
    FROM position p, person pe WHERE p.code='T-PROM-a' AND pe.names='T_PEDRO';
  RAISE EXCEPTION 'FALLO: permitió dos titulares';
EXCEPTION WHEN exclusion_violation THEN RAISE NOTICE 'OK: rechazado (exclusion_violation)';
END $$;
ROLLBACK TO s1;

\echo '-- 4b. un PROMOTER colgando de otro PROMOTER:'
SAVEPOINT s2;
DO $$ BEGIN
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, code)
    SELECT s.id,'PROMOTER', pr.id, t.id,'T-PROM-MALO'
    FROM structure s, position pr, territory t
    WHERE s.name='Principal (prueba)' AND pr.code='T-PROM-a' AND t.code='TEST-30301-a';
  RAISE EXCEPTION 'FALLO: permitió promotor bajo promotor';
EXCEPTION WHEN raise_exception THEN RAISE NOTICE 'OK: rechazado (%)', SQLERRM;
END $$;
ROLLBACK TO s2;

\echo '-- 4c. padre e hijo en estructuras distintas:'
SAVEPOINT s3;
DO $$ BEGIN
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, code)
    SELECT s.id,'PROMOTER', cz.id, t.id,'T-PROM-CRUZADO'
    FROM structure s, position cz, territory t
    WHERE s.name='Aliada CNC (prueba)' AND cz.code='T-CZ-A' AND t.code='TEST-30301-a';
  RAISE EXCEPTION 'FALLO: permitió cruzar estructuras';
EXCEPTION WHEN raise_exception THEN RAISE NOTICE 'OK: rechazado (%)', SQLERRM;
END $$;
ROLLBACK TO s3;

\echo '-- 4d. promotor en un municipio ajeno al de su coordinador:'
SAVEPOINT s4;
DO $$ BEGIN
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, code)
    SELECT s.id,'PROMOTER', cz.id, t.id,'T-PROM-FUERA'
    FROM structure s, position cz, territory t
    WHERE s.name='Principal (prueba)' AND cz.code='T-CZ-A' AND t.code='TEST-27-009';
  RAISE EXCEPTION 'FALLO: permitió territorio fuera del padre';
EXCEPTION WHEN raise_exception THEN RAISE NOTICE 'OK: rechazado (%)', SQLERRM;
END $$;
ROLLBACK TO s4;

\echo '-- 4e. reescribir el linaje de una membresía:'
SAVEPOINT s5;
DO $$ BEGIN
  UPDATE membership SET captured_by_assignment_id = NULL
   WHERE id = (SELECT min(id) FROM membership WHERE legacy_id IS NULL);
  RAISE EXCEPTION 'FALLO: permitió borrar el linaje';
EXCEPTION WHEN raise_exception THEN RAISE NOTICE 'OK: rechazado (%)', SQLERRM;
END $$;
ROLLBACK TO s5;

\echo '-- 4f. misma persona dos veces en la MISMA estructura:'
SAVEPOINT s6;
DO $$ BEGIN
  INSERT INTO membership (person_id, structure_id, position_id, validity)
    SELECT m.person_id, m.structure_id, m.position_id, tstzrange(now(), NULL)
    FROM membership m WHERE m.legacy_id IS NULL LIMIT 1;
  RAISE EXCEPTION 'FALLO: permitió duplicado en la misma estructura';
EXCEPTION WHEN exclusion_violation THEN RAISE NOTICE 'OK: rechazado (exclusion_violation)';
END $$;
ROLLBACK TO s6;

\echo ''
\echo '=== CASO 5: la MISMA persona SÍ puede estar en otra estructura ==='
INSERT INTO position (structure_id, level, territory_id, code)
  SELECT s.id,'REFERENT', t.id,'T-REF-ALIADA' FROM structure s, territory t
  WHERE s.name='Aliada CNC (prueba)' AND t.code='TEST-27-004';
INSERT INTO position (structure_id, level, parent_position_id, territory_id, code)
  SELECT s.id,'PROMOTER', ref.id, t.id,'T-PROM-ALIADA' FROM structure s, position ref, territory t
  WHERE s.name='Aliada CNC (prueba)' AND ref.code='T-REF-ALIADA' AND t.code='TEST-30301-a';
INSERT INTO membership (person_id, structure_id, position_id, validity)
  SELECT m.person_id, s.id, p.id, tstzrange(now(), NULL)
  FROM membership m, structure s, position p
  WHERE s.name='Aliada CNC (prueba)' AND p.code='T-PROM-ALIADA'
    AND m.id=(SELECT min(id) FROM membership WHERE legacy_id IS NULL);

SELECT 'OK: la persona quedó en 2 estructuras' AS resultado
WHERE EXISTS (
  SELECT 1 FROM membership m JOIN structure s ON s.id=m.structure_id
  WHERE s.name IN ('Principal (prueba)','Aliada CNC (prueba)')
  GROUP BY m.person_id HAVING count(DISTINCT m.structure_id) > 1);

ROLLBACK;
