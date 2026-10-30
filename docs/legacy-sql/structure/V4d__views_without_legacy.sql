-- =====================================================================
-- Electorama — msvc-structure
-- V4d: las vistas de msvc_dash_followers dejan de leer `*_legacy`
--
-- Es el último paso antes de la fase 5, y el que la vuelve segura: mientras
-- las vistas peguen columnas desde `followers_legacy` y `promoters_legacy`,
-- tirar esas tablas es perder datos. Con V4 y V4b el modelo ya los tiene y
-- con V4c las vistas de `compat` ya los sirven, así que aquí las vistas del
-- lado viejo se reescriben para leer SÓLO del modelo.
--
-- Después de esto, `followers_legacy` y `promoters_legacy` no las lee nadie:
-- se quedan como red de seguridad del rollback (`V3R`) hasta la fase 5.
--
-- El orden de las columnas se conserva exactamente: un `SELECT *` de
-- msvc-followers no cambia de forma.
--
-- Es IDEMPOTENTE. Requiere V4c ejecutado en msvc_dash_structure.
--
--   psql -U postgres -d msvc_dash_followers -v ON_ERROR_STOP=1 -f V4d__views_without_legacy.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

-- Las vistas remotas cambiaron de forma, así que hay que re-importar las
-- tablas foráneas: cada una guarda la lista de columnas del momento del
-- IMPORT. Y para tirarlas hay que tirar antes las vistas que las usan, así
-- que todo el reemplazo va en una transacción: nadie ve la base sin
-- `followers`. En PostgreSQL el DDL es transaccional, IMPORT incluido.
DROP VIEW followers;
DROP VIEW promoters;
DROP FOREIGN TABLE IF EXISTS str.followers;
DROP FOREIGN TABLE IF EXISTS str.promoters;
IMPORT FOREIGN SCHEMA compat LIMIT TO (followers, promoters)
  FROM SERVER srv_structure INTO str;
ALTER FOREIGN TABLE str.followers OPTIONS (ADD updatable 'false');
ALTER FOREIGN TABLE str.promoters OPTIONS (ADD updatable 'false');

CREATE VIEW followers AS
SELECT r.id,
       r.birth_entity_id,
       r.birthdate,
       r.cell_phone_number,
       r.created,
       r.curp,
       r.deleted,
       r.email,
       r.ine,
       r.last_name,
       r.names,
       r.section_number,
       r.status,
       r.sur_name,
       r.updated,
       r.user_create_id,
       r.cat_family_role,
       r.cat_marital_status,
       r.cat_sex,
       r.cell_phone_number_other,
       r.committee_participation,
       r.device_id,
       r.external_number,
       r.internal_number,
       r.inviting_others_to_participate,
       r.is_adult,
       r.is_in_sync,
       r.latitude,
       r.longitude,
       r.neighborhood,
       r.number_of_people_invited,
       r.pollin_station_representative_participation,
       r.residence_entity_id,
       r.residence_municipality_id,
       r.section_number_ine,
       r.street,
       r.support_whatsapp,
       r.supports_movement,
       r.uuid,
       r.validity,
       r.zip_code,
       r.auth_id,
       r.promoter_auth_id,
       r.promoter_alt_auth_id,
       r.vote_flag,
       r.last_name_resp,
       r.sur_name_resp,
       r.vote_flag_timestamp,
       r.federal_district_id,
       r.federal_district_name,
       r.local_district_id,
       r.local_district_name,
       r.municipality_id,
       r.municipality_name,
       r.section_id
FROM str.followers r;

COMMENT ON VIEW followers IS
  'Vista de compatibilidad (fase 3). Lee ÚNICAMENTE msvc_dash_structure; ya no toca '
  'followers_legacy. Escribe por triggers INSTEAD OF.';

