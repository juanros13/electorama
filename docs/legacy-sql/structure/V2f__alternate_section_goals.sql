-- =====================================================================
-- Electorama — msvc-structure
-- V2f: las metas y límites por sección que sobreviven al nombramiento municipal
--
-- V2d convirtió el nombramiento de la rama alterna en un puesto municipal,
-- y con eso se perdían 9 filas de `users_seccions` que traen meta o límite
-- de promotores: 8 coordinadores distintos sobre la misma sección (57551),
-- con meta 250 y límite 10, más uno con límite 15 y sin meta.
--
-- Son pocas pero no son ruido: son la prueba de que un nombramiento
-- municipal puede tener una sección con meta propia. El modelo lo expresa
-- con un puesto sobre esa sección, que convive con el municipal bajo la
-- regla de precedencia de `compat.user_sections`: donde hay puesto de
-- sección, la fila sale de él y el municipio no la repite.
--
-- El padre es el mismo del puesto municipal —el referente—, porque el
-- trigger `position_validate_hierarchy` exige que el padre sea de nivel
-- superior: un coordinador no puede colgar de otro coordinador.
--
-- Es IDEMPOTENTE. Se ejecuta después de V2d.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V2f__alternate_section_goals.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

-- las filas con meta de la rama alterna y el puesto municipal del que salen
CREATE TEMP TABLE stg_goal AS
SELECT DISTINCT ON (us.user_auth_id, us.seccion_geo_id)
       us.user_auth_id                AS auth_id,
       us.meta,
       us.limit_promoters,
       sec.id                         AS territory_id,
       sec.code                       AS section_code,
       muni.id                        AS municipal_position_id,
       muni.parent_position_id        AS parent_position_id,
       muni.valid_from                AS valid_from,
       a.person_id
FROM leg.users_seccions us
JOIN leg.users u    ON u.auth_id = us.user_auth_id
                    AND COALESCE(u.flag_alter, false) = true
JOIN territory sec  ON sec.section_geo_id = us.seccion_geo_id AND sec.type = 'SECTION'
JOIN assignment a   ON a.auth_id = us.user_auth_id
                    AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
JOIN position muni  ON muni.id = a.position_id
                    AND muni.level = 'ZONE_COORDINATOR'
                    AND muni.structure_id = 2
                    AND muni.valid_to IS NULL
JOIN territory mt   ON mt.id = muni.territory_id AND mt.type = 'MUNICIPALITY'
WHERE (us.meta IS NOT NULL OR us.limit_promoters IS NOT NULL)
  AND NOT EXISTS (
        SELECT 1 FROM position p
         WHERE p.structure_id = 2
           AND p.level = 'ZONE_COORDINATOR'
           AND p.territory_id = sec.id
           AND p.valid_to IS NULL
           AND p.code = 'CZ|' || sec.code || '|' || us.user_auth_id)
ORDER BY us.user_auth_id, us.seccion_geo_id, us.meta DESC NULLS LAST,
         us.limit_promoters DESC NULLS LAST;

\echo '--- metas y límites por sección de la rama alterna por migrar ---'
SELECT count(*) AS puestos, sum(meta) AS suma_meta, count(limit_promoters) AS con_limite,
       count(DISTINCT auth_id) AS coordinadores,
       count(DISTINCT territory_id) AS secciones
FROM stg_goal;

INSERT INTO position (structure_id, level, parent_position_id, territory_id,
                      code, goal, child_limit, valid_from)
SELECT 2, 'ZONE_COORDINATOR', g.parent_position_id, g.territory_id,
       'CZ|' || g.section_code || '|' || g.auth_id, g.meta, g.limit_promoters,
       g.valid_from
FROM stg_goal g;

INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
SELECT pos.id, g.person_id, 'HOLDER', g.auth_id,
       tstzrange(g.valid_from, NULL),
       'migración V2f: meta propia sobre una sección del nombramiento municipal'
FROM stg_goal g
JOIN position pos ON pos.code = 'CZ|' || g.section_code || '|' || g.auth_id
                 AND pos.structure_id = 2
                 AND pos.valid_to IS NULL
WHERE NOT EXISTS (SELECT 1 FROM assignment x
                   WHERE x.position_id = pos.id AND x.role = 'HOLDER'
                     AND upper(x.validity) IS NULL);

-- los promotores de esa sección pasan al puesto de sección: donde hay
-- puesto específico, es el que manda
UPDATE position pr
   SET parent_position_id = pos.id
  FROM stg_goal g
  JOIN position pos ON pos.code = 'CZ|' || g.section_code || '|' || g.auth_id
                   AND pos.structure_id = 2
                   AND pos.valid_to IS NULL
 WHERE pr.level = 'PROMOTER'
   AND pr.parent_position_id = g.municipal_position_id
   AND pr.territory_id = g.territory_id;

COMMIT;

ANALYZE position;
ANALYZE assignment;

\echo '--- puestos de coordinador vigentes por estructura y territorio ---'
SELECT p.structure_id, t.type AS territorio, count(*) AS puestos,
       count(p.goal) AS con_meta, sum(p.goal) AS suma_meta
FROM position p JOIN territory t ON t.id = p.territory_id
WHERE p.level = 'ZONE_COORDINATOR' AND p.valid_to IS NULL
GROUP BY 1, 2 ORDER BY 1, 2;
