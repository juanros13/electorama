-- Prepara electoral_history para cargar elecciones de cualquier estado (V5):
--
--   municipality          los municipios de geography, con su clave INE, más el
--                         municipio 0 de cada estado (voto anticipado, en prisión
--                         y en el extranjero)
--   section_municipality  año 2026: sección → municipio y distritos según
--                         geography (vigentes y dadas de baja). Es el respaldo
--                         cuando el año de la elección no trae su propia
--                         relación, como hace Tabasco con la del IEPCT.
--
-- No toca lo que ya existe. Es IDEMPOTENTE; requiere la geografía cargada.
--
--   psql -d electorama -v ON_ERROR_STOP=1 -f infra/db/reference/electoral-history/prepare-states.sql

BEGIN;

INSERT INTO electoral_history.municipality (state_id, cve, name)
SELECT m.entidad_id, m.cve_mun::smallint, m.nom_mun
  FROM geography.geo_municipios m
ON CONFLICT DO NOTHING;

INSERT INTO electoral_history.municipality (state_id, cve, name)
SELECT s.id, 0, 'VOTO ANTICIPADO, EN PRISIÓN Y EN EL EXTRANJERO'
  FROM electoral_history.state s
ON CONFLICT DO NOTHING;

INSERT INTO electoral_history.section_municipality (year, state_id, section, municipality_cve,
                                                   local_district, federal_district)
SELECT 2026, s.entidad_id, s.seccion::int, m.cve_mun::smallint, s.distrito_loc, s.distrito_fed
  FROM geography.geo_secciones s
  JOIN geography.geo_municipios m ON m.id = s.municipio_id
ON CONFLICT DO NOTHING;

COMMIT;

SELECT state_id, count(*) AS municipios FROM electoral_history.municipality GROUP BY 1 ORDER BY 1;
