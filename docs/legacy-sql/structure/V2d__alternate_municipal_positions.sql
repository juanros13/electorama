-- =====================================================================
-- Electorama — msvc-structure
-- V2d: la rama alterna nombra por MUNICIPIO, no por sección
--
-- `users_seccions` tiene 63,280 filas de la rama alterna, de 489 usuarios,
-- con 8 metas en total y ~129 secciones por usuario. No son 129
-- nombramientos: `UserService.java:1147` crea al coordinador alterno
-- trayendo TODAS las secciones de su municipio del catálogo de geography y
-- escribiendo una fila por cada una. La prueba está en el dato: 268 de los
-- 489 tienen exactamente todas las secciones de su municipio y los otros
-- 221 están a 1–14 de tenerlas — la deriva de una copia hecha el día del
-- alta contra un catálogo que después creció.
--
-- Así que en el modelo nuevo eso es UN puesto ZONE_COORDINATOR con
-- `territory_id` apuntando al MUNICIPIO. El organigrama y el territorio son
-- árboles distintos justamente para esto: el puesto vive en el municipio y
-- los promotores debajo, cada uno en su sección.
--
-- V2 no podía saberlo: construyó los puestos de la rama alterna desde
-- `promoters`, que sólo conoce las secciones donde ya hay promotores, y le
-- salieron 3,567 puestos de sección para esos mismos 358 coordinadores.
-- Este script los sustituye por 489 municipales y recuelga a los 4,966
-- promotores alternos.
--
-- Se puede colapsar sin perder nada porque el dato viejo ya es 1:1: cada
-- coordinador de la rama alterna tiene exactamente 1 referente y 1
-- municipio en `promoters` (medido: 358 de 358).
--
-- Los puestos de sección NO se borran, se cierran con `valid_to`, igual que
-- manda el modelo. El histórico queda.
--
-- Es IDEMPOTENTE. Se ejecuta después de V2c y antes de V3a.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V2d__alternate_municipal_positions.sql
-- =====================================================================

\set ON_ERROR_STOP on

\echo '--- puestos de coordinador por estructura y territorio, antes ---'
SELECT p.structure_id, t.type AS territorio, count(*) AS puestos
FROM position p JOIN territory t ON t.id = p.territory_id
WHERE p.level = 'ZONE_COORDINATOR' AND p.valid_to IS NULL
GROUP BY 1, 2 ORDER BY 1, 2;

BEGIN;

-- ---------------------------------------------------------------------
-- 1. los coordinadores de la rama alterna, uno por municipio
--    El referente se toma primero de `promoters` —que es quien manda en la
--    jerarquía vieja— y si ahí no aparece, de `users.auth_id_reference`.
-- ---------------------------------------------------------------------
CREATE TEMP TABLE stg_alt AS
SELECT us.user_auth_id                                   AS auth_id,
       us.municipality_id,
       COALESCE(pr.referent_auth_id, u.auth_id_reference) AS referent_auth_id,
       u.created                                         AS user_created,
       mun.id                                            AS territory_id,
       'CZ|' || mun.code || '|' || us.user_auth_id        AS code
FROM (SELECT DISTINCT user_auth_id, municipality_id FROM leg.users_seccions) us
JOIN leg.users u   ON u.auth_id = us.user_auth_id
                  AND COALESCE(u.flag_alter, false) = true
JOIN territory mun ON mun.municipality_geo_id = us.municipality_id
                  AND mun.type = 'MUNICIPALITY'
LEFT JOIN LATERAL (
    SELECT p.referent_auth_id
    FROM leg.promoters p
    WHERE p.cordinator_zone_auth_id = us.user_auth_id
      AND COALESCE(p.flag_alter, false) = true
      AND p.referent_auth_id IS NOT NULL
    LIMIT 1) pr ON true
WHERE NOT EXISTS (
        SELECT 1 FROM position p
         WHERE p.code = 'CZ|' || mun.code || '|' || us.user_auth_id
           AND p.structure_id = 2);

CREATE INDEX ON stg_alt (auth_id);

\echo '--- coordinadores alternos por migrar ---'
SELECT count(*) AS puestos_municipales,
       count(referent_auth_id) AS con_referente,
       count(DISTINCT municipality_id) AS municipios
FROM stg_alt;

