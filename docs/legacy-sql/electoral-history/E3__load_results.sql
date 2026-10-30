-- =====================================================================
-- Electorama — msvc-electoral-history
-- E3: carga los resultados por casilla de UNA elección
--
-- Lee lo que dejó tools/normalize/normalize.py:
--   st_<año>_<cargo>.txt   casillas
--   vt_<año>_<cargo>.txt   votos por casilla y columna de la boleta
--   load_<año>_<cargo>.sql los \copy de esos dos archivos
--
-- Reglas:
--   · Toda etiqueta de vt debe estar en ballot_option de esa elección (E2).
--     Si llega una que no, aborta: es una columna nueva que nadie clasificó.
--   · El municipio: si el archivo lo trae (IEPCT), se casa por nombre; si no
--     (INE), sale de section_municipality del MISMO año (geography, 2026,
--     sólo de respaldo).
--   · Las casillas de voto anticipado (A), en prisión preventiva (P) y del
--     extranjero (V, M) van al municipio 0: son del estado, no de un municipio.
--   · Reemplaza la elección completa en una transacción: se puede volver a
--     correr sin duplicar.
--
-- Es IDEMPOTENTE. Requiere E1 y E2. Se ejecuta desde
-- C:\Users\juanr\geo-2026\electoral-history:
--
--   psql -U postgres -d msvc_dash_electoral_history -v ON_ERROR_STOP=1 \
--        -v year=2024 -v office=PRESIDENT -f E3__load_results.sql
-- =====================================================================

\set ON_ERROR_STOP on
-- psql no expande variables dentro de \copy; el normalizador deja un
-- load_<año>_<cargo>.sql con los dos \copy y aquí se incluye con \i.
\set load_file 'load_' :year '_' :office '.sql'

CREATE TEMP TABLE stg_station (
  station_key text, section int, district text, municipality text, station_type text,
  nominal_list int, total_votes int, null_votes int, non_registered_votes int,
  counted boolean, source_row int
);
CREATE TEMP TABLE stg_vote (station_key text, raw_label text, votes int);

\i :load_file

BEGIN;

CREATE TEMP TABLE target AS
SELECT id, year FROM election WHERE year = :year AND office = :'office';

DO $$
DECLARE
  n int;
  labels text;
  orphans text;
BEGIN
  SELECT count(*) INTO n FROM target;
  IF n <> 1 THEN
    RAISE EXCEPTION 'la elección no está en el catálogo (E2)';
  END IF;

  SELECT string_agg(DISTINCT v.raw_label, ', ') INTO labels
    FROM stg_vote v
   WHERE NOT EXISTS (SELECT 1 FROM ballot_option o
                      WHERE o.election_id = (SELECT id FROM target) AND o.raw_label = v.raw_label);
  IF labels IS NOT NULL THEN
    RAISE EXCEPTION 'columnas de votos sin clasificar en ballot_option: %', labels;
  END IF;

  -- una casilla que no es de mecanismo estatal debe tener municipio
  SELECT string_agg(DISTINCT s.section::text, ', ') INTO orphans
    FROM stg_station s
   WHERE s.station_type NOT IN ('A', 'P', 'V', 'M')
     AND coalesce(s.municipality, '') = ''
     AND NOT EXISTS (SELECT 1 FROM section_municipality m
                      WHERE m.section = s.section
                        AND m.year IN ((SELECT year FROM target), 2026));
  IF orphans IS NOT NULL THEN
    RAISE EXCEPTION 'secciones sin municipio: %', orphans;
  END IF;
END $$;

DELETE FROM polling_station WHERE election_id = (SELECT id FROM target);

INSERT INTO polling_station (election_id, station_key, section, district, municipality_cve, station_type,
                             nominal_list, total_votes, null_votes, non_registered_votes, counted, source_row)
SELECT (SELECT id FROM target), s.station_key, s.section, s.district,
       CASE
         -- el IEPCT sí dice de qué municipio es cada mesa de voto anticipado:
         -- en ayuntamientos ese voto es de la elección de ese municipio
         WHEN coalesce(s.municipality, '') = '' AND s.station_type IN ('A', 'P', 'V', 'M') THEN 0
         WHEN coalesce(s.municipality, '') <> '' THEN
           (SELECT m.cve FROM municipality m
             WHERE upper(translate(m.name, 'ÁÉÍÓÚÜÑ', 'AEIOUUN'))
                 = upper(translate(s.municipality, 'ÁÉÍÓÚÜÑáéíóúüñ', 'AEIOUUNaeiouun')))
         -- la del año de la elección; geography (2026) sólo si ese año no la tiene
         ELSE (SELECT sm.municipality_cve FROM section_municipality sm
                WHERE sm.section = s.section AND sm.year IN ((SELECT year FROM target), 2026)
                ORDER BY sm.year = (SELECT year FROM target) DESC
                LIMIT 1)
       END,
       s.station_type, s.nominal_list, s.total_votes, s.null_votes, s.non_registered_votes,
       s.counted, s.source_row
  FROM stg_station s;

-- los dos distritos de cada casilla, para leer la elección por distrito local o
-- federal: el que trae el archivo (IEPCT: local; INE: federal) y el otro de la
-- relación de secciones de ese año. Los mecanismos estatales (municipio 0) no
-- son de ningún distrito.
UPDATE polling_station p
   SET local_district = CASE
         WHEN p.municipality_cve = 0 THEN NULL
         WHEN src.publisher = 'IEPCT' AND p.district ~ '^\d+$' THEN p.district::smallint
         ELSE (SELECT s.local_district FROM section_municipality s
                WHERE s.section = p.section AND s.year IN (e.year, 2026)
                ORDER BY s.year = e.year DESC LIMIT 1)
       END,
       federal_district = CASE
         WHEN p.municipality_cve = 0 THEN NULL
         WHEN src.publisher = 'INE' AND p.district ~ '^\d+$' THEN p.district::smallint
         ELSE (SELECT s.federal_district FROM section_municipality s
                WHERE s.section = p.section AND s.year IN (e.year, 2026)
                ORDER BY s.year = e.year DESC LIMIT 1)
       END
  FROM election e
  JOIN source src ON src.id = e.source_id
 WHERE p.election_id = e.id
   AND e.id = (SELECT id FROM target);

INSERT INTO station_vote (station_id, option_id, votes)
SELECT p.id, o.id, v.votes
  FROM stg_vote v
  JOIN polling_station p ON p.election_id = (SELECT id FROM target) AND p.station_key = v.station_key
  JOIN ballot_option o ON o.election_id = p.election_id AND o.raw_label = v.raw_label;

-- verificación: casillas y votos cargados; ningún municipio nulo
SELECT e.year, e.office,
       count(DISTINCT p.id) AS casillas,
       sum(p.total_votes) AS votos_totales,
       sum(p.nominal_list) AS lista_nominal,
       count(*) FILTER (WHERE p.municipality_cve IS NULL) AS sin_municipio,
       (SELECT count(*) FROM station_vote sv JOIN polling_station ps ON ps.id = sv.station_id
         WHERE ps.election_id = e.id) AS filas_voto
  FROM election e
  JOIN polling_station p ON p.election_id = e.id
 WHERE e.id = (SELECT id FROM target)
 GROUP BY e.id, e.year, e.office;

COMMIT;
