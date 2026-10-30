-- =====================================================================
-- Electorama — msvc-structure
-- V5a: FASE 5 — quitar la capa de compatibilidad de msvc_dash_followers
--
-- ⚠️ PUNTO DE NO RETORNO. Después de esto `followers` y `promoters` no
-- existen en ninguna forma: ni tabla, ni vista. Todo lo que las lea tiene que
-- estar migrado a la API de msvc-structure ANTES de correr esto. No hay
-- rollback: `V3R` deshacía el corte devolviendo las tablas, y aquí las
-- tablas se borran.
--
-- Qué comprueba antes de borrar:
--   1. que el modelo tenga absorbido lo que sólo vivía aquí (V4, V4b),
--   2. que las vistas ya no dependan de `*_legacy` (V4d),
--   3. que quien lo ejecuta lo haya dicho dos veces.
--
-- Lo que NO puede comprobar, y por eso la confirmación es a mano: si algún
-- servicio sigue leyendo estas vistas. El inventario a cerrar antes:
--   · msvc-followers: 13 controladores, 8 facades y ~36 consultas nativas
--     contra `promoters`. Migrado hasta ahora: `FollowersPromotersFacade`.
--   · msvc-users: los tres endpoints de secciones y los facades de metas
--     (ésos dependen de V5b, no de este archivo).
--
-- Antes de ejecutar: pg_dump de msvc_dash_followers. La red de seguridad de
-- la fase 0 tiene meses.
--
--   psql -U postgres -d msvc_dash_followers -v ON_ERROR_STOP=1 \
--        -v confirm=BORRAR -f V5a__drop_compat_followers.sql
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

-- `membership_detail` no estaba entre las tablas importadas: la comprobación
-- de más abajo la necesita, así que se trae aquí si falta.
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                  WHERE table_schema = 'str' AND table_name = 'membership_detail') THEN
    EXECUTE 'IMPORT FOREIGN SCHEMA public LIMIT TO (membership_detail)'
            || ' FROM SERVER srv_structure INTO str';
  END IF;
END $$;

DO $$
DECLARE
  v_confirm text := current_setting('mig.confirm', true);
  v_detalle bigint;
  v_dep     int;
BEGIN
  IF v_confirm <> 'BORRAR' THEN
    RAISE EXCEPTION E'La fase 5 no se ejecuta por accidente.\n'
      '  Esto borra `followers` y `promoters` para siempre y no tiene vuelta.\n'
      '  Si de verdad toca: psql … -v confirm=BORRAR -f V5a__drop_compat_followers.sql';
  END IF;

  -- 1. el modelo tiene lo que sólo vivía aquí
  SELECT count(*) INTO v_detalle FROM str.membership_detail;
  IF v_detalle = 0 THEN
    RAISE EXCEPTION 'el modelo no tiene expedientes de captura: corre V4 antes de borrar nada';
  END IF;

  -- 2. las vistas ya no leen las tablas congeladas
  SELECT count(*) INTO v_dep
  FROM pg_depend d
  JOIN pg_rewrite r ON r.oid = d.objid
  JOIN pg_class v ON v.oid = r.ev_class
  JOIN pg_class t ON t.oid = d.refobjid
  WHERE v.relname IN ('followers', 'promoters')
    AND t.relname IN ('followers_legacy', 'promoters_legacy');
  IF v_dep > 0 THEN
    RAISE EXCEPTION 'las vistas todavía leen *_legacy (% dependencias): corre V4d antes', v_dep;
  END IF;

  RAISE NOTICE 'comprobaciones en orden: % expedientes en el modelo, 0 dependencias legacy', v_detalle;
END $$;

BEGIN;

-- ---------------------------------------------------------------------
-- 1. las vistas y sus triggers
-- ---------------------------------------------------------------------
DROP VIEW IF EXISTS followers CASCADE;
DROP VIEW IF EXISTS promoters CASCADE;

DROP FUNCTION IF EXISTS compat.followers_insert() CASCADE;
DROP FUNCTION IF EXISTS compat.followers_update() CASCADE;
DROP FUNCTION IF EXISTS compat.followers_delete() CASCADE;
DROP FUNCTION IF EXISTS compat.promoters_insert() CASCADE;
DROP FUNCTION IF EXISTS compat.promoters_update() CASCADE;
DROP FUNCTION IF EXISTS compat.promoters_delete() CASCADE;

-- ---------------------------------------------------------------------
-- 2. las tablas congeladas
--    Aquí se va la red de seguridad. El dump de la fase 0 y el que se pide
--    en la cabecera son lo único que queda.
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS followers_legacy;
DROP TABLE IF EXISTS promoters_legacy;

-- ---------------------------------------------------------------------
-- 3. el andamiaje
--    El esquema `str` y el servidor foráneo existían para que las vistas
--    leyeran el modelo. Sin vistas, no hacen falta: msvc-followers habla con
--    msvc-structure por HTTP.
-- ---------------------------------------------------------------------
DROP SCHEMA IF EXISTS str CASCADE;
DROP SCHEMA IF EXISTS compat CASCADE;

COMMIT;

-- El servidor foráneo se queda para el final, cuando también se haya cortado
-- msvc_dash_user (V5b): borrarlo aquí dejaría a esa base sin enlace.
\echo '--- lo que queda en msvc_dash_followers ---'
SELECT table_name, table_type
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY 1;

\echo '--- recordatorio ---'
\echo 'El servidor foráneo srv_structure sigue en pie. Se borra cuando msvc_dash_user'
\echo 'también esté cortada (V5b) y ninguna base lo use.'
