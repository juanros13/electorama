-- =====================================================================
-- Electorama — msvc-structure
-- V2: backfill desde el modelo viejo (fase 2 de la migración)
--
-- Lee msvc_dash_followers, msvc_dash_user y msvc_dash_geografia vía
-- postgres_fdw, materializa una copia local y puebla el modelo nuevo.
-- Es IDEMPOTENTE: vacía las tablas destino antes de empezar.
--
-- Los identificadores del modelo nuevo van en inglés; los del esquema `leg`
-- conservan los nombres originales del modelo viejo, a propósito.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V2__backfill.sql
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS postgres_fdw;

-- --- conexiones a las bases legacy -----------------------------------
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_foreign_server WHERE srvname='srv_followers') THEN
    CREATE SERVER srv_followers FOREIGN DATA WRAPPER postgres_fdw
      OPTIONS (host 'localhost', port '5432', dbname 'msvc_dash_followers');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_foreign_server WHERE srvname='srv_users') THEN
    CREATE SERVER srv_users FOREIGN DATA WRAPPER postgres_fdw
      OPTIONS (host 'localhost', port '5432', dbname 'msvc_dash_user');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_foreign_server WHERE srvname='srv_geo') THEN
    CREATE SERVER srv_geo FOREIGN DATA WRAPPER postgres_fdw
      OPTIONS (host 'localhost', port '5432', dbname 'msvc_dash_geografia');
  END IF;
END $$;

CREATE USER MAPPING IF NOT EXISTS FOR CURRENT_USER SERVER srv_followers
  OPTIONS (user 'postgres', password '<redacted>');
CREATE USER MAPPING IF NOT EXISTS FOR CURRENT_USER SERVER srv_users
  OPTIONS (user 'postgres', password '<redacted>');
CREATE USER MAPPING IF NOT EXISTS FOR CURRENT_USER SERVER srv_geo
  OPTIONS (user 'postgres', password '<redacted>');

DROP SCHEMA IF EXISTS leg_fol CASCADE;
DROP SCHEMA IF EXISTS leg_usr CASCADE;
DROP SCHEMA IF EXISTS leg_geo CASCADE;
CREATE SCHEMA leg_fol;
CREATE SCHEMA leg_usr;
CREATE SCHEMA leg_geo;

IMPORT FOREIGN SCHEMA public LIMIT TO (promoters, followers)
  FROM SERVER srv_followers INTO leg_fol;
IMPORT FOREIGN SCHEMA public LIMIT TO (users, users_seccions, users_municipalities)
  FROM SERVER srv_users INTO leg_usr;

-- Las tablas del catálogo traen columnas PostGIS (`geom`) que aquí no hacen
-- falta y que no se pueden importar sin la extensión. Se declaran a mano.
CREATE FOREIGN TABLE leg_geo.geo_entidades (
  id integer, cve_ent varchar(2), nom_ent varchar(20), status boolean
) SERVER srv_geo OPTIONS (schema_name 'public', table_name 'geo_entidades');

CREATE FOREIGN TABLE leg_geo.geo_municipios (
  id integer, cve_ent varchar, cve_munc varchar, cve_mun varchar,
  nom_ent varchar, nom_mun varchar, entidad_id integer
) SERVER srv_geo OPTIONS (schema_name 'public', table_name 'geo_municipios');

CREATE FOREIGN TABLE leg_geo.geo_secciones (
  id integer, seccion varchar, municipio_id integer
) SERVER srv_geo OPTIONS (schema_name 'public', table_name 'geo_secciones');

-- --- copia local de las tablas legacy --------------------------------
-- postgres_fdw resuelve los joins emitiendo una consulta remota por fila del
-- lado externo, lo que vuelve el backfill inviable (>10 min sólo en los
-- coordinadores). Se materializan primero y todo el resto se hace en local.
DROP SCHEMA IF EXISTS leg CASCADE;
CREATE SCHEMA leg;

CREATE TABLE leg.promoters AS SELECT * FROM leg_fol.promoters;
CREATE TABLE leg.followers AS SELECT * FROM leg_fol.followers WHERE deleted IS NULL;
CREATE TABLE leg.users AS SELECT * FROM leg_usr.users;
CREATE TABLE leg.users_seccions AS SELECT * FROM leg_usr.users_seccions WHERE deleted IS NULL;
CREATE TABLE leg.geo_entidades AS SELECT * FROM leg_geo.geo_entidades;
CREATE TABLE leg.geo_municipios AS SELECT * FROM leg_geo.geo_municipios;
CREATE TABLE leg.geo_secciones AS SELECT * FROM leg_geo.geo_secciones;

