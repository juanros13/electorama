-- =====================================================================
-- Electorama — msvc-structure
-- V4c: las vistas de compatibilidad, ahora completas desde el modelo
--
-- Hasta aquí `compat.followers` y `compat.promoters` servían sólo lo que el
-- modelo poseía, y la vista del otro lado completaba el resto pegando
-- columnas de `followers_legacy` y `promoters_legacy`. Con V4 y V4b el modelo
-- ya tiene esos datos, así que las vistas pueden servirlo TODO — que es la
-- condición para que la fase 5 pueda tirar las tablas legacy sin perder nada.
--
-- Los NOMBRES de municipio y distrito no se copiaron al modelo a propósito
-- (geography es su dueño), así que las vistas los leen en vivo del catálogo
-- por postgres_fdw, en el esquema `geo_ref`. Ese esquema existe sólo para la
-- capa de compatibilidad y se va con ella en la fase 5: el servicio migrado
-- le pregunta a geography por HTTP, una vez por reporte y no un millón de
-- veces como hacía la copia denormalizada.
--
-- Dato de paso: `federal_district_name` y `local_district_name` del modelo
-- viejo no son nombres, son el NÚMERO del distrito en texto ("4", "9"). El
-- catálogo lo tiene como `distrito_federal` y `distrito_local`, así que la
-- vista lo deriva en vez de arrastrar la copia.
--
-- Y la copia estaba desactualizada, como era de esperar: el distrito que
-- reporta la vista difiere del copiado en 3,159 convencidos del distrito
-- federal y 10,117 del local. Pero no es ruido por convencido —la copia es
-- consistente dentro de cada sección, 0 de 1,182 con dos valores— sino
-- REDISTRITACIÓN: una sección cambió de distrito federal y dos de distrito
-- local. El catálogo es el dueño y está al día; la copia se quedó en el día
-- de la captura.
--
-- Es IDEMPOTENTE. Después de V4b, antes de V4d.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V4c__compat_source_full.sql
-- =====================================================================

\set ON_ERROR_STOP on

-- ---------------------------------------------------------------------
-- 0. lectura en vivo del catálogo de geography
-- ---------------------------------------------------------------------
DROP SCHEMA IF EXISTS geo_ref CASCADE;
CREATE SCHEMA geo_ref;

COMMENT ON SCHEMA geo_ref IS
  'Lectura en vivo del catálogo de geography para las vistas de compatibilidad. '
  'No es una copia y muere con la fase 5.';

CREATE FOREIGN TABLE geo_ref.municipalities (
  id integer, nom_mun varchar(50)
) SERVER srv_geo OPTIONS (schema_name 'public', table_name 'geo_municipios');

CREATE FOREIGN TABLE geo_ref.federal_districts (
  id bigint, distrito_federal bigint
) SERVER srv_geo OPTIONS (schema_name 'public', table_name 'geo_distritos_federales');

CREATE FOREIGN TABLE geo_ref.local_districts (
  id bigint, distrito_local bigint
) SERVER srv_geo OPTIONS (schema_name 'public', table_name 'geo_distritos_locales');

