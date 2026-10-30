-- =====================================================================
-- Electorama — msvc-structure
-- V4: el modelo absorbe lo que sólo vivía en `followers` (prepara la fase 5)
--
-- LA FASE 5 NO SE PUEDE EJECUTAR SIN ESTO. La vista `followers` de la fase 3
-- pega 24 columnas desde `followers_legacy` porque el modelo nuevo no las
-- tiene, así que tirar la tabla legacy —que es lo que hace la fase 5— sería
-- perder datos, no limpiar andamiaje.
--
-- Medido sobre las 1,067,949 filas congeladas, esas columnas son tres cosas
-- distintas y cada una va a un sitio distinto:
--
-- 1. IDENTIDAD Y DOMICILIO de la persona → a `person`
--    birth_entity_id (1,063,512), residence_entity_id (1,021,216),
--    residence_municipality_id (1,067,944), cell_phone_number_other (66,785).
--    Son referencias al catálogo del INE por id, no copias de sus nombres.
--
-- 2. EL EXPEDIENTE DE CAPTURA del convencido → a `membership_detail`, 1:1
--    Las respuestas (¿participa en comité?, ¿invita a otros?, ¿apoya al
--    movimiento?) y el rastro del levantamiento (coordenadas, dispositivo,
--    si venía de la app sin conexión). ~1.02M filas cada una. Va en tabla
--    aparte y no en `membership` para que la adhesión siga siendo lo que es
--    —persona, estructura, puesto, vigencia— y no un cajón de sastre.
--
-- 3. EL DISTRITO, que NO se absorbe por convencido → a `territory`
--    `federal_district_*` y `local_district_*` están llenas en 1,067,943
--    filas: un millón de copias de un dato que pertenece a la SECCIÓN. El
--    catálogo de geografía ya lo tiene (`geo_secciones.distrito_federal_id` y
--    `distrito_local_id`), así que el modelo guarda la referencia al nivel que le
--    toca: 1,199 territorios en vez de 1,067,943 convencidos. Los nombres no
--    se copian —geography es su dueño y se le preguntan una vez por reporte,
--    no un millón de veces.
--
-- Lo que se queda fuera a propósito:
--   · `section_number`, `municipality_name`: derivables del catálogo.
--   · `device_id` (6 filas), `uuid` (6), `section_number_ine` (2): se
--     absorben igual porque cuestan una columna, pero que estén casi vacías
--     dice que la app nunca las llenó.
--
-- Es IDEMPOTENTE. Se ejecuta con la fase 3 puesta (lee `followers_legacy`
-- por el FDW que ya existe) y antes de la fase 5.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V4__absorb_capture_data.sql
-- =====================================================================

\set ON_ERROR_STOP on

-- ---------------------------------------------------------------------
-- 0. de dónde se lee
--    `leg.followers` es la copia local que hizo V2 (antes del corte) y
--    `leg_fol.followers_legacy` la tabla congelada de hoy. Se prefiere la
--    congelada: es la verdad actual del modelo viejo.
-- ---------------------------------------------------------------------
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_foreign_server WHERE srvname = 'srv_followers') THEN
    RAISE EXCEPTION 'falta el servidor foráneo srv_followers: corre V2 antes';
  END IF;
END $$;

-- Los esquemas de las tablas foráneas se crean si faltan: la limpieza del
-- andamiaje los borra, y este script tiene que seguir siendo re-ejecutable.
CREATE SCHEMA IF NOT EXISTS leg_fol;
CREATE SCHEMA IF NOT EXISTS leg_geo;

DROP FOREIGN TABLE IF EXISTS leg_fol.followers_legacy;
IMPORT FOREIGN SCHEMA public LIMIT TO (followers_legacy)
  FROM SERVER srv_followers INTO leg_fol;

DROP FOREIGN TABLE IF EXISTS leg_geo.geo_secciones_districts;
-- Ojo con las columnas: `geo_secciones` arrastra seis candidatas de distrito y
-- las que están llenas son `distrito_federal_id` —varchar, aunque sea un id— y
-- `distrito_local_id`. `distrito_fed` tiene 4,765 filas de 74,335 y
-- `distrito_loc` cero: son restos de una carga a medias.
CREATE FOREIGN TABLE leg_geo.geo_secciones_districts (
  id integer, distrito_federal_id varchar(255), distrito_local_id integer
) SERVER srv_geo OPTIONS (schema_name 'public', table_name 'geo_secciones');