-- ---------------------------------------------------------------------
-- 2. el puesto municipal
--    El padre es el puesto de referente de ese municipio en la propia rama
--    alterna. `compat.promoters` lee el referente como titular del abuelo,
--    así que sin este padre 4,966 promotores reportarían referente nulo.
-- ---------------------------------------------------------------------
INSERT INTO position (structure_id, level, parent_position_id, territory_id,
                      code, valid_from)
SELECT 2, 'ZONE_COORDINATOR', ref.id, a.territory_id, a.code,
       COALESCE(a.user_created, now())
FROM stg_alt a
LEFT JOIN territory mun ON mun.id = a.territory_id
LEFT JOIN position ref  ON ref.code = 'REF|' || mun.code || '|' || a.referent_auth_id
                       AND ref.structure_id = 2
                       AND ref.level = 'REFERENT';

-- ---------------------------------------------------------------------
-- 3. el nombramiento (persona por authId y, si no, por CURP)
-- ---------------------------------------------------------------------
CREATE TEMP TABLE map_alt AS
SELECT a.auth_id,
       COALESCE(
         (SELECT p.id FROM person p WHERE p.auth_id = a.auth_id),
         (SELECT p.id FROM person p
            JOIN leg.users u ON nullif(trim(u.curp), '') = p.curp
           WHERE u.auth_id = a.auth_id
           LIMIT 1)
       ) AS person_id
FROM (SELECT DISTINCT auth_id FROM stg_alt) a;
CREATE INDEX ON map_alt (auth_id);

INSERT INTO person (auth_id, legacy_source, created)
SELECT DISTINCT m.auth_id, 'keycloak', now()
FROM map_alt m
WHERE m.person_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM person p WHERE p.auth_id = m.auth_id);

UPDATE map_alt m SET person_id = p.id FROM person p
 WHERE m.person_id IS NULL AND p.auth_id = m.auth_id;

INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
SELECT pos.id, m.person_id, 'HOLDER', a.auth_id,
       tstzrange(COALESCE(a.user_created, now() - interval '3 years'), NULL),
       'migración V2d: coordinador de la rama alterna, ámbito municipal'
FROM stg_alt a
JOIN position pos ON pos.code = a.code AND pos.structure_id = 2
JOIN map_alt m    ON m.auth_id = a.auth_id
WHERE NOT EXISTS (
        SELECT 1 FROM assignment x
         WHERE x.position_id = pos.id AND x.role = 'HOLDER' AND upper(x.validity) IS NULL);

-- ---------------------------------------------------------------------
-- 4. recolgar a los promotores
--    De cada puesto de sección de la rama alterna al puesto municipal del
--    mismo coordinador. El promotor conserva SU territorio (la sección):
--    lo único que cambia es de quién depende.
-- ---------------------------------------------------------------------
CREATE TEMP TABLE reparent AS
SELECT pr.id       AS promoter_position_id,
       cz.id       AS old_cz,
       muni.id     AS new_cz
FROM position pr
JOIN position cz   ON cz.id = pr.parent_position_id
                  AND cz.level = 'ZONE_COORDINATOR'
                  AND cz.structure_id = 2
JOIN territory ct  ON ct.id = cz.territory_id AND ct.type = 'SECTION'
JOIN position muni ON muni.structure_id = 2
                  AND muni.level = 'ZONE_COORDINATOR'
                  AND muni.code = 'CZ|' || (SELECT m.code FROM territory m
                                             WHERE m.id = ct.parent_territory_id)
                                       || '|' || split_part(cz.code, '|', 3)
WHERE pr.level = 'PROMOTER';

\echo '--- promotores por recolgar ---'
SELECT count(*) AS promotores, count(DISTINCT old_cz) AS puestos_seccion,
       count(DISTINCT new_cz) AS puestos_municipales
FROM reparent;

UPDATE position p SET parent_position_id = r.new_cz
  FROM reparent r WHERE p.id = r.promoter_position_id;