-- ---------------------------------------------------------------------
-- 1. compat.followers, completa
-- ---------------------------------------------------------------------
DROP VIEW IF EXISTS compat.followers;
CREATE VIEW compat.followers AS
SELECT m.legacy_id                                    AS id,
       per.birth_entity_id                            AS birth_entity_id,
       per.birthdate::timestamp                       AS birthdate,
       per.cell_phone_number::varchar(255)            AS cell_phone_number,
       lower(m.validity)::timestamp                   AS created,
       per.curp::varchar(255)                         AS curp,
       upper(m.validity)::timestamp                   AS deleted,
       per.email::varchar(255)                        AS email,
       per.ine::varchar(255)                          AS ine,
       per.last_name::varchar(255)                    AS last_name,
       per.names::varchar(255)                        AS names,
       -- el número de sección se deriva del código del territorio: la copia
       -- que guardaba el modelo viejo estaba mal en 840 de los casos medidos
       lpad(regexp_replace(sec.code, '^S-', ''), 4, '0')::varchar(255) AS section_number,
       m.status::varchar(255)                         AS status,
       per.sur_name::varchar(255)                     AS sur_name,
       d.updated::timestamp                           AS updated,
       COALESCE(ca.auth_id, cap.auth_id,
                m.captured_by_auth_id)::varchar(255)  AS user_create_id,
       d.family_role_id                               AS cat_family_role,
       d.marital_status_id                            AS cat_marital_status,
       d.sex_id                                       AS cat_sex,
       per.cell_phone_number_other::varchar(255)      AS cell_phone_number_other,
       d.committee_participation::varchar(255)        AS committee_participation,
       d.device_id::varchar(255)                      AS device_id,
       per.external_number::varchar(255)              AS external_number,
       per.internal_number::varchar(255)              AS internal_number,
       d.inviting_others::varchar(255)                AS inviting_others_to_participate,
       d.is_adult                                     AS is_adult,
       d.is_in_sync                                   AS is_in_sync,
       d.latitude::varchar(255)                       AS latitude,
       d.longitude::varchar(255)                      AS longitude,
       per.neighborhood::varchar(255)                 AS neighborhood,
       d.number_of_people_invited::varchar(255)       AS number_of_people_invited,
       d.polling_rep_participation::varchar(255)      AS pollin_station_representative_participation,
       per.residence_entity_id                        AS residence_entity_id,
       per.residence_municipality_id                  AS residence_municipality_id,
       d.section_number_ine::varchar(255)             AS section_number_ine,
       per.street::varchar(255)                       AS street,
       d.support_whatsapp::varchar(255)               AS support_whatsapp,
       d.supports_movement::varchar(255)              AS supports_movement,
       d.app_uuid::varchar(255)                       AS uuid,
       d.ine_validity                                 AS validity,
       per.zip_code::varchar(255)                     AS zip_code,
       d.follower_auth_id::varchar(255)               AS auth_id,
       (CASE WHEN st.type = 'MAIN'      THEN ph.auth_id END)::varchar(255) AS promoter_auth_id,
       (CASE WHEN st.type = 'ALTERNATE' THEN ph.auth_id END)::varchar(255) AS promoter_alt_auth_id,
       m.vote_flag                                    AS vote_flag,
       d.respondent_last_name::varchar(255)           AS last_name_resp,
       d.respondent_sur_name::varchar(255)            AS sur_name_resp,
       m.vote_flag_timestamp::timestamp               AS vote_flag_timestamp,
       sec.federal_district_geo_id                    AS federal_district_id,
       fd.distrito_federal::varchar(255)              AS federal_district_name,
       sec.local_district_geo_id                      AS local_district_id,
       ld.distrito_local::varchar(255)                AS local_district_name,
       mun.municipality_geo_id::bigint                AS municipality_id,
       gm.nom_mun::varchar(255)                       AS municipality_name,
       sec.section_geo_id::bigint                     AS section_id
FROM membership m
JOIN structure st ON st.id = m.structure_id
LEFT JOIN membership_detail d ON d.membership_id = m.id
LEFT JOIN person per    ON per.id = m.person_id
LEFT JOIN territory sec ON sec.id = m.territory_id
LEFT JOIN territory mun ON mun.id = sec.parent_territory_id
LEFT JOIN geo_ref.municipalities gm      ON gm.id = mun.municipality_geo_id
LEFT JOIN geo_ref.federal_districts fd   ON fd.id = sec.federal_district_geo_id
LEFT JOIN geo_ref.local_districts ld     ON ld.id = sec.local_district_geo_id
LEFT JOIN compat.position_holder ph ON ph.position_id = m.position_id
LEFT JOIN assignment ca ON ca.id = m.captured_by_assignment_id
LEFT JOIN person cap    ON cap.id = ca.person_id
WHERE m.legacy_id IS NOT NULL
  AND upper(m.validity) IS NULL;

