-- =====================================================================
-- Electorama — msvc-electoral-history
-- T1: la carga cuadra con lo publicado
--
-- Para cada elección cargada revisa:
--   1. Casilla por casilla: opciones + no registrados + nulos = total
--      (las columnas DERIVED_TOTAL no cuentan: repiten votos).
--   2. Contra official_total, en el territorio que publique el órgano
--      electoral (estado, municipio o distrito):
--        TOTAL, NULL, NON_REGISTERED, NOMINAL_LIST, STATIONS
--        una candidatura (candidacy.code): suma lo que caiga en ella en el
--          territorio de cada casilla, combinaciones incluidas
--        SET:<partidos>: la suma de las opciones con exactamente esos partidos
--          (así se compara columna por columna con los concentrados del IEPCT)
--        KIND:INDEPENDENT: todas las independientes juntas
--   3. Ninguna opción con votos se queda sin candidatura en su territorio.
--   4. Fuera de los mecanismos estatales, ninguna casilla en el municipio 0.
--
-- Sólo cuentan los votos de las casillas computadas (counted): las anuladas
-- no están en el cómputo oficial. Su lista nominal sí: el IEPCT la conserva.
--
-- Si algo no cuadra, RAISE EXCEPTION con el detalle. Sin excepción = cuadra.
--
--   psql -U postgres -d msvc_dash_electoral_history -v ON_ERROR_STOP=1 -f T1__reconcile_tests.sql
-- =====================================================================

\set ON_ERROR_STOP on

-- las casillas computadas, con su territorio en las tres escalas
CREATE TEMP VIEW station AS
SELECT p.*, e.scope
  FROM polling_station p
  JOIN election e ON e.id = p.election_id
 WHERE p.counted;

-- cada voto con su casilla y su opción
CREATE TEMP VIEW vote AS
SELECT s.election_id, s.id AS station_id, s.municipality_cve, s.district, s.scope,
       o.kind, o.party_codes, v.votes
  FROM station_vote v
  JOIN station s ON s.id = v.station_id
  JOIN ballot_option o ON o.id = v.option_id AND o.kind <> 'DERIVED_TOTAL';

-- el código con que se compara cada voto: candidatura, conjunto de partidos o tipo
CREATE TEMP VIEW coded_vote AS
SELECT v.election_id, v.municipality_cve, v.district, c.code, v.votes
  FROM vote v
  JOIN candidacy c
    ON c.election_id = v.election_id
   AND cardinality(v.party_codes) > 0
   AND v.party_codes <@ c.party_codes
   AND (c.territory IS NULL
        OR (v.scope = 'MUNICIPALITY' AND c.territory = v.municipality_cve::text)
        OR (v.scope IN ('FEDERAL_DISTRICT', 'LOCAL_DISTRICT') AND c.territory = v.district))
UNION ALL
SELECT v.election_id, v.municipality_cve, v.district,
       'SET:' || array_to_string(ARRAY(SELECT unnest(v.party_codes) ORDER BY 1), ','), v.votes
  FROM vote v
 WHERE v.kind IN ('PARTY', 'COMBINATION')
UNION ALL
SELECT v.election_id, v.municipality_cve, v.district, 'KIND:INDEPENDENT', v.votes
  FROM vote v
 WHERE v.kind = 'INDEPENDENT';

-- lo mismo que official_total, calculado de las casillas, en las tres escalas
CREATE TEMP VIEW measure AS
SELECT election_id, municipality_cve, district, 'STATIONS' AS code, 1::bigint AS value FROM station
-- la LN sí incluye las casillas anuladas: el órgano electoral la conserva
UNION ALL SELECT p.election_id, p.municipality_cve, p.district, 'NOMINAL_LIST', p.nominal_list
            FROM polling_station p
UNION ALL SELECT election_id, municipality_cve, district, 'TOTAL', total_votes FROM station
UNION ALL SELECT election_id, municipality_cve, district, 'NULL', null_votes FROM station
UNION ALL SELECT election_id, municipality_cve, district, 'NON_REGISTERED', non_registered_votes FROM station
UNION ALL SELECT election_id, municipality_cve, district, code, votes FROM coded_vote;

CREATE TEMP VIEW computed AS
SELECT election_id, NULL::smallint AS municipality_cve, NULL::varchar AS district, code, sum(value) AS value
  FROM measure GROUP BY 1, 4