CREATE INDEX ON leg.promoters (promoter_auth_id);
CREATE INDEX ON leg.promoters (cordinator_zone_auth_id, seccion_id);
CREATE INDEX ON leg.promoters (referent_auth_id, municipality_id);
CREATE INDEX ON leg.promoters (id);
CREATE INDEX ON leg.followers (promoter_auth_id);
CREATE INDEX ON leg.followers (promoter_alt_auth_id);
CREATE INDEX ON leg.followers (section_id);
CREATE INDEX ON leg.followers (user_create_id);
CREATE INDEX ON leg.users (auth_id);
CREATE INDEX ON leg.users_seccions (user_auth_id, seccion_id);
CREATE INDEX ON leg.geo_secciones (municipio_id);
ANALYZE;

-- --- limpieza (idempotencia) -----------------------------------------
TRUNCATE change_log, membership, assignment, position, structure, campaign, territory, person
  RESTART IDENTITY CASCADE;

-- =====================================================================
-- 1. TERRITORY — referencias al catálogo del INE, nunca copias
-- =====================================================================
INSERT INTO territory (type, code, name, state_geo_id)
SELECT 'STATE', e.cve_ent, e.nom_ent, e.id
FROM leg.geo_entidades e WHERE e.cve_ent = '27';

INSERT INTO territory (type, parent_territory_id, code, name, state_geo_id, municipality_geo_id)
SELECT 'MUNICIPALITY', st.id, '27-' || m.cve_mun, m.nom_mun, st.state_geo_id, m.id
FROM leg.geo_municipios m
CROSS JOIN (SELECT id, state_geo_id FROM territory WHERE type='STATE') st
WHERE m.cve_ent = '27';

INSERT INTO territory (type, parent_territory_id, code, name, municipality_geo_id, section_geo_id)
SELECT 'SECTION', mun.id, 'S-' || s.seccion, 'Sección ' || s.seccion, s.municipio_id, s.id
FROM leg.geo_secciones s
JOIN territory mun ON mun.municipality_geo_id = s.municipio_id AND mun.type = 'MUNICIPALITY';

-- =====================================================================
-- 2. CAMPAIGN y STRUCTURE
-- =====================================================================
INSERT INTO campaign (name, type, year, status, territory_id)
SELECT 'Tabasco 2024', 'STATE', 2024, 'CLOSED', id FROM territory WHERE type='STATE';

INSERT INTO structure (campaign_id, name, type)
SELECT id, 'Principal', 'MAIN' FROM campaign WHERE name='Tabasco 2024';
INSERT INTO structure (campaign_id, name, type)
SELECT id, 'Alterna', 'ALTERNATE' FROM campaign WHERE name='Tabasco 2024';

-- =====================================================================
-- 3. PERSON — deduplicada por componentes conexos (CURP + authId)
--    Prioridad de fuente: users (operador) > promoters > followers
-- =====================================================================
CREATE TEMP TABLE stg_person AS
SELECT 'users'::text AS src, u.id AS src_id, 1 AS priority,
       nullif(trim(u.curp),'') AS curp, nullif(trim(u.ine),'') AS ine,
       -- 54,328 usuarios no tienen `names` porque su perfil vive en Keycloak;
       -- `auth_name` rescata 50,920 de ellos. Los 3,408 restantes quedan sin
       -- nombre, que es la verdad del dato.
       COALESCE(nullif(trim(u.names),''), nullif(trim(u.auth_name),'')) AS names,
       u.last_name, u.sur_name,
       u.birthdate::date AS birthdate, u.email, NULL::varchar AS cell_phone_number,
       u.street, u.external_number, u.internal_number,
       u.neighborhood, u.zip_code,
       nullif(trim(u.auth_id),'') AS auth_id, u.created
