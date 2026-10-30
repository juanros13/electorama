-- =====================================================================
-- Electorama — msvc-structure
-- V4b: el modelo absorbe lo que sólo vivía en `promoters`
--
-- La vista `promoters` de la fase 3 pega 8 columnas desde `promoters_legacy`.
-- Medidas sobre las 49,355 filas congeladas:
--
--   neighborhood_id  49,354 → NO es un id: es el nombre de la colonia escrito
--                            a mano. 49,323 de sus 49,354 valores no son
--                            numéricos ("POB C27 EDUARDO CHAVEZ", "EJ EL
--                            ABEJONAL"). Va a `person.neighborhood`, que es
--                            texto, y no a una referencia de catálogo.
--   vencimiento      49,355 → vigencia de la credencial → person
--   notes            49,354 → nota del nombramiento → assignment
--   user_create_id   49,355 → quién dio de alta al promotor → assignment
--   updated          49,355 → assignment
--   auth_id          23,239 → duplicado a medio llenar de promoter_auth_id
--   cat_committee         0 → columna muerta
--   follower_id      27,680 → PUNTERO ROTO, ver abajo
--
-- `auth_id` no se absorbe porque no es un dato propio: de sus 23,239 filas,
-- 23,231 son idénticas a `promoter_auth_id` y el modelo ya guarda ese dato en
-- `assignment.auth_id`. La vista lo sirve desde ahí.
--
-- `follower_id` tampoco, y esto conviene dejarlo escrito: de los 27,680
-- valores, **sólo 86 apuntan a una membresía que existe**, y en esos 86 el
-- modelo dice que es OTRA persona. No es el enlace promotor↔convencido que
-- parece: son ids viejos que no resuelven. Lo que ese enlace quería decir ya
-- lo dice `person`, que es la misma fila para el humano sea promotor,
-- convencido o las dos cosas.
--
-- `cat_committee` se queda fuera por vacía. Los comités viven en
-- `cat_committee`/`follower_mark` dentro de msvc_dash_followers, que es de
-- msvc-followers y no de este modelo.
--
-- Es IDEMPOTENTE. Después de V4, antes de V4c.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V4b__absorb_promoter_data.sql
-- =====================================================================

\set ON_ERROR_STOP on

-- Los esquemas de las tablas foráneas se crean si faltan: la limpieza del
-- andamiaje los borra, y este script tiene que seguir siendo re-ejecutable.
CREATE SCHEMA IF NOT EXISTS leg_fol;

DROP FOREIGN TABLE IF EXISTS leg_fol.promoters_legacy;
IMPORT FOREIGN SCHEMA public LIMIT TO (promoters_legacy)
  FROM SERVER srv_followers INTO leg_fol;

-- ---------------------------------------------------------------------
-- 1. persona: la colonia y la vigencia de la credencial
--
--    `ine_validity` vive en la persona porque es un dato de su credencial,
--    no de un nombramiento. El valor que trae cada captura de convencido se
--    queda además en `membership_detail.ine_validity`: eso es lo que la app
--    registró ese día, y no tiene por qué coincidir con el dato vigente.
-- ---------------------------------------------------------------------
ALTER TABLE person ADD COLUMN IF NOT EXISTS ine_validity INTEGER;

COMMENT ON COLUMN person.ine_validity IS
  'Año de vigencia de la credencial de elector. El dato de la persona, no el de una captura.';

-- el nombramiento manda sobre la captura: el registro del promotor lo
-- llenó un operador, el del convencido un levantamiento en la calle
UPDATE person p
   SET neighborhood = COALESCE(nullif(trim(p.neighborhood), ''),
                               nullif(trim(pl.neighborhood_id), '')),
       ine_validity = COALESCE(pl.vencimiento, p.ine_validity)
  FROM leg_fol.promoters_legacy pl
  JOIN position pos ON pos.code = 'PROM|' || pl.id
  JOIN assignment a ON a.position_id = pos.id AND a.role = 'HOLDER'
 WHERE a.person_id = p.id
   AND (nullif(trim(p.neighborhood), '') IS NULL
          AND nullif(trim(pl.neighborhood_id), '') IS NOT NULL
     OR p.ine_validity IS DISTINCT FROM pl.vencimiento AND pl.vencimiento IS NOT NULL);

-- y para quien sólo es convencido, la vigencia que trajo su captura
UPDATE person p
   SET ine_validity = d.ine_validity
  FROM membership m
  JOIN membership_detail d ON d.membership_id = m.id
 WHERE m.person_id = p.id
   AND p.ine_validity IS NULL
   AND d.ine_validity IS NOT NULL;

\echo '--- personas con colonia y vigencia ---'
SELECT count(nullif(trim(neighborhood), '')) AS con_colonia,
       count(ine_validity) AS con_vigencia FROM person;

-- ---------------------------------------------------------------------
-- 2. el nombramiento: su nota, quién lo dio de alta y cuándo se tocó
--
--    `created_by_person_id` ya existe, pero el modelo viejo sólo guarda el
--    authId de quien creó al promotor, y ese authId puede no tener persona.
--    Se guarda crudo, por la misma razón que `membership.captured_by_auth_id`:
--    no se inventa linaje, se conserva el que había.
-- ---------------------------------------------------------------------
ALTER TABLE assignment ADD COLUMN IF NOT EXISTS notes               VARCHAR(500);
ALTER TABLE assignment ADD COLUMN IF NOT EXISTS created_by_auth_id  VARCHAR(100);
ALTER TABLE assignment ADD COLUMN IF NOT EXISTS updated             TIMESTAMPTZ;

COMMENT ON COLUMN assignment.created_by_auth_id IS
  'Cuenta que dio de alta el nombramiento, tal como venía. Cruda, porque puede no tener persona.';

UPDATE assignment a
   SET notes              = COALESCE(a.notes, nullif(trim(pl.notes), '')),
       created_by_auth_id = COALESCE(a.created_by_auth_id, nullif(trim(pl.user_create_id), '')),
       updated            = COALESCE(a.updated, pl.updated)
  FROM leg_fol.promoters_legacy pl
  JOIN position pos ON pos.code = 'PROM|' || pl.id
 WHERE a.position_id = pos.id
   AND a.role = 'HOLDER'
   AND (a.notes IS NULL AND nullif(trim(pl.notes), '') IS NOT NULL
     OR a.created_by_auth_id IS NULL AND nullif(trim(pl.user_create_id), '') IS NOT NULL
     OR a.updated IS NULL AND pl.updated IS NOT NULL);

CREATE INDEX IF NOT EXISTS assignment_created_by_auth_idx
    ON assignment (created_by_auth_id) WHERE created_by_auth_id IS NOT NULL;

ANALYZE person;
ANALYZE assignment;

-- =====================================================================
-- verificación contra la tabla congelada
-- =====================================================================
\echo '--- nombramientos de promotor con nota y alta ---'
SELECT count(*) AS asignaciones_de_promotor,
       count(notes) AS con_nota,
       count(created_by_auth_id) AS con_quien_lo_creo,
       count(updated) AS con_updated
FROM assignment a
JOIN position p ON p.id = a.position_id
WHERE p.level = 'PROMOTER' AND a.role = 'HOLDER';

\echo '--- fidelidad columna a columna contra promoters_legacy ---'
SELECT count(*) AS filas,
       count(*) FILTER (WHERE a.notes IS DISTINCT FROM nullif(trim(pl.notes), ''))   AS dif_nota,
       count(*) FILTER (WHERE a.created_by_auth_id
                              IS DISTINCT FROM nullif(trim(pl.user_create_id), ''))   AS dif_creador,
       count(*) FILTER (WHERE p2.ine_validity IS DISTINCT FROM pl.vencimiento)        AS dif_vencimiento,
       count(*) FILTER (WHERE nullif(trim(p2.neighborhood), '')
                              IS DISTINCT FROM nullif(trim(pl.neighborhood_id), ''))   AS dif_colonia
FROM leg_fol.promoters_legacy pl
JOIN position pos ON pos.code = 'PROM|' || pl.id
JOIN assignment a ON a.position_id = pos.id AND a.role = 'HOLDER'
JOIN person p2    ON p2.id = a.person_id;
