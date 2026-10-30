-- =====================================================================
-- Pruebas de V6 (campaña 2027 y cierre de 2024), contra la base real.
-- Se ejecuta dentro de una transacción y termina con ROLLBACK: no deja datos.
--   psql -U postgres -d msvc_dash_structure -f T5__campaign_tests.sql
-- =====================================================================
BEGIN;

\echo '=== CASO 1: 2024 es de sólo lectura ==='

\echo '-- 1a. marcar «votó» a un convencido 2024:'
SAVEPOINT s1;
DO $$ BEGIN
  UPDATE membership SET vote_flag = NOT vote_flag
   WHERE id = (SELECT m.id FROM membership m JOIN structure s ON s.id = m.structure_id
                WHERE s.campaign_id = (SELECT id FROM campaign WHERE name = 'Tabasco 2024') LIMIT 1);
  RAISE EXCEPTION 'FALLO: permitió escribir un convencido 2024';
EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: rechazado (check_violation)';
END $$;
ROLLBACK TO s1;

\echo '-- 1b. cambiar la meta de un puesto 2024:'
SAVEPOINT s2;
DO $$ BEGIN
  UPDATE position SET goal = coalesce(goal, 0) + 1
   WHERE id = (SELECT p.id FROM position p WHERE p.structure_id = 1 LIMIT 1);
  RAISE EXCEPTION 'FALLO: permitió escribir un puesto 2024';
EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: rechazado (check_violation)';
END $$;
ROLLBACK TO s2;

\echo '-- 1c. cerrar el titular de un puesto 2024:'
SAVEPOINT s3;
DO $$ BEGIN
  UPDATE assignment SET validity = tstzrange(lower(validity), now())
   WHERE id = (SELECT a.id FROM assignment a JOIN position p ON p.id = a.position_id
                WHERE p.structure_id = 1 AND upper_inf(a.validity) LIMIT 1);
  RAISE EXCEPTION 'FALLO: permitió escribir un titular 2024';
EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: rechazado (check_violation)';
END $$;
ROLLBACK TO s3;

\echo ''
\echo '=== CASO 2: una sola campaña ACTIVE en Tabasco ==='
SAVEPOINT s4;
DO $$ BEGIN
  INSERT INTO campaign (name, type, year, status, territory_id)
  SELECT 'Otra 2027', 'STATE', 2027, 'ACTIVE', territory_id FROM campaign WHERE name = 'Tabasco 2027';
  RAISE EXCEPTION 'FALLO: permitió dos campañas activas';
EXCEPTION WHEN unique_violation THEN RAISE NOTICE 'OK: rechazado (unique_violation)';
END $$;
ROLLBACK TO s4;

\echo ''
\echo '=== CASO 3: en 2027 se arma estructura y una persona de 2024 vuelve a ser convencida ==='
-- un referente y un promotor 2027 sobre el primer municipio y su primera sección
INSERT INTO position (structure_id, level, territory_id, code, goal)
SELECT s.id, 'REFERENT', t.id, 'T5-REF', 1000
  FROM structure s, territory t
 WHERE s.campaign_id = (SELECT id FROM campaign WHERE name = 'Tabasco 2027') AND s.type = 'MAIN'
   AND t.id = (SELECT id FROM territory WHERE type = 'MUNICIPALITY' AND valid_to IS NULL ORDER BY id LIMIT 1);
INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal)
SELECT r.structure_id, 'ZONE_COORDINATOR', r.id, t.id, 'T5-CZ', 200
  FROM position r, territory t
 WHERE r.code = 'T5-REF'
   AND t.id = (SELECT id FROM territory WHERE type = 'SECTION' AND valid_to IS NULL
                 AND parent_territory_id = r.territory_id ORDER BY id LIMIT 1);
INSERT INTO assignment (position_id, person_id, role)
SELECT p.id, (SELECT id FROM person ORDER BY id LIMIT 1), 'HOLDER' FROM position p WHERE p.code = 'T5-CZ';

-- la misma persona que ya es convencida en 2024
INSERT INTO membership (person_id, structure_id, position_id, captured_by_assignment_id, territory_id)
SELECT m.person_id, cz.structure_id, cz.id, a.id, cz.territory_id
  FROM position cz
  JOIN assignment a ON a.position_id = cz.id
 CROSS JOIN LATERAL (SELECT m.person_id FROM membership m WHERE m.structure_id = 1 LIMIT 1) m
 WHERE cz.code = 'T5-CZ';

SELECT c.name AS campaign, count(*) AS memberships_de_esa_persona
  FROM membership m JOIN structure s ON s.id = m.structure_id JOIN campaign c ON c.id = s.campaign_id
 WHERE m.person_id = (SELECT m2.person_id FROM membership m2 JOIN position p ON p.id = m2.position_id
                       WHERE p.code = 'T5-CZ')
 GROUP BY c.name ORDER BY c.name;

\echo ''
\echo '=== CASO 4: un convencido 2027 no se ancla a un puesto 2024 ==='
SAVEPOINT s5;
DO $$ BEGIN
  INSERT INTO membership (person_id, structure_id, position_id, territory_id)
  SELECT (SELECT id FROM person ORDER BY id DESC LIMIT 1),
         (SELECT id FROM structure WHERE campaign_id = (SELECT id FROM campaign WHERE name = 'Tabasco 2027') AND type = 'MAIN'),
         p.id, p.territory_id
    FROM position p WHERE p.structure_id = 1 AND p.level = 'PROMOTER' LIMIT 1;
  RAISE EXCEPTION 'FALLO: permitió anclar un convencido 2027 a un puesto 2024';
EXCEPTION WHEN check_violation THEN RAISE NOTICE 'OK: rechazado (check_violation)';
END $$;
ROLLBACK TO s5;

ROLLBACK;
