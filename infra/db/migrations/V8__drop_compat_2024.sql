-- Electorama: se quita la capa compat del flujo 2024 que el flujo nuevo ya no usa.
--
-- La captura de convencidos y los puestos viven en msvc-structure; msvc-users ya no
-- expone users_seccions (la app usa /positions/scope) y msvc-followers sólo conserva lo
-- del día de la elección: la marca de voto (UPDATE sobre followers.followers) y las
-- lecturas de followers / promoters / follower_mark.
--
-- Se queda:
--   · followers.followers y followers.promoters (vistas de lectura) con sus fuentes
--     structure_compat.followers / promoters / position_holder / holder_auth;
--   · followers_compat.followers_update y lo que llama (position_for_promoter,
--     territory_for_section, close_validity, next_change_log_id) y
--     structure.active_structure (V7);
--   · las tablas propias de followers (follower_mark, cat_*, preregister, followers_sirena…),
--     que tienen datos aunque su API se haya quitado.
--
-- Sin CASCADE salvo en users_compat: si algo inesperado depende de lo que se borra, falla.

-- 1. lo muerto: vistas y tablas que nadie lee
DROP VIEW followers.followers_compat;
DROP VIEW followers.followers_compat_mat;
DROP VIEW followers.nw_hierarchy_v;
DROP TABLE followers.nw_hierarchy;
DROP TABLE followers.users_seccions;
DROP TABLE users.promoters;

-- 2. escrituras de promotores (escribían en promoters_legacy, que ya no existe)
DROP TRIGGER promoters_insert_trg ON followers.promoters;
DROP TRIGGER promoters_update_trg ON followers.promoters;
DROP TRIGGER promoters_delete_trg ON followers.promoters;
DROP FUNCTION followers_compat.promoters_insert();
DROP FUNCTION followers_compat.promoters_update();
DROP FUNCTION followers_compat.promoters_delete();
DROP SEQUENCE followers.promoters_id_seq;

-- 3. altas y bajas de seguidores: la captura es POST /memberships de msvc-structure
DROP TRIGGER followers_insert_trg ON followers.followers;
DROP TRIGGER followers_delete_trg ON followers.followers;
DROP FUNCTION followers_compat.followers_insert();
DROP FUNCTION followers_compat.followers_delete();

-- la edición (marca de voto) se queda, sin escribir ya en followers_legacy
CREATE OR REPLACE FUNCTION followers_compat.followers_update() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_person    bigint;
  v_position  bigint;
  v_structure bigint;
  v_alternate boolean;
  v_auth      varchar;
BEGIN
  SELECT person_id INTO v_person FROM structure."membership" WHERE legacy_id = OLD.id;
  IF v_person IS NULL THEN
    RAISE EXCEPTION 'No hay membresía vigente para el convencido %', OLD.id;
  END IF;

  UPDATE structure."person"
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
    v_position  := followers_compat.position_for_promoter(v_auth, v_alternate, NEW.section_id);
    SELECT structure_id INTO v_structure FROM structure."position" WHERE id = v_position;

    UPDATE structure."membership"
       SET position_id = v_position, structure_id = v_structure
     WHERE legacy_id = OLD.id;

    INSERT INTO structure."change_log" (id, occurred_at, type, entity, entity_id, before_data, after_data, reason)
    VALUES (followers_compat.next_change_log_id(), now(), 'TRANSFER', 'membership', OLD.id,
            jsonb_build_object('promoter_auth_id', OLD.promoter_auth_id,
                               'promoter_alt_auth_id', OLD.promoter_alt_auth_id),
            jsonb_build_object('promoter_auth_id', NEW.promoter_auth_id,
                               'promoter_alt_auth_id', NEW.promoter_alt_auth_id,
                               'position_id', v_position),
            'traslado por vista de compatibilidad');
  END IF;

  UPDATE structure."membership"
     SET status              = COALESCE(NEW.status, status),
         vote_flag           = COALESCE(NEW.vote_flag, false),
         vote_flag_timestamp = NEW.vote_flag_timestamp,
         territory_id        = COALESCE(followers_compat.territory_for_section(NEW.section_id), territory_id)
   WHERE legacy_id = OLD.id;

  -- el modelo viejo daba de baja poniendo `deleted`; aquí se cierra la
  -- vigencia, que hace desaparecer la fila de la vista sin perder historia
  IF NEW.deleted IS NOT NULL AND OLD.deleted IS NULL THEN
    UPDATE structure."membership"
       SET validity = followers_compat.close_validity(validity, NEW.deleted)
     WHERE legacy_id = OLD.id;
    INSERT INTO structure."change_log" (id, occurred_at, type, entity, entity_id, reason)
    VALUES (followers_compat.next_change_log_id(), now(), 'TERMINATE', 'membership', OLD.id,
            'baja por vista de compatibilidad');
  END IF;

  RETURN NEW;
END;
$$;

DROP TABLE followers.followers_legacy;
DROP SEQUENCE followers.followers_id_seq;

-- helpers que sólo usaban las altas
DROP FUNCTION followers_compat.position_for_coordinator(character varying, character varying, bigint, bigint, boolean);
DROP FUNCTION followers_compat.territory_for_municipality(bigint);
DROP FUNCTION followers_compat.assignment_for_auth(character varying);
DROP FUNCTION followers_compat.person_upsert(character varying, character varying, character varying,
  character varying, character varying, timestamp without time zone, character varying, character varying,
  character varying, character varying, character varying, character varying, character varying,
  character varying, bigint);
DROP FUNCTION followers_compat.next_membership_id();
DROP FUNCTION followers_compat.next_person_id();
DROP FUNCTION followers_compat.next_position_id();
DROP FUNCTION followers_compat.next_assignment_id();

-- 4. users_seccions: la app y el front leen /positions/scope de msvc-structure
DROP VIEW users.users_seccions;
DROP SCHEMA users_compat CASCADE;  -- sólo sus funciones; los triggers se fueron con la vista
DROP VIEW structure_compat.user_sections;
DROP FUNCTION structure_compat.user_section_add(character varying, integer, integer, boolean, integer, integer);
DROP FUNCTION structure_compat.user_section_close(bigint);
DROP FUNCTION structure_compat.user_section_override(bigint);
DROP FUNCTION structure_compat.user_section_update(bigint, integer, integer);
DROP FUNCTION structure_compat.materialize_municipal_position(bigint, integer);
DROP FUNCTION structure_compat.us_id(bigint, integer);
DROP FUNCTION structure_compat.us_position(bigint);
DROP FUNCTION structure_compat.us_section(bigint);

-- secuencias-vista que ya nadie llama (next_change_log_id sigue usando seq_change_log)
DROP VIEW structure_compat.seq_assignment;
DROP VIEW structure_compat.seq_membership;
DROP VIEW structure_compat.seq_person;
DROP VIEW structure_compat.seq_position;

-- 5. helpers de V7 que sólo servían a las altas
DROP FUNCTION structure.require_active_structure(bigint, boolean);
DROP FUNCTION structure.compat_parent(bigint, text, bigint);