FROM leg.users u
UNION ALL
SELECT 'promoters', p.id, 2,
       nullif(trim(p.curp),''), nullif(trim(p.ine),''),
       p.names, p.last_name, p.sur_name, NULL::date, p.email, p.cell_phone_number,
       p.street, p.outdoor_number, p.interior_number, NULL, p.postal_code,
       nullif(trim(p.promoter_auth_id),''), p.created
FROM leg.promoters p
UNION ALL
SELECT 'followers', f.id, 3,
       nullif(trim(f.curp),''), nullif(trim(f.ine),''),
       f.names, f.last_name, f.sur_name, f.birthdate::date, f.email, f.cell_phone_number,
       f.street, f.external_number, f.internal_number, f.neighborhood, f.zip_code,
       -- `followers.auth_id` NO es la identidad Keycloak del convencido: tiene
       -- 93,467 valores pero sólo 7,882 distintos (uno repetido 65 veces),
       -- contaminado con el authId de quien capturó. Usarlo como señal de
       -- identidad fusionaba 85,585 personas que no son la misma.
       -- Un convencido no es operador: no aporta authId.
       NULL::varchar, f.created
FROM leg.followers f;

-- Deduplicación por COMPONENTES CONEXOS sobre dos señales de identidad:
-- CURP y authId de Keycloak. El INE NO se usa: hay 53,674 credenciales
-- repetidas entre personas con CURP distinta, y al encadenar componentes
-- fusionaba gente que no es la misma (una "persona" llegó a absorber 517
-- filas de convencidos). Deduplicar sólo por CURP no basta: un
-- promotor existe como fila en `users` (a menudo SIN curp) y como fila en
-- `promoters` (con curp), así que nunca se fusionaban y el mismo humano
-- quedaba como dos personas — con el authId pegado a la equivocada.
-- 49,311 de 59,195 authIds estaban en ese caso.
ALTER TABLE stg_person ADD COLUMN rid BIGSERIAL;
ALTER TABLE stg_person ADD COLUMN comp BIGINT;
UPDATE stg_person SET comp = rid;
CREATE INDEX ON stg_person (curp) WHERE curp IS NOT NULL;
CREATE INDEX ON stg_person (ine) WHERE ine IS NOT NULL;
CREATE INDEX ON stg_person (auth_id) WHERE auth_id IS NOT NULL;
CREATE INDEX ON stg_person (comp);
ANALYZE stg_person;

-- propagación de etiqueta mínima hasta punto fijo
DO $$
DECLARE moved BIGINT; total BIGINT; iter INT := 0;
BEGIN
  LOOP
    iter := iter + 1;
    total := 0;

    WITH mins AS (SELECT upper(curp) AS k, min(comp) AS mc FROM stg_person
                  WHERE curp IS NOT NULL GROUP BY 1)
    UPDATE stg_person s SET comp = m.mc FROM mins m
     WHERE upper(s.curp) = m.k AND s.comp > m.mc;
    GET DIAGNOSTICS moved = ROW_COUNT; total := total + moved;

    WITH mins AS (SELECT auth_id AS k, min(comp) AS mc FROM stg_person
                  WHERE auth_id IS NOT NULL GROUP BY 1)
    UPDATE stg_person s SET comp = m.mc FROM mins m
     WHERE s.auth_id = m.k AND s.comp > m.mc;
    GET DIAGNOSTICS moved = ROW_COUNT; total := total + moved;

    RAISE NOTICE 'dedup iteracion %: % filas reetiquetadas', iter, total;
    EXIT WHEN total = 0 OR iter > 20;
  END LOOP;
END $$;

-- una persona por componente; gana la fuente de mayor prioridad
INSERT INTO person (curp, ine, names, last_name, sur_name,
                    birthdate, email, cell_phone_number, street, external_number,
                    internal_number, neighborhood, zip_code,
                    legacy_source, legacy_id, created)
SELECT DISTINCT ON (comp)
       upper(curp), upper(ine), names, last_name, sur_name,
       birthdate, email, cell_phone_number, street, external_number,
       internal_number, neighborhood, zip_code,
       src, src_id, COALESCE(created, now())
FROM stg_person
ORDER BY comp, priority, src_id;

-- el representante puede no traer CURP ni INE: se completan desde el componente
UPDATE person p SET curp = c.curp
FROM (SELECT comp, upper(max(curp)) AS curp FROM stg_person WHERE curp IS NOT NULL GROUP BY comp) c
JOIN stg_person r ON r.comp = c.comp
WHERE p.legacy_source = r.src AND p.legacy_id = r.src_id AND p.curp IS NULL;