-- ---------------------------------------------------------------------
-- 5. cerrar los puestos de sección que quedaron vacíos
--    `clock_timestamp()` y no `now()`: dentro de una transacción `now()` es
--    constante, así que cerrar algo creado en la misma produce el rango
--    vacío y la baja no da de baja. El GREATEST lo impide.
-- ---------------------------------------------------------------------
CREATE TEMP TABLE close_cz AS
SELECT DISTINCT cz.id
FROM position cz
JOIN territory ct ON ct.id = cz.territory_id AND ct.type = 'SECTION'
WHERE cz.level = 'ZONE_COORDINATOR'
  AND cz.structure_id = 2
  AND cz.valid_to IS NULL
  AND NOT EXISTS (SELECT 1 FROM position ch
                   WHERE ch.parent_position_id = cz.id AND ch.valid_to IS NULL)
  -- sólo si su coordinador ya tiene puesto municipal; si no, se queda
  AND EXISTS (SELECT 1 FROM position muni
               WHERE muni.structure_id = 2
                 AND muni.level = 'ZONE_COORDINATOR'
                 AND muni.valid_to IS NULL
                 AND split_part(muni.code, '|', 3) = split_part(cz.code, '|', 3)
                 AND muni.territory_id IN (SELECT m.id FROM territory m
                                            WHERE m.type = 'MUNICIPALITY'));

UPDATE assignment a
   SET validity = tstzrange(lower(a.validity),
                            GREATEST(clock_timestamp(), lower(a.validity) + interval '1 microsecond')),
       end_reason = 'migración V2d: el nombramiento de la rama alterna pasa a ser municipal'
  FROM close_cz c
 WHERE a.position_id = c.id AND upper(a.validity) IS NULL;

UPDATE position p
   SET valid_to = GREATEST(clock_timestamp(), p.valid_from + interval '1 microsecond')
  FROM close_cz c
 WHERE p.id = c.id AND p.valid_to IS NULL;

\echo '--- puestos de sección cerrados ---'
SELECT count(*) AS cerrados FROM close_cz;

COMMIT;

ANALYZE position;
ANALYZE assignment;

-- =====================================================================
-- verificación
-- =====================================================================
\echo '--- puestos de coordinador vigentes por estructura y territorio, después ---'
SELECT p.structure_id, t.type AS territorio, count(*) AS puestos
FROM position p JOIN territory t ON t.id = p.territory_id
WHERE p.level = 'ZONE_COORDINATOR' AND p.valid_to IS NULL
GROUP BY 1, 2 ORDER BY 1, 2;

\echo '--- promotores alternos: de qué territorio es su puesto padre ---'
SELECT t.type AS territorio_del_coordinador, count(*) AS promotores
FROM position pr
JOIN position cz  ON cz.id = pr.parent_position_id
JOIN territory t  ON t.id = cz.territory_id
WHERE pr.level = 'PROMOTER' AND cz.structure_id = 2
GROUP BY 1 ORDER BY 1;

\echo '--- fidelidad: coordinador y referente de los promotores alternos contra el modelo viejo ---'
WITH nuevo AS (
  SELECT pr.legacy_id,
         (SELECT COALESCE(a.auth_id, pe.auth_id) FROM assignment a JOIN person pe ON pe.id = a.person_id
           WHERE a.position_id = pr.parent_position_id AND a.role='HOLDER' AND upper(a.validity) IS NULL
           LIMIT 1) AS cz_auth,
         (SELECT COALESCE(a.auth_id, pe.auth_id) FROM assignment a JOIN person pe ON pe.id = a.person_id
           WHERE a.position_id = (SELECT cz.parent_position_id FROM position cz WHERE cz.id = pr.parent_position_id)
             AND a.role='HOLDER' AND upper(a.validity) IS NULL
           LIMIT 1) AS ref_auth
  FROM position pr
  WHERE pr.level='PROMOTER' AND pr.legacy_id IS NOT NULL AND pr.valid_to IS NULL
    AND pr.structure_id = 2)
SELECT count(*) AS promotores_alternos,
       count(*) FILTER (WHERE n.cz_auth  IS DISTINCT FROM p.cordinator_zone_auth_id) AS dif_coordinador,
       count(*) FILTER (WHERE n.ref_auth IS DISTINCT FROM p.referent_auth_id)        AS dif_referente
FROM nuevo n JOIN leg.promoters p ON p.id = n.legacy_id;

\echo '--- puestos de coordinador vigentes sin titular ---'
SELECT p.structure_id, count(*) AS sin_titular
FROM position p
LEFT JOIN assignment a ON a.position_id = p.id AND a.role='HOLDER' AND upper(a.validity) IS NULL
WHERE p.level='ZONE_COORDINATOR' AND p.valid_to IS NULL AND a.id IS NULL
GROUP BY 1 ORDER BY 1;
