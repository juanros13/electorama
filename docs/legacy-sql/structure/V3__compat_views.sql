-- =====================================================================
-- Electorama — fase 3: vista de compatibilidad para `followers`
--
-- Se ejecuta sobre msvc_dash_followers. La jerarquía pasa a mandarla el
-- modelo nuevo (msvc_dash_structure, vía FDW); el resto de las columnas
-- sigue saliendo de la tabla legacy, porque 29 de las 55 no existen en el
-- modelo nuevo y están pobladas al 95-100%.
--
-- COALESCE(nuevo, legacy): el modelo nuevo gana donde tiene opinión; donde
-- no la tiene, se ve el valor de siempre. Así la vista es 100% fiel hoy y
-- refleja de inmediato cualquier rotación o traslado que se haga en el
-- modelo nuevo.
--
-- ROLLBACK: ver R3__compat_rollback.sql
-- =====================================================================

BEGIN;

ALTER TABLE followers RENAME TO followers_legacy;

CREATE OR REPLACE VIEW nw_hierarchy_v AS
SELECT m.legacy_id AS follower_id,
       CASE WHEN s.type='MAIN'      THEN h.auth_id END AS promoter_auth_id,
       CASE WHEN s.type='ALTERNATE' THEN h.auth_id END AS promoter_alt_auth_id,
       cap.auth_id AS user_create_id,
       m.status, m.vote_flag, m.vote_flag_timestamp
FROM nw.membership m
JOIN nw.structure s ON s.id = m.structure_id
LEFT JOIN nw.assignment h   ON h.position_id = m.position_id AND h.role='HOLDER'
LEFT JOIN nw.assignment cap ON cap.id = m.captured_by_assignment_id;

CREATE VIEW followers AS
SELECT
       l.id,
       l.birth_entity_id,
       l.birthdate,
       l.cell_phone_number,
       l.created,
       l.curp,
       l.deleted,
       l.email,
       l.ine,
       l.last_name,
       l.names,
       l.section_number,
       COALESCE(n.status, l.status) AS status,
       l.sur_name,
       l.updated,
       COALESCE(n.user_create_id, l.user_create_id) AS user_create_id,
       l.cat_family_role,
       l.cat_marital_status,
       l.cat_sex,
       l.cell_phone_number_other,
       l.committee_participation,
       l.device_id,
       l.external_number,
       l.internal_number,
       l.inviting_others_to_participate,
       l.is_adult,
       l.is_in_sync,
       l.latitude,
       l.longitude,
       l.neighborhood,
       l.number_of_people_invited,
       l.pollin_station_representative_participation,
       l.residence_entity_id,
       l.residence_municipality_id,
       l.section_number_ine,
       l.street,
       l.support_whatsapp,
       l.supports_movement,
       l.uuid,
       l.validity,
       l.zip_code,
       l.auth_id,
       COALESCE(n.promoter_auth_id, l.promoter_auth_id) AS promoter_auth_id,
       COALESCE(n.promoter_alt_auth_id, l.promoter_alt_auth_id) AS promoter_alt_auth_id,
       COALESCE(n.vote_flag, l.vote_flag) AS vote_flag,
       l.last_name_resp,
       l.sur_name_resp,
       COALESCE(n.vote_flag_timestamp, l.vote_flag_timestamp) AS vote_flag_timestamp,
       l.federal_district_id,
       l.federal_district_name,
       l.local_district_id,
       l.local_district_name,
       l.municipality_id,
       l.municipality_name,
       l.section_id

FROM followers_legacy l
LEFT JOIN nw_hierarchy_v n ON n.follower_id = l.id;

COMMIT;
