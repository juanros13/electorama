-- =====================================================================
-- Electorama — msvc-structure
-- V3b: el corte (fase 3) sobre la base msvc_dash_followers
--
-- Renombra `followers` y `promoters` a `*_legacy` y las sustituye por
-- vistas del mismo nombre que leen el modelo nuevo a través de
-- postgres_fdw, con triggers INSTEAD OF para escribir. msvc-followers y
-- todo lo que consulte esta base siguen funcionando sin tocar una línea.
--
-- ALCANCE: sólo `followers` y `promoters`. `users_seccions` NO se corta
-- todavía — el backfill construyó los puestos de coordinador de zona
-- únicamente desde `promoters` (8,060 parejas coordinador/sección) y
-- `users_seccions` tiene 44,805 vivas, así que una vista sobre el modelo
-- nuevo perdería ~36,700 asignaciones con sus metas. Va en una fase 3b,
-- después de extender el paso 4b de V2.
--
-- REVERSIBLE: V3R__rollback_followers.sql deshace todo esto.
--
-- Requiere V3a ya ejecutado en msvc_dash_structure.
--
--   psql -U postgres -d msvc_dash_followers -v ON_ERROR_STOP=1 \
--        -v fdw_user=postgres -v fdw_password=… -f V3b__compat_followers.sql
-- =====================================================================

\if :{?fdw_user}
\else
\set fdw_user 'postgres'
\endif
\if :{?fdw_password}
\else
\set fdw_password '<redacted>'
\endif
\if :{?fdw_host}
\else
\set fdw_host 'localhost'
\endif
\if :{?fdw_port}
\else
\set fdw_port '5432'
\endif

-- ---------------------------------------------------------------------
-- 0. enlace al modelo nuevo
-- ---------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS postgres_fdw;

-- Las variables de psql no se interpolan dentro de un bloque con comillas
-- de dólar, así que viajan como parámetros de sesión.
SET compat.fdw_host = :'fdw_host';
SET compat.fdw_port = :'fdw_port';
SET compat.fdw_user = :'fdw_user';
SET compat.fdw_pw   = :'fdw_password';

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_foreign_server WHERE srvname = 'srv_structure') THEN
    EXECUTE format(
      'CREATE SERVER srv_structure FOREIGN DATA WRAPPER postgres_fdw
         OPTIONS (host %L, port %L, dbname %L, fetch_size %L)',
      current_setting('compat.fdw_host'),
      current_setting('compat.fdw_port'),
      'msvc_dash_structure', '10000');
  END IF;
END $$;

-- Sin esto postgres_fdw trae las filas de 100 en 100: un reporte de un
-- millón de convencidos serían 10,000 idas y vueltas. Se aplica aparte del
-- CREATE para que también arregle un servidor que ya existiera; ADD y SET
-- no son intercambiables, así que hay que mirar antes si la opción está.
DO $$
DECLARE
  o text;
BEGIN
  FOREACH o IN ARRAY ARRAY['fetch_size=50000', 'use_remote_estimate=true'] LOOP
    IF EXISTS (SELECT 1 FROM pg_foreign_server s, unnest(s.srvoptions) x
                WHERE s.srvname = 'srv_structure'
                  AND split_part(x, '=', 1) = split_part(o, '=', 1)) THEN
      EXECUTE format('ALTER SERVER srv_structure OPTIONS (SET %s %L)',
                     split_part(o, '=', 1), split_part(o, '=', 2));
    ELSE
      EXECUTE format('ALTER SERVER srv_structure OPTIONS (ADD %s %L)',
                     split_part(o, '=', 1), split_part(o, '=', 2));
    END IF;
  END LOOP;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM pg_user_mappings
      WHERE srvname = 'srv_structure'
        AND (usename = current_user OR usename = 'public')) THEN
    EXECUTE format(
      'CREATE USER MAPPING FOR CURRENT_USER SERVER srv_structure
         OPTIONS (user %L, password %L)',
      current_setting('compat.fdw_user'), current_setting('compat.fdw_pw'));
  END IF;
END $$;

DROP SCHEMA IF EXISTS str CASCADE;
CREATE SCHEMA str;

-- las vistas de forma vieja: el join pesado se resuelve del otro lado
IMPORT FOREIGN SCHEMA compat
  LIMIT TO (followers, promoters, position_holder,
            seq_person, seq_position, seq_assignment, seq_membership, seq_change_log)
  FROM SERVER srv_structure INTO str;

-- las tablas reales: es donde escriben los triggers
IMPORT FOREIGN SCHEMA public
  LIMIT TO (person, territory, structure, position, assignment, membership, change_log)
  FROM SERVER srv_structure INTO str;

-- una vista remota no tiene ctid: postgres_fdw no puede hacerle UPDATE ni
-- DELETE. Se marca para que el error salga al planear y no a mitad de una
-- transacción de producción.
ALTER FOREIGN TABLE str.followers       OPTIONS (ADD updatable 'false');
ALTER FOREIGN TABLE str.promoters       OPTIONS (ADD updatable 'false');
ALTER FOREIGN TABLE str.position_holder OPTIONS (ADD updatable 'false');

-- ---------------------------------------------------------------------
-- 1. comprobación previa: si el modelo nuevo no cuadra, no se corta
-- ---------------------------------------------------------------------
DO $$
DECLARE
  v_remote_prom bigint;
  v_local_prom  bigint;
  v_remote_fol  bigint;
  v_local_fol   bigint;
