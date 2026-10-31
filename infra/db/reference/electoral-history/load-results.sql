-- Carga los resultados por casilla de UNA elección en electoral_history.
-- Es E3 (docs/legacy-sql/electoral-history) para cualquier estado.
--
-- Lee lo que deja tools/normalize/normalize.py en la carpeta de salida:
--   st_<año>_<cargo>.txt   casillas (la última columna es el estado)
--   vt_<año>_<cargo>.txt   votos por casilla y columna de la boleta
--   load_<año>_<cargo>.sql los \copy de esos dos archivos
--
-- Reglas (las de E3):
--   · Toda etiqueta de vt debe estar en ballot_option de esa elección; si no,
--     aborta. Las federales las da `ine_catalog.py national`.
--   · Municipio: si el archivo lo trae (institutos locales) se casa por nombre
--     dentro del estado, o por clave si viene numérica (IECM); si no (INE),
--     sale de section_municipality del mismo
--     año, o de 2026 (geography) como respaldo.
--   · Voto anticipado (A), en prisión (P) y del extranjero (V, M) van al
--     municipio 0 del estado. Una sección que no aparece en ninguna relación
--     también, y se reporta al final.
--   · Reemplaza la elección completa en una transacción.
--
-- `state` es la clave del estado en las locales; vacío en las federales.
-- Se ejecuta desde la carpeta de los archivos normalizados:
--
--   psql -d electorama -v ON_ERROR_STOP=1 -v year=2024 -v office=PRESIDENT -v state= \
--        -f <repo>/infra/db/reference/electoral-history/load-results.sql

\set ON_ERROR_STOP on
\set load_file 'load_' :year '_' :office '.sql'

CREATE TEMP TABLE stg_station (
  station_key text, section int, district text, municipality text, station_type text,
  nominal_list int, total_votes int, null_votes int, non_registered_votes int,
  counted boolean, source_row int, state_id smallint
);
CREATE TEMP TABLE stg_vote (station_key text, raw_label text, votes int);

\i :load_file

BEGIN;

CREATE TEMP TABLE target AS
SELECT id, year FROM electoral_history.election
 WHERE year = :year AND office = :'office'
   AND coalesce(state_id, 0) = coalesce(nullif(:'state', '')::smallint, 0);

DO $$
DECLARE
  n int;
  labels text;
BEGIN
  SELECT count(*) INTO n FROM target;
  IF n <> 1 THEN
    RAISE EXCEPTION 'la elección no está en el catálogo';
  END IF;

  SELECT string_agg(DISTINCT v.raw_label, ', ') INTO labels
    FROM stg_vote v
   WHERE NOT EXISTS (SELECT 1 FROM electoral_history.ballot_option o
                      WHERE o.election_id = (SELECT id FROM target) AND o.raw_label = v.raw_label);
  IF labels IS NOT NULL THEN
    RAISE EXCEPTION 'columnas de votos sin clasificar en ballot_option: %', labels;
  END IF;
END $$;

-- se reemplaza la elección completa
DELETE FROM electoral_history.station_vote
 WHERE station_id IN (SELECT id FROM electoral_history.polling_station WHERE election_id = (SELECT id FROM target));
DELETE FROM electoral_history.polling_station WHERE election_id = (SELECT id FROM target);

INSERT INTO electoral_history.polling_station (
       election_id, state_id, station_key, section, district, municipality_cve, station_type,
       nominal_list, total_votes, null_votes, non_registered_votes, counted, source_row)
SELECT (SELECT id FROM target), s.state_id, s.station_key, s.section, s.district,
       coalesce(
         CASE
           WHEN coalesce(s.municipality, '') = '' AND s.station_type IN ('A', 'P', 'V', 'M') THEN 0
           -- el IECM trae la clave de demarcación del INE
           WHEN s.municipality ~ '^\d+$' THEN
             (SELECT m.cve FROM electoral_history.municipality m
               WHERE m.state_id = s.state_id AND m.cve = s.municipality::smallint)
           WHEN coalesce(s.municipality, '') <> '' THEN
             (SELECT m.cve FROM electoral_history.municipality m
               WHERE m.state_id = s.state_id
                 AND upper(translate(m.name, 'ÁÉÍÓÚÜÑ', 'AEIOUUN'))
                   = upper(translate(s.municipality, 'ÁÉÍÓÚÜÑáéíóúüñ', 'AEIOUUNaeiouun')))
           ELSE (SELECT sm.municipality_cve FROM electoral_history.section_municipality sm
                  WHERE sm.state_id = s.state_id AND sm.section = s.section
                    AND sm.year IN ((SELECT year FROM target), 2026)
                  ORDER BY sm.year = (SELECT year FROM target) DESC
                  LIMIT 1)
         END, 0),
       s.station_type, s.nominal_list, s.total_votes, s.null_votes, s.non_registered_votes,
       s.counted, s.source_row
  FROM stg_station s;

-- los dos distritos de cada casilla: el que trae el archivo (INE: federal;
-- institutos locales: local) y el otro de la relación de secciones. Los
-- mecanismos estatales no son de ningún distrito; una sección que ya no está
-- en ninguna relación (reseccionada) conserva el distrito que trae el archivo.
UPDATE electoral_history.polling_station p
   SET local_district = CASE
         WHEN p.station_type IN ('A', 'P', 'V', 'M') THEN NULL
         WHEN src.publisher <> 'INE' AND p.district ~ '^\d+$' THEN p.district::smallint
         ELSE (SELECT s.local_district FROM electoral_history.section_municipality s
                WHERE s.state_id = p.state_id AND s.section = p.section AND s.year IN (e.year, 2026)
                ORDER BY s.year = e.year DESC LIMIT 1)
       END,
       federal_district = CASE
         WHEN p.station_type IN ('A', 'P', 'V', 'M') THEN NULL
         WHEN src.publisher = 'INE' AND p.district ~ '^\d+$' THEN p.district::smallint
         ELSE (SELECT s.federal_district FROM electoral_history.section_municipality s
                WHERE s.state_id = p.state_id AND s.section = p.section AND s.year IN (e.year, 2026)
                ORDER BY s.year = e.year DESC LIMIT 1)
       END
  FROM electoral_history.election e
  JOIN electoral_history.source src ON src.id = e.source_id
 WHERE p.election_id = e.id
   AND e.id = (SELECT id FROM target);

INSERT INTO electoral_history.station_vote (station_id, option_id, votes)
SELECT p.id, o.id, v.votes
  FROM stg_vote v
  JOIN electoral_history.polling_station p ON p.election_id = (SELECT id FROM target) AND p.station_key = v.station_key
  JOIN electoral_history.ballot_option o ON o.election_id = p.election_id AND o.raw_label = v.raw_label;

COMMIT;

-- verificación por estado: casillas, votos y secciones que no se pudieron ubicar
SELECT p.state_id,
       count(*) AS casillas,
       sum(p.total_votes) AS votos_totales,
       count(*) FILTER (WHERE p.municipality_cve = 0 AND p.station_type NOT IN ('A', 'P', 'V', 'M')) AS sin_municipio
  FROM electoral_history.polling_station p
 WHERE p.election_id = (SELECT id FROM target)
 GROUP BY p.state_id
 ORDER BY p.state_id;
