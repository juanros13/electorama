-- =====================================================================
-- Electorama — msvc-geography → msvc-structure
-- G4: resincroniza los distritos de `territory` con geography
--
-- `territory.federal_district_geo_id` / `local_district_geo_id` son copia
-- de `geo_secciones.distrito_federal_id` / `distrito_local_id` (V4 §1).
-- Después de G3 hay que volver a copiarlos. Es lo mismo que V4 §1, pero
-- V5c ya quitó la tabla foránea que usaba: aquí se crea y se borra.
--
-- También avisa si alguna sección del territorio quedó en otro municipio
-- (G2 no movió ninguna de Tabasco; si aparece alguna, hay que re-colgarla
-- a mano porque cambia el padre en el árbol).
--
-- Es IDEMPOTENTE. Se ejecuta en la BD de structure, después de G3.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f G4__sync_structure_territory.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

CREATE SCHEMA IF NOT EXISTS leg_geo;

DROP FOREIGN TABLE IF EXISTS leg_geo.g4_sections;
CREATE FOREIGN TABLE leg_geo.g4_sections (
  id integer, municipio_id integer, distrito_federal_id varchar(255), distrito_local_id integer
) SERVER srv_geo OPTIONS (schema_name 'public', table_name 'geo_secciones');

CREATE TEMP TABLE g4 AS SELECT * FROM leg_geo.g4_sections;

UPDATE territory t
   SET federal_district_geo_id = nullif(trim(g.distrito_federal_id), '')::bigint,
       local_district_geo_id   = g.distrito_local_id
  FROM g4 g
 WHERE t.type = 'SECTION'
   AND t.section_geo_id = g.id
   AND (t.federal_district_geo_id IS DISTINCT FROM nullif(trim(g.distrito_federal_id), '')::bigint
     OR t.local_district_geo_id   IS DISTINCT FROM g.distrito_local_id);

SELECT count(*) AS secciones_en_otro_municipio
  FROM territory t
  JOIN g4 g ON g.id = t.section_geo_id
 WHERE t.type = 'SECTION' AND t.valid_to IS NULL
   AND t.municipality_geo_id IS DISTINCT FROM g.municipio_id;

DROP FOREIGN TABLE leg_geo.g4_sections;

COMMIT;