BEGIN
  SELECT count(*) INTO v_remote_prom FROM str.promoters;
  SELECT count(*) INTO v_local_prom  FROM promoters;
  SELECT count(*) INTO v_remote_fol  FROM str.followers;
  SELECT count(*) INTO v_local_fol   FROM followers WHERE deleted IS NULL;

  RAISE NOTICE 'promoters: modelo nuevo %, modelo viejo %', v_remote_prom, v_local_prom;
  RAISE NOTICE 'followers: modelo nuevo %, modelo viejo %', v_remote_fol, v_local_fol;

  IF v_remote_prom <> v_local_prom THEN
    RAISE EXCEPTION 'El modelo nuevo tiene % promotores y el viejo %. Corre V2 y V3a antes de cortar.',
      v_remote_prom, v_local_prom;
  END IF;
  -- en followers sí se admite diferencia: son los duplicados que el
  -- backfill descartó a propósito y dejó en migration_duplicate
  IF v_remote_fol > v_local_fol OR (v_local_fol - v_remote_fol) > v_local_fol / 100 THEN
    RAISE EXCEPTION 'Diferencia de % convencidos entre modelos; se esperaba sólo el descarte de duplicados.',
      v_local_fol - v_remote_fol;
  END IF;
END $$;

-- ---------------------------------------------------------------------
-- 2. índices que las tablas viejas nunca tuvieron
--    `followers` (1,067,949 filas) y `promoters` no tienen ni llave
--    primaria ni un solo índice: cada búsqueda por id es un recorrido
--    secuencial. La vista las une por id, así que sin esto el corte
--    degradaría todo. Se crean ANTES de la transacción del corte para no
--    alargar la ventana.
-- ---------------------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS followers_legacy_id_uq ON followers (id);
CREATE UNIQUE INDEX IF NOT EXISTS promoters_legacy_id_uq ON promoters (id);
ANALYZE followers;
ANALYZE promoters;

-- =====================================================================
-- 3. EL CORTE — todo lo que sigue va en una sola transacción
-- =====================================================================
BEGIN;

ALTER TABLE followers RENAME TO followers_legacy;
ALTER TABLE promoters RENAME TO promoters_legacy;

COMMENT ON TABLE followers_legacy IS
  'Tabla original de convencidos, congelada en la fase 3. Conserva las columnas que el modelo nuevo no posee (distritos, catalogos, uuid, coordenadas): la vista `followers` las pega por id. Las columnas estructurales de las filas anteriores al corte quedan como estaban y ya no se leen.';
COMMENT ON TABLE promoters_legacy IS
  'Tabla original de promotores, congelada en la fase 3. Conserva neighborhood_id, notes, vencimiento, cat_committee, follower_id y updated, que el modelo nuevo no posee.';

CREATE SCHEMA IF NOT EXISTS compat;

-- ---------------------------------------------------------------------
-- 4. resolución de llaves viejas → modelo nuevo
-- ---------------------------------------------------------------------

-- Repartidores de id. postgres_fdw manda TODAS las columnas al insertar en
-- una tabla foránea, así que el `id` viaja explícitamente como NULL y el
-- DEFAULT de la secuencia remota nunca llega a aplicarse: hay que pedirlo
-- antes. Cuesta una ida y vuelta por fila insertada.
--
-- La misma trampa vale para CUALQUIER columna NOT NULL con DEFAULT del otro
-- lado: `assignment.created`, `change_log.occurred_at`, `membership.source`,
-- `status`, `validity` y `vote_flag` se pasan a mano por eso, no por gusto.
-- Al agregar una columna así al modelo hay que volver aquí.
CREATE OR REPLACE FUNCTION compat.next_person_id()     RETURNS bigint AS $fn$ SELECT id FROM str.seq_person     $fn$ LANGUAGE sql;
CREATE OR REPLACE FUNCTION compat.next_position_id()   RETURNS bigint AS $fn$ SELECT id FROM str.seq_position   $fn$ LANGUAGE sql;
CREATE OR REPLACE FUNCTION compat.next_assignment_id() RETURNS bigint AS $fn$ SELECT id FROM str.seq_assignment $fn$ LANGUAGE sql;
CREATE OR REPLACE FUNCTION compat.next_membership_id() RETURNS bigint AS $fn$ SELECT id FROM str.seq_membership $fn$ LANGUAGE sql;
CREATE OR REPLACE FUNCTION compat.next_change_log_id() RETURNS bigint AS $fn$ SELECT id FROM str.seq_change_log $fn$ LANGUAGE sql;

-- Cerrar una vigencia NO puede usar now(): dentro de una transacción es
-- constante, así que dar de baja algo creado en la misma transacción
-- produce `tstzrange(t, t)`, que es el rango VACÍO. Y `upper()` de un rango
-- vacío es NULL, o sea que la fila se leería como vigente: una baja que no
-- da de baja. clock_timestamp() avanza, y el GREATEST garantiza que el
-- rango nunca quede vacío ni invertido.
CREATE OR REPLACE FUNCTION compat.close_validity(p_range tstzrange, p_at timestamptz DEFAULT NULL)
RETURNS tstzrange AS $fn$
  SELECT tstzrange(lower(p_range),
                   GREATEST(COALESCE(p_at, clock_timestamp()),
                            lower(p_range) + interval '1 microsecond'));
$fn$ LANGUAGE sql VOLATILE;