-- ---------------------------------------------------------------------
-- 2. compat.promoters, completa
--
-- `auth_id` sale del authId del nombramiento: en el modelo viejo era un
-- duplicado a medio llenar de `promoter_auth_id` (23,239 de 49,355, y 23,231
-- idénticas). `follower_id` sale NULL a propósito: de sus 27,680 valores sólo
-- 86 resolvían a una membresía, y en esos 86 apuntaban a otra persona.
-- `cat_committee` también, que en la tabla vieja estaba vacía al 100%.
-- ---------------------------------------------------------------------
DROP VIEW IF EXISTS compat.promoters;
CREATE VIEW compat.promoters AS
SELECT pos.legacy_id                                  AS id,
       per.cell_phone_number::varchar(255)            AS cell_phone_number,
       pos.valid_from::timestamp                      AS created,
       per.curp::varchar(255)                         AS curp,
       pos.valid_to::timestamp                        AS deleted,
       per.email::varchar(255)                        AS email,
       per.ine::varchar(255)                          AS ine,
       per.internal_number::varchar(255)              AS interior_number,
       per.last_name::varchar(255)                    AS last_name,
       mun.municipality_geo_id                        AS municipality_id,
       per.names::varchar(255)                        AS names,
       per.neighborhood::varchar(255)                 AS neighborhood_id,
       hold.notes::varchar(255)                       AS notes,
       per.external_number::varchar(255)              AS outdoor_number,
       per.zip_code::varchar(255)                     AS postal_code,
       per.street::varchar(255)                       AS street,
       per.sur_name::varchar(255)                     AS sur_name,
       hold.updated::timestamp                        AS updated,
       hold.created_by_auth_id::varchar(255)          AS user_create_id,
       per.ine_validity                               AS vencimiento,
       NULL::bigint                                   AS cat_committee,
       hold.holder_auth::varchar(255)                 AS auth_id,
       NULL::bigint                                   AS follower_id,
       sec.section_geo_id                             AS seccion_id,
       compat.holder_auth(pos.parent_position_id)::varchar(100) AS cordinator_zone_auth_id,
       compat.holder_auth(cz.parent_position_id)::varchar(100)  AS referent_auth_id,
       hold.holder_auth::varchar(255)                 AS promoter_auth_id,
       pos.goal                                       AS followers_goal,
       (st.type = 'ALTERNATE')                        AS flag_alter
FROM position pos
JOIN structure st       ON st.id = pos.structure_id
LEFT JOIN territory sec ON sec.id = pos.territory_id
LEFT JOIN territory mun ON mun.id = sec.parent_territory_id
LEFT JOIN position cz   ON cz.id = pos.parent_position_id
LEFT JOIN LATERAL (
    SELECT a.person_id, a.notes, a.updated, a.created_by_auth_id,
           COALESCE(a.auth_id, pe.auth_id) AS holder_auth
    FROM assignment a
    JOIN person pe ON pe.id = a.person_id
    WHERE a.position_id = pos.id
      AND a.role = 'HOLDER'
      AND upper(a.validity) IS NULL
    LIMIT 1) hold ON true
LEFT JOIN person per ON per.id = hold.person_id
WHERE pos.level = 'PROMOTER'
  AND pos.legacy_id IS NOT NULL
  AND pos.valid_to IS NULL;

-- Los esquemas de las tablas foráneas se crean si faltan: la limpieza del
-- andamiaje los borra, y este script tiene que seguir siendo re-ejecutable.
CREATE SCHEMA IF NOT EXISTS leg_fol;

-- =====================================================================
-- verificación
-- =====================================================================
\echo '--- filas que sirven las vistas ---'
SELECT (SELECT count(*) FROM compat.followers) AS followers,
       (SELECT count(*) FROM compat.promoters) AS promoters;

\echo '--- distrito: la vista contra la copia del modelo viejo, por sección ---'
-- Se mide por SECCIÓN a propósito: si la diferencia fuera ruido por
-- convencido habría que preocuparse; si es por sección, es redistritación y
-- la vista tiene razón.
SELECT count(DISTINCT section_id) FILTER (WHERE dif_federal) AS secciones_dif_federal,
       count(DISTINCT section_id) FILTER (WHERE dif_local)   AS secciones_dif_local,
       sum(filas) FILTER (WHERE dif_federal)                 AS convencidos_federal,
       sum(filas) FILTER (WHERE dif_local)                   AS convencidos_local
FROM (SELECT v.section_id,
             bool_or(v.federal_district_id IS DISTINCT FROM l.federal_district_id) AS dif_federal,
             bool_or(v.local_district_id IS DISTINCT FROM l.local_district_id)      AS dif_local,
             count(*) AS filas
      FROM compat.followers v
      JOIN leg_fol.followers_legacy l ON l.id = v.id
      GROUP BY 1) q;

\echo '--- la copia vieja, era consistente dentro de cada sección? ---'
SELECT count(*) AS secciones,
       count(*) FILTER (WHERE valores > 1) AS con_dos_valores_distintos
FROM (SELECT section_id, count(DISTINCT federal_district_id) AS valores
      FROM leg_fol.followers_legacy WHERE section_id IS NOT NULL GROUP BY 1) y;