CREATE VIEW promoters AS
SELECT r.id,
       r.cell_phone_number,
       r.created,
       r.curp,
       r.deleted,
       r.email,
       r.ine,
       r.interior_number,
       r.last_name,
       r.municipality_id,
       r.names,
       r.neighborhood_id,
       r.notes,
       r.outdoor_number,
       r.postal_code,
       r.street,
       r.sur_name,
       r.updated,
       r.user_create_id,
       r.vencimiento,
       r.cat_committee,
       r.auth_id,
       r.follower_id,
       r.seccion_id,
       r.cordinator_zone_auth_id,
       r.referent_auth_id,
       r.promoter_auth_id,
       r.followers_goal,
       r.flag_alter
FROM str.promoters r;

COMMENT ON VIEW promoters IS
  'Vista de compatibilidad (fase 3). Lee ÚNICAMENTE msvc_dash_structure; ya no toca '
  'promoters_legacy. Escribe por triggers INSTEAD OF.';

-- Los triggers INSTEAD OF se recrean: apuntaban a las vistas que acabamos de
-- tirar. Las funciones no cambian — escriben en el modelo, no en las vistas.
CREATE TRIGGER followers_insert_trg INSTEAD OF INSERT ON followers
  FOR EACH ROW EXECUTE FUNCTION compat.followers_insert();
CREATE TRIGGER followers_update_trg INSTEAD OF UPDATE ON followers
  FOR EACH ROW EXECUTE FUNCTION compat.followers_update();
CREATE TRIGGER followers_delete_trg INSTEAD OF DELETE ON followers
  FOR EACH ROW EXECUTE FUNCTION compat.followers_delete();

CREATE TRIGGER promoters_insert_trg INSTEAD OF INSERT ON promoters
  FOR EACH ROW EXECUTE FUNCTION compat.promoters_insert();
CREATE TRIGGER promoters_update_trg INSTEAD OF UPDATE ON promoters
  FOR EACH ROW EXECUTE FUNCTION compat.promoters_update();
CREATE TRIGGER promoters_delete_trg INSTEAD OF DELETE ON promoters
  FOR EACH ROW EXECUTE FUNCTION compat.promoters_delete();

COMMIT;

-- =====================================================================
-- verificación
-- =====================================================================
\echo '--- filas y forma ---'
SELECT (SELECT count(*) FROM followers) AS followers,
       (SELECT count(*) FROM promoters) AS promoters,
       (SELECT count(*) FROM information_schema.columns WHERE table_name = 'followers') AS columnas_followers,
       (SELECT count(*) FROM information_schema.columns WHERE table_name = 'promoters') AS columnas_promoters;

\echo '--- ninguna vista depende ya de las tablas legacy ---'
SELECT count(*) AS dependencias_legacy
FROM pg_depend d
JOIN pg_rewrite r ON r.oid = d.objid
JOIN pg_class v ON v.oid = r.ev_class
JOIN pg_class t ON t.oid = d.refobjid
WHERE v.relname IN ('followers', 'promoters')
  AND t.relname IN ('followers_legacy', 'promoters_legacy');

\echo '--- fidelidad de lo recién absorbido, fila a fila ---'
SELECT count(*) AS filas,
       count(*) FILTER (WHERE v.cat_family_role IS DISTINCT FROM l.cat_family_role)   AS dif_rol,
       count(*) FILTER (WHERE v.cat_sex IS DISTINCT FROM l.cat_sex)                   AS dif_sexo,
       count(*) FILTER (WHERE v.is_adult IS DISTINCT FROM l.is_adult)                 AS dif_adulto,
       count(*) FILTER (WHERE v.supports_movement IS DISTINCT FROM nullif(trim(l.supports_movement), '')) AS dif_apoya,
       count(*) FILTER (WHERE v.validity IS DISTINCT FROM l.validity)                 AS dif_vigencia,
       count(*) FILTER (WHERE v.birth_entity_id IS DISTINCT FROM l.birth_entity_id)    AS dif_entidad,
       count(*) FILTER (WHERE v.residence_municipality_id IS DISTINCT FROM l.residence_municipality_id) AS dif_mun_residencia
FROM followers v
JOIN followers_legacy l ON l.id = v.id;