-- sección del catálogo INE → territorio
CREATE OR REPLACE FUNCTION compat.territory_for_section(p_section bigint)
RETURNS bigint AS $$
  SELECT id FROM str.territory
   WHERE section_geo_id = p_section::integer AND type = 'SECTION'
   LIMIT 1;
$$ LANGUAGE sql STABLE;

-- municipio del catálogo INE → territorio
CREATE OR REPLACE FUNCTION compat.territory_for_municipality(p_mun bigint)
RETURNS bigint AS $$
  SELECT id FROM str.territory
   WHERE municipality_geo_id = p_mun::integer AND type = 'MUNICIPALITY'
   LIMIT 1;
$$ LANGUAGE sql STABLE;

-- authId de Keycloak → su asignación de titular vigente (el linaje)
CREATE OR REPLACE FUNCTION compat.assignment_for_auth(p_auth varchar)
RETURNS bigint AS $$
  SELECT assignment_id FROM str.position_holder
   WHERE p_auth IS NOT NULL AND auth_id = p_auth
   ORDER BY assignment_id
   LIMIT 1;
$$ LANGUAGE sql STABLE;

-- authId del promotor + rama → su puesto.
-- Si el puntero está roto se cae a la bolsa de la sección y, si tampoco hay
-- sección, a la rama sin ubicación: la misma política que el backfill, para
-- que un convencido nunca se pierda por un authId que no existe.
CREATE OR REPLACE FUNCTION compat.position_for_promoter(
    p_auth varchar, p_alternate boolean, p_section bigint)
RETURNS bigint AS $$
DECLARE v_id bigint;
BEGIN
  IF p_auth IS NOT NULL THEN
    SELECT p.id INTO v_id
      FROM str.position_holder h
      JOIN str.position  p ON p.id = h.position_id
      JOIN str.structure s ON s.id = p.structure_id
     WHERE h.auth_id = p_auth
       AND p.level = 'PROMOTER'
       AND p.valid_to IS NULL
       AND s.type = CASE WHEN p_alternate THEN 'ALTERNATE' ELSE 'MAIN' END
     ORDER BY p.id
     LIMIT 1;
    IF v_id IS NOT NULL THEN RETURN v_id; END IF;
  END IF;

  IF p_section IS NOT NULL THEN
    SELECT p.id INTO v_id
      FROM str.position p
      JOIN str.territory t ON t.id = p.territory_id
     WHERE t.section_geo_id = p_section::integer
       AND p.code = 'POOL|' || t.code
     LIMIT 1;
    IF v_id IS NOT NULL THEN RETURN v_id; END IF;
  END IF;

  SELECT id INTO v_id FROM str.position WHERE code = 'UNPLACED|POOL' LIMIT 1;
  RETURN v_id;
END;
$$ LANGUAGE plpgsql STABLE;

-- persona por CURP; si no existe, se crea. La CURP es la llave de identidad
-- del modelo nuevo (índice único parcial), igual que en el backfill.
CREATE OR REPLACE FUNCTION compat.person_upsert(
    p_curp varchar, p_ine varchar, p_names varchar,
    p_last_name varchar, p_sur_name varchar, p_birthdate timestamp,
    p_email varchar, p_cell varchar, p_street varchar,
    p_external varchar, p_internal varchar, p_neighborhood varchar,
    p_zip varchar, p_legacy_source varchar, p_legacy_id bigint)
RETURNS bigint AS $$
DECLARE
  v_curp varchar := nullif(upper(trim(p_curp)), '');
  v_ine  varchar := nullif(upper(trim(p_ine)), '');
  v_id   bigint;
BEGIN
  IF v_curp IS NOT NULL THEN
    SELECT id INTO v_id FROM str.person WHERE curp = v_curp LIMIT 1;
  END IF;

  IF v_id IS NOT NULL THEN
    -- se completan huecos, nunca se pisa un dato existente con uno nulo
    UPDATE str.person
       SET ine               = COALESCE(ine, v_ine),
           names             = COALESCE(names, p_names),
           last_name         = COALESCE(last_name, p_last_name),
           sur_name          = COALESCE(sur_name, p_sur_name),
           birthdate         = COALESCE(birthdate, p_birthdate::date),
           email             = COALESCE(email, p_email),
           cell_phone_number = COALESCE(cell_phone_number, p_cell),
           street            = COALESCE(street, p_street),
           external_number   = COALESCE(external_number, p_external),
           internal_number   = COALESCE(internal_number, p_internal),
           neighborhood      = COALESCE(neighborhood, p_neighborhood),
           zip_code          = COALESCE(zip_code, p_zip),
           updated           = now()
     WHERE id = v_id;
    RETURN v_id;
  END IF;

  v_id := compat.next_person_id();
  INSERT INTO str.person (id, curp, ine, names, last_name, sur_name, birthdate,
                          email, cell_phone_number, street, external_number,
                          internal_number, neighborhood, zip_code,
                          legacy_source, legacy_id, created)
  VALUES (v_id, v_curp, v_ine, p_names, p_last_name, p_sur_name, p_birthdate::date,
          p_email, p_cell, p_street, p_external, p_internal, p_neighborhood,
          p_zip, p_legacy_source, p_legacy_id, now());

  RETURN v_id;
END;
$$ LANGUAGE plpgsql;

