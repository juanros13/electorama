-- =====================================================================
-- Electorama — msvc-structure
-- V2b: reparaciones del backfill (titulares y linaje)
--
-- El paso 5 de V2 crea las asignaciones de REFERENT y ZONE_COORDINATOR
-- con un JOIN contra `map_auth`, que sólo conoce los authId que aparecen
-- como fila de `users` o de `promoters`. Un referente que existe
-- únicamente como PUNTERO —`promoters.referent_auth_id` apuntando a una
-- cuenta de Keycloak que nunca se dio de alta en la base local— se queda
-- con su puesto creado pero sin nadie ocupándolo.
--
-- En los datos: 3 puestos de referente y 7 de coordinador de zona. Suena a
-- poco, pero esos 3 referentes cuelgan de 1,264 promotores (2.6%), que por
-- la vista de compatibilidad reportaban `referent_auth_id` nulo.
--
-- La reparación crea una persona mínima —sólo el authId, sin nombre,
-- porque no hay de dónde sacarlo— y la asigna al puesto. Es la verdad del
-- dato: la cuenta existe en Keycloak y no en la base.
--
-- NO toca los puestos POOL|* ni UNPLACED|*: ésos no tienen titular a
-- propósito, son la bolsa de los convencidos huérfanos.
--
-- Es idempotente. Se ejecuta después de V2 y antes de V3a.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V2b__repair_holders.sql
-- =====================================================================

\echo '--- puestos de mando sin titular, antes ---'
SELECT p.level, count(*)
FROM position p
LEFT JOIN assignment a
       ON a.position_id = p.id AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
WHERE p.valid_to IS NULL
  AND p.level IN ('REFERENT', 'ZONE_COORDINATOR')
  AND a.id IS NULL
GROUP BY 1 ORDER BY 1;

-- Los puestos huérfanos y el authId que llevan en el código. El código es
-- 'REF|<territorio>|<authId>' o 'CZ|<territorio>|<authId>': el separador es
-- '|' justamente para que el authId salga entero, porque '-' ya aparece
-- dentro de los códigos de territorio y de los propios UUID.
CREATE TEMP TABLE orphan_position AS
SELECT p.id AS position_id,
       p.level,
       split_part(p.code, '|', 3) AS auth_id
FROM position p
LEFT JOIN assignment a
       ON a.position_id = p.id AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
WHERE p.valid_to IS NULL
  AND p.level IN ('REFERENT', 'ZONE_COORDINATOR')
  AND a.id IS NULL
  -- sólo si el código trae un UUID reconocible; si no, no se inventa nada
  AND split_part(p.code, '|', 3) ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- 1. persona mínima para los authId que no tienen ninguna
INSERT INTO person (auth_id, legacy_source, created)
SELECT DISTINCT o.auth_id, 'keycloak', now()
FROM orphan_position o
WHERE NOT EXISTS (SELECT 1 FROM person p WHERE p.auth_id = o.auth_id);

-- 2. el nombramiento, con su authId propio
--    `assignment.auth_id` es la cuenta con la que se ocupa el puesto, que
--    no es lo mismo que la identidad de la persona: una persona puede
--    tener varias cuentas y `person.auth_id` sólo admite una.
INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
SELECT o.position_id, p.id, 'HOLDER', o.auth_id,
       tstzrange(now(), NULL),
       'reparación V2b: titular que sólo existía como puntero'
FROM orphan_position o
JOIN person p ON p.auth_id = o.auth_id;

\echo '--- puestos de mando sin titular, después ---'
SELECT p.level, count(*)
FROM position p
LEFT JOIN assignment a
       ON a.position_id = p.id AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
WHERE p.valid_to IS NULL
  AND p.level IN ('REFERENT', 'ZONE_COORDINATOR')
  AND a.id IS NULL
GROUP BY 1 ORDER BY 1;

\echo '--- authId del modelo viejo que siguen sin persona ---'
SELECT count(*) AS sin_persona
FROM (SELECT DISTINCT promoter_auth_id AS auth_id FROM leg.promoters WHERE promoter_auth_id IS NOT NULL
      UNION SELECT DISTINCT cordinator_zone_auth_id FROM leg.promoters WHERE cordinator_zone_auth_id IS NOT NULL
      UNION SELECT DISTINCT referent_auth_id FROM leg.promoters WHERE referent_auth_id IS NOT NULL
      UNION SELECT DISTINCT auth_id FROM leg.users WHERE auth_id IS NOT NULL) a
WHERE NOT EXISTS (SELECT 1 FROM person p WHERE p.auth_id = a.auth_id)
  AND NOT EXISTS (SELECT 1 FROM assignment g WHERE g.auth_id = a.auth_id);

ANALYZE assignment;
ANALYZE person;


