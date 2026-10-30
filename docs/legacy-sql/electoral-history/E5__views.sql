-- =====================================================================
-- Electorama — msvc-electoral-history
-- E5: agregados por sección para el tablero
--
-- Todo se agrega a SECCIÓN, con su municipio, su distrito local y su distrito
-- federal (los del año de la elección, E3). El servicio suma encima al
-- territorio que se pida —municipio, distrito local o distrito federal— y al
-- estado. Así el mismo agregado sirve para cualquier tabla y para el
-- desglose por sección.
--
--   mv_section_turnout  casillas, LN, votos, nulos y no registrados
--   mv_section_group    votos por sección en tres agrupaciones:
--     candidacy  la coalición registrada en ese territorio (en ayuntamientos
--                y diputaciones cambia por municipio/distrito). Una opción
--                suma a la candidatura si sus partidos están dentro de los de
--                la candidatura.
--     party      sólo los votos marcados con UN emblema (kind = PARTY). Los de
--                combinación no se reparten: el reparto oficial tiene reglas de
--                redondeo que no vale la pena imitar; se ven en `candidacy`.
--     bloc       el bloque analítico del año (bloc_party). Una opción es del
--                bloque si TODOS sus partidos son de ese bloque; si mezcla
--                bloques (no debería) va a OTHERS.
--
-- Participación: sólo casillas ordinarias (B, C, E) en numerador y
-- denominador. Las especiales (S) reciben votantes de otras secciones y los
-- mecanismos estatales (A, P, V, M) no tienen LN propia del municipio.
--
-- Las columnas DERIVED_TOTAL nunca se suman, y las casillas no computadas
-- (anuladas por el órgano electoral: 14 de ayuntamientos 2024, 8 de
-- gubernatura 2024) quedan fuera, igual que en el cómputo oficial.
--
-- Es IDEMPOTENTE: recrea las vistas y las refresca. Correr después de cada E3.
--
--   psql -U postgres -d msvc_dash_electoral_history -v ON_ERROR_STOP=1 -f E5__views.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

DROP MATERIALIZED VIEW IF EXISTS mv_section_group;
DROP MATERIALIZED VIEW IF EXISTS mv_section_turnout;

CREATE MATERIALIZED VIEW mv_section_turnout AS
SELECT p.election_id,
       p.municipality_cve,
       p.local_district,
       p.federal_district,
       p.section,
       count(*) FILTER (WHERE p.counted)                               AS stations,
       count(*) FILTER (WHERE p.counted AND p.station_type NOT IN ('B', 'C', 'E')) AS special_stations,
       -- la LN de las anuladas se conserva, como en el cómputo oficial
       sum(p.nominal_list) FILTER (WHERE p.station_type IN ('B', 'C', 'E')) AS nominal_list,
       sum(p.total_votes) FILTER (WHERE p.counted)                     AS total_votes,
       sum(p.total_votes) FILTER (WHERE p.counted AND p.station_type IN ('B', 'C', 'E')) AS ordinary_votes,
       sum(p.null_votes) FILTER (WHERE p.counted)                      AS null_votes,
       sum(p.non_registered_votes) FILTER (WHERE p.counted)            AS non_registered_votes
  FROM polling_station p
 GROUP BY 1, 2, 3, 4, 5;

CREATE UNIQUE INDEX mv_section_turnout_uq
  ON mv_section_turnout (election_id, municipality_cve, coalesce(local_district, 0),
                         coalesce(federal_district, 0), section);

CREATE MATERIALIZED VIEW mv_section_group AS
WITH votes AS (
  SELECT p.election_id, p.municipality_cve, p.local_district, p.federal_district, p.section,
         p.district, e.year, e.scope, o.kind, o.raw_label, o.party_codes, v.votes
    FROM station_vote v
    JOIN polling_station p ON p.id = v.station_id
    JOIN election e ON e.id = p.election_id
    JOIN ballot_option o ON o.id = v.option_id
   WHERE o.kind <> 'DERIVED_TOTAL'
     AND p.counted
)
-- candidatura, según el territorio donde se compitió
SELECT v.election_id, v.municipality_cve, v.local_district, v.federal_district, v.section,
       'candidacy'::varchar(10) AS group_by, c.code, sum(v.votes) AS votes
  FROM votes v
  JOIN candidacy c
    ON c.election_id = v.election_id
   AND cardinality(v.party_codes) > 0
   AND v.party_codes <@ c.party_codes
   AND (c.territory IS NULL
        OR (v.scope = 'MUNICIPALITY' AND c.territory = v.municipality_cve::text)
        OR (v.scope IN ('FEDERAL_DISTRICT', 'LOCAL_DISTRICT') AND c.territory = v.district))
 GROUP BY 1, 2, 3, 4, 5, 7
UNION ALL
-- partido: sólo votos de un emblema
SELECT v.election_id, v.municipality_cve, v.local_district, v.federal_district, v.section,
       'party', v.party_codes[1], sum(v.votes)
  FROM votes v
 WHERE v.kind = 'PARTY'
 GROUP BY 1, 2, 3, 4, 5, 7
UNION ALL
-- bloque analítico del año
SELECT v.election_id, v.municipality_cve, v.local_district, v.federal_district, v.section,
       'bloc', coalesce(b.bloc_code, 'OTHERS'), sum(v.votes)
  FROM votes v
  LEFT JOIN LATERAL (
         SELECT min(bp.bloc_code) AS bloc_code
           FROM unnest(v.party_codes) pc
           JOIN bloc_party bp ON bp.year = v.year AND bp.party_code = pc
         HAVING count(DISTINCT bp.bloc_code) = 1
            AND count(*) = cardinality(v.party_codes)
       ) b ON true
 GROUP BY 1, 2, 3, 4, 5, 7;

CREATE UNIQUE INDEX mv_section_group_uq
  ON mv_section_group (election_id, municipality_cve, coalesce(local_district, 0),
                       coalesce(federal_district, 0), section, group_by, code);

-- verificación: los tres agrupamientos suman lo mismo que la boleta (sin
-- DERIVED_TOTAL), salvo `party`, que deja fuera las combinaciones
SELECT e.year, e.office, g.group_by, sum(g.votes) AS votos,
       (SELECT sum(t.total_votes - t.null_votes - t.non_registered_votes)
          FROM mv_section_turnout t WHERE t.election_id = e.id) AS votos_por_opcion
  FROM mv_section_group g
  JOIN election e ON e.id = g.election_id
 GROUP BY e.id, e.year, e.office, g.group_by
 ORDER BY e.year, e.office, g.group_by;

-- verificación: fuera de los mecanismos estatales, toda sección tiene sus dos distritos
SELECT e.year, e.office,
       count(*) FILTER (WHERE t.municipality_cve <> 0 AND t.local_district IS NULL) AS sin_distrito_local,
       count(*) FILTER (WHERE t.municipality_cve <> 0 AND t.federal_district IS NULL) AS sin_distrito_federal
  FROM mv_section_turnout t
  JOIN election e ON e.id = t.election_id
 GROUP BY e.year, e.office
HAVING count(*) FILTER (WHERE t.municipality_cve <> 0
                          AND (t.local_district IS NULL OR t.federal_district IS NULL)) > 0;

COMMIT;
