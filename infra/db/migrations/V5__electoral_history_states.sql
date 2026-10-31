-- Electorama: historial electoral de cualquier estado.
--
-- Hasta aquí el historial era sólo de Tabasco y nada decía de qué estado era
-- una casilla: el municipio era su clave (1..17), la sección su número y el
-- distrito su número, que se repiten entre estados. Ahora:
--
--   state                 las 32 entidades (clave INE)
--   election.state_id     NULL en las federales (una sola elección para todo el
--                         país); el estado en las locales
--   municipality          (state_id, cve); cada estado tiene su municipio 0 para
--                         voto anticipado, en prisión y en el extranjero
--   section_municipality  (year, state_id, section)
--   polling_station       state_id
--   candidacy             state_id: NULL sólo si la candidatura es nacional
--                         (presidencia); en senado y diputaciones es el estado
--                         y `territory` el distrito o municipio dentro de él
--   official_total        state_id: NULL = total nacional
--
-- Lo que ya había es Tabasco: se marca con 27.

CREATE TABLE electoral_history.state (
    id   smallint PRIMARY KEY,
    name character varying(40) NOT NULL
);

INSERT INTO electoral_history.state (id, name) VALUES
  (1, 'AGUASCALIENTES'), (2, 'BAJA CALIFORNIA'), (3, 'BAJA CALIFORNIA SUR'), (4, 'CAMPECHE'),
  (5, 'COAHUILA'), (6, 'COLIMA'), (7, 'CHIAPAS'), (8, 'CHIHUAHUA'), (9, 'CIUDAD DE MÉXICO'),
  (10, 'DURANGO'), (11, 'GUANAJUATO'), (12, 'GUERRERO'), (13, 'HIDALGO'), (14, 'JALISCO'),
  (15, 'MÉXICO'), (16, 'MICHOACÁN'), (17, 'MORELOS'), (18, 'NAYARIT'), (19, 'NUEVO LEÓN'),
  (20, 'OAXACA'), (21, 'PUEBLA'), (22, 'QUERÉTARO'), (23, 'QUINTANA ROO'), (24, 'SAN LUIS POTOSÍ'),
  (25, 'SINALOA'), (26, 'SONORA'), (27, 'TABASCO'), (28, 'TAMAULIPAS'), (29, 'TLAXCALA'),
  (30, 'VERACRUZ'), (31, 'YUCATÁN'), (32, 'ZACATECAS');

-- ===== fuentes: los institutos locales de los estados que se cargan =====
ALTER TABLE electoral_history.source DROP CONSTRAINT source_publisher_check;
ALTER TABLE electoral_history.source ADD CONSTRAINT source_publisher_check
  CHECK (publisher IN ('INE', 'IEPCT', 'IECM', 'OPLEV'));

-- ===== las vistas materializadas se rehacen al final =====
DROP MATERIALIZED VIEW electoral_history.mv_section_group;
DROP MATERIALIZED VIEW electoral_history.mv_section_turnout;

-- ===== election =====
ALTER TABLE electoral_history.election ADD COLUMN state_id smallint REFERENCES electoral_history.state (id);
UPDATE electoral_history.election
   SET state_id = 27
 WHERE office IN ('GOVERNOR', 'LOCAL_DEPUTY', 'MAYOR');
ALTER TABLE electoral_history.election DROP CONSTRAINT election_year_office_key;
CREATE UNIQUE INDEX election_year_office_uq
  ON electoral_history.election (year, office, coalesce(state_id, 0));
ALTER TABLE electoral_history.election ADD CONSTRAINT election_state_chk
  CHECK ((office IN ('PRESIDENT', 'SENATOR', 'FEDERAL_DEPUTY')) = (state_id IS NULL));

-- ===== municipality =====
ALTER TABLE electoral_history.official_total DROP CONSTRAINT official_total_municipality_cve_fkey;
ALTER TABLE electoral_history.polling_station DROP CONSTRAINT polling_station_municipality_cve_fkey;
ALTER TABLE electoral_history.section_municipality DROP CONSTRAINT section_municipality_municipality_cve_fkey;

ALTER TABLE electoral_history.municipality
  ADD COLUMN state_id smallint NOT NULL DEFAULT 27 REFERENCES electoral_history.state (id);
ALTER TABLE electoral_history.municipality ALTER COLUMN state_id DROP DEFAULT;
ALTER TABLE electoral_history.municipality DROP CONSTRAINT municipality_pkey;
ALTER TABLE electoral_history.municipality ADD PRIMARY KEY (state_id, cve);

-- ===== section_municipality =====
ALTER TABLE electoral_history.section_municipality ADD COLUMN state_id smallint NOT NULL DEFAULT 27;
ALTER TABLE electoral_history.section_municipality ALTER COLUMN state_id DROP DEFAULT;
ALTER TABLE electoral_history.section_municipality DROP CONSTRAINT section_municipality_pkey;
ALTER TABLE electoral_history.section_municipality ADD PRIMARY KEY (year, state_id, section);
ALTER TABLE electoral_history.section_municipality ADD FOREIGN KEY (state_id, municipality_cve)
  REFERENCES electoral_history.municipality (state_id, cve);

-- ===== polling_station =====
ALTER TABLE electoral_history.polling_station ADD COLUMN state_id smallint NOT NULL DEFAULT 27;
ALTER TABLE electoral_history.polling_station ALTER COLUMN state_id DROP DEFAULT;
ALTER TABLE electoral_history.polling_station ADD FOREIGN KEY (state_id, municipality_cve)
  REFERENCES electoral_history.municipality (state_id, cve);
DROP INDEX electoral_history.polling_station_mun_idx;
DROP INDEX electoral_history.polling_station_section_idx;
CREATE INDEX polling_station_mun_idx ON electoral_history.polling_station (election_id, state_id, municipality_cve);
CREATE INDEX polling_station_section_idx ON electoral_history.polling_station (election_id, state_id, section);