-- =====================================================================
-- 5. VISTA followers
--    Estructura e identidad salen del modelo nuevo; lo que sólo existía
--    aquí se pega desde followers_legacy. El orden de las columnas es el
--    de la tabla original, para que un SELECT * no cambie de forma.
-- =====================================================================
CREATE VIEW followers AS
SELECT r.id,
       l.birth_entity_id,
       r.birthdate,
       r.cell_phone_number,
       r.created,
       r.curp,
       r.deleted,
       r.email,
       r.ine,
       r.last_name,
       r.names,
       l.section_number,
       r.status,
       r.sur_name,
       l.updated,
       r.user_create_id,
       l.cat_family_role,
       l.cat_marital_status,
       l.cat_sex,
       l.cell_phone_number_other,
       l.committee_participation,
       l.device_id,
       r.external_number,
       r.internal_number,
       l.inviting_others_to_participate,
       l.is_adult,
       l.is_in_sync,
       l.latitude,
       l.longitude,
       r.neighborhood,
       l.number_of_people_invited,
       l.pollin_station_representative_participation,
       l.residence_entity_id,
       l.residence_municipality_id,
       l.section_number_ine,
       r.street,
       l.support_whatsapp,
       l.supports_movement,
       l.uuid,
       l.validity,
       r.zip_code,
       l.auth_id,
       r.promoter_auth_id,
       r.promoter_alt_auth_id,
       r.vote_flag,
       l.last_name_resp,
       l.sur_name_resp,
       r.vote_flag_timestamp,
       l.federal_district_id,
       l.federal_district_name,
       l.local_district_id,
       l.local_district_name,
       r.municipality_id,
       l.municipality_name,
       r.section_id
FROM str.followers r
LEFT JOIN followers_legacy l ON l.id = r.id;

COMMENT ON VIEW followers IS
  'Vista de compatibilidad (fase 3). Lee msvc_dash_structure; las columnas propias del modelo viejo vienen de followers_legacy. Escribe por triggers INSTEAD OF.';

-- =====================================================================
-- 6. VISTA promoters
-- =====================================================================
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
       l.neighborhood_id,
       l.notes,
       r.outdoor_number,
       r.postal_code,
       r.street,
       r.sur_name,
       l.updated,
       l.user_create_id,
       l.vencimiento,
       l.cat_committee,
       -- residual: `promoters.auth_id` es un duplicado a medio llenar de
       -- promoter_auth_id, no algo que el modelo nuevo tenga
       l.auth_id,
       l.follower_id,
       r.seccion_id,
       r.cordinator_zone_auth_id,
       r.referent_auth_id,
       r.promoter_auth_id,
       r.followers_goal,
       r.flag_alter
FROM str.promoters r
LEFT JOIN promoters_legacy l ON l.id = r.id;

COMMENT ON VIEW promoters IS
  'Vista de compatibilidad (fase 3). El promotor es un PUESTO ocupado por una PERSONA: rotar al titular ya no arrastra convencidos. Escribe por triggers INSTEAD OF.';

-- =====================================================================
-- 7. ESCRITURA — followers
-- =====================================================================
CREATE OR REPLACE FUNCTION compat.followers_insert() RETURNS TRIGGER AS $$
DECLARE
  v_id         bigint;
  v_person     bigint;
  v_position   bigint;
  v_structure  bigint;
  v_territory  bigint;
  v_assignment bigint;
  v_alternate  boolean;
  v_auth       varchar;
BEGIN
  v_id := COALESCE(NEW.id, nextval('followers_id_seq'));

  -- la rama se deduce igual que en el backfill: alterna es la que llega sin
  -- promoter_auth_id pero con el alterno
  v_alternate := (NEW.promoter_auth_id IS NULL AND NEW.promoter_alt_auth_id IS NOT NULL);
  v_auth      := COALESCE(NEW.promoter_auth_id, NEW.promoter_alt_auth_id);

  v_person    := compat.person_upsert(NEW.curp, NEW.ine, NEW.names,
                    NEW.last_name, NEW.sur_name, NEW.birthdate, NEW.email,
                    NEW.cell_phone_number, NEW.street, NEW.external_number,
                    NEW.internal_number, NEW.neighborhood, NEW.zip_code,
                    'followers', v_id);
  v_position  := compat.position_for_promoter(v_auth, v_alternate, NEW.section_id);
  v_territory := compat.territory_for_section(NEW.section_id);
  v_assignment := compat.assignment_for_auth(NEW.user_create_id);

  SELECT structure_id INTO v_structure FROM str.position WHERE id = v_position;

  INSERT INTO str.membership (id, person_id, structure_id, position_id,
                              captured_by_assignment_id, captured_by_auth_id,
                              territory_id,
                              source, status, validity, vote_flag,
                              vote_flag_timestamp, legacy_id)
  VALUES (compat.next_membership_id(),
          v_person, v_structure, v_position, v_assignment,
          -- el authId crudo se guarda siempre, tenga o no asignación detrás
          NEW.user_create_id, v_territory,
          CASE WHEN NEW.device_id IS NOT NULL THEN 'APP' ELSE 'WEB' END,
          COALESCE(NEW.status, 'ACTIVO'),
          tstzrange(COALESCE(NEW.created, now()), NULL),
          COALESCE(NEW.vote_flag, false), NEW.vote_flag_timestamp, v_id);

  -- las columnas que sólo existen en el modelo viejo siguen viviendo aquí
  INSERT INTO followers_legacy (
      id, birth_entity_id, section_number, updated, cat_family_role,
      cat_marital_status, cat_sex, cell_phone_number_other,
      committee_participation, device_id, inviting_others_to_participate,
      is_adult, is_in_sync, latitude, longitude, number_of_people_invited,
      pollin_station_representative_participation, residence_entity_id,
      residence_municipality_id, section_number_ine, support_whatsapp,
      supports_movement, uuid, validity, auth_id, last_name_resp,
      sur_name_resp, federal_district_id, federal_district_name,
      local_district_id, local_district_name, municipality_name)
  VALUES (
      v_id, NEW.birth_entity_id, NEW.section_number, now(), NEW.cat_family_role,
      NEW.cat_marital_status, NEW.cat_sex, NEW.cell_phone_number_other,
      NEW.committee_participation, NEW.device_id, NEW.inviting_others_to_participate,
      NEW.is_adult, NEW.is_in_sync, NEW.latitude, NEW.longitude, NEW.number_of_people_invited,
      NEW.pollin_station_representative_participation, NEW.residence_entity_id,
      NEW.residence_municipality_id, NEW.section_number_ine, NEW.support_whatsapp,
      NEW.supports_movement, NEW.uuid, NEW.validity, NEW.auth_id, NEW.last_name_resp,
      NEW.sur_name_resp, NEW.federal_district_id, NEW.federal_district_name,
      NEW.local_district_id, NEW.local_district_name, NEW.municipality_name);

  INSERT INTO str.change_log (id, occurred_at, type, entity, entity_id, after_data, reason)
  VALUES (compat.next_change_log_id(), now(), 'CREATE', 'membership', v_id,
          jsonb_build_object('person_id', v_person, 'position_id', v_position,
                             'legacy_id', v_id),
          'alta por vista de compatibilidad');

  NEW.id := v_id;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION compat.followers_update() RETURNS TRIGGER AS $$
