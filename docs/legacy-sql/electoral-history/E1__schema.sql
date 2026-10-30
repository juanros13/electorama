-- =====================================================================
-- Electorama — msvc-electoral-history
-- E1: esquema del histórico electoral de Tabasco (2018, 2021, 2024)
--
-- Resultados OFICIALES por casilla (cómputos del INE y del IEPCT) para
-- analizar cómo votó cada municipio y sección. Nada de aquí se captura a
-- mano: todo entra con E2..E4 desde archivos con su sha256 (fuentes/manifest.txt).
--
-- Piezas:
--   source / election      de dónde sale cada elección y su fecha de corte
--   municipality           los 17 de Tabasco + 0 = voto anticipado, en
--                          prisión y en el extranjero (no son de ningún
--                          municipio, pero cuentan en el total estatal)
--   section_municipality   sección → municipio POR AÑO: el INE no trae
--                          municipio. Sale del archivo de ayuntamientos del
--                          IEPCT de ese año (2018, 2021, 2024), que tiene todas
--                          las secciones de su marco; geography queda de
--                          respaldo como año 2026. geography ya no conserva
--                          todas las secciones de 2018/2021: algunas se
--                          eliminaron.
--   party / ballot_option  lo que trae la boleta. `ballot_option` es la
--                          fuente de verdad: toda columna de votos del archivo
--                          debe estar aquí (E3 aborta si no), con su tipo y
--                          los partidos que suma. DERIVED_TOTAL son columnas
--                          "TOTAL PVEM Y MORENA" del IEPCT que repiten votos:
--                          se guardan pero NO se suman.
--   candidacy              la coalición real en cada territorio
--   bloc / bloc_party      la agrupación analítica entre años (bloque 4T)
--   polling_station /      resultados por casilla
--   station_vote
--   official_total         lo publicado, para cuadrar (T1)
--
-- Es IDEMPOTENTE. Se ejecuta desde C:\Users\juanr\geo-2026\electoral-history.
-- Necesita sections_2018/2021/2024.txt (tools/normalize: normalize.py
-- sections <año>) y geo_sections_27.txt, exportado de geography:
--
--   psql -U postgres -d msvc_dash_geografia -At -F"|" \
--        -c "select s.seccion::int, m.cve_mun, s.status, s.distrito_l::int, s.distrito_f::int from geo_secciones s join geo_municipios m on m.id = s.municipio_id where s.entidad_id = 27 order by 1" \
--        > geo_sections_27.txt
--   psql -U postgres -d msvc_dash_electoral_history -v ON_ERROR_STOP=1 -f E1__schema.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

CREATE TABLE IF NOT EXISTS source (
  id            serial PRIMARY KEY,
  publisher     varchar(10)  NOT NULL CHECK (publisher IN ('INE', 'IEPCT')),
  url           text         NOT NULL,
  file_name     text         NOT NULL UNIQUE,
  sha256        char(64)     NOT NULL,
  cut_label     text         NOT NULL,   -- la fecha de corte tal como la publica
  downloaded_at date         NOT NULL,
  notes         text
);

CREATE TABLE IF NOT EXISTS election (
  id            serial PRIMARY KEY,
  year          smallint     NOT NULL,
  office        varchar(20)  NOT NULL CHECK (office IN
                  ('PRESIDENT', 'SENATOR', 'FEDERAL_DEPUTY', 'GOVERNOR', 'LOCAL_DEPUTY', 'MAYOR')),
  -- el territorio en que se gana: el estado, cada distrito o cada municipio
  scope         varchar(20)  NOT NULL CHECK (scope IN
                  ('STATE', 'FEDERAL_DISTRICT', 'LOCAL_DISTRICT', 'MUNICIPALITY')),
  election_date date         NOT NULL,
  source_id     integer      NOT NULL REFERENCES source (id),
  source_member text,                    -- el archivo dentro del zip, si aplica
  UNIQUE (year, office)
);

CREATE TABLE IF NOT EXISTS municipality (
  cve           smallint     PRIMARY KEY,   -- clave INE, igual que geo_municipios.cve_mun
  name          varchar(60)  NOT NULL
);

-- Se recrea entera en cada corrida: sale sólo de archivos.
DROP TABLE IF EXISTS section_municipality;
CREATE TABLE section_municipality (
  year             smallint  NOT NULL,   -- 2026 = geography, el respaldo
  section          integer   NOT NULL,
  municipality_cve smallint  NOT NULL REFERENCES municipality (cve),
  local_district   smallint,             -- del IEPCT de ese año
  federal_district smallint,             -- del INE de ese año
  PRIMARY KEY (year, section)
);

