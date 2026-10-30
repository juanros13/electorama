-- =====================================================================
-- Electorama — msvc-structure
-- V5c: FASE 5 — quitar el andamiaje de la migración
--
-- Lo último. Borra de msvc_dash_structure todo lo que existía sólo para
-- migrar: las copias locales del modelo viejo (`leg`), las tablas foráneas a
-- las bases legacy y a geography, y el esquema `compat` con sus vistas.
--
-- Se ejecuta DESPUÉS de V5a y V5b, y con ellas ya corridas nada de esto tiene
-- lectores. El modelo se queda solo, que es el objetivo: 8 tablas más
-- `membership_detail`, sin vistas de compatibilidad y sin enlaces a las bases
-- viejas.
--
-- Lo que se pierde y hay que asumir: `leg.*` era la única copia local de las
-- tablas viejas dentro de esta base, así que después de esto las
-- reconciliaciones de V2..V4b ya no se pueden repetir. Los dumps de la fase 0
-- son la única forma de volver a mirar el modelo viejo.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 \
--        -v confirm=BORRAR -f V5c__drop_migration_scaffolding.sql
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
BEGIN
  IF v_confirm <> 'BORRAR' THEN
    RAISE EXCEPTION E'La fase 5 no se ejecuta por accidente.\n'
      '  Esto borra las copias locales del modelo viejo y las vistas de compatibilidad.\n'
      '  Si de verdad toca: psql … -v confirm=BORRAR -f V5c__drop_migration_scaffolding.sql';
  END IF;
END $$;

BEGIN;

-- las vistas de compatibilidad y sus ayudantes
DROP SCHEMA IF EXISTS compat CASCADE;

-- las copias locales del modelo viejo y las tablas foráneas
DROP SCHEMA IF EXISTS leg     CASCADE;
DROP SCHEMA IF EXISTS leg_fol CASCADE;
DROP SCHEMA IF EXISTS leg_usr CASCADE;
DROP SCHEMA IF EXISTS leg_geo CASCADE;
DROP SCHEMA IF EXISTS geo_ref CASCADE;

COMMIT;

-- los enlaces a las bases viejas
DROP SERVER IF EXISTS srv_followers CASCADE;
DROP SERVER IF EXISTS srv_users     CASCADE;
DROP SERVER IF EXISTS srv_geo       CASCADE;

-- postgres_fdw se queda instalado: el modelo no lo usa, pero desinstalarlo no
-- gana nada y sí obliga a un superusuario para volver a ponerlo.

\echo '--- el modelo, solo ---'
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
ORDER BY 1;

\echo '--- esquemas que quedan ---'
SELECT nspname FROM pg_namespace
WHERE nspname NOT LIKE 'pg_%' AND nspname <> 'information_schema'
ORDER BY 1;

\echo '--- tamaño de las tablas del modelo ---'
SELECT relname AS tabla, pg_size_pretty(pg_total_relation_size(c.oid)) AS tamano,
       n_live_tup AS filas
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
LEFT JOIN pg_stat_user_tables s ON s.relid = c.oid
WHERE n.nspname = 'public' AND c.relkind = 'r'
ORDER BY pg_total_relation_size(c.oid) DESC;