DECLARE
  v_person    bigint;
  v_position  bigint;
  v_structure bigint;
  v_alternate boolean;
  v_auth      varchar;
BEGIN
  SELECT person_id INTO v_person FROM str.membership WHERE legacy_id = OLD.id;
  IF v_person IS NULL THEN
    RAISE EXCEPTION 'No hay membresía vigente para el convencido %', OLD.id;
  END IF;

  UPDATE str.person
     SET curp              = COALESCE(nullif(upper(trim(NEW.curp)), ''), curp),
         ine               = COALESCE(nullif(upper(trim(NEW.ine)), ''), ine),
         names             = NEW.names,
         last_name         = NEW.last_name,
         sur_name          = NEW.sur_name,
         birthdate         = NEW.birthdate::date,
         email             = NEW.email,
         cell_phone_number = NEW.cell_phone_number,
         street            = NEW.street,
         external_number   = NEW.external_number,
         internal_number   = NEW.internal_number,
         neighborhood      = NEW.neighborhood,
         zip_code          = NEW.zip_code,
         updated           = now()
   WHERE id = v_person;

  -- traslado: cambió el promotor. Es una decisión explícita, así que mueve
  -- position_id pero NO toca captured_by_assignment_id — el linaje es
  -- inmutable y el trigger del modelo nuevo lo haría fallar igual.
  IF NEW.promoter_auth_id     IS DISTINCT FROM OLD.promoter_auth_id
  OR NEW.promoter_alt_auth_id IS DISTINCT FROM OLD.promoter_alt_auth_id THEN
    v_alternate := (NEW.promoter_auth_id IS NULL AND NEW.promoter_alt_auth_id IS NOT NULL);
    v_auth      := COALESCE(NEW.promoter_auth_id, NEW.promoter_alt_auth_id);
    v_position  := compat.position_for_promoter(v_auth, v_alternate, NEW.section_id);
    SELECT structure_id INTO v_structure FROM str.position WHERE id = v_position;

    UPDATE str.membership
       SET position_id = v_position, structure_id = v_structure
     WHERE legacy_id = OLD.id;

    INSERT INTO str.change_log (id, occurred_at, type, entity, entity_id, before_data, after_data, reason)
    VALUES (compat.next_change_log_id(), now(), 'TRANSFER', 'membership', OLD.id,
            jsonb_build_object('promoter_auth_id', OLD.promoter_auth_id,
                               'promoter_alt_auth_id', OLD.promoter_alt_auth_id),
            jsonb_build_object('promoter_auth_id', NEW.promoter_auth_id,
                               'promoter_alt_auth_id', NEW.promoter_alt_auth_id,
                               'position_id', v_position),
            'traslado por vista de compatibilidad');
  END IF;

  UPDATE str.membership
     SET status              = COALESCE(NEW.status, status),
         vote_flag           = COALESCE(NEW.vote_flag, false),
         vote_flag_timestamp = NEW.vote_flag_timestamp,
         territory_id        = COALESCE(compat.territory_for_section(NEW.section_id), territory_id)
   WHERE legacy_id = OLD.id;

  -- el modelo viejo daba de baja poniendo `deleted`; aquí se cierra la
  -- vigencia, que hace desaparecer la fila de la vista sin perder historia
  IF NEW.deleted IS NOT NULL AND OLD.deleted IS NULL THEN
    UPDATE str.membership
       SET validity = compat.close_validity(validity, NEW.deleted)
     WHERE legacy_id = OLD.id;
    INSERT INTO str.change_log (id, occurred_at, type, entity, entity_id, reason)
    VALUES (compat.next_change_log_id(), now(), 'TERMINATE', 'membership', OLD.id,
            'baja por vista de compatibilidad');
  END IF;

  UPDATE followers_legacy
     SET birth_entity_id = NEW.birth_entity_id,
         section_number  = NEW.section_number,
         updated         = now(),
         cat_family_role = NEW.cat_family_role,
         cat_marital_status = NEW.cat_marital_status,
         cat_sex         = NEW.cat_sex,
         cell_phone_number_other = NEW.cell_phone_number_other,
         committee_participation = NEW.committee_participation,
         device_id       = NEW.device_id,
         inviting_others_to_participate = NEW.inviting_others_to_participate,
         is_adult        = NEW.is_adult,
         is_in_sync      = NEW.is_in_sync,
         latitude        = NEW.latitude,
         longitude       = NEW.longitude,
         number_of_people_invited = NEW.number_of_people_invited,
         pollin_station_representative_participation = NEW.pollin_station_representative_participation,
         residence_entity_id = NEW.residence_entity_id,
         residence_municipality_id = NEW.residence_municipality_id,
         section_number_ine = NEW.section_number_ine,
         support_whatsapp = NEW.support_whatsapp,
         supports_movement = NEW.supports_movement,
         uuid            = NEW.uuid,
         validity        = NEW.validity,
         auth_id         = NEW.auth_id,
         last_name_resp  = NEW.last_name_resp,
         sur_name_resp   = NEW.sur_name_resp,
         federal_district_id = NEW.federal_district_id,
         federal_district_name = NEW.federal_district_name,
         local_district_id = NEW.local_district_id,
         local_district_name = NEW.local_district_name,
         municipality_name = NEW.municipality_name
   WHERE id = OLD.id;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- El servicio viejo borra físicamente (`deleteById`), y de hecho las
