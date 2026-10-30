-- =====================================================================
-- Electorama — msvc-geography
-- G1: carga a `staging` el catálogo geoelectoral INE 2026
--
-- Es el insumo de G2..G4. Las fuentes son las descargas manuales del
-- SIGE (corte enero 2026) que viven en C:\Users\juanr\geo-2026\:
--
--   ine_mun_nac.txt     ent|mun|nombre            2,477 municipios
--                       awk -F'|' 'NR>1 {k=$1"|"$3"|"$4; if(!(k in s)){s[k]=1; print k}}' \
--                         "Catalogodeinformaciongeoelectoral(EDMSLM).txt"
--                       (el archivo ya viene en UTF-8: no pasarlo por iconv)
--   ine_secc_nac.txt    ent|df|mun|seccion|tipo   73,264 secciones
--   ine_27_secc_dl.txt  el catálogo de secciones con distrito local de
--                       Tabasco, sin encabezado; es el único estado con DL
--
-- Es IDEMPOTENTE. Se ejecuta desde la carpeta de los archivos:
--
--   cd C:\Users\juanr\geo-2026
--   psql -U postgres -d msvc_dash_geografia -v ON_ERROR_STOP=1 -f G1__ine_2026_staging.sql
-- =====================================================================

\set ON_ERROR_STOP on

CREATE SCHEMA IF NOT EXISTS staging;
CREATE EXTENSION IF NOT EXISTS unaccent;

DROP TABLE IF EXISTS staging.stg_ine_mun, staging.stg_ine_secc, staging.stg_ine_secc_dl_27;

CREATE TABLE staging.stg_ine_mun (ent int, mun int, nom text);
CREATE TABLE staging.stg_ine_secc (ent int, df int, mun int, seccion int, tipo text);
CREATE TABLE staging.stg_ine_secc_dl_27 (ent int, nom_ent text, df int, dl int, mun int, seccion int);

\copy staging.stg_ine_mun from 'ine_mun_nac.txt' with (format csv, delimiter '|')
\copy staging.stg_ine_secc from 'ine_secc_nac.txt' with (format csv, delimiter '|')
\copy staging.stg_ine_secc_dl_27 from 'ine_27_secc_dl.txt' with (format csv, delimiter '|')

-- nombre comparable: sin acentos, en mayúsculas y sin signos ("GRAL. TREVIÑO" = "GRALTREVINO")
CREATE OR REPLACE FUNCTION staging.norm(t text) RETURNS text
  LANGUAGE sql IMMUTABLE AS
$$ SELECT regexp_replace(upper(unaccent(coalesce(t, ''))), '[^A-Z0-9]', '', 'g') $$;

SELECT (SELECT count(*) FROM staging.stg_ine_mun) AS municipios,
       (SELECT count(*) FROM staging.stg_ine_secc) AS secciones,
       (SELECT count(*) FROM staging.stg_ine_secc_dl_27) AS secciones_dl_tabasco;
