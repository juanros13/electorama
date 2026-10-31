-- Da de alta en structure.territory los estados, municipios y secciones vigentes
-- de geography que todavía no están, con los mismos códigos que Tabasco:
--
--   STATE         <cve_ent>            "27"
--   MUNICIPALITY  <cve_ent>-<cve_mun>  "27-004"
--   SECTION       S-<seccion>          "S-0815" (único dentro del estado, V4)
--
-- No toca los territorios existentes (Tabasco viene de V2 con ids fijos).
-- Es IDEMPOTENTE; requiere V4 y la geografía cargada.
--
--   psql -d electorama -v ON_ERROR_STOP=1 -f infra/db/reference/territory-nacional.sql

BEGIN;

INSERT INTO structure.territory (type, parent_territory_id, code, name, state_geo_id)
SELECT 'STATE', NULL, lpad(e.id::text, 2, '0'), upper(e.nom_ent), e.id
  FROM geography.geo_entidades e
 WHERE NOT EXISTS (SELECT 1 FROM structure.territory t
                    WHERE t.type = 'STATE' AND t.state_geo_id = e.id AND t.valid_to IS NULL);

INSERT INTO structure.territory (type, parent_territory_id, code, name, state_geo_id, municipality_geo_id)
SELECT 'MUNICIPALITY', st.id, lpad(m.entidad_id::text, 2, '0') || '-' || m.cve_mun, m.nom_mun,
       m.entidad_id, m.id
  FROM geography.geo_municipios m
  JOIN structure.territory st ON st.type = 'STATE' AND st.state_geo_id = m.entidad_id
                             AND st.valid_to IS NULL
 WHERE NOT EXISTS (SELECT 1 FROM structure.territory t
                    WHERE t.type = 'MUNICIPALITY' AND t.municipality_geo_id = m.id
                      AND t.valid_to IS NULL);

INSERT INTO structure.territory (type, parent_territory_id, code, name, state_geo_id,
                                 municipality_geo_id, section_geo_id,
                                 federal_district_geo_id, local_district_geo_id)
SELECT 'SECTION', mt.id, 'S-' || lpad(s.seccion, 4, '0'), 'Sección ' || lpad(s.seccion, 4, '0'),
       s.entidad_id, s.municipio_id, s.id,
       nullif(trim(s.distrito_federal_id), '')::bigint, s.distrito_local_id
  FROM geography.geo_secciones s
  JOIN structure.territory mt ON mt.type = 'MUNICIPALITY' AND mt.municipality_geo_id = s.municipio_id
                             AND mt.valid_to IS NULL
 WHERE s.status
   AND NOT EXISTS (SELECT 1 FROM structure.territory t
                    WHERE t.type = 'SECTION' AND t.section_geo_id = s.id AND t.valid_to IS NULL);

COMMIT;

SELECT state_geo_id, type, count(*)
  FROM structure.territory
 WHERE valid_to IS NULL
 GROUP BY 1, 2
 ORDER BY 1, 2;