-- ---------------------------------------------------------------------
-- 1. el distrito, en el territorio
-- ---------------------------------------------------------------------
ALTER TABLE territory ADD COLUMN IF NOT EXISTS federal_district_geo_id BIGINT;
ALTER TABLE territory ADD COLUMN IF NOT EXISTS local_district_geo_id   BIGINT;

COMMENT ON COLUMN territory.federal_district_geo_id IS
  'Referencia al distrito federal del catálogo de geography. Nunca su nombre: geography es el dueño.';
COMMENT ON COLUMN territory.local_district_geo_id IS
  'Referencia al distrito local del catálogo de geography.';

UPDATE territory t
   SET federal_district_geo_id = nullif(trim(g.distrito_federal_id), '')::bigint,
       local_district_geo_id   = g.distrito_local_id
  FROM leg_geo.geo_secciones_districts g
 WHERE t.type = 'SECTION'
   AND t.section_geo_id = g.id
   AND (t.federal_district_geo_id
          IS DISTINCT FROM nullif(trim(g.distrito_federal_id), '')::bigint
     OR t.local_district_geo_id IS DISTINCT FROM g.distrito_local_id);

\echo '--- secciones con distrito resuelto ---'
SELECT count(*) AS secciones,
       count(federal_district_geo_id) AS con_distrito_federal,
       count(local_district_geo_id)   AS con_distrito_local
FROM territory WHERE type = 'SECTION';

-- ---------------------------------------------------------------------
-- 2. identidad y domicilio, en la persona
-- ---------------------------------------------------------------------
ALTER TABLE person ADD COLUMN IF NOT EXISTS birth_entity_id            INTEGER;
ALTER TABLE person ADD COLUMN IF NOT EXISTS residence_entity_id        INTEGER;
ALTER TABLE person ADD COLUMN IF NOT EXISTS residence_municipality_id  INTEGER;
ALTER TABLE person ADD COLUMN IF NOT EXISTS cell_phone_number_other    VARCHAR(255);

-- Sólo rellena el hueco: si el backfill ya puso un valor desde `users` o
-- `promoters`, esa fuente tiene prioridad y no se pisa.
UPDATE person p
   SET birth_entity_id           = COALESCE(p.birth_entity_id, l.birth_entity_id),
       residence_entity_id       = COALESCE(p.residence_entity_id, l.residence_entity_id),
       residence_municipality_id = COALESCE(p.residence_municipality_id, l.residence_municipality_id),
       cell_phone_number_other   = COALESCE(p.cell_phone_number_other,
                                            nullif(trim(l.cell_phone_number_other), ''))
  FROM membership m
  JOIN leg_fol.followers_legacy l ON l.id = m.legacy_id
 WHERE m.person_id = p.id
   AND (p.birth_entity_id IS NULL AND l.birth_entity_id IS NOT NULL
     OR p.residence_entity_id IS NULL AND l.residence_entity_id IS NOT NULL
     OR p.residence_municipality_id IS NULL AND l.residence_municipality_id IS NOT NULL
     OR p.cell_phone_number_other IS NULL
        AND nullif(trim(l.cell_phone_number_other), '') IS NOT NULL);

\echo '--- personas con datos de domicilio absorbidos ---'
SELECT count(birth_entity_id) AS entidad_nacimiento,
       count(residence_entity_id) AS entidad_residencia,
       count(residence_municipality_id) AS municipio_residencia,
       count(cell_phone_number_other) AS telefono_2
FROM person;

-- ---------------------------------------------------------------------
-- 3. el expediente de captura
--    1:1 con la membresía, y se va con ella: si la adhesión se borra —que no
--    pasa, se cierra— el expediente no tiene sentido por su cuenta.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS membership_detail (
    membership_id       BIGINT PRIMARY KEY REFERENCES membership (id) ON DELETE CASCADE,
    -- catálogos que viven en msvc_dash_followers; se guardan por id, igual
    -- que el resto de las referencias a catálogos ajenos
    family_role_id      BIGINT,
    marital_status_id   BIGINT,
    sex_id              BIGINT,
    is_adult            BOOLEAN,
    -- respuestas del levantamiento
    committee_participation         VARCHAR(255),
    inviting_others                 VARCHAR(255),
    number_of_people_invited        VARCHAR(255),
    polling_rep_participation       VARCHAR(255),
    supports_movement               VARCHAR(255),
    support_whatsapp                VARCHAR(255),
    -- quién contestó, cuando no fue el propio convencido
    respondent_last_name VARCHAR(255),
    respondent_sur_name  VARCHAR(255),
    -- rastro del levantamiento
    latitude            VARCHAR(50),
    longitude           VARCHAR(50),
    device_id           VARCHAR(255),
    app_uuid            VARCHAR(255),
    is_in_sync          BOOLEAN,
    -- vigencia de la credencial de elector y su sección impresa
    ine_validity        INTEGER,
    section_number_ine  VARCHAR(255),
    -- cuenta propia del convencido, cuando la tiene. NO va en `person.auth_id`:
    -- ésa es única y la ocupa la cuenta de operador con la que se dedupli­có.
    follower_auth_id    VARCHAR(100),
    updated             TIMESTAMPTZ
);

