-- =====================================================================
-- Electorama — msvc-structure
-- V2c: puestos de coordinador que sólo existen en `users_seccions`
--
-- El paso 4b de V2 construye los puestos ZONE_COORDINATOR desde
-- `promoters`, así que un coordinador nombrado en `users_seccions` que
-- todavía no tiene ni un promotor colgando no queda en el modelo nuevo.
--
-- MEDIDO EN LOCAL (22-sep-2026), y corrige lo que suponía la nota de la
-- migración. `users_seccions` tiene 68,960 filas vivas, pero no son todas
-- de la misma naturaleza:
--
--   rama PRINCIPAL   5,680 filas · 5,673 usuarios · 4,498 con meta · ~1 sección por usuario
--   rama ALTERNA    63,280 filas ·   489 usuarios ·     8 con meta · ~129 secciones (máx 307)
--
-- Las de la rama alterna NO son nombramientos: son ámbito de visibilidad.
-- Un municipio tiene 30 secciones en promedio y esos usuarios cargan 129
-- cada uno, sin meta y sin promotores debajo. Convertirlas en ~59,300
-- puestos inventaría una estructura que no existe, así que este script
-- **sólo migra la rama principal**. El ámbito de la alterna se resuelve
-- aparte (ver la nota de la migración, fase 3b).
--
-- Con eso, el hueco real son 1,198 puestos, de los que 16 traen meta
-- (4,000 de 1,143,550 = 0.35%). El resto de las metas ya las tomó V2.
--
-- Otro detalle del dato: `users_seccions.seccion_id` viene NULL en 24,101
-- filas —todas de la rama alterna— mientras `seccion_geo_id` está siempre
-- lleno. Aquí se usa `seccion_geo_id`, que es la columna confiable.
--
-- Es IDEMPOTENTE: sólo inserta lo que no existe. Se ejecuta después de
-- V2b y antes de V3a.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V2c__coordinator_sections.sql
-- =====================================================================

\set ON_ERROR_STOP on

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                  WHERE table_schema='leg' AND table_name='users_seccions') THEN
    RAISE EXCEPTION 'falta el esquema leg: corre V2 antes que esto';
  END IF;
END $$;

\echo '--- puestos de coordinador de zona, antes ---'
SELECT count(*) AS zone_coordinator FROM position WHERE level='ZONE_COORDINATOR';

BEGIN;

-- ---------------------------------------------------------------------
-- 1. las parejas (coordinador, sección) de la rama principal que todavía
--    no tienen puesto
-- ---------------------------------------------------------------------
CREATE TEMP TABLE stg_cz AS
SELECT DISTINCT ON (us.user_auth_id, us.seccion_geo_id)
       us.user_auth_id                AS auth_id,
       us.seccion_geo_id,
       us.municipality_id,
       us.meta,
       us.limit_promoters,
       u.auth_id_reference,
       u.created                      AS user_created,
       sec.id                         AS territory_id,
       s.id                           AS structure_id,
       'CZ|' || sec.code || '|' || us.user_auth_id AS code
FROM leg.users_seccions us
JOIN leg.users u   ON u.auth_id = us.user_auth_id
                  AND COALESCE(u.flag_alter, false) = false
JOIN territory sec ON sec.section_geo_id = us.seccion_geo_id AND sec.type = 'SECTION'
JOIN structure s   ON s.type = 'MAIN'
WHERE NOT EXISTS (
        SELECT 1 FROM position p
         WHERE p.code = 'CZ|' || sec.code || '|' || us.user_auth_id
           AND p.structure_id = s.id)
ORDER BY us.user_auth_id, us.seccion_geo_id, us.meta NULLS LAST;

CREATE INDEX ON stg_cz (auth_id);

\echo '--- parejas por migrar (rama principal) ---'
SELECT count(*) AS pares, count(meta) AS con_meta, sum(meta) AS suma_meta,
       count(DISTINCT auth_id) AS coordinadores
FROM stg_cz;

-- ---------------------------------------------------------------------
-- 2. el puesto
--    El padre es el puesto de referente de ese municipio, si el usuario
--    trae `auth_id_reference` y ese puesto existe. Si no, el puesto queda
--    sin padre: `position_parent_chk` lo permite para todo lo que no sea
--    REFERENT, y es la verdad del dato — un coordinador nombrado sin
--    referente conocido. No se inventa una rama.
-- ---------------------------------------------------------------------
INSERT INTO position (structure_id, level, parent_position_id, territory_id,
                      code, goal, child_limit, valid_from)
SELECT c.structure_id, 'ZONE_COORDINATOR', ref.id, c.territory_id,
       c.code, c.meta, c.limit_promoters,
       COALESCE(c.user_created, now())
FROM stg_cz c
LEFT JOIN territory mun ON mun.municipality_geo_id = c.municipality_id
                       AND mun.type = 'MUNICIPALITY'
LEFT JOIN position ref  ON ref.code = 'REF|' || mun.code || '|' || c.auth_id_reference
                       AND ref.structure_id = c.structure_id
                       AND ref.level = 'REFERENT';