-- 1,067,949 filas tienen `deleted` nulo. Aquí se cierra la vigencia: la
-- fila desaparece de la vista igual que antes, pero queda consultable
-- «quién estaba y cuándo», que es el punto del modelo nuevo.
CREATE OR REPLACE FUNCTION compat.followers_delete() RETURNS TRIGGER AS $$
BEGIN
  UPDATE str.membership
     SET validity = compat.close_validity(validity)
   WHERE legacy_id = OLD.id AND upper(validity) IS NULL;

  INSERT INTO str.change_log (id, occurred_at, type, entity, entity_id, before_data, reason)
  VALUES (compat.next_change_log_id(), now(), 'TERMINATE', 'membership', OLD.id,
          jsonb_build_object('curp', OLD.curp, 'promoter_auth_id', OLD.promoter_auth_id),
          'borrado por vista de compatibilidad');

  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER followers_insert_trg INSTEAD OF INSERT ON followers
  FOR EACH ROW EXECUTE FUNCTION compat.followers_insert();
CREATE TRIGGER followers_update_trg INSTEAD OF UPDATE ON followers
  FOR EACH ROW EXECUTE FUNCTION compat.followers_update();
CREATE TRIGGER followers_delete_trg INSTEAD OF DELETE ON followers
  FOR EACH ROW EXECUTE FUNCTION compat.followers_delete();

-- =====================================================================
-- 8. ESCRITURA — promoters
--    Dar de alta un promotor es crear un PUESTO y asignarle una PERSONA.
--    Si el coordinador de zona todavía no tiene puesto sobre esa sección
--    se crea, igual que habría hecho el backfill.
-- =====================================================================
CREATE OR REPLACE FUNCTION compat.position_for_coordinator(
    p_cz_auth varchar, p_ref_auth varchar, p_section bigint,
    p_municipality bigint, p_alternate boolean)
RETURNS bigint AS $$
DECLARE
  v_structure bigint;
  v_sec_terr  bigint;
  v_mun_terr  bigint;
  v_ref       bigint;
  v_cz        bigint;
  v_person    bigint;
