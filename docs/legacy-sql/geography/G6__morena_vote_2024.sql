-- =====================================================================
-- Electorama — msvc-geography
-- G6: votación de MORENA 2024 por municipio, como referencia de la campaña
--
-- El tablero de campaña repite la hoja "TBD_RESUMEN" de
-- "estructura estatal.xlsx", que trae por municipio la "Suma de VOTACION
-- PRES MPAL MORENA". Se carga sólo el total municipal:
--
--   · Por sección no es confiable: en 181 filas no cuadra con
--     PVEM + MORENA + PVEM_MORENA, y no trae las secciones 1204-1215.
--   · La base de resultados (msvc_dash_elections_results) sólo tiene
--     capturado entre 1 % y 10 % de las casillas: no sirve para esto.
--
-- Cuando estén los cómputos oficiales del IEPCT, se reemplaza la fuente.
--
--   voto_morena_2024_27.txt   municipio|votos   17 filas, total 1,031,868
--
-- Requiere G1 (`staging.norm`). Es IDEMPOTENTE. Se ejecuta desde la
-- carpeta de los archivos:
--
--   cd C:\Users\juanr\geo-2026
--   psql -U postgres -d msvc_dash_geografia -v ON_ERROR_STOP=1 -f G6__morena_vote_2024.sql
-- =====================================================================

\set ON_ERROR_STOP on

CREATE SCHEMA IF NOT EXISTS staging;

DROP TABLE IF EXISTS staging.stg_morena_vote_2024_27;
CREATE TABLE staging.stg_morena_vote_2024_27 (nom_mun text, votes int);
\copy staging.stg_morena_vote_2024_27 from 'voto_morena_2024_27.txt' with (format csv, delimiter '|')

BEGIN;

ALTER TABLE geo_municipios ADD COLUMN IF NOT EXISTS morena_vote_2024 integer;

UPDATE geo_municipios m
   SET morena_vote_2024 = v.votes
  FROM staging.stg_morena_vote_2024_27 v
 WHERE m.entidad_id = 27
   AND staging.norm(m.nom_mun) = staging.norm(v.nom_mun)
   AND m.morena_vote_2024 IS DISTINCT FROM v.votes;

-- verificación: los 17 municipios con dato y el total de la hoja
SELECT (SELECT count(*) FROM staging.stg_morena_vote_2024_27) AS filas_fuente,
       (SELECT count(*) FROM geo_municipios WHERE entidad_id = 27 AND morena_vote_2024 IS NOT NULL) AS municipios_con_voto,
       (SELECT sum(morena_vote_2024) FROM geo_municipios WHERE entidad_id = 27) AS votos_total,
       (SELECT string_agg(v.nom_mun, ', ') FROM staging.stg_morena_vote_2024_27 v
         WHERE NOT EXISTS (SELECT 1 FROM geo_municipios m
                            WHERE m.entidad_id = 27 AND staging.norm(m.nom_mun) = staging.norm(v.nom_mun))) AS sin_municipio;

COMMIT;