-- ---------------------------------------------------------------------
-- 3. la persona que lo ocupa
--    Se resuelve primero por `person.auth_id` y luego por CURP, porque el
--    backfill dedupica por CURP y `person.auth_id` sólo admite una cuenta:
--    146 de los 6,162 coordinadores sobreviven con el authId de otra de
--    sus cuentas. El authId del nombramiento va en `assignment.auth_id`,
--    que es donde vive.
-- ---------------------------------------------------------------------
CREATE TEMP TABLE map_coord AS
SELECT c.auth_id,
       COALESCE(
         (SELECT p.id FROM person p WHERE p.auth_id = c.auth_id),
         (SELECT p.id FROM person p
            JOIN leg.users u ON nullif(trim(u.curp), '') = p.curp
           WHERE u.auth_id = c.auth_id
           LIMIT 1)
       ) AS person_id
FROM (SELECT DISTINCT auth_id FROM stg_cz) c;
CREATE INDEX ON map_coord (auth_id);

\echo '--- coordinadores sin persona (deberían ser 0) ---'
SELECT count(*) AS sin_persona FROM map_coord WHERE person_id IS NULL;

-- persona mínima para el caso que no se resuelva: la cuenta existe en
-- Keycloak y no en la base, igual que en V2b
INSERT INTO person (auth_id, legacy_source, created)
SELECT DISTINCT mc.auth_id, 'keycloak', now()
FROM map_coord mc
WHERE mc.person_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM person p WHERE p.auth_id = mc.auth_id);

UPDATE map_coord mc
   SET person_id = p.id
  FROM person p
 WHERE mc.person_id IS NULL AND p.auth_id = mc.auth_id;

-- ---------------------------------------------------------------------
-- 4. el nombramiento
--    `assignment_single_holder` impide dos titulares vigentes en el mismo
--    puesto; los puestos recién creados no tienen ninguno.
-- ---------------------------------------------------------------------
INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
SELECT pos.id, mc.person_id, 'HOLDER', c.auth_id,
       tstzrange(COALESCE(c.user_created, now() - interval '3 years'), NULL),
       'migración V2c: coordinador nombrado en users_seccions sin promotores'
FROM stg_cz c
JOIN position pos ON pos.code = c.code AND pos.structure_id = c.structure_id
JOIN map_coord mc ON mc.auth_id = c.auth_id
WHERE NOT EXISTS (
        SELECT 1 FROM assignment a
         WHERE a.position_id = pos.id AND a.role = 'HOLDER' AND upper(a.validity) IS NULL);

-- ---------------------------------------------------------------------
-- 5. metas que se hayan quedado atrás en puestos que ya existían
--    Sólo rellena el nulo; nunca pisa una meta ya puesta.
-- ---------------------------------------------------------------------
UPDATE position p
   SET goal        = COALESCE(p.goal, us.meta),
       child_limit = COALESCE(p.child_limit, us.limit_promoters)
  FROM leg.users_seccions us
  JOIN leg.users u   ON u.auth_id = us.user_auth_id
                    AND COALESCE(u.flag_alter, false) = false
  JOIN territory sec ON sec.section_geo_id = us.seccion_geo_id AND sec.type = 'SECTION'
 WHERE p.code = 'CZ|' || sec.code || '|' || us.user_auth_id
   AND p.level = 'ZONE_COORDINATOR'
   AND ((p.goal IS NULL AND us.meta IS NOT NULL)
     OR (p.child_limit IS NULL AND us.limit_promoters IS NOT NULL));

COMMIT;

ANALYZE position;
ANALYZE assignment;

-- =====================================================================
-- verificación
-- =====================================================================
\echo '--- puestos de coordinador de zona, después ---'
SELECT count(*) AS zone_coordinator,
       count(*) FILTER (WHERE goal IS NOT NULL) AS con_meta,
       sum(goal) AS suma_meta
FROM position WHERE level='ZONE_COORDINATOR';

\echo '--- nombramientos de la rama principal que siguen sin puesto (debería ser 0) ---'
SELECT count(*) AS sin_puesto
FROM leg.users_seccions us
JOIN leg.users u   ON u.auth_id = us.user_auth_id AND COALESCE(u.flag_alter,false) = false
JOIN territory sec ON sec.section_geo_id = us.seccion_geo_id AND sec.type='SECTION'
LEFT JOIN position p ON p.code = 'CZ|' || sec.code || '|' || us.user_auth_id
WHERE p.id IS NULL;

\echo '--- puestos de coordinador sin titular vigente ---'
SELECT count(*) AS sin_titular
FROM position p
LEFT JOIN assignment a ON a.position_id = p.id AND a.role='HOLDER' AND upper(a.validity) IS NULL
WHERE p.level='ZONE_COORDINATOR' AND p.valid_to IS NULL AND a.id IS NULL;

\echo '--- ámbito de la rama alterna que este script NO migra ---'
SELECT count(*) AS filas_ambito, count(DISTINCT us.user_auth_id) AS usuarios
FROM leg.users_seccions us
JOIN leg.users u ON u.auth_id = us.user_auth_id AND COALESCE(u.flag_alter,false) = true;