COMMENT ON TABLE membership_detail IS
  'El expediente de captura del convencido: respuestas del levantamiento y rastro de cómo se '
  'levantó. Aparte de `membership` para que la adhesión siga siendo persona + estructura + puesto '
  '+ vigencia.';

CREATE INDEX IF NOT EXISTS membership_detail_auth_idx
    ON membership_detail (follower_auth_id) WHERE follower_auth_id IS NOT NULL;

INSERT INTO membership_detail (
    membership_id, family_role_id, marital_status_id, sex_id, is_adult,
    committee_participation, inviting_others, number_of_people_invited,
    polling_rep_participation, supports_movement, support_whatsapp,
    respondent_last_name, respondent_sur_name,
    latitude, longitude, device_id, app_uuid, is_in_sync,
    ine_validity, section_number_ine, follower_auth_id, updated)
SELECT m.id, l.cat_family_role, l.cat_marital_status, l.cat_sex, l.is_adult,
       nullif(trim(l.committee_participation), ''),
       nullif(trim(l.inviting_others_to_participate), ''),
       nullif(trim(l.number_of_people_invited), ''),
       nullif(trim(l.pollin_station_representative_participation), ''),
       nullif(trim(l.supports_movement), ''),
       nullif(trim(l.support_whatsapp), ''),
       nullif(trim(l.last_name_resp), ''),
       nullif(trim(l.sur_name_resp), ''),
       nullif(trim(l.latitude), ''),
       nullif(trim(l.longitude), ''),
       nullif(trim(l.device_id), ''),
       nullif(trim(l.uuid), ''),
       l.is_in_sync, l.validity,
       nullif(trim(l.section_number_ine), ''),
       nullif(trim(l.auth_id), ''),
       l.updated
FROM membership m
JOIN leg_fol.followers_legacy l ON l.id = m.legacy_id
WHERE NOT EXISTS (SELECT 1 FROM membership_detail d WHERE d.membership_id = m.id);

ANALYZE membership_detail;
ANALYZE person;
ANALYZE territory;

-- =====================================================================
-- verificación: lo absorbido contra la tabla congelada
-- =====================================================================
\echo '--- expedientes de captura ---'
SELECT count(*) AS expedientes,
       count(family_role_id) AS rol_familiar, count(marital_status_id) AS estado_civil,
       count(sex_id) AS sexo, count(latitude) AS coordenadas,
       count(ine_validity) AS vigencia_ine, count(follower_auth_id) AS con_cuenta
FROM membership_detail;

\echo '--- membresías vivas sin expediente (debería ser 0) ---'
SELECT count(*) AS sin_expediente
FROM membership m
LEFT JOIN membership_detail d ON d.membership_id = m.id
WHERE m.legacy_id IS NOT NULL AND upper(m.validity) IS NULL AND d.membership_id IS NULL;

\echo '--- fidelidad columna a columna contra followers_legacy (vivas) ---'
SELECT count(*) AS filas,
       count(*) FILTER (WHERE d.family_role_id IS DISTINCT FROM l.cat_family_role)      AS dif_rol,
       count(*) FILTER (WHERE d.marital_status_id IS DISTINCT FROM l.cat_marital_status) AS dif_civil,
       count(*) FILTER (WHERE d.sex_id IS DISTINCT FROM l.cat_sex)                      AS dif_sexo,
       count(*) FILTER (WHERE d.is_adult IS DISTINCT FROM l.is_adult)                   AS dif_adulto,
       count(*) FILTER (WHERE d.latitude IS DISTINCT FROM nullif(trim(l.latitude), ''))  AS dif_lat,
       count(*) FILTER (WHERE d.ine_validity IS DISTINCT FROM l.validity)               AS dif_vigencia,
       count(*) FILTER (WHERE d.supports_movement
                              IS DISTINCT FROM nullif(trim(l.supports_movement), ''))    AS dif_apoya
FROM membership m
JOIN membership_detail d ON d.membership_id = m.id
JOIN leg_fol.followers_legacy l ON l.id = m.legacy_id
WHERE upper(m.validity) IS NULL;