BEGIN
  SELECT id INTO v_structure FROM str.structure
   WHERE type = CASE WHEN p_alternate THEN 'ALTERNATE' ELSE 'MAIN' END
   ORDER BY id LIMIT 1;

  v_sec_terr := compat.territory_for_section(p_section);
  IF v_sec_terr IS NULL THEN
    RAISE EXCEPTION 'La sección % no existe en el catálogo de territorios', p_section;
  END IF;
  SELECT parent_territory_id INTO v_mun_terr FROM str.territory WHERE id = v_sec_terr;
  -- si la sección quedó colgando sin municipio, se usa el que trae el alta
  v_mun_terr := COALESCE(v_mun_terr, compat.territory_for_municipality(p_municipality));

  -- ¿ya hay puesto de coordinador para esa pareja?
  --
  -- Se acepta el puesto sobre la SECCIÓN o sobre su MUNICIPIO. El segundo
  -- caso es el nombramiento de la rama alterna, que es municipal por
  -- naturaleza (ver V2d): el coordinador cubre el municipio completo y sus
  -- promotores cuelgan de ese único puesto, cada uno en su sección. Sin
  -- esto, un alta en la rama alterna crearía un puesto de sección
  -- paralelo al municipal y el mismo coordinador acabaría con dos.
  -- El de sección gana cuando existen los dos: es el más específico.
  SELECT p.id INTO v_cz
    FROM str.position_holder h
    JOIN str.position p ON p.id = h.position_id
   WHERE h.auth_id = p_cz_auth
     AND p.level = 'ZONE_COORDINATOR'
     AND p.territory_id IN (v_sec_terr, v_mun_terr)
     AND p.structure_id = v_structure
     AND p.valid_to IS NULL
   ORDER BY (p.territory_id = v_sec_terr) DESC
   LIMIT 1;
  IF v_cz IS NOT NULL THEN RETURN v_cz; END IF;

  -- el referente sobre el municipio
  SELECT p.id INTO v_ref
    FROM str.position_holder h
    JOIN str.position p ON p.id = h.position_id
   WHERE h.auth_id = p_ref_auth
     AND p.level = 'REFERENT'
     AND p.territory_id = v_mun_terr
     AND p.structure_id = v_structure
     AND p.valid_to IS NULL
   LIMIT 1;

  IF v_ref IS NULL AND p_ref_auth IS NOT NULL THEN
    v_ref := compat.next_position_id();
    INSERT INTO str.position (id, structure_id, level, territory_id, code, valid_from)
    VALUES (v_ref, v_structure, 'REFERENT', v_mun_terr,
            'REF|' || (SELECT code FROM str.territory WHERE id = v_mun_terr) || '|' || p_ref_auth,
            now());

    SELECT id INTO v_person FROM str.person WHERE auth_id = p_ref_auth LIMIT 1;
    IF v_person IS NOT NULL THEN
      INSERT INTO str.assignment (id, created, position_id, person_id, role,
                                  auth_id, validity, start_reason)
      VALUES (compat.next_assignment_id(), now(), v_ref, v_person, 'HOLDER',
              p_ref_auth, tstzrange(now(), NULL), 'alta por vista de compatibilidad');
    END IF;
  END IF;

  v_cz := compat.next_position_id();
  INSERT INTO str.position (id, structure_id, level, parent_position_id, territory_id, code, valid_from)
  VALUES (v_cz, v_structure, 'ZONE_COORDINATOR', v_ref, v_sec_terr,
          'CZ|' || (SELECT code FROM str.territory WHERE id = v_sec_terr) || '|' || p_cz_auth,
          now());

  SELECT id INTO v_person FROM str.person WHERE auth_id = p_cz_auth LIMIT 1;
  IF v_person IS NOT NULL THEN
    INSERT INTO str.assignment (id, created, position_id, person_id, role,
                                auth_id, validity, start_reason)
    VALUES (compat.next_assignment_id(), now(), v_cz, v_person, 'HOLDER',
            p_cz_auth, tstzrange(now(), NULL), 'alta por vista de compatibilidad');
  END IF;

  RETURN v_cz;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION compat.promoters_insert() RETURNS TRIGGER AS $$
DECLARE
  v_id        bigint;
  v_person    bigint;
  v_structure bigint;
  v_cz        bigint;
  v_position  bigint;
  v_sec_terr  bigint;
  v_alternate boolean := COALESCE(NEW.flag_alter, false);
BEGIN
  v_id := COALESCE(NEW.id, nextval('promoters_id_seq'));

  v_person := compat.person_upsert(NEW.curp, NEW.ine, NEW.names,
                NEW.last_name, NEW.sur_name, NULL, NEW.email,
                NEW.cell_phone_number, NEW.street, NEW.outdoor_number,
                NEW.interior_number, NULL, NEW.postal_code, 'promoters', v_id);

  -- `person.auth_id` es unico, asi que sólo se rellena si está libre; la
  -- identidad que de verdad manda es la de la asignación, más abajo
  IF NEW.promoter_auth_id IS NOT NULL THEN
    UPDATE str.person SET auth_id = NEW.promoter_auth_id
     WHERE id = v_person
       AND auth_id IS NULL
       AND NOT EXISTS (SELECT 1 FROM str.person o WHERE o.auth_id = NEW.promoter_auth_id);
  END IF;

  v_cz := compat.position_for_coordinator(NEW.cordinator_zone_auth_id,
            NEW.referent_auth_id, NEW.seccion_id, NEW.municipality_id, v_alternate);
  v_sec_terr := compat.territory_for_section(NEW.seccion_id);
  SELECT structure_id INTO v_structure FROM str.position WHERE id = v_cz;

  v_position := compat.next_position_id();
  INSERT INTO str.position (id, structure_id, level, parent_position_id, territory_id,
                            code, goal, valid_from, legacy_id)
  VALUES (v_position, v_structure, 'PROMOTER', v_cz, v_sec_terr,
          'PROM|' || v_id, NEW.followers_goal, COALESCE(NEW.created, now()), v_id);

  -- el authId va en la ASIGNACION: es la cuenta con la que se ocupa el
  -- puesto, no un atributo de la persona (que puede tener varias)
  INSERT INTO str.assignment (id, created, position_id, person_id, role,
                              auth_id, validity, start_reason)
  VALUES (compat.next_assignment_id(), now(), v_position, v_person, 'HOLDER',
          NEW.promoter_auth_id, tstzrange(COALESCE(NEW.created, now()), NULL),
          'alta por vista de compatibilidad');

  INSERT INTO promoters_legacy (id, neighborhood_id, notes, updated,
                                user_create_id, vencimiento, cat_committee,
                                follower_id, auth_id)
  VALUES (v_id, NEW.neighborhood_id, NEW.notes, now(),
          NEW.user_create_id, NEW.vencimiento, NEW.cat_committee,
          NEW.follower_id, NEW.auth_id);

  INSERT INTO str.change_log (id, occurred_at, type, entity, entity_id, after_data, reason)
  VALUES (compat.next_change_log_id(), now(), 'CREATE', 'position', v_position,
          jsonb_build_object('legacy_id', v_id, 'person_id', v_person,
                             'parent_position_id', v_cz),
          'alta de promotor por vista de compatibilidad');

  NEW.id := v_id;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION compat.promoters_update() RETURNS TRIGGER AS $$
