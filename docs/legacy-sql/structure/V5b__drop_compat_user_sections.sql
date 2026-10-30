-- =====================================================================
-- Electorama — msvc-structure
-- V5b: FASE 5 — quitar la capa de compatibilidad de msvc_dash_user
--
-- ⚠️ PUNTO DE NO RETORNO. Borra la vista `users_seccions` y la tabla
-- congelada `users_seccions_legacy`. `V3dR` deja de tener sentido después de
-- esto: deshacía el corte devolviendo la tabla, y aquí la tabla se va.
--
-- Antes de ejecutar, en `msvc-users` tienen que estar migrados:
--   · `UserSeccionServiceImpl` y los tres endpoints de ADMIN
--     (alta de sección, baja, cambio de meta),
--   · `MetaReportByCoordinateZoneFacade`, `MetaReportByMunicipalitiesFacade`,
--     `MetaReportByUsersFacade` y `ReportMetaFacade`, que consultan
--     `users_seccions` en SQL nativo.
--
-- El sustituto ya existe: `GET /positions/scope/{authId}` da el ámbito con su
-- meta, y `PATCH /positions/{id}/goal` la cambia. El ámbito se calcula contra
-- el catálogo, así que además deja de quedarse atrás.
--
-- Antes de ejecutar: pg_dump de msvc_dash_user.
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 \
--        -v confirm=BORRAR -f V5b__drop_compat_user_sections.sql
-- =====================================================================

\set ON_ERROR_STOP on

\if :{?confirm}
\else
\set confirm 'no'
\endif

-- Las variables de psql NO se interpolan dentro de un bloque con comillas de
-- dólar, así que la confirmación viaja como parámetro de sesión. Mismo truco
-- que en V3b.
SET mig.confirm = :'confirm';

DO $$
DECLARE
  v_confirm text := current_setting('mig.confirm', true);
  v_filas   bigint;
BEGIN
  IF v_confirm <> 'BORRAR' THEN
    RAISE EXCEPTION E'La fase 5 no se ejecuta por accidente.\n'
      '  Esto borra `users_seccions` y su tabla congelada, sin vuelta.\n'
      '  Si de verdad toca: psql … -v confirm=BORRAR -f V5b__drop_compat_user_sections.sql';
  END IF;

  -- el modelo tiene que estar sirviendo el ámbito, o se pierde
  SELECT count(*) INTO v_filas FROM str.user_sections;
  IF v_filas = 0 THEN
    RAISE EXCEPTION 'el modelo no está sirviendo ámbito de coordinadores: revisa V3c/V2d antes';
  END IF;
  RAISE NOTICE 'el modelo sirve % filas de ámbito', v_filas;
END $$;

BEGIN;

DROP VIEW IF EXISTS users_seccions CASCADE;

DROP FUNCTION IF EXISTS compat.user_sections_insert() CASCADE;
DROP FUNCTION IF EXISTS compat.user_sections_update() CASCADE;
DROP FUNCTION IF EXISTS compat.user_sections_delete() CASCADE;
DROP FUNCTION IF EXISTS compat.us_resolve(bigint);
DROP FUNCTION IF EXISTS compat.us_override(bigint);
DROP FUNCTION IF EXISTS compat.materialize_municipal(bigint, integer);

DROP TABLE IF EXISTS users_seccions_legacy;

DROP SCHEMA IF EXISTS str CASCADE;
DROP SCHEMA IF EXISTS compat CASCADE;

COMMIT;

-- Ya ninguna base legacy habla con el modelo por FDW: el enlace se va.
DROP SERVER IF EXISTS srv_structure CASCADE;

\echo '--- lo que queda de secciones en msvc_dash_user ---'
SELECT table_name, table_type
FROM information_schema.tables
WHERE table_schema = 'public' AND table_name LIKE '%secci%'
ORDER BY 1;
