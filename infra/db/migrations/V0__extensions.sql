-- Electorama: extensiones compartidas. Viven en `public` y cada servicio las ve
-- porque su search_path es `<schema>,public`.
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS btree_gist;