-- ===== candidacy =====
ALTER TABLE electoral_history.candidacy ADD COLUMN state_id smallint REFERENCES electoral_history.state (id);
UPDATE electoral_history.candidacy c
   SET state_id = 27
  FROM electoral_history.election e
 WHERE e.id = c.election_id AND e.office <> 'PRESIDENT';
DROP INDEX electoral_history.candidacy_uq;
CREATE UNIQUE INDEX candidacy_uq
  ON electoral_history.candidacy (election_id, coalesce(state_id, 0), coalesce(territory, ''), code);

-- ===== official_total =====
ALTER TABLE electoral_history.official_total ADD COLUMN state_id smallint REFERENCES electoral_history.state (id);
UPDATE electoral_history.official_total SET state_id = 27;
ALTER TABLE electoral_history.official_total ADD FOREIGN KEY (state_id, municipality_cve)
  REFERENCES electoral_history.municipality (state_id, cve);
DROP INDEX electoral_history.official_total_territory_uq;
CREATE UNIQUE INDEX official_total_territory_uq
  ON electoral_history.official_total (election_id, coalesce(state_id, 0), coalesce(municipality_cve::integer, -1),
                                       coalesce(district, ''), code);

-- ===== vistas materializadas, ahora por estado =====
CREATE MATERIALIZED VIEW electoral_history.mv_section_group AS
 WITH votes AS (
         SELECT p.election_id, p.state_id, p.municipality_cve, p.local_district, p.federal_district,
                p.section, p.district, e.year, e.scope, o.kind, o.raw_label, o.party_codes, v.votes
           FROM electoral_history.station_vote v
           JOIN electoral_history.polling_station p ON p.id = v.station_id
           JOIN electoral_history.election e ON e.id = p.election_id
           JOIN electoral_history.ballot_option o ON o.id = v.option_id
          WHERE o.kind <> 'DERIVED_TOTAL' AND p.counted
        )
 SELECT v.election_id, v.state_id, v.municipality_cve, v.local_district, v.federal_district, v.section,
        'candidacy'::character varying(10) AS group_by, c.code, sum(v.votes) AS votes
   FROM votes v
   JOIN electoral_history.candidacy c
     ON c.election_id = v.election_id
    AND cardinality(v.party_codes) > 0
    AND v.party_codes <@ c.party_codes
    AND (c.state_id IS NULL OR c.state_id = v.state_id)
    AND (c.territory IS NULL
         OR (v.scope = 'MUNICIPALITY' AND c.territory = v.municipality_cve::text)
         OR (v.scope IN ('FEDERAL_DISTRICT', 'LOCAL_DISTRICT') AND c.territory = v.district))
  GROUP BY v.election_id, v.state_id, v.municipality_cve, v.local_district, v.federal_district, v.section, c.code
UNION ALL
 SELECT v.election_id, v.state_id, v.municipality_cve, v.local_district, v.federal_district, v.section,
        'party'::character varying, v.party_codes[1], sum(v.votes)
   FROM votes v
  WHERE v.kind = 'PARTY'
  GROUP BY v.election_id, v.state_id, v.municipality_cve, v.local_district, v.federal_district, v.section,
           v.party_codes[1]
UNION ALL
 SELECT v.election_id, v.state_id, v.municipality_cve, v.local_district, v.federal_district, v.section,
        'bloc'::character varying, coalesce(b.bloc_code, 'OTHERS'), sum(v.votes)
   FROM votes v
   LEFT JOIN LATERAL (SELECT min(bp.bloc_code::text) AS bloc_code
                        FROM unnest(v.party_codes) pc(pc)
                        JOIN electoral_history.bloc_party bp ON bp.year = v.year AND bp.party_code = pc.pc
                      HAVING count(DISTINCT bp.bloc_code) = 1 AND count(*) = cardinality(v.party_codes)) b ON true
  GROUP BY v.election_id, v.state_id, v.municipality_cve, v.local_district, v.federal_district, v.section,
           coalesce(b.bloc_code, 'OTHERS')
  WITH NO DATA;

CREATE UNIQUE INDEX mv_section_group_uq ON electoral_history.mv_section_group
  (election_id, state_id, municipality_cve, coalesce(local_district::integer, 0),
   coalesce(federal_district::integer, 0), section, group_by, code);

CREATE MATERIALIZED VIEW electoral_history.mv_section_turnout AS
 SELECT p.election_id, p.state_id, p.municipality_cve, p.local_district, p.federal_district, p.section,
        count(*) FILTER (WHERE p.counted) AS stations,
        count(*) FILTER (WHERE p.counted AND p.station_type NOT IN ('B', 'C', 'E')) AS special_stations,
        sum(p.nominal_list) FILTER (WHERE p.station_type IN ('B', 'C', 'E')) AS nominal_list,
        sum(p.total_votes) FILTER (WHERE p.counted) AS total_votes,
        sum(p.total_votes) FILTER (WHERE p.counted AND p.station_type IN ('B', 'C', 'E')) AS ordinary_votes,
        sum(p.null_votes) FILTER (WHERE p.counted) AS null_votes,
        sum(p.non_registered_votes) FILTER (WHERE p.counted) AS non_registered_votes
   FROM electoral_history.polling_station p
  GROUP BY p.election_id, p.state_id, p.municipality_cve, p.local_district, p.federal_district, p.section
  WITH NO DATA;

CREATE UNIQUE INDEX mv_section_turnout_uq ON electoral_history.mv_section_turnout
  (election_id, state_id, municipality_cve, coalesce(local_district::integer, 0),
   coalesce(federal_district::integer, 0), section);