-- =====================================================================
-- 2. Linaje: el authId de quien capturó
--
-- `membership.captured_by_assignment_id` apunta a la ASIGNACIÓN que
-- capturó al convencido, que es lo que responde «qué persona y desde qué
-- puesto». Pero el paso 6 de V2 resuelve ese puntero yendo de
-- `user_create_id` → persona → cualquier asignación suya, y eso deja fuera
-- dos casos:
--
--   - 1,589 convencidos capturados por alguien cuya asignación sí existe,
--     pero cuyo authId no es el de la persona (una persona tiene varias
--     cuentas de Keycloak);
--   - 18,026 capturados por cuentas que NUNCA ocuparon un puesto —
--     administradores, cargas masivas—, para las que no hay ninguna
--     asignación a la que apuntar.
--
-- En total 19,615 convencidos (1.8%) salían con `user_create_id` nulo por
-- la vista de compatibilidad. La columna `captured_by_auth_id` guarda el
-- dato crudo: no inventa linaje, conserva el que había.
-- =====================================================================

ALTER TABLE membership ADD COLUMN IF NOT EXISTS captured_by_auth_id VARCHAR(100);
CREATE INDEX IF NOT EXISTS membership_captured_auth_idx
    ON membership (captured_by_auth_id) WHERE captured_by_auth_id IS NOT NULL;

UPDATE membership m
   SET captured_by_auth_id = f.user_create_id
  FROM leg.followers f
 WHERE f.id = m.legacy_id
   AND f.user_create_id IS NOT NULL
   AND m.captured_by_auth_id IS DISTINCT FROM f.user_create_id;

-- Donde sí hay una asignación con ese authId exacto, se endereza el puntero
-- estructurado. El trigger que protege el linaje sólo impide reescribir un
-- valor ya existente, así que esto únicamente rellena los que están nulos.
UPDATE membership m
   SET captured_by_assignment_id = a.id
  FROM assignment a
 WHERE a.auth_id = m.captured_by_auth_id
   AND m.captured_by_assignment_id IS NULL
   AND a.role = 'HOLDER';

\echo '--- convencidos con captor registrado ---'
SELECT count(*)                                              AS total,
       count(captured_by_auth_id)                            AS con_authid,
       count(captured_by_assignment_id)                      AS con_asignacion
FROM membership;

ANALYZE membership;


-- =====================================================================
-- 3. El nombre: preferir la fuente que lo trae partido
--
-- El paso 3 de V2 elige, dentro de cada componente de deduplicacion, la
-- fila de mayor prioridad: users > promoters > followers. Pero
-- `users.names` suele venir de `auth_name` de Keycloak, que es el nombre
-- COMPLETO en un solo campo, con `last_name` y `sur_name` vacios. Asi que
-- para una persona que es a la vez operador y convencido gana la version
-- peor formada: 46,972 convencidos (4.4%) salen por la vista con el
-- nombre completo pegado en `names` y sin apellido.
--
-- La regla correcta es: dentro de una misma CURP, gana la fila que SI
-- trae el nombre partido, y sólo entre iguales decide la prioridad de
-- fuente. Esto lo aplica sobre el resultado, sin repetir el backfill.
-- (En V2 conviene arreglar el ORDER BY del DISTINCT ON por lo mismo.)
-- =====================================================================

CREATE TEMP TABLE best_name AS
SELECT DISTINCT ON (curp) curp, names, last_name, sur_name
FROM (
  SELECT upper(trim(u.curp)) AS curp,
         COALESCE(nullif(trim(u.names), ''), nullif(trim(u.auth_name), '')) AS names,
         u.last_name, u.sur_name, 1 AS pri
    FROM leg.users u WHERE nullif(trim(u.curp), '') IS NOT NULL
  UNION ALL
  SELECT upper(trim(p.curp)), p.names, p.last_name, p.sur_name, 2
    FROM leg.promoters p WHERE nullif(trim(p.curp), '') IS NOT NULL
  UNION ALL
  SELECT upper(trim(f.curp)), f.names, f.last_name, f.sur_name, 3
    FROM leg.followers f WHERE nullif(trim(f.curp), '') IS NOT NULL
) t
ORDER BY curp,
         -- primero las que traen apellido: ese es el criterio que faltaba
         (nullif(trim(last_name), '') IS NULL),
         (nullif(trim(names), '') IS NULL),
         pri;

CREATE INDEX ON best_name (curp);
ANALYZE best_name;

UPDATE person p
   SET names     = b.names,
       last_name = b.last_name,
       sur_name  = b.sur_name
  FROM best_name b
 WHERE b.curp = p.curp
   AND (p.names, p.last_name, p.sur_name) IS DISTINCT FROM (b.names, b.last_name, b.sur_name);

\echo '--- personas sin apellido pero con nombre ---'
SELECT count(*) AS sin_apellido
FROM person
WHERE nullif(trim(coalesce(last_name, '')), '') IS NULL
  AND nullif(trim(coalesce(names, '')), '') IS NOT NULL;

ANALYZE person;