CREATE TABLE IF NOT EXISTS party (
  code          varchar(20)  PRIMARY KEY,   -- PES2018 y PES2021 son partidos distintos
  name          varchar(120) NOT NULL,
  color         char(7)
);

CREATE TABLE IF NOT EXISTS ballot_option (
  id            serial PRIMARY KEY,
  election_id   integer      NOT NULL REFERENCES election (id),
  raw_label     varchar(120) NOT NULL,      -- la columna tal como viene en el archivo
  kind          varchar(15)  NOT NULL CHECK (kind IN
                  ('PARTY', 'COMBINATION', 'INDEPENDENT', 'DERIVED_TOTAL')),
  party_codes   varchar(20)[] NOT NULL,     -- los partidos que suma; vacío en independientes
  label         varchar(120) NOT NULL,      -- cómo se muestra
  UNIQUE (election_id, raw_label)
);

-- La coalición real con que se compitió. territory NULL = en todo el estado;
-- si no, la clave del municipio o el número de distrito según el scope.
CREATE TABLE IF NOT EXISTS candidacy (
  id            serial PRIMARY KEY,
  election_id   integer      NOT NULL REFERENCES election (id),
  territory     varchar(10),
  code          varchar(40)  NOT NULL,
  name          varchar(160) NOT NULL,
  party_codes   varchar(20)[] NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS candidacy_uq
  ON candidacy (election_id, coalesce(territory, ''), code);

CREATE TABLE IF NOT EXISTS bloc (
  code          varchar(20)  PRIMARY KEY,
  name          varchar(80)  NOT NULL,
  color         char(7)
);

CREATE TABLE IF NOT EXISTS bloc_party (
  year          smallint     NOT NULL,
  party_code    varchar(20)  NOT NULL REFERENCES party (code),
  bloc_code     varchar(20)  NOT NULL REFERENCES bloc (code),
  PRIMARY KEY (year, party_code)
);

CREATE TABLE IF NOT EXISTS polling_station (
  id                   serial PRIMARY KEY,
  election_id          integer     NOT NULL REFERENCES election (id),
  station_key          varchar(120) NOT NULL,  -- INE: clave de acta; IEPCT: distrito/municipio/sección/casilla
  section              integer     NOT NULL,
  district             varchar(10) NOT NULL,
  municipality_cve     smallint    NOT NULL REFERENCES municipality (cve),
  -- B básica, C contigua, E extraordinaria, S especial; A anticipado,
  -- P prisión preventiva, V/M extranjero
  station_type         varchar(10) NOT NULL,
  nominal_list         integer     NOT NULL,
  total_votes          integer     NOT NULL,
  null_votes           integer     NOT NULL,
  non_registered_votes integer     NOT NULL,
  counted              boolean     NOT NULL,
  source_row           integer     NOT NULL,
  UNIQUE (election_id, station_key)
);
-- la clave del IEPCT es larga (distrito/municipio/sección/casilla)
ALTER TABLE polling_station ALTER COLUMN station_key TYPE varchar(120);
CREATE INDEX IF NOT EXISTS polling_station_mun_idx ON polling_station (election_id, municipality_cve);
CREATE INDEX IF NOT EXISTS polling_station_section_idx ON polling_station (election_id, section);

CREATE TABLE IF NOT EXISTS station_vote (
  station_id    integer      NOT NULL REFERENCES polling_station (id) ON DELETE CASCADE,
  option_id     integer      NOT NULL REFERENCES ballot_option (id),
  votes         integer      NOT NULL,
  PRIMARY KEY (station_id, option_id)
);

-- Lo que publicó el órgano electoral: estatal (municipio y distrito NULL), por
-- municipio o por distrito. code: una candidatura (candidacy.code), el
-- conjunto de partidos de una columna (SET:MC,PAN,PRD), KIND:INDEPENDENT, o
-- TOTAL, NULL, NON_REGISTERED, NOMINAL_LIST, STATIONS.
CREATE TABLE IF NOT EXISTS official_total (
  election_id      integer     NOT NULL REFERENCES election (id),
  municipality_cve smallint    REFERENCES municipality (cve),
  code             varchar(40) NOT NULL,
  value            bigint      NOT NULL
);
-- el distrito, para los concentrados distritales del IEPCT
ALTER TABLE official_total ADD COLUMN IF NOT EXISTS district varchar(10);
DROP INDEX IF EXISTS official_total_uq;
CREATE UNIQUE INDEX IF NOT EXISTS official_total_territory_uq
  ON official_total (election_id, coalesce(municipality_cve, -1), coalesce(district, ''), code);

-- -------------------------------------------------------------- catálogos fijos

INSERT INTO municipality (cve, name) VALUES
  (0, 'VOTO ANTICIPADO, EN PRISIÓN Y EN EL EXTRANJERO'),
  (1, 'BALANCÁN'), (2, 'CÁRDENAS'), (3, 'CENTLA'), (4, 'CENTRO'), (5, 'COMALCALCO'),
  (6, 'CUNDUACÁN'), (7, 'EMILIANO ZAPATA'), (8, 'HUIMANGUILLO'), (9, 'JALAPA'),
  (10, 'JALPA DE MÉNDEZ'), (11, 'JONUTA'), (12, 'MACUSPANA'), (13, 'NACAJUCA'),
  (14, 'PARAÍSO'), (15, 'TACOTALPA'), (16, 'TEAPA'), (17, 'TENOSIQUE')
ON CONFLICT (cve) DO UPDATE SET name = EXCLUDED.name;

INSERT INTO bloc (code, name, color) VALUES
  ('MORENA_ALLIES', 'MORENA y aliados (4T)', '#9D2449'),
  ('PAN_PRI_PRD',   'PAN, PRI y PRD',        '#1F4E9E'),
  ('MC',            'Movimiento Ciudadano',  '#F28C28'),
  ('OTHERS',        'Otros',                 '#8A8A8A')
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, color = EXCLUDED.color;

CREATE TEMP TABLE stg_geo (section int, cve_mun text, status text, local int, federal int);
\copy stg_geo from 'geo_sections_27.txt' with (format csv, delimiter '|')
INSERT INTO section_municipality (year, section, municipality_cve, local_district, federal_district)
SELECT 2026, section, cve_mun::smallint, local, federal FROM stg_geo;

-- el nombre del municipio del IEPCT, sin acentos, contra el catálogo
CREATE TEMP TABLE stg_year (year int, section int, municipality text, local int, federal int);
\copy stg_year (section, municipality, local, federal) from 'sections_2018.txt' with (format csv, delimiter '|')
UPDATE stg_year SET year = 2018 WHERE year IS NULL;
\copy stg_year (section, municipality, local, federal) from 'sections_2021.txt' with (format csv, delimiter '|')
UPDATE stg_year SET year = 2021 WHERE year IS NULL;
\copy stg_year (section, municipality, local, federal) from 'sections_2024.txt' with (format csv, delimiter '|')
UPDATE stg_year SET year = 2024 WHERE year IS NULL;

DO $$
DECLARE unmatched text;
BEGIN
  SELECT string_agg(DISTINCT s.municipality, ', ') INTO unmatched
    FROM stg_year s
   WHERE NOT EXISTS (SELECT 1 FROM municipality m
                      WHERE upper(translate(m.name, 'ÁÉÍÓÚÜÑ', 'AEIOUUN'))
                          = upper(translate(s.municipality, 'ÁÉÍÓÚÜÑáéíóúüñ', 'AEIOUUNaeiouun')));
  IF unmatched IS NOT NULL THEN
    RAISE EXCEPTION 'municipios del IEPCT sin casar: %', unmatched;
  END IF;
END $$;

INSERT INTO section_municipality (year, section, municipality_cve, local_district, federal_district)
SELECT s.year, s.section, m.cve, s.local, s.federal
  FROM stg_year s
  JOIN municipality m
    ON upper(translate(m.name, 'ÁÉÍÓÚÜÑ', 'AEIOUUN'))
     = upper(translate(s.municipality, 'ÁÉÍÓÚÜÑáéíóúüñ', 'AEIOUUNaeiouun'));

-- en cada casilla, sus distritos (local y federal) del año de su elección:
-- permiten leer cualquier elección por municipio, distrito local o federal
ALTER TABLE polling_station ADD COLUMN IF NOT EXISTS local_district smallint;
ALTER TABLE polling_station ADD COLUMN IF NOT EXISTS federal_district smallint;

-- verificación: cada año con sus 17 municipios, 21 distritos locales y 6 federales
SELECT year, count(*) AS secciones, count(DISTINCT municipality_cve) AS municipios,
       count(DISTINCT local_district) AS distritos_locales,
       count(DISTINCT federal_district) AS distritos_federales
  FROM section_municipality GROUP BY year ORDER BY year;

COMMIT;