UPDATE person p SET ine = c.ine
FROM (SELECT comp, upper(max(ine)) AS ine FROM stg_person WHERE ine IS NOT NULL GROUP BY comp) c
JOIN stg_person r ON r.comp = c.comp
WHERE p.legacy_source = r.src AND p.legacy_id = r.src_id AND p.ine IS NULL;

-- mapa fuente → person, por componente
CREATE TEMP TABLE map_person AS
SELECT s.src, s.src_id, p.id AS person_id, s.auth_id
FROM stg_person s
JOIN (SELECT DISTINCT ON (comp) comp, src, src_id FROM stg_person ORDER BY comp, priority, src_id) r
  ON r.comp = s.comp
JOIN person p ON p.legacy_source = r.src AND p.legacy_id = r.src_id;
CREATE INDEX ON map_person (src, src_id);
CREATE INDEX ON map_person (auth_id);

-- auth_id: una sola persona por authId (el índice único lo exige)
UPDATE person p SET auth_id = m.auth_id
FROM (SELECT DISTINCT ON (auth_id) auth_id, person_id
      FROM map_person WHERE auth_id IS NOT NULL
      ORDER BY auth_id, person_id) m
WHERE p.id = m.person_id;

CREATE TEMP TABLE map_auth AS
SELECT DISTINCT ON (auth_id) auth_id, person_id
FROM map_person WHERE auth_id IS NOT NULL
ORDER BY auth_id, person_id;
CREATE INDEX ON map_auth (auth_id);

-- =====================================================================
-- 4. POSITION — tres niveles
--    Los códigos llevan el authId COMPLETO y separador '|'. Un prefijo corto
--    colisiona, y '-' ya aparece dentro de los códigos de territorio
--    ('27-004', 'S-0866'), así que parsear el código con split_part da el
--    pedazo equivocado. Los mapas se construyen con las columnas, no con la
--    cadena.
-- =====================================================================
-- 4a. REFERENT: un puesto por (referente, municipio)
INSERT INTO position (structure_id, level, territory_id, code, valid_from)
SELECT DISTINCT s.id, 'REFERENT', mun.id,
       'REF|' || mun.code || '|' || r.referent_auth_id, now()
FROM (SELECT DISTINCT p.referent_auth_id, p.municipality_id, p.flag_alter
      FROM leg.promoters p WHERE p.referent_auth_id IS NOT NULL) r
JOIN territory mun ON mun.municipality_geo_id = r.municipality_id AND mun.type='MUNICIPALITY'
JOIN structure s ON s.type = CASE WHEN r.flag_alter THEN 'ALTERNATE' ELSE 'MAIN' END;

CREATE TEMP TABLE map_ref AS
SELECT DISTINCT r.referent_auth_id AS auth_id, r.municipality_id,
       COALESCE(r.flag_alter,false) AS flag_alter, pos.id AS position_id
FROM (SELECT DISTINCT p.referent_auth_id, p.municipality_id, p.flag_alter
      FROM leg.promoters p WHERE p.referent_auth_id IS NOT NULL) r
JOIN territory mun ON mun.municipality_geo_id = r.municipality_id AND mun.type='MUNICIPALITY'
JOIN structure s ON s.type = CASE WHEN r.flag_alter THEN 'ALTERNATE' ELSE 'MAIN' END
JOIN position pos ON pos.code = 'REF|' || mun.code || '|' || r.referent_auth_id
                 AND pos.structure_id = s.id;
CREATE INDEX ON map_ref (auth_id, municipality_id, flag_alter);

-- 4b. ZONE_COORDINATOR: un puesto por (coordinador, sección) — así se
--     representa que una sección tenga varios coordinadores, que es lo normal
INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal, child_limit, valid_from)
SELECT DISTINCT ON (cz.auth_id, cz.seccion_id, cz.flag_alter)
       s.id, 'ZONE_COORDINATOR', mr.position_id, sec.id,
       'CZ|' || sec.code || '|' || cz.auth_id, us.meta, us.limit_promoters, now()