DECLARE
  v_position bigint;
  v_person   bigint;
  v_cz       bigint;
  v_sec_terr bigint;
BEGIN
  SELECT id INTO v_position FROM str.position WHERE legacy_id = OLD.id;
  IF v_position IS NULL THEN
    RAISE EXCEPTION 'No hay puesto para el promotor %', OLD.id;
  END IF;
  SELECT person_id INTO v_person FROM str.position_holder WHERE position_id = v_position;

  IF v_person IS NOT NULL THEN
    UPDATE str.person
       SET curp              = COALESCE(nullif(upper(trim(NEW.curp)), ''), curp),
           ine               = COALESCE(nullif(upper(trim(NEW.ine)), ''), ine),
           names             = NEW.names,
           last_name         = NEW.last_name,
           sur_name          = NEW.sur_name,
           email             = NEW.email,
           cell_phone_number = NEW.cell_phone_number,
           street            = NEW.street,
           external_number   = NEW.outdoor_number,
           internal_number   = NEW.interior_number,
           zip_code          = NEW.postal_code,
           updated           = now()
     WHERE id = v_person;
  END IF;

  -- reorganización: cambió la sección o el coordinador de zona. Mover el
  -- puesto NO toca las membresías que cuelgan de él: ése es el punto.
  IF NEW.seccion_id              IS DISTINCT FROM OLD.seccion_id
  OR NEW.cordinator_zone_auth_id IS DISTINCT FROM OLD.cordinator_zone_auth_id THEN
    v_cz := compat.position_for_coordinator(NEW.cordinator_zone_auth_id,
              NEW.referent_auth_id, NEW.seccion_id, NEW.municipality_id,
              COALESCE(NEW.flag_alter, false));
    v_sec_terr := compat.territory_for_section(NEW.seccion_id);

    UPDATE str.position
       SET parent_position_id = v_cz, territory_id = v_sec_terr
     WHERE id = v_position;

    INSERT INTO str.change_log (id, occurred_at, type, entity, entity_id, before_data, after_data, reason)
    VALUES (compat.next_change_log_id(), now(), 'REORG_POSITION', 'position', v_position,
            jsonb_build_object('seccion_id', OLD.seccion_id,
                               'cordinator_zone_auth_id', OLD.cordinator_zone_auth_id),
            jsonb_build_object('seccion_id', NEW.seccion_id,
                               'cordinator_zone_auth_id', NEW.cordinator_zone_auth_id),
            'reorganización por vista de compatibilidad');
  END IF;

  UPDATE str.position SET goal = NEW.followers_goal WHERE id = v_position;

  IF NEW.deleted IS NOT NULL AND OLD.deleted IS NULL THEN
    UPDATE str.position SET valid_to = NEW.deleted WHERE id = v_position;
    UPDATE str.assignment
       SET validity = compat.close_validity(validity, NEW.deleted),
           end_reason = 'baja por vista de compatibilidad'
     WHERE position_id = v_position AND upper(validity) IS NULL;
  END IF;

  UPDATE promoters_legacy
     SET neighborhood_id = NEW.neighborhood_id,
         notes           = NEW.notes,
         updated         = now(),
         user_create_id  = NEW.user_create_id,
         vencimiento     = NEW.vencimiento,
         cat_committee   = NEW.cat_committee,
         follower_id     = NEW.follower_id,
         auth_id         = NEW.auth_id
   WHERE id = OLD.id;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- La baja del promotor cierra el puesto y su asignación. Las membresías se
-- quedan donde están, apuntando a un puesto cerrado: visibles y contables,
-- en vez de huérfanas. Reubicarlas es una operación aparte y deliberada.
CREATE OR REPLACE FUNCTION compat.promoters_delete() RETURNS TRIGGER AS $$
DECLARE v_position bigint;
BEGIN
  SELECT id INTO v_position FROM str.position WHERE legacy_id = OLD.id;
  IF v_position IS NULL THEN RETURN OLD; END IF;

  UPDATE str.assignment
     SET validity = compat.close_validity(validity),
         end_reason = 'borrado por vista de compatibilidad'
   WHERE position_id = v_position AND upper(validity) IS NULL;

  UPDATE str.position SET valid_to = now() WHERE id = v_position;

  INSERT INTO str.change_log (id, occurred_at, type, entity, entity_id, before_data, reason)
  VALUES (compat.next_change_log_id(), now(), 'TERMINATE', 'position', v_position,
          jsonb_build_object('legacy_id', OLD.id, 'curp', OLD.curp),
          'borrado de promotor por vista de compatibilidad');

  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER promoters_insert_trg INSTEAD OF INSERT ON promoters
  FOR EACH ROW EXECUTE FUNCTION compat.promoters_insert();
CREATE TRIGGER promoters_update_trg INSTEAD OF UPDATE ON promoters
  FOR EACH ROW EXECUTE FUNCTION compat.promoters_update();
CREATE TRIGGER promoters_delete_trg INSTEAD OF DELETE ON promoters
  FOR EACH ROW EXECUTE FUNCTION compat.promoters_delete();

COMMIT;

-- ---------------------------------------------------------------------
-- 9. el corte quedó hecho; comprobación de lectura
-- ---------------------------------------------------------------------
\echo '--- conteos por la vista ---'
SELECT (SELECT count(*) FROM followers) AS followers,
       (SELECT count(*) FROM promoters) AS promoters;