UNION ALL
SELECT election_id, municipality_cve, NULL, code, sum(value) FROM measure GROUP BY 1, 2, 4
UNION ALL
SELECT election_id, NULL, district, code, sum(value) FROM measure GROUP BY 1, 3, 4;

DO $$
DECLARE
  r record;
  failures text := '';
BEGIN
  -- 1. cada casilla suma su total
  FOR r IN
    SELECT e.year, e.office, p.station_key, p.total_votes,
           coalesce(sum(v.votes) FILTER (WHERE o.kind <> 'DERIVED_TOTAL'), 0)
             + p.null_votes + p.non_registered_votes AS summed
      FROM polling_station p
      JOIN election e ON e.id = p.election_id
      LEFT JOIN station_vote v ON v.station_id = p.id
      LEFT JOIN ballot_option o ON o.id = v.option_id
     WHERE p.counted
     GROUP BY e.year, e.office, p.id
    HAVING p.total_votes <> coalesce(sum(v.votes) FILTER (WHERE o.kind <> 'DERIVED_TOTAL'), 0)
                           + p.null_votes + p.non_registered_votes
     LIMIT 10
  LOOP
    failures := failures || format(E'\n  casilla %s %s %s: total %s, suma %s',
                                   r.year, r.office, r.station_key, r.total_votes, r.summed);
  END LOOP;

  -- 2. contra lo publicado
  FOR r IN
    SELECT e.year, e.office,
           coalesce(m.name, 'DTO ' || t.district, 'ESTATAL') AS territory,
           t.code, t.value AS official, c.value AS computed
      FROM official_total t
      JOIN election e ON e.id = t.election_id
      LEFT JOIN municipality m ON m.cve = t.municipality_cve
      LEFT JOIN computed c ON c.election_id = t.election_id
                          AND c.code = t.code
                          AND c.municipality_cve IS NOT DISTINCT FROM t.municipality_cve
                          AND c.district IS NOT DISTINCT FROM t.district
     WHERE coalesce(c.value, 0) <> t.value
     ORDER BY 1, 2, 3, 4
     LIMIT 40
  LOOP
    failures := failures || format(E'\n  %s %s %s %s: oficial %s, carga %s',
                                   r.year, r.office, r.territory, r.code, r.official, r.computed);
  END LOOP;

  -- 3. ninguna opción con votos se queda sin candidatura en su territorio
  FOR r IN
    SELECT e.year, e.office, v.municipality_cve, v.district, v.party_codes, sum(v.votes) AS votes
      FROM vote v
      JOIN election e ON e.id = v.election_id
     WHERE v.votes > 0
       AND NOT EXISTS (
             SELECT 1 FROM candidacy c
              WHERE c.election_id = v.election_id
                AND cardinality(v.party_codes) > 0
                AND v.party_codes <@ c.party_codes
                AND (c.territory IS NULL
                     OR (v.scope = 'MUNICIPALITY' AND c.territory = v.municipality_cve::text)
                     OR (v.scope IN ('FEDERAL_DISTRICT', 'LOCAL_DISTRICT') AND c.territory = v.district)))
     GROUP BY 1, 2, 3, 4, 5
     LIMIT 20
  LOOP
    failures := failures || format(E'\n  %s %s mun %s dto %s: %s votos de %s sin candidatura',
                                   r.year, r.office, r.municipality_cve, r.district, r.votes, r.party_codes);
  END LOOP;

  -- 4. municipios
  FOR r IN
    SELECT e.year, e.office, count(*) AS n
      FROM polling_station p
      JOIN election e ON e.id = p.election_id
     WHERE p.station_type NOT IN ('A', 'P', 'V', 'M') AND p.municipality_cve = 0
     GROUP BY 1, 2
  LOOP
    failures := failures || format(E'\n  %s %s: %s casillas ordinarias en el municipio 0',
                                   r.year, r.office, r.n);
  END LOOP;

  IF failures <> '' THEN
    RAISE EXCEPTION 'T1 no cuadra:%', failures;
  END IF;
END $$;

-- resumen de lo que se revisó
SELECT e.year, e.office, count(t.*) AS cifras_oficiales_revisadas, 'OK' AS resultado
  FROM election e
  JOIN official_total t ON t.election_id = e.id
 GROUP BY 1, 2
 ORDER BY 2, 1;
