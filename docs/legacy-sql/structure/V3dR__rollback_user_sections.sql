-- =====================================================================
-- Electorama — msvc-structure
-- V3dR: deshacer el corte de `users_seccions` (fase 3b)
--
-- Igual que V3R con followers: basta tirar la vista y devolver el nombre.
-- Lo único delicado es lo escrito DESPUÉS del corte, que vive en el modelo
-- nuevo y no dejó rastro en `users_seccions_legacy`.
--
-- Aquí no hay `change_log` que consultar —los puestos de coordinador no lo
-- llevan—, así que lo escrito después se reconstruye de la propia vista:
-- toda pareja (coordinador, sección) que la vista sirve y la tabla
-- congelada no tiene, se inserta; y toda la que la tabla tiene y la vista
-- ya no sirve, se marca como borrada. Es el estado, no el diario.
--
-- OJO: la reconstrucción usa los ids de la tabla vieja donde existen y la
-- secuencia para los nuevos. Los ids sintéticos de la vista NO se copian:
-- son (puesto << 20 | sección) y no tienen sentido fuera de ella.
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V3dR__rollback_user_sections.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = 'users_seccions_legacy') THEN
    RAISE EXCEPTION 'no hay users_seccions_legacy: el corte no está hecho';
  END IF;
END $$;

-- ---------------------------------------------------------------------
-- 1. lo que la vista sirve ahora, congelado antes de tirarla
-- ---------------------------------------------------------------------
CREATE TEMP TABLE vista_ahora AS
SELECT user_auth_id, seccion_geo_id, seccion_number, meta, municipality_id, limit_promoters
FROM users_seccions;

\echo '--- diferencias contra la tabla congelada ---'
SELECT (SELECT count(*) FROM vista_ahora v
         WHERE NOT EXISTS (SELECT 1 FROM users_seccions_legacy l
                            WHERE l.deleted IS NULL
                              AND l.user_auth_id = v.user_auth_id
                              AND l.seccion_geo_id = v.seccion_geo_id)) AS altas_despues_del_corte,
       (SELECT count(*) FROM users_seccions_legacy l
         WHERE l.deleted IS NULL
           AND NOT EXISTS (SELECT 1 FROM vista_ahora v
                            WHERE v.user_auth_id = l.user_auth_id
                              AND v.seccion_geo_id = l.seccion_geo_id)) AS bajas_despues_del_corte;

-- ---------------------------------------------------------------------
-- 2. la vista y sus triggers se van
-- ---------------------------------------------------------------------
DROP VIEW users_seccions;
ALTER TABLE users_seccions_legacy RENAME TO users_seccions;

-- ---------------------------------------------------------------------
-- 3. devolver lo escrito después del corte
--
-- Las bajas: la vista ya no las sirve, así que se marcan como borradas con
-- la misma semántica del servicio viejo (un timestamp en `deleted`).
-- ---------------------------------------------------------------------
UPDATE users_seccions l
   SET deleted = now()
 WHERE l.deleted IS NULL
   AND NOT EXISTS (SELECT 1 FROM vista_ahora v
                    WHERE v.user_auth_id = l.user_auth_id
                      AND v.seccion_geo_id = l.seccion_geo_id);

-- Las altas y los cambios de meta. `seccion_id` se rellena como lo hacía la
-- rama principal del modelo viejo (igual a `seccion_geo_id`); las filas que
-- vengan de un nombramiento municipal lo traen nulo, igual que antes.
INSERT INTO users_seccions (user_auth_id, seccion_id, seccion_number, seccion_geo_id,
                            meta, municipality_id, limit_promoters)
SELECT v.user_auth_id,
       CASE WHEN v.meta IS NOT NULL THEN v.seccion_geo_id END,
       v.seccion_number, v.seccion_geo_id, v.meta, v.municipality_id, v.limit_promoters
FROM vista_ahora v
WHERE NOT EXISTS (SELECT 1 FROM users_seccions l
                   WHERE l.deleted IS NULL
                     AND l.user_auth_id = v.user_auth_id
                     AND l.seccion_geo_id = v.seccion_geo_id);

UPDATE users_seccions l
   SET meta = v.meta,
       limit_promoters = v.limit_promoters
  FROM vista_ahora v
 WHERE l.deleted IS NULL
   AND l.user_auth_id = v.user_auth_id
   AND l.seccion_geo_id = v.seccion_geo_id
   AND (l.meta IS DISTINCT FROM v.meta
     OR l.limit_promoters IS DISTINCT FROM v.limit_promoters);

-- ---------------------------------------------------------------------
-- 4. limpieza del andamiaje
--    El servidor foráneo y el mapeo se quedan: los usa el corte de
--    followers y volver a crearlos cuesta una contraseña en claro.
-- ---------------------------------------------------------------------
DROP FUNCTION IF EXISTS compat.user_sections_insert() CASCADE;
DROP FUNCTION IF EXISTS compat.user_sections_update() CASCADE;
DROP FUNCTION IF EXISTS compat.user_sections_delete() CASCADE;
DROP FUNCTION IF EXISTS compat.us_resolve(bigint);
DROP FUNCTION IF EXISTS compat.us_override(bigint);
DROP FUNCTION IF EXISTS compat.materialize_municipal(bigint, integer);
DROP FUNCTION IF EXISTS compat.us_id(bigint, integer);
DROP FUNCTION IF EXISTS compat.us_position(bigint);
DROP FUNCTION IF EXISTS compat.us_section(bigint);
DROP FUNCTION IF EXISTS compat.close_validity(tstzrange, timestamptz);
DROP FUNCTION IF EXISTS compat.next_person_id();
DROP FUNCTION IF EXISTS compat.next_position_id();
DROP FUNCTION IF EXISTS compat.next_assignment_id();
DROP SCHEMA IF EXISTS str CASCADE;

COMMIT;

\echo '--- estado después del rollback ---'
SELECT count(*) AS filas, count(*) FILTER (WHERE deleted IS NULL) AS vivas
FROM users_seccions;