FROM (SELECT DISTINCT p.cordinator_zone_auth_id AS auth_id, p.seccion_id, p.referent_auth_id,
             p.municipality_id, COALESCE(p.flag_alter,false) AS flag_alter
      FROM leg.promoters p WHERE p.cordinator_zone_auth_id IS NOT NULL) cz
JOIN territory sec ON sec.section_geo_id = cz.seccion_id AND sec.type='SECTION'
JOIN structure s ON s.type = CASE WHEN cz.flag_alter THEN 'ALTERNATE' ELSE 'MAIN' END
JOIN map_ref mr ON mr.auth_id = cz.referent_auth_id
               AND mr.municipality_id = cz.municipality_id
               AND mr.flag_alter = cz.flag_alter
LEFT JOIN leg.users_seccions us ON us.user_auth_id = cz.auth_id AND us.seccion_id = cz.seccion_id
ORDER BY cz.auth_id, cz.seccion_id, cz.flag_alter, us.meta NULLS LAST;

CREATE TEMP TABLE map_cz AS
SELECT DISTINCT cz.auth_id, cz.seccion_id, cz.flag_alter, pos.id AS position_id
FROM (SELECT DISTINCT p.cordinator_zone_auth_id AS auth_id, p.seccion_id,
             COALESCE(p.flag_alter,false) AS flag_alter
      FROM leg.promoters p WHERE p.cordinator_zone_auth_id IS NOT NULL) cz
JOIN territory sec ON sec.section_geo_id = cz.seccion_id AND sec.type='SECTION'
JOIN structure s ON s.type = CASE WHEN cz.flag_alter THEN 'ALTERNATE' ELSE 'MAIN' END
JOIN position pos ON pos.code = 'CZ|' || sec.code || '|' || cz.auth_id
                 AND pos.structure_id = s.id;
CREATE INDEX ON map_cz (auth_id, seccion_id, flag_alter);
CREATE INDEX ON map_cz (seccion_id, flag_alter);

-- 4c. PROMOTER: uno por fila de promoters
INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, goal, valid_from)
SELECT s.id, 'PROMOTER', mc.position_id, sec.id,
       'PROM|' || p.id, p.followers_goal, COALESCE(p.created, now())
FROM leg.promoters p
JOIN territory sec ON sec.section_geo_id = p.seccion_id AND sec.type='SECTION'
JOIN structure s ON s.type = CASE WHEN p.flag_alter THEN 'ALTERNATE' ELSE 'MAIN' END
JOIN map_cz mc ON mc.auth_id = p.cordinator_zone_auth_id
              AND mc.seccion_id = p.seccion_id
              AND mc.flag_alter = COALESCE(p.flag_alter,false);

CREATE TEMP TABLE map_prom AS
SELECT p.promoter_auth_id AS auth_id, p.id AS legacy_id, pos.id AS position_id,
       COALESCE(p.flag_alter,false) AS flag_alter
FROM leg.promoters p JOIN position pos ON pos.code = 'PROM|' || p.id;
CREATE INDEX ON map_prom (auth_id, flag_alter);
CREATE INDEX ON map_prom (legacy_id);

-- 4d. POOL: un puesto por sección para los convencidos sin promotor resoluble
INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, valid_from)
SELECT DISTINCT ON (sec.id) s.id, 'PROMOTER', mc.position_id, sec.id,
       'POOL|' || sec.code, now()
FROM leg.followers f
JOIN territory sec ON sec.section_geo_id = f.section_id AND sec.type='SECTION'
JOIN structure s ON s.type='MAIN'
JOIN map_cz mc ON mc.seccion_id = f.section_id AND mc.flag_alter = false
WHERE COALESCE(f.promoter_auth_id, f.promoter_alt_auth_id) IS NULL
   OR NOT EXISTS (SELECT 1 FROM leg.promoters p
                  WHERE p.promoter_auth_id = COALESCE(f.promoter_auth_id, f.promoter_alt_auth_id))
ORDER BY sec.id, mc.position_id;

-- 4e. Rama UNPLACED: hay convencidos sin promotor Y sin sección. No se pueden
--     colgar de ningún puesto territorial, pero tampoco deben perderse: se les
--     da una rama propia sobre el territorio estatal, visible y contable.
INSERT INTO position (structure_id, level, territory_id, code, valid_from)
SELECT s.id, 'REFERENT', st.id, 'UNPLACED|REF', now()
FROM structure s, territory st WHERE s.type='MAIN' AND st.type='STATE';

INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, valid_from)
SELECT s.id, 'ZONE_COORDINATOR', ref.id, st.id, 'UNPLACED|CZ', now()
FROM structure s, position ref, territory st
WHERE s.type='MAIN' AND ref.code='UNPLACED|REF' AND st.type='STATE';

INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, valid_from)
SELECT s.id, 'PROMOTER', cz.id, st.id, 'UNPLACED|POOL', now()
FROM structure s, position cz, territory st
WHERE s.type='MAIN' AND cz.code='UNPLACED|CZ' AND st.type='STATE';

-- =====================================================================
-- 5. ASSIGNMENT — un titular vigente por puesto, desde su fecha de alta
-- =====================================================================
INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
SELECT mp.position_id, m.person_id, 'HOLDER', p.promoter_auth_id,
       tstzrange(COALESCE(p.created, now() - interval '3 years'), NULL), 'migración'
FROM leg.promoters p
JOIN map_prom mp ON mp.legacy_id = p.id
JOIN map_person m ON m.src='promoters' AND m.src_id = p.id;

INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
SELECT DISTINCT ON (mr.position_id) mr.position_id, ma.person_id, 'HOLDER', mr.auth_id,
       tstzrange(COALESCE(u.created, now() - interval '3 years'), NULL), 'migración'
FROM map_ref mr
JOIN map_auth ma ON ma.auth_id = mr.auth_id
LEFT JOIN leg.users u ON u.auth_id = mr.auth_id
ORDER BY mr.position_id, ma.person_id;

INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
SELECT DISTINCT ON (mc.position_id) mc.position_id, ma.person_id, 'HOLDER', mc.auth_id,
       tstzrange(COALESCE(u.created, now() - interval '3 years'), NULL), 'migración'
FROM map_cz mc
JOIN map_auth ma ON ma.auth_id = mc.auth_id
LEFT JOIN leg.users u ON u.auth_id = mc.auth_id
ORDER BY mc.position_id, ma.person_id;

-- para resolver el linaje: qué asignación tenía quien capturó
CREATE INDEX ON assignment (person_id, role);
ANALYZE assignment;

-- =====================================================================
-- 6. MEMBERSHIP — el convencido, colgado del PUESTO
--    position_id = responsabilidad actual · captured_by_assignment_id = linaje
-- =====================================================================
CREATE TEMP TABLE stg_mem AS
SELECT f.id AS legacy_id, mp.person_id,
       COALESCE(f.promoter_auth_id, f.promoter_alt_auth_id) AS prom_auth,
       (f.promoter_auth_id IS NULL) AS is_alternate,
       f.user_create_id, f.section_id, f.device_id, f.status, f.created,
       f.vote_flag, f.vote_flag_timestamp
FROM leg.followers f
JOIN map_person mp ON mp.src='followers' AND mp.src_id = f.id;
CREATE INDEX ON stg_mem (prom_auth, is_alternate);
CREATE INDEX ON stg_mem (user_create_id);
CREATE INDEX ON stg_mem (section_id);
ANALYZE stg_mem;

-- linaje: authId de quien capturó → su asignación de titular
-- El linaje se resuelve por la CUENTA que capturó, no por la persona: un
-- humano puede tener varias cuentas y antes se elegía una asignación
-- arbitraria, lo que perdía con cuál cuenta se hizo la captura.
CREATE TEMP TABLE map_captor AS
SELECT DISTINCT ON (a.auth_id) a.auth_id, a.id AS assignment_id
FROM assignment a
WHERE a.auth_id IS NOT NULL AND a.role='HOLDER'
ORDER BY a.auth_id, a.id;
CREATE INDEX ON map_captor (auth_id);

-- 6a. candidatas: todas las membresías posibles, con su puesto resuelto
CREATE TEMP TABLE mem_candidate AS
SELECT m.legacy_id, m.person_id, pos.structure_id, pos.id AS position_id,
       mcap.assignment_id, sec.id AS territory_id,
       CASE WHEN m.device_id IS NOT NULL THEN 'APP' ELSE 'WEB' END AS source,
       COALESCE(m.status,'ACTIVO') AS status,
       COALESCE(m.created, now() - interval '3 years') AS since,
       COALESCE(m.vote_flag,false) AS vote_flag, m.vote_flag_timestamp
