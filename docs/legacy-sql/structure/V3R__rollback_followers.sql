-- =====================================================================
-- Electorama — msvc-structure
-- V3R: deshacer el corte de la fase 3 sobre msvc_dash_followers
--
-- La fase 3 es reversible por diseño: basta tirar las vistas y devolver
-- los nombres. Lo único que hay que cuidar es lo escrito DESPUÉS del
-- corte, que vive en el modelo nuevo y sólo dejó columnas residuales en
-- `*_legacy`. Este script lo devuelve a la tabla vieja antes de renombrar,
-- usando `change_log` para tocar nada más las filas afectadas.
--
--   psql -U postgres -d msvc_dash_followers -v ON_ERROR_STOP=1 -f V3R__rollback_followers.sql
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1. devolver a la tabla vieja lo que se escribió después del corte
-- ---------------------------------------------------------------------
CREATE TEMP TABLE touched_followers AS
SELECT DISTINCT entity_id AS id
FROM str.change_log
WHERE entity = 'membership' AND entity_id IS NOT NULL;

CREATE TEMP TABLE touched_promoters AS
SELECT DISTINCT (before_data ->> 'legacy_id')::bigint AS id
FROM str.change_log
WHERE entity = 'position' AND before_data ? 'legacy_id'
UNION
SELECT DISTINCT (after_data ->> 'legacy_id')::bigint
FROM str.change_log
WHERE entity = 'position' AND after_data ? 'legacy_id';

\echo '--- filas tocadas desde el corte ---'
SELECT (SELECT count(*) FROM touched_followers) AS followers,
       (SELECT count(*) FROM touched_promoters) AS promoters;

-- altas posteriores al corte: la fila residual existe pero sin estructura
UPDATE followers_legacy l
   SET birthdate            = r.birthdate,
       cell_phone_number    = r.cell_phone_number,
       created              = r.created,
       curp                 = r.curp,
       deleted              = r.deleted,
       email                = r.email,
       ine                  = r.ine,
       last_name            = r.last_name,
       names                = r.names,
       status               = r.status,
       sur_name             = r.sur_name,
       user_create_id       = r.user_create_id,
       external_number      = r.external_number,
       internal_number      = r.internal_number,
       neighborhood         = r.neighborhood,
       street               = r.street,
       zip_code             = r.zip_code,
       promoter_auth_id     = r.promoter_auth_id,
       promoter_alt_auth_id = r.promoter_alt_auth_id,
       vote_flag            = r.vote_flag,
       vote_flag_timestamp  = r.vote_flag_timestamp,
       municipality_id      = r.municipality_id,
       section_id           = r.section_id
  FROM str.followers r
 WHERE r.id = l.id
   AND l.id IN (SELECT id FROM touched_followers);

-- bajas posteriores al corte: la membresía ya no sale por la vista, así que
-- el dato se toma de la tabla del modelo nuevo
UPDATE followers_legacy l
   SET deleted = upper(m.validity)::timestamp
  FROM str.membership m
 WHERE m.legacy_id = l.id
   AND upper(m.validity) IS NOT NULL
   AND l.id IN (SELECT id FROM touched_followers);

UPDATE promoters_legacy l
   SET cell_phone_number       = r.cell_phone_number,
       created                 = r.created,
       curp                    = r.curp,
       deleted                 = r.deleted,
       email                   = r.email,
       ine                     = r.ine,
       interior_number         = r.interior_number,
       last_name               = r.last_name,
       municipality_id         = r.municipality_id,
       names                   = r.names,
       outdoor_number          = r.outdoor_number,
       postal_code             = r.postal_code,
       street                  = r.street,
       sur_name                = r.sur_name,
       seccion_id              = r.seccion_id,
       cordinator_zone_auth_id = r.cordinator_zone_auth_id,
       referent_auth_id        = r.referent_auth_id,
       promoter_auth_id        = r.promoter_auth_id,
       followers_goal          = r.followers_goal,
       flag_alter              = r.flag_alter
  FROM str.promoters r
 WHERE r.id = l.id
   AND l.id IN (SELECT id FROM touched_promoters);

UPDATE promoters_legacy l
   SET deleted = p.valid_to::timestamp
  FROM str.position p
 WHERE p.legacy_id = l.id
   AND p.valid_to IS NOT NULL
   AND l.id IN (SELECT id FROM touched_promoters);

-- ---------------------------------------------------------------------
-- 2. devolver los nombres
-- ---------------------------------------------------------------------
DROP VIEW followers;   -- se lleva sus triggers INSTEAD OF
DROP VIEW promoters;

ALTER TABLE followers_legacy RENAME TO followers;
ALTER TABLE promoters_legacy RENAME TO promoters;

COMMENT ON TABLE followers IS NULL;
COMMENT ON TABLE promoters IS NULL;

-- las secuencias nunca cambiaron de nombre, pero sí avanzaron: se dejan
-- donde están para no reutilizar ids ya entregados

-- ---------------------------------------------------------------------
-- 3. el andamio del corte
--    Se quita el esquema de funciones y las tablas foráneas. El SERVER y
--    el USER MAPPING se dejan: no molestan y evitan volver a pedir la
--    contraseña si se repite el corte.
--
--    Va DENTRO de la transacción a propósito. Estando fuera, un fallo en
--    el paso 1 abortaba el COMMIT pero estos DROP corrían igual, y como
--    las vistas dependen de `str` se las llevaba por delante: quedaban las
--    tablas con nombre `*_legacy` y sin vista encima. En PostgreSQL el
--    DDL es transaccional, así que aquí o pasa todo o no pasa nada.
-- ---------------------------------------------------------------------
DROP SCHEMA IF EXISTS compat CASCADE;
DROP SCHEMA IF EXISTS str CASCADE;

COMMIT;

-- los índices que la fase 3 creó se conservan a propósito: las tablas
-- nunca tuvieron llave primaria ni un solo índice, y quitarlos sería
-- devolver el problema de rendimiento junto con el modelo viejo
\echo '--- conteos tras el rollback ---'
SELECT (SELECT count(*) FROM followers) AS followers,
       (SELECT count(*) FROM promoters) AS promoters;