FROM stg_mem m
JOIN map_prom mp ON mp.auth_id = m.prom_auth AND mp.flag_alter = m.is_alternate
JOIN position pos ON pos.id = mp.position_id
LEFT JOIN territory sec ON sec.section_geo_id = m.section_id AND sec.type='SECTION'
LEFT JOIN map_captor mcap ON mcap.auth_id = m.user_create_id
UNION ALL
-- huérfanos y punteros rotos: a la bolsa de su sección
SELECT m.legacy_id, m.person_id, pos.structure_id, pos.id, NULL, sec.id,
       'IMPORT', COALESCE(m.status,'ACTIVO'),
       COALESCE(m.created, now() - interval '3 years'),
       COALESCE(m.vote_flag,false), m.vote_flag_timestamp
FROM stg_mem m
JOIN territory sec ON sec.section_geo_id = m.section_id AND sec.type='SECTION'
JOIN position pos ON pos.code = 'POOL|' || sec.code
WHERE m.prom_auth IS NULL
   OR NOT EXISTS (SELECT 1 FROM map_prom mp2
                  WHERE mp2.auth_id = m.prom_auth AND mp2.flag_alter = m.is_alternate)
UNION ALL
-- ni promotor ni sección: a la rama sin ubicación
SELECT m.legacy_id, m.person_id, pos.structure_id, pos.id, NULL, NULL,
       'IMPORT', COALESCE(m.status,'ACTIVO'),
       COALESCE(m.created, now() - interval '3 years'),
       COALESCE(m.vote_flag,false), m.vote_flag_timestamp
FROM stg_mem m
CROSS JOIN position pos
WHERE pos.code = 'UNPLACED|POOL'
  AND NOT EXISTS (SELECT 1 FROM territory sec
                  WHERE sec.section_geo_id = m.section_id AND sec.type='SECTION')
  AND (m.prom_auth IS NULL
       OR NOT EXISTS (SELECT 1 FROM map_prom mp3
                      WHERE mp3.auth_id = m.prom_auth AND mp3.flag_alter = m.is_alternate));
CREATE INDEX ON mem_candidate (person_id, structure_id, since, legacy_id);

-- 6b. una sola membresía vigente por (persona, estructura): se conserva la más
--     antigua. Las demás son convencidos duplicados que el modelo viejo
--     permitía y este detecta.
--
-- La restricción de exclusión se quita durante la carga. Con ~1M de filas,
-- validarla fila por fila contra el índice GiST es lo que dejó colgado el
-- backfill del 22-sep (murió aquí, con `membership` en cero). Reconstruirla
-- después valida el mismo invariante de una sola pasada, y si algo lo viola
-- el ALTER falla y el script se detiene igual.
ALTER TABLE membership DROP CONSTRAINT membership_single_per_structure;

INSERT INTO membership (person_id, structure_id, position_id, captured_by_assignment_id,
                        territory_id, source, status, validity, vote_flag,
                        vote_flag_timestamp, legacy_id)
SELECT DISTINCT ON (person_id, structure_id)
       person_id, structure_id, position_id, assignment_id, territory_id,
       source::varchar, status, tstzrange(since, NULL), vote_flag,
       vote_flag_timestamp, legacy_id
FROM mem_candidate
ORDER BY person_id, structure_id, since, legacy_id;

ALTER TABLE membership ADD CONSTRAINT membership_single_per_structure
    EXCLUDE USING gist (person_id WITH =, structure_id WITH =, validity WITH &&);

-- 6c. las descartadas quedan registradas, no se pierden en silencio
DROP TABLE IF EXISTS migration_duplicate;
CREATE TABLE migration_duplicate AS
SELECT c.legacy_id AS follower_legacy_id, c.person_id, c.structure_id,
       c.position_id, c.since, m.legacy_id AS kept_legacy_id
FROM mem_candidate c
JOIN membership m ON m.person_id = c.person_id AND m.structure_id = c.structure_id
WHERE m.legacy_id <> c.legacy_id;
COMMENT ON TABLE migration_duplicate IS
  'Convencidos duplicados detectados al migrar: la misma persona aparecia mas de una vez en la misma estructura. Se conservo la membresia mas antigua.';

ANALYZE;
