-- Electorama: baseline schema (one schema per service).
-- Generated from the legacy msvc_dash_* databases; foreign tables replaced by
-- direct cross-schema references. Extensions are created in V0__extensions.sql.

SET LOCAL check_function_bodies = false;

CREATE SCHEMA IF NOT EXISTS geography;
CREATE SCHEMA IF NOT EXISTS geography_staging;
CREATE SCHEMA IF NOT EXISTS structure;
CREATE SCHEMA IF NOT EXISTS structure_compat;
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS users_compat;
CREATE SCHEMA IF NOT EXISTS followers;
CREATE SCHEMA IF NOT EXISTS followers_compat;
CREATE SCHEMA IF NOT EXISTS calendar;
CREATE SCHEMA IF NOT EXISTS canvas_and_walls;
CREATE SCHEMA IF NOT EXISTS citizen_issues;
CREATE SCHEMA IF NOT EXISTS document_management;
CREATE SCHEMA IF NOT EXISTS elections;
CREATE SCHEMA IF NOT EXISTS elections_results;
CREATE SCHEMA IF NOT EXISTS elections_structure;
CREATE SCHEMA IF NOT EXISTS electoral_history;
CREATE SCHEMA IF NOT EXISTS logfile;
CREATE SCHEMA IF NOT EXISTS polls;
CREATE SCHEMA IF NOT EXISTS survey;
CREATE SCHEMA IF NOT EXISTS visits;


-- ===== types =====




-- ===== functions =====

-- geography: FUNCTION norm(text)
CREATE FUNCTION geography_staging.norm(t text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $$ SELECT regexp_replace(upper(unaccent(coalesce(t, ''))), '[^A-Z0-9]', '', 'g') $$;

-- structure: FUNCTION holder_auth(bigint)
CREATE FUNCTION structure_compat.holder_auth(p_position bigint) RETURNS character varying
    LANGUAGE sql STABLE
    AS $$
  SELECT COALESCE(a.auth_id, p.auth_id)
  FROM structure.assignment a
  JOIN structure.person p ON p.id = a.person_id
  WHERE a.position_id = p_position
    AND a.role = 'HOLDER'
    AND upper(a.validity) IS NULL
  LIMIT 1;
$$;

-- structure: FUNCTION materialize_municipal_position(bigint, integer)
CREATE FUNCTION structure_compat.materialize_municipal_position(p_position bigint, p_section integer) RETURNS bigint
    LANGUAGE plpgsql
    SET search_path TO 'structure_compat', 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_mun_terr   bigint;
  v_structure  bigint;
  v_parent     bigint;
  v_auth       varchar(100);
  v_person     bigint;
  v_from       timestamptz;
  v_target     bigint;
  v_closed_at  timestamptz := clock_timestamp();
BEGIN
  SELECT p.territory_id, p.structure_id, p.parent_position_id, p.valid_from
    INTO v_mun_terr, v_structure, v_parent, v_from
    FROM position p
   WHERE p.id = p_position AND p.level = 'ZONE_COORDINATOR' AND p.valid_to IS NULL;

  IF v_mun_terr IS NULL THEN
    RAISE EXCEPTION 'el puesto % no es un coordinador vigente', p_position;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM territory t
                  WHERE t.id = v_mun_terr AND t.type = 'MUNICIPALITY') THEN
    -- ya está a nivel sección: no hay nada que materializar
    RETURN p_position;
  END IF;

  SELECT a.auth_id, a.person_id INTO v_auth, v_person
    FROM assignment a
   WHERE a.position_id = p_position AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
   LIMIT 1;

  -- un puesto por cada sección del municipio, con su titular
  WITH nuevas AS (
    INSERT INTO position (structure_id, level, parent_position_id, territory_id,
                          code, valid_from)
    SELECT v_structure, 'ZONE_COORDINATOR', v_parent, sec.id,
           'CZ|' || sec.code || '|' || v_auth, v_from
    FROM territory sec
    WHERE sec.parent_territory_id = v_mun_terr
      AND sec.type = 'SECTION'
      AND sec.valid_to IS NULL
      AND NOT EXISTS (SELECT 1 FROM position x
                       WHERE x.structure_id = v_structure
                         AND x.level = 'ZONE_COORDINATOR'
                         AND x.territory_id = sec.id
                         AND x.valid_to IS NULL
                         AND x.code = 'CZ|' || sec.code || '|' || v_auth)
    RETURNING id, territory_id)
  INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
  SELECT n.id, v_person, 'HOLDER', v_auth, tstzrange(v_from, NULL),
         'materialización del nombramiento municipal'
  FROM nuevas n
  WHERE v_person IS NOT NULL;

  -- los promotores se recuelgan en el puesto de SU sección
  UPDATE position pr
     SET parent_position_id = nueva.id
    FROM position nueva
    JOIN territory nt ON nt.id = nueva.territory_id AND nt.type = 'SECTION'
   WHERE pr.parent_position_id = p_position
     AND pr.level = 'PROMOTER'
     AND nueva.structure_id = v_structure
     AND nueva.level = 'ZONE_COORDINATOR'
     AND nueva.valid_to IS NULL
     AND nueva.code = 'CZ|' || nt.code || '|' || v_auth
     AND nueva.territory_id = pr.territory_id;

  -- el municipal se cierra: `clock_timestamp()` y no `now()`, o el rango
  -- saldría vacío si el puesto se creó en esta misma transacción
  UPDATE assignment a
     SET validity = tstzrange(lower(a.validity),
                              GREATEST(v_closed_at, lower(a.validity) + interval '1 microsecond')),
         end_reason = 'materialización del nombramiento municipal'
   WHERE a.position_id = p_position AND upper(a.validity) IS NULL;

  UPDATE position p
     SET valid_to = GREATEST(v_closed_at, p.valid_from + interval '1 microsecond')
   WHERE p.id = p_position;

  SELECT p.id INTO v_target
    FROM position p
    JOIN territory t ON t.id = p.territory_id
   WHERE p.structure_id = v_structure
     AND p.level = 'ZONE_COORDINATOR'
     AND p.valid_to IS NULL
     AND t.section_geo_id = p_section
   ORDER BY p.id DESC
   LIMIT 1;

  RETURN v_target;
END;
$$;

-- structure: FUNCTION us_id(bigint, integer)
CREATE FUNCTION structure_compat.us_id(p_position bigint, p_section integer) RETURNS bigint
    LANGUAGE sql IMMUTABLE
    AS $$
  SELECT (p_position << 20) | p_section::bigint;
$$;

-- structure: FUNCTION us_position(bigint)
CREATE FUNCTION structure_compat.us_position(p_id bigint) RETURNS bigint
    LANGUAGE sql IMMUTABLE
    AS $$
  SELECT p_id >> 20;
$$;

-- structure: FUNCTION us_section(bigint)
CREATE FUNCTION structure_compat.us_section(p_id bigint) RETURNS integer
    LANGUAGE sql IMMUTABLE
    AS $$
  SELECT (p_id & 1048575)::integer;
$$;

-- structure: FUNCTION user_section_add(character varying, integer, integer, boolean, integer, integer)
CREATE FUNCTION structure_compat.user_section_add(p_auth character varying, p_section integer, p_municipality integer, p_alternate boolean DEFAULT false, p_meta integer DEFAULT NULL::integer, p_limit integer DEFAULT NULL::integer) RETURNS bigint
    LANGUAGE plpgsql
    SET search_path TO 'structure_compat', 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_sec_terr  bigint;
  v_mun_terr  bigint;
  v_structure bigint;
  v_parent    bigint;
  v_person    bigint;
  v_pos       bigint;
  v_ref_auth  varchar(100);
BEGIN
  SELECT id INTO v_sec_terr FROM territory
   WHERE section_geo_id = p_section AND type = 'SECTION' AND valid_to IS NULL LIMIT 1;
  IF v_sec_terr IS NULL THEN
    RAISE EXCEPTION 'la sección % no existe en el catálogo de territorios', p_section;
  END IF;
  SELECT parent_territory_id INTO v_mun_terr FROM territory WHERE id = v_sec_terr;
  v_mun_terr := COALESCE(v_mun_terr,
                         (SELECT id FROM territory
                           WHERE municipality_geo_id = p_municipality
                             AND type = 'MUNICIPALITY' LIMIT 1));

  SELECT s.id INTO v_structure FROM structure s
   WHERE s.type = CASE WHEN p_alternate THEN 'ALTERNATE' ELSE 'MAIN' END
   ORDER BY s.id LIMIT 1;

  -- ¿ya está cubierta, por el puesto de sección o por el municipal?
  SELECT p.id INTO v_pos
    FROM assignment a
    JOIN position p ON p.id = a.position_id
   WHERE a.auth_id = p_auth
     AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
     AND p.level = 'ZONE_COORDINATOR'
     AND p.valid_to IS NULL
     AND p.territory_id IN (v_sec_terr, v_mun_terr)
   ORDER BY (p.territory_id = v_sec_terr) DESC
   LIMIT 1;

  IF v_pos IS NOT NULL THEN
    -- si viene meta y el puesto es el de la sección, se aplica
    IF p_meta IS NOT NULL AND EXISTS (SELECT 1 FROM position p
                                       WHERE p.id = v_pos AND p.territory_id = v_sec_terr) THEN
      UPDATE position SET goal = p_meta,
                          child_limit = COALESCE(p_limit, child_limit)
       WHERE id = v_pos;
    END IF;
    RETURN structure_compat.us_id(v_pos, p_section);
  END IF;

  -- El puesto de referente sobre el municipio DE ESA SECCIÓN, con el mismo
  -- referente del que ya cuelga el coordinador. `position_validate_hierarchy`
  -- exige que el territorio del hijo esté contenido en el del padre, así que
  -- no vale reutilizar el padre de otro municipio. Si no existe, se crea; si
  -- no se sabe quién es el referente, el puesto queda sin padre.
  SELECT rh.auth_id INTO v_ref_auth
    FROM assignment a
    JOIN position p ON p.id = a.position_id
    JOIN structure_compat.position_holder rh ON rh.position_id = p.parent_position_id
   WHERE a.auth_id = p_auth AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
     AND p.level = 'ZONE_COORDINATOR' AND p.structure_id = v_structure
   ORDER BY p.id DESC LIMIT 1;

  IF v_ref_auth IS NOT NULL THEN
    SELECT p.id INTO v_parent
      FROM position p
      JOIN structure_compat.position_holder h ON h.position_id = p.id
     WHERE p.level = 'REFERENT' AND p.structure_id = v_structure
       AND p.territory_id = v_mun_terr AND p.valid_to IS NULL
       AND h.auth_id = v_ref_auth
     LIMIT 1;

    IF v_parent IS NULL THEN
      INSERT INTO position (structure_id, level, territory_id, code, valid_from)
      VALUES (v_structure, 'REFERENT', v_mun_terr,
              'REF|' || (SELECT code FROM territory WHERE id = v_mun_terr) || '|' || v_ref_auth,
              now())
      RETURNING id INTO v_parent;

      INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
      SELECT v_parent, pe.id, 'HOLDER', v_ref_auth, tstzrange(now(), NULL),
             'alta por vista de compatibilidad (users_seccions)'
      FROM person pe WHERE pe.auth_id = v_ref_auth;
    END IF;
  END IF;

  INSERT INTO position (structure_id, level, parent_position_id, territory_id,
                        code, goal, child_limit, valid_from)
  VALUES (v_structure, 'ZONE_COORDINATOR', v_parent, v_sec_terr,
          'CZ|' || (SELECT code FROM territory WHERE id = v_sec_terr) || '|' || p_auth,
          p_meta, p_limit, now())
  RETURNING id INTO v_pos;

  SELECT id INTO v_person FROM person WHERE auth_id = p_auth LIMIT 1;
  IF v_person IS NULL THEN
    INSERT INTO person (auth_id, legacy_source, created)
    VALUES (p_auth, 'keycloak', now()) RETURNING id INTO v_person;
  END IF;

  INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
  VALUES (v_pos, v_person, 'HOLDER', p_auth, tstzrange(now(), NULL),
          'alta por vista de compatibilidad (users_seccions)');

  RETURN structure_compat.us_id(v_pos, p_section);
END;
$$;

-- structure: FUNCTION user_section_close(bigint)
CREATE FUNCTION structure_compat.user_section_close(p_id bigint) RETURNS void
    LANGUAGE plpgsql
    SET search_path TO 'structure_compat', 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_pos bigint := structure_compat.us_position(p_id);
  v_sec integer := structure_compat.us_section(p_id);
  v_at  timestamptz := clock_timestamp();
BEGIN
  IF EXISTS (SELECT 1 FROM position p JOIN territory t ON t.id = p.territory_id
              WHERE p.id = v_pos AND t.type = 'MUNICIPALITY') THEN
    v_pos := structure_compat.materialize_municipal_position(v_pos, v_sec);
  END IF;

  -- quitarle la sección a un coordinador que todavía tiene promotores ahí
  -- dejaría a esos promotores sin padre; el modelo viejo lo permitía porque
  -- no había FK, aquí se avisa.
  IF EXISTS (SELECT 1 FROM position ch
              WHERE ch.parent_position_id = v_pos AND ch.valid_to IS NULL) THEN
    RAISE EXCEPTION 'el puesto % todavía tiene promotores vigentes: reubícalos antes de quitar la sección', v_pos;
  END IF;

  UPDATE assignment a
     SET validity = tstzrange(lower(a.validity),
                              GREATEST(v_at, lower(a.validity) + interval '1 microsecond')),
         end_reason = 'baja por vista de compatibilidad (users_seccions)'
   WHERE a.position_id = v_pos AND upper(a.validity) IS NULL;

  UPDATE position p
     SET valid_to = GREATEST(v_at, p.valid_from + interval '1 microsecond')
   WHERE p.id = v_pos AND p.valid_to IS NULL;
END;
$$;

-- structure: FUNCTION user_section_override(bigint)
CREATE FUNCTION structure_compat.user_section_override(p_id bigint) RETURNS bigint
    LANGUAGE plpgsql
    SET search_path TO 'structure_compat', 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_pos       bigint := structure_compat.us_position(p_id);
  v_sec       integer := structure_compat.us_section(p_id);
  v_structure bigint;
  v_parent    bigint;
  v_from      timestamptz;
  v_auth      varchar(100);
  v_person    bigint;
  v_sec_terr  bigint;
  v_code      varchar(80);
  v_new       bigint;
BEGIN
  SELECT p.structure_id, p.parent_position_id, p.valid_from
    INTO v_structure, v_parent, v_from
    FROM position p
   WHERE p.id = v_pos AND p.level = 'ZONE_COORDINATOR' AND p.valid_to IS NULL;

  IF v_structure IS NULL THEN
    RAISE EXCEPTION 'el puesto % no es un coordinador vigente', v_pos;
  END IF;

  IF EXISTS (SELECT 1 FROM position p JOIN territory t ON t.id = p.territory_id
              WHERE p.id = v_pos AND t.type = 'SECTION') THEN
    RETURN v_pos;
  END IF;

  SELECT a.auth_id, a.person_id INTO v_auth, v_person
    FROM assignment a
   WHERE a.position_id = v_pos AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
   LIMIT 1;

  SELECT t.id, 'CZ|' || t.code || '|' || v_auth INTO v_sec_terr, v_code
    FROM territory t
   WHERE t.section_geo_id = v_sec AND t.type = 'SECTION' AND t.valid_to IS NULL
   LIMIT 1;

  IF v_sec_terr IS NULL THEN
    RAISE EXCEPTION 'la sección % no existe en el catálogo de territorios', v_sec;
  END IF;

  SELECT p.id INTO v_new FROM position p
   WHERE p.structure_id = v_structure AND p.level = 'ZONE_COORDINATOR'
     AND p.territory_id = v_sec_terr AND p.valid_to IS NULL AND p.code = v_code
   LIMIT 1;

  IF v_new IS NULL THEN
    INSERT INTO position (structure_id, level, parent_position_id, territory_id,
                          code, valid_from)
    VALUES (v_structure, 'ZONE_COORDINATOR', v_parent, v_sec_terr, v_code, v_from)
    RETURNING id INTO v_new;

    IF v_person IS NOT NULL THEN
      INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
      VALUES (v_new, v_person, 'HOLDER', v_auth, tstzrange(v_from, NULL),
              'meta propia sobre una sección del nombramiento municipal');
    END IF;

    UPDATE position pr SET parent_position_id = v_new
     WHERE pr.parent_position_id = v_pos
       AND pr.level = 'PROMOTER'
       AND pr.territory_id = v_sec_terr;
  END IF;

  RETURN v_new;
END;
$$;

-- structure: FUNCTION user_section_update(bigint, integer, integer)
CREATE FUNCTION structure_compat.user_section_update(p_id bigint, p_meta integer, p_limit integer) RETURNS void
    LANGUAGE plpgsql
    SET search_path TO 'structure_compat', 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_pos bigint := structure_compat.user_section_override(p_id);
BEGIN
  UPDATE position
     SET goal        = COALESCE(p_meta,  goal),
         child_limit = COALESCE(p_limit, child_limit)
   WHERE id = v_pos;
END;
$$;

-- structure: FUNCTION campaign_closed_guard()
CREATE FUNCTION structure.campaign_closed_guard() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_structure bigint;
  v_status    text;
BEGIN
  IF TG_TABLE_NAME = 'assignment' THEN
    SELECT p.structure_id INTO v_structure
      FROM position p WHERE p.id = coalesce(NEW.position_id, OLD.position_id);
  ELSIF TG_OP = 'DELETE' THEN
    v_structure := OLD.structure_id;
  ELSE
    v_structure := NEW.structure_id;
  END IF;

  SELECT c.status INTO v_status
    FROM structure s JOIN campaign c ON c.id = s.campaign_id
   WHERE s.id = v_structure;
  IF v_status = 'CLOSED' THEN
    RAISE EXCEPTION 'la campaña de la estructura % está cerrada: sólo se consulta (%.%)',
      v_structure, TG_TABLE_NAME, TG_OP
      USING ERRCODE = 'check_violation';
  END IF;

  -- al sacar algo de una estructura, la de origen tampoco puede estar cerrada
  -- (IF anidados: PL/pgSQL no corta el AND y OLD no existe en un INSERT)
  IF TG_OP = 'UPDATE' THEN
    IF TG_TABLE_NAME <> 'assignment' THEN
      IF OLD.structure_id <> NEW.structure_id THEN
        SELECT c.status INTO v_status
          FROM structure s JOIN campaign c ON c.id = s.campaign_id
         WHERE s.id = OLD.structure_id;
        IF v_status = 'CLOSED' THEN
          RAISE EXCEPTION 'la campaña de la estructura % está cerrada: sólo se consulta',
            OLD.structure_id USING ERRCODE = 'check_violation';
        END IF;
      END IF;
    END IF;
  END IF;

  RETURN coalesce(NEW, OLD);
END;
$$;

-- structure: FUNCTION membership_protect_lineage()
CREATE FUNCTION structure.membership_protect_lineage() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
BEGIN
    IF OLD.captured_by_assignment_id IS NOT NULL
       AND NEW.captured_by_assignment_id IS DISTINCT FROM OLD.captured_by_assignment_id THEN
        RAISE EXCEPTION 'captured_by_assignment_id es inmutable (membership %): era %, se intentó poner %',
            OLD.id, OLD.captured_by_assignment_id, NEW.captured_by_assignment_id;
    END IF;
    RETURN NEW;
END;
$$;

-- structure: FUNCTION membership_same_structure()
CREATE FUNCTION structure.membership_same_structure() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_structure bigint;
BEGIN
  SELECT structure_id INTO v_structure FROM position WHERE id = NEW.position_id;
  IF v_structure IS DISTINCT FROM NEW.structure_id THEN
    RAISE EXCEPTION 'el convencido va en la estructura % pero su puesto % es de la %',
      NEW.structure_id, NEW.position_id, v_structure
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END;
$$;

-- structure: FUNCTION position_next_section_key(bigint, text, bigint, bigint)
CREATE FUNCTION structure.position_next_section_key(p_structure bigint, p_level text, p_parent bigint, p_territory bigint) RETURNS character varying
    LANGUAGE plpgsql
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $_$
DECLARE
  v_letter  text;
  v_section text;
  v_parent  text;
  v_next    int;
BEGIN
  IF p_level = 'ZONE_COORDINATOR' THEN
    SELECT CASE s.type WHEN 'MAIN' THEN 'A' WHEN 'ALTERNATE' THEN 'B' END
      INTO v_letter FROM structure s WHERE s.id = p_structure;
    SELECT t.code INTO v_section
      FROM territory t WHERE t.id = p_territory AND t.type = 'SECTION';
    IF v_letter IS NULL OR v_section IS NULL THEN
      RETURN NULL;
    END IF;
    -- dos altas a la vez en la misma sección no deben sacar el mismo número
    PERFORM pg_advisory_xact_lock(hashtext('section_key'), hashtext(p_structure || ':' || p_territory));
    SELECT coalesce(max(substring(p.section_key FROM '[AB]([0-9]+)$')::int), 0) + 1
      INTO v_next
      FROM position p
     WHERE p.structure_id = p_structure
       AND p.level = 'ZONE_COORDINATOR'
       AND p.section_key LIKE v_section || v_letter || '%';
    RETURN v_section || v_letter || v_next;

  ELSIF p_level = 'PROMOTER' THEN
    SELECT p.section_key INTO v_parent FROM position p WHERE p.id = p_parent;
    IF v_parent IS NULL THEN
      RETURN NULL;
    END IF;
    PERFORM pg_advisory_xact_lock(hashtext('section_key'), hashtext('parent:' || p_parent));
    SELECT coalesce(max(substring(c.section_key FROM '-([0-9]+)$')::int), 0) + 1
      INTO v_next
      FROM position c
     WHERE c.parent_position_id = p_parent
       AND c.level = 'PROMOTER'
       AND c.section_key IS NOT NULL;
    RETURN v_parent || '-' || lpad(v_next::text, 2, '0');
  END IF;

  RETURN NULL;
END;
$_$;

-- structure: FUNCTION position_section_key()
CREATE FUNCTION structure.position_section_key() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    IF NEW.section_key IS NULL THEN
      NEW.section_key := position_next_section_key(
          NEW.structure_id, NEW.level, NEW.parent_position_id, NEW.territory_id);
    END IF;
  ELSIF OLD.section_key IS NOT NULL AND NEW.section_key IS DISTINCT FROM OLD.section_key THEN
    RAISE EXCEPTION 'la clave % del puesto % no se cambia', OLD.section_key, OLD.id
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END;
$$;

-- structure: FUNCTION position_validate_hierarchy()
CREATE FUNCTION structure.position_validate_hierarchy() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
    parent          RECORD;
    child_rank      INT;
    parent_rank     INT;
BEGIN
    IF NEW.parent_position_id IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT * INTO parent FROM position WHERE id = NEW.parent_position_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'El puesto padre % no existe', NEW.parent_position_id;
    END IF;

    -- 1. el padre debe ser de un nivel superior
    child_rank  := CASE NEW.level   WHEN 'REFERENT' THEN 1 WHEN 'ZONE_COORDINATOR' THEN 2 ELSE 3 END;
    parent_rank := CASE parent.level WHEN 'REFERENT' THEN 1 WHEN 'ZONE_COORDINATOR' THEN 2 ELSE 3 END;
    IF parent_rank >= child_rank THEN
        RAISE EXCEPTION 'Un puesto % no puede colgar de un puesto % (el padre debe ser de nivel superior)',
            NEW.level, parent.level;
    END IF;

    -- 2. padre e hijo en la misma estructura
    IF parent.structure_id <> NEW.structure_id THEN
        RAISE EXCEPTION 'El puesto padre pertenece a la estructura % y el hijo a la %',
            parent.structure_id, NEW.structure_id;
    END IF;

    -- 3. el territorio del hijo debe estar dentro del territorio del padre
    IF NOT territory_contained_in(NEW.territory_id, parent.territory_id) THEN
        RAISE EXCEPTION 'El territorio % del puesto hijo no está contenido en el territorio % del padre',
            NEW.territory_id, parent.territory_id;
    END IF;

    RETURN NEW;
END;
$$;

-- structure: FUNCTION territory_contained_in(bigint, bigint)
CREATE FUNCTION structure.territory_contained_in(candidate bigint, ancestor bigint) RETURNS boolean
    LANGUAGE plpgsql STABLE
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
    current_id BIGINT := candidate;
BEGIN
    IF ancestor IS NULL OR candidate IS NULL THEN
        RETURN TRUE;
    END IF;
    WHILE current_id IS NOT NULL LOOP
        IF current_id = ancestor THEN
            RETURN TRUE;
        END IF;
        SELECT parent_territory_id INTO current_id FROM territory WHERE id = current_id;
    END LOOP;
    RETURN FALSE;
END;
$$;

-- users: FUNCTION close_validity(tstzrange, timestamp with time zone)
CREATE FUNCTION users_compat.close_validity(p_range tstzrange, p_at timestamp with time zone DEFAULT NULL::timestamp with time zone) RETURNS tstzrange
    LANGUAGE sql
    SET search_path TO 'users_compat', 'users', 'public', 'pg_catalog'
    AS $$
  SELECT tstzrange(lower(p_range),
                   GREATEST(COALESCE(p_at, clock_timestamp()),
                            lower(p_range) + interval '1 microsecond'));
$$;

-- users: FUNCTION materialize_municipal(bigint, integer)
CREATE FUNCTION users_compat.materialize_municipal(p_position bigint, p_section integer) RETURNS bigint
    LANGUAGE plpgsql
    SET search_path TO 'users_compat', 'users', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_mun_terr  bigint;
  v_structure bigint;
  v_parent    bigint;
  v_from      timestamptz;
  v_auth      varchar(100);
  v_person    bigint;
  v_target    bigint;
  v_at        timestamptz := clock_timestamp();
  r           RECORD;
BEGIN
  SELECT p.territory_id, p.structure_id, p.parent_position_id, p.valid_from
    INTO v_mun_terr, v_structure, v_parent, v_from
    FROM structure."position" p
   WHERE p.id = p_position AND p.level = 'ZONE_COORDINATOR' AND p.valid_to IS NULL;

  IF v_mun_terr IS NULL THEN
    RAISE EXCEPTION 'el puesto % no es un coordinador vigente', p_position;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM structure."territory" t
                  WHERE t.id = v_mun_terr AND t.type = 'MUNICIPALITY') THEN
    RETURN p_position;   -- ya está a nivel sección
  END IF;

  SELECT a.auth_id, a.person_id INTO v_auth, v_person
    FROM structure."assignment" a
   WHERE a.position_id = p_position AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
   LIMIT 1;

  FOR r IN
    SELECT sec.id AS territory_id, sec.code, sec.section_geo_id
    FROM structure."territory" sec
    WHERE sec.parent_territory_id = v_mun_terr
      AND sec.type = 'SECTION'
      AND sec.valid_to IS NULL
  LOOP
    -- si ya hubiera un puesto de ese coordinador en la sección, se respeta
    SELECT p.id INTO v_target
      FROM structure."position" p
     WHERE p.structure_id = v_structure
       AND p.level = 'ZONE_COORDINATOR'
       AND p.territory_id = r.territory_id
       AND p.valid_to IS NULL
       AND p.code = 'CZ|' || r.code || '|' || v_auth
     LIMIT 1;

    IF v_target IS NULL THEN
      v_target := users_compat.next_position_id();
      INSERT INTO structure."position" (id, structure_id, level, parent_position_id,
                                territory_id, code, valid_from)
      VALUES (v_target, v_structure, 'ZONE_COORDINATOR', v_parent,
              r.territory_id, 'CZ|' || r.code || '|' || v_auth, v_from);

      IF v_person IS NOT NULL THEN
        INSERT INTO structure."assignment" (id, created, position_id, person_id, role,
                                    auth_id, validity, start_reason)
        VALUES (users_compat.next_assignment_id(), now(), v_target, v_person, 'HOLDER',
                v_auth, tstzrange(v_from, NULL),
                'materialización del nombramiento municipal');
      END IF;
    END IF;

    -- los promotores de esa sección pasan a colgar del puesto de la sección
    UPDATE structure."position" pr
       SET parent_position_id = v_target
     WHERE pr.parent_position_id = p_position
       AND pr.level = 'PROMOTER'
       AND pr.territory_id = r.territory_id;
  END LOOP;

  -- el municipal se cierra
  UPDATE structure."assignment" a
     SET validity = users_compat.close_validity(a.validity, v_at),
         end_reason = 'materialización del nombramiento municipal'
   WHERE a.position_id = p_position AND upper(a.validity) IS NULL;

  UPDATE structure."position" p
     SET valid_to = GREATEST(v_at, p.valid_from + interval '1 microsecond')
   WHERE p.id = p_position AND p.valid_to IS NULL;

  SELECT p.id INTO v_target
    FROM structure."position" p
    JOIN structure."territory" t ON t.id = p.territory_id
   WHERE p.structure_id = v_structure
     AND p.level = 'ZONE_COORDINATOR'
     AND p.valid_to IS NULL
     AND t.section_geo_id = p_section
     AND p.code = 'CZ|' || t.code || '|' || v_auth
   LIMIT 1;

  RETURN v_target;
END;
$$;

-- users: FUNCTION next_assignment_id()
CREATE FUNCTION users_compat.next_assignment_id() RETURNS bigint
    LANGUAGE sql
    SET search_path TO 'users_compat', 'users', 'public', 'pg_catalog'
    AS $$ SELECT id FROM structure_compat."seq_assignment" $$;

-- users: FUNCTION next_person_id()
CREATE FUNCTION users_compat.next_person_id() RETURNS bigint
    LANGUAGE sql
    SET search_path TO 'users_compat', 'users', 'public', 'pg_catalog'
    AS $$ SELECT id FROM structure_compat."seq_person"     $$;

-- users: FUNCTION next_position_id()
CREATE FUNCTION users_compat.next_position_id() RETURNS bigint
    LANGUAGE sql
    SET search_path TO 'users_compat', 'users', 'public', 'pg_catalog'
    AS $$ SELECT id FROM structure_compat."seq_position"   $$;

-- users: FUNCTION us_id(bigint, integer)
CREATE FUNCTION users_compat.us_id(p_position bigint, p_section integer) RETURNS bigint
    LANGUAGE sql IMMUTABLE
    AS $$ SELECT (p_position << 20) | p_section::bigint $$;

-- users: FUNCTION us_override(bigint)
CREATE FUNCTION users_compat.us_override(p_id bigint) RETURNS bigint
    LANGUAGE plpgsql
    SET search_path TO 'users_compat', 'users', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_pos       bigint := users_compat.us_position(p_id);
  v_sec       integer := users_compat.us_section(p_id);
  v_structure bigint;
  v_parent    bigint;
  v_from      timestamptz;
  v_auth      varchar(100);
  v_person    bigint;
  v_sec_terr  bigint;
  v_code      varchar(80);
  v_new       bigint;
BEGIN
  SELECT p.structure_id, p.parent_position_id, p.valid_from
    INTO v_structure, v_parent, v_from
    FROM structure."position" p
   WHERE p.id = v_pos AND p.level = 'ZONE_COORDINATOR' AND p.valid_to IS NULL;

  IF v_structure IS NULL THEN
    RAISE EXCEPTION 'el puesto % no es un coordinador vigente', v_pos;
  END IF;

  -- ya está a nivel sección: la meta va directa al puesto
  IF EXISTS (SELECT 1 FROM structure."territory" t
              JOIN structure."position" p ON p.territory_id = t.id
             WHERE p.id = v_pos AND t.type = 'SECTION') THEN
    RETURN v_pos;
  END IF;

  SELECT a.auth_id, a.person_id INTO v_auth, v_person
    FROM structure."assignment" a
   WHERE a.position_id = v_pos AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
   LIMIT 1;

  SELECT t.id, 'CZ|' || t.code || '|' || v_auth INTO v_sec_terr, v_code
    FROM structure."territory" t
   WHERE t.section_geo_id = v_sec AND t.type = 'SECTION' AND t.valid_to IS NULL
   LIMIT 1;

  IF v_sec_terr IS NULL THEN
    RAISE EXCEPTION 'la sección % no existe en el catálogo de territorios', v_sec;
  END IF;

  SELECT p.id INTO v_new FROM structure."position" p
   WHERE p.structure_id = v_structure
     AND p.level = 'ZONE_COORDINATOR'
     AND p.territory_id = v_sec_terr
     AND p.valid_to IS NULL
     AND p.code = v_code
   LIMIT 1;

  IF v_new IS NULL THEN
    v_new := users_compat.next_position_id();
    INSERT INTO structure."position" (id, structure_id, level, parent_position_id,
                              territory_id, code, valid_from)
    VALUES (v_new, v_structure, 'ZONE_COORDINATOR', v_parent, v_sec_terr, v_code, v_from);

    IF v_person IS NOT NULL THEN
      INSERT INTO structure."assignment" (id, created, position_id, person_id, role,
                                  auth_id, validity, start_reason)
      VALUES (users_compat.next_assignment_id(), now(), v_new, v_person, 'HOLDER',
              v_auth, tstzrange(v_from, NULL),
              'meta propia sobre una sección del nombramiento municipal');
    END IF;

    -- los promotores de esa sección pasan al puesto específico
    UPDATE structure."position" pr
       SET parent_position_id = v_new
     WHERE pr.parent_position_id = v_pos
       AND pr.level = 'PROMOTER'
       AND pr.territory_id = v_sec_terr;
  END IF;

  RETURN v_new;
END;
$$;

-- users: FUNCTION us_position(bigint)
CREATE FUNCTION users_compat.us_position(p_id bigint) RETURNS bigint
    LANGUAGE sql IMMUTABLE
    AS $$ SELECT p_id >> 20 $$;

-- users: FUNCTION us_resolve(bigint)
CREATE FUNCTION users_compat.us_resolve(p_id bigint) RETURNS bigint
    LANGUAGE plpgsql
    SET search_path TO 'users_compat', 'users', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_pos bigint := users_compat.us_position(p_id);
  v_sec integer := users_compat.us_section(p_id);
BEGIN
  IF EXISTS (SELECT 1 FROM structure."position" p
              JOIN structure."territory" t ON t.id = p.territory_id
             WHERE p.id = v_pos AND t.type = 'MUNICIPALITY') THEN
    RETURN users_compat.materialize_municipal(v_pos, v_sec);
  END IF;
  RETURN v_pos;
END;
$$;

-- users: FUNCTION us_section(bigint)
CREATE FUNCTION users_compat.us_section(p_id bigint) RETURNS integer
    LANGUAGE sql IMMUTABLE
    AS $$ SELECT (p_id & 1048575)::integer $$;

-- users: FUNCTION user_sections_delete()
CREATE FUNCTION users_compat.user_sections_delete() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'users_compat', 'users', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_pos bigint := users_compat.us_resolve(OLD.id);
  v_at  timestamptz := clock_timestamp();
BEGIN
  IF EXISTS (SELECT 1 FROM structure."position" ch
              WHERE ch.parent_position_id = v_pos AND ch.valid_to IS NULL) THEN
    RAISE EXCEPTION 'el coordinador todavía tiene promotores en esa sección: reubícalos antes de quitarla';
  END IF;

  UPDATE structure."assignment" a
     SET validity = users_compat.close_validity(a.validity, v_at),
         end_reason = 'baja por vista de compatibilidad (users_seccions)'
   WHERE a.position_id = v_pos AND upper(a.validity) IS NULL;

  UPDATE structure."position" p
     SET valid_to = GREATEST(v_at, p.valid_from + interval '1 microsecond')
   WHERE p.id = v_pos AND p.valid_to IS NULL;

  RETURN OLD;
END;
$$;

-- users: FUNCTION user_sections_insert()
CREATE FUNCTION users_compat.user_sections_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'users_compat', 'users', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_sec_terr  bigint;
  v_mun_terr  bigint;
  v_structure bigint;
  v_alternate boolean;
  v_parent    bigint;
  v_person    bigint;
  v_pos       bigint;
  v_code      varchar(80);
  v_ref_auth  varchar(100);
BEGIN
  IF NEW.user_auth_id IS NULL OR NEW.seccion_geo_id IS NULL THEN
    RAISE EXCEPTION 'falta el authId del coordinador o la sección';
  END IF;

  SELECT t.id INTO v_sec_terr FROM structure."territory" t
   WHERE t.section_geo_id = NEW.seccion_geo_id::integer
     AND t.type = 'SECTION' AND t.valid_to IS NULL LIMIT 1;
  IF v_sec_terr IS NULL THEN
    RAISE EXCEPTION 'la sección % no existe en el catálogo de territorios', NEW.seccion_geo_id;
  END IF;

  SELECT t.parent_territory_id INTO v_mun_terr FROM structure."territory" t WHERE t.id = v_sec_terr;
  v_mun_terr := COALESCE(v_mun_terr,
                         (SELECT t.id FROM structure."territory" t
                           WHERE t.municipality_geo_id = NEW.municipality_id::integer
                             AND t.type = 'MUNICIPALITY' LIMIT 1));

  -- la rama sale de `users`, que vive en esta misma base
  SELECT COALESCE(u.flag_alter, false) INTO v_alternate
    FROM users u WHERE u.auth_id = NEW.user_auth_id LIMIT 1;
  v_alternate := COALESCE(v_alternate, false);

  SELECT s.id INTO v_structure FROM structure."structure" s
   WHERE s.type = CASE WHEN v_alternate THEN 'ALTERNATE' ELSE 'MAIN' END
   ORDER BY s.id LIMIT 1;

  -- ¿ya está cubierta? el puesto de sección gana al municipal
  SELECT p.id INTO v_pos
    FROM structure."assignment" a
    JOIN structure."position" p ON p.id = a.position_id
   WHERE a.auth_id = NEW.user_auth_id
     AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
     AND p.level = 'ZONE_COORDINATOR'
     AND p.valid_to IS NULL
     AND p.territory_id IN (v_sec_terr, v_mun_terr)
   ORDER BY (p.territory_id = v_sec_terr) DESC
   LIMIT 1;

  IF v_pos IS NOT NULL THEN
    IF NEW.meta IS NOT NULL
       AND EXISTS (SELECT 1 FROM structure."position" p
                    WHERE p.id = v_pos AND p.territory_id = v_sec_terr) THEN
      UPDATE structure."position"
         SET goal = NEW.meta,
             child_limit = COALESCE(NEW.limit_promoters, child_limit)
       WHERE id = v_pos;
    END IF;
    NEW.id := users_compat.us_id(v_pos, NEW.seccion_geo_id::integer);
    RETURN NEW;
  END IF;

  -- El referente del que ya cuelga ese coordinador, PERO el puesto de
  -- referente sobre el municipio de la sección nueva: el trigger
  -- `position_validate_hierarchy` exige que el territorio del hijo esté
  -- dentro del territorio del padre, así que colgar una sección de Centro
  -- del referente de Cárdenas se rechaza. Si ese puesto de referente no
  -- existe todavía, se crea; y si no se sabe quién es el referente, el
  -- coordinador queda sin padre, que el modelo permite.
  SELECT rh.auth_id INTO v_ref_auth
    FROM structure."assignment" a
    JOIN structure."position" p  ON p.id = a.position_id
    JOIN structure_compat."position_holder" rh ON rh.position_id = p.parent_position_id
   WHERE a.auth_id = NEW.user_auth_id
     AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
     AND p.level = 'ZONE_COORDINATOR'
     AND p.structure_id = v_structure
   ORDER BY p.id DESC LIMIT 1;

  IF v_ref_auth IS NOT NULL THEN
    SELECT p.id INTO v_parent
      FROM structure."position" p
      JOIN structure_compat."position_holder" h ON h.position_id = p.id
     WHERE p.level = 'REFERENT'
       AND p.structure_id = v_structure
       AND p.territory_id = v_mun_terr
       AND p.valid_to IS NULL
       AND h.auth_id = v_ref_auth
     LIMIT 1;

    IF v_parent IS NULL THEN
      v_parent := users_compat.next_position_id();
      INSERT INTO structure."position" (id, structure_id, level, territory_id, code, valid_from)
      VALUES (v_parent, v_structure, 'REFERENT', v_mun_terr,
              'REF|' || (SELECT t.code FROM structure."territory" t WHERE t.id = v_mun_terr)
                     || '|' || v_ref_auth,
              now());

      SELECT p.id INTO v_person FROM structure."person" p WHERE p.auth_id = v_ref_auth LIMIT 1;
      IF v_person IS NOT NULL THEN
        INSERT INTO structure."assignment" (id, created, position_id, person_id, role,
                                    auth_id, validity, start_reason)
        VALUES (users_compat.next_assignment_id(), now(), v_parent, v_person, 'HOLDER',
                v_ref_auth, tstzrange(now(), NULL),
                'alta por vista de compatibilidad (users_seccions)');
      END IF;
      v_person := NULL;
    END IF;
  END IF;

  SELECT 'CZ|' || t.code || '|' || NEW.user_auth_id INTO v_code
    FROM structure."territory" t WHERE t.id = v_sec_terr;

  v_pos := users_compat.next_position_id();
  INSERT INTO structure."position" (id, structure_id, level, parent_position_id, territory_id,
                            code, goal, child_limit, valid_from)
  VALUES (v_pos, v_structure, 'ZONE_COORDINATOR', v_parent, v_sec_terr,
          v_code, NEW.meta, NEW.limit_promoters, now());

  SELECT p.id INTO v_person FROM structure."person" p WHERE p.auth_id = NEW.user_auth_id LIMIT 1;
  IF v_person IS NULL THEN
    v_person := users_compat.next_person_id();
    INSERT INTO structure."person" (id, auth_id, legacy_source, created)
    VALUES (v_person, NEW.user_auth_id, 'keycloak', now());
  END IF;

  INSERT INTO structure."assignment" (id, created, position_id, person_id, role,
                              auth_id, validity, start_reason)
  VALUES (users_compat.next_assignment_id(), now(), v_pos, v_person, 'HOLDER',
          NEW.user_auth_id, tstzrange(now(), NULL),
          'alta por vista de compatibilidad (users_seccions)');

  NEW.id := users_compat.us_id(v_pos, NEW.seccion_geo_id::integer);
  RETURN NEW;
END;
$$;

-- users: FUNCTION user_sections_update()
CREATE FUNCTION users_compat.user_sections_update() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'users_compat', 'users', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_pos bigint;
  v_at  timestamptz := clock_timestamp();
BEGIN
  IF NEW.deleted IS NOT NULL AND OLD.deleted IS NULL THEN
    v_pos := users_compat.us_resolve(OLD.id);

    IF EXISTS (SELECT 1 FROM structure."position" ch
                WHERE ch.parent_position_id = v_pos AND ch.valid_to IS NULL) THEN
      RAISE EXCEPTION 'el coordinador todavía tiene promotores en esa sección: reubícalos antes de quitarla';
    END IF;

    UPDATE structure."assignment" a
       SET validity = users_compat.close_validity(a.validity, v_at),
           end_reason = 'baja por vista de compatibilidad (users_seccions)'
     WHERE a.position_id = v_pos AND upper(a.validity) IS NULL;

    UPDATE structure."position" p
       SET valid_to = GREATEST(v_at, p.valid_from + interval '1 microsecond')
     WHERE p.id = v_pos AND p.valid_to IS NULL;

    RETURN NEW;
  END IF;

  IF NEW.meta IS DISTINCT FROM OLD.meta
     OR NEW.limit_promoters IS DISTINCT FROM OLD.limit_promoters THEN
    -- la meta se pone como excepción sobre la sección, sin romper el
    -- nombramiento municipal
    v_pos := users_compat.us_override(OLD.id);
    UPDATE structure."position"
       SET goal        = NEW.meta,
           child_limit = NEW.limit_promoters
     WHERE id = v_pos;
  END IF;

  RETURN NEW;
END;
$$;

-- followers: FUNCTION assignment_for_auth(character varying)
CREATE FUNCTION followers_compat.assignment_for_auth(p_auth character varying) RETURNS bigint
    LANGUAGE sql STABLE
    AS $$
  SELECT assignment_id FROM structure_compat."position_holder"
   WHERE p_auth IS NOT NULL AND auth_id = p_auth
   ORDER BY assignment_id
   LIMIT 1;
$$;

-- followers: FUNCTION close_validity(tstzrange, timestamp with time zone)
CREATE FUNCTION followers_compat.close_validity(p_range tstzrange, p_at timestamp with time zone DEFAULT NULL::timestamp with time zone) RETURNS tstzrange
    LANGUAGE sql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$
  SELECT tstzrange(lower(p_range),
                   GREATEST(COALESCE(p_at, clock_timestamp()),
                            lower(p_range) + interval '1 microsecond'));
$$;

-- followers: FUNCTION followers_delete()
CREATE FUNCTION followers_compat.followers_delete() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$
BEGIN
  UPDATE structure."membership"
     SET validity = followers_compat.close_validity(validity)
   WHERE legacy_id = OLD.id AND upper(validity) IS NULL;

  INSERT INTO structure."change_log" (id, occurred_at, type, entity, entity_id, before_data, reason)
  VALUES (followers_compat.next_change_log_id(), now(), 'TERMINATE', 'membership', OLD.id,
          jsonb_build_object('curp', OLD.curp, 'promoter_auth_id', OLD.promoter_auth_id),
          'borrado por vista de compatibilidad');

  RETURN OLD;
END;
$$;

-- followers: FUNCTION followers_insert()
CREATE FUNCTION followers_compat.followers_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$
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

  v_person    := followers_compat.person_upsert(NEW.curp, NEW.ine, NEW.names,
                    NEW.last_name, NEW.sur_name, NEW.birthdate, NEW.email,
                    NEW.cell_phone_number, NEW.street, NEW.external_number,
                    NEW.internal_number, NEW.neighborhood, NEW.zip_code,
                    'followers', v_id);
  v_position  := followers_compat.position_for_promoter(v_auth, v_alternate, NEW.section_id);
  v_territory := followers_compat.territory_for_section(NEW.section_id);
  v_assignment := followers_compat.assignment_for_auth(NEW.user_create_id);

  SELECT structure_id INTO v_structure FROM structure."position" WHERE id = v_position;

  INSERT INTO structure."membership" (id, person_id, structure_id, position_id,
                              captured_by_assignment_id, captured_by_auth_id,
                              territory_id,
                              source, status, validity, vote_flag,
                              vote_flag_timestamp, legacy_id)
  VALUES (followers_compat.next_membership_id(),
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

  INSERT INTO structure."change_log" (id, occurred_at, type, entity, entity_id, after_data, reason)
  VALUES (followers_compat.next_change_log_id(), now(), 'CREATE', 'membership', v_id,
          jsonb_build_object('person_id', v_person, 'position_id', v_position,
                             'legacy_id', v_id),
          'alta por vista de compatibilidad');

  NEW.id := v_id;
  RETURN NEW;
END;
$$;

-- followers: FUNCTION followers_update()
CREATE FUNCTION followers_compat.followers_update() RETURNS trigger
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
$$;

-- followers: FUNCTION next_assignment_id()
CREATE FUNCTION followers_compat.next_assignment_id() RETURNS bigint
    LANGUAGE sql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$ SELECT id FROM structure_compat."seq_assignment" $$;

-- followers: FUNCTION next_change_log_id()
CREATE FUNCTION followers_compat.next_change_log_id() RETURNS bigint
    LANGUAGE sql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$ SELECT id FROM structure_compat."seq_change_log" $$;

-- followers: FUNCTION next_membership_id()
CREATE FUNCTION followers_compat.next_membership_id() RETURNS bigint
    LANGUAGE sql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$ SELECT id FROM structure_compat."seq_membership" $$;

-- followers: FUNCTION next_person_id()
CREATE FUNCTION followers_compat.next_person_id() RETURNS bigint
    LANGUAGE sql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$ SELECT id FROM structure_compat."seq_person"     $$;

-- followers: FUNCTION next_position_id()
CREATE FUNCTION followers_compat.next_position_id() RETURNS bigint
    LANGUAGE sql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$ SELECT id FROM structure_compat."seq_position"   $$;

-- followers: FUNCTION person_upsert(character varying, character varying, character varying, character varying, character varying, timestamp without time zone, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, bigint)
CREATE FUNCTION followers_compat.person_upsert(p_curp character varying, p_ine character varying, p_names character varying, p_last_name character varying, p_sur_name character varying, p_birthdate timestamp without time zone, p_email character varying, p_cell character varying, p_street character varying, p_external character varying, p_internal character varying, p_neighborhood character varying, p_zip character varying, p_legacy_source character varying, p_legacy_id bigint) RETURNS bigint
    LANGUAGE plpgsql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_curp varchar := nullif(upper(trim(p_curp)), '');
  v_ine  varchar := nullif(upper(trim(p_ine)), '');
  v_id   bigint;
BEGIN
  IF v_curp IS NOT NULL THEN
    SELECT id INTO v_id FROM structure."person" WHERE curp = v_curp LIMIT 1;
  END IF;

  IF v_id IS NOT NULL THEN
    -- se completan huecos, nunca se pisa un dato existente con uno nulo
    UPDATE structure."person"
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

  v_id := followers_compat.next_person_id();
  INSERT INTO structure."person" (id, curp, ine, names, last_name, sur_name, birthdate,
                          email, cell_phone_number, street, external_number,
                          internal_number, neighborhood, zip_code,
                          legacy_source, legacy_id, created)
  VALUES (v_id, v_curp, v_ine, p_names, p_last_name, p_sur_name, p_birthdate::date,
          p_email, p_cell, p_street, p_external, p_internal, p_neighborhood,
          p_zip, p_legacy_source, p_legacy_id, now());

  RETURN v_id;
END;
$$;

-- followers: FUNCTION position_for_coordinator(character varying, character varying, bigint, bigint, boolean)
CREATE FUNCTION followers_compat.position_for_coordinator(p_cz_auth character varying, p_ref_auth character varying, p_section bigint, p_municipality bigint, p_alternate boolean) RETURNS bigint
    LANGUAGE plpgsql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_structure bigint;
  v_sec_terr  bigint;
  v_mun_terr  bigint;
  v_ref       bigint;
  v_cz        bigint;
  v_person    bigint;
BEGIN
  SELECT id INTO v_structure FROM structure."structure"
   WHERE type = CASE WHEN p_alternate THEN 'ALTERNATE' ELSE 'MAIN' END
   ORDER BY id LIMIT 1;

  v_sec_terr := followers_compat.territory_for_section(p_section);
  IF v_sec_terr IS NULL THEN
    RAISE EXCEPTION 'La sección % no existe en el catálogo de territorios', p_section;
  END IF;
  SELECT parent_territory_id INTO v_mun_terr FROM structure."territory" WHERE id = v_sec_terr;
  -- si la sección quedó colgando sin municipio, se usa el que trae el alta
  v_mun_terr := COALESCE(v_mun_terr, followers_compat.territory_for_municipality(p_municipality));

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
    FROM structure_compat."position_holder" h
    JOIN structure."position" p ON p.id = h.position_id
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
    FROM structure_compat."position_holder" h
    JOIN structure."position" p ON p.id = h.position_id
   WHERE h.auth_id = p_ref_auth
     AND p.level = 'REFERENT'
     AND p.territory_id = v_mun_terr
     AND p.structure_id = v_structure
     AND p.valid_to IS NULL
   LIMIT 1;

  IF v_ref IS NULL AND p_ref_auth IS NOT NULL THEN
    v_ref := followers_compat.next_position_id();
    INSERT INTO structure."position" (id, structure_id, level, territory_id, code, valid_from)
    VALUES (v_ref, v_structure, 'REFERENT', v_mun_terr,
            'REF|' || (SELECT code FROM structure."territory" WHERE id = v_mun_terr) || '|' || p_ref_auth,
            now());

    SELECT id INTO v_person FROM structure."person" WHERE auth_id = p_ref_auth LIMIT 1;
    IF v_person IS NOT NULL THEN
      INSERT INTO structure."assignment" (id, created, position_id, person_id, role,
                                  auth_id, validity, start_reason)
      VALUES (followers_compat.next_assignment_id(), now(), v_ref, v_person, 'HOLDER',
              p_ref_auth, tstzrange(now(), NULL), 'alta por vista de compatibilidad');
    END IF;
  END IF;

  v_cz := followers_compat.next_position_id();
  INSERT INTO structure."position" (id, structure_id, level, parent_position_id, territory_id, code, valid_from)
  VALUES (v_cz, v_structure, 'ZONE_COORDINATOR', v_ref, v_sec_terr,
          'CZ|' || (SELECT code FROM structure."territory" WHERE id = v_sec_terr) || '|' || p_cz_auth,
          now());

  SELECT id INTO v_person FROM structure."person" WHERE auth_id = p_cz_auth LIMIT 1;
  IF v_person IS NOT NULL THEN
    INSERT INTO structure."assignment" (id, created, position_id, person_id, role,
                                auth_id, validity, start_reason)
    VALUES (followers_compat.next_assignment_id(), now(), v_cz, v_person, 'HOLDER',
            p_cz_auth, tstzrange(now(), NULL), 'alta por vista de compatibilidad');
  END IF;

  RETURN v_cz;
END;
$$;

-- followers: FUNCTION position_for_promoter(character varying, boolean, bigint)
CREATE FUNCTION followers_compat.position_for_promoter(p_auth character varying, p_alternate boolean, p_section bigint) RETURNS bigint
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE v_id bigint;
BEGIN
  IF p_auth IS NOT NULL THEN
    SELECT p.id INTO v_id
      FROM structure_compat."position_holder" h
      JOIN structure."position"  p ON p.id = h.position_id
      JOIN structure."structure" s ON s.id = p.structure_id
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
      FROM structure."position" p
      JOIN structure."territory" t ON t.id = p.territory_id
     WHERE t.section_geo_id = p_section::integer
       AND p.code = 'POOL|' || t.code
     LIMIT 1;
    IF v_id IS NOT NULL THEN RETURN v_id; END IF;
  END IF;

  SELECT id INTO v_id FROM structure."position" WHERE code = 'UNPLACED|POOL' LIMIT 1;
  RETURN v_id;
END;
$$;

-- followers: FUNCTION promoters_delete()
CREATE FUNCTION followers_compat.promoters_delete() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$
DECLARE v_position bigint;
BEGIN
  SELECT id INTO v_position FROM structure."position" WHERE legacy_id = OLD.id;
  IF v_position IS NULL THEN RETURN OLD; END IF;

  UPDATE structure."assignment"
     SET validity = followers_compat.close_validity(validity),
         end_reason = 'borrado por vista de compatibilidad'
   WHERE position_id = v_position AND upper(validity) IS NULL;

  UPDATE structure."position" SET valid_to = now() WHERE id = v_position;

  INSERT INTO structure."change_log" (id, occurred_at, type, entity, entity_id, before_data, reason)
  VALUES (followers_compat.next_change_log_id(), now(), 'TERMINATE', 'position', v_position,
          jsonb_build_object('legacy_id', OLD.id, 'curp', OLD.curp),
          'borrado de promotor por vista de compatibilidad');

  RETURN OLD;
END;
$$;

-- followers: FUNCTION promoters_insert()
CREATE FUNCTION followers_compat.promoters_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$
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

  v_person := followers_compat.person_upsert(NEW.curp, NEW.ine, NEW.names,
                NEW.last_name, NEW.sur_name, NULL, NEW.email,
                NEW.cell_phone_number, NEW.street, NEW.outdoor_number,
                NEW.interior_number, NULL, NEW.postal_code, 'promoters', v_id);

  -- `person.auth_id` es unico, asi que sólo se rellena si está libre; la
  -- identidad que de verdad manda es la de la asignación, más abajo
  IF NEW.promoter_auth_id IS NOT NULL THEN
    UPDATE structure."person" SET auth_id = NEW.promoter_auth_id
     WHERE id = v_person
       AND auth_id IS NULL
       AND NOT EXISTS (SELECT 1 FROM structure."person" o WHERE o.auth_id = NEW.promoter_auth_id);
  END IF;

  v_cz := followers_compat.position_for_coordinator(NEW.cordinator_zone_auth_id,
            NEW.referent_auth_id, NEW.seccion_id, NEW.municipality_id, v_alternate);
  v_sec_terr := followers_compat.territory_for_section(NEW.seccion_id);
  SELECT structure_id INTO v_structure FROM structure."position" WHERE id = v_cz;

  v_position := followers_compat.next_position_id();
  INSERT INTO structure."position" (id, structure_id, level, parent_position_id, territory_id,
                            code, goal, valid_from, legacy_id)
  VALUES (v_position, v_structure, 'PROMOTER', v_cz, v_sec_terr,
          'PROM|' || v_id, NEW.followers_goal, COALESCE(NEW.created, now()), v_id);

  -- el authId va en la ASIGNACION: es la cuenta con la que se ocupa el
  -- puesto, no un atributo de la persona (que puede tener varias)
  INSERT INTO structure."assignment" (id, created, position_id, person_id, role,
                              auth_id, validity, start_reason)
  VALUES (followers_compat.next_assignment_id(), now(), v_position, v_person, 'HOLDER',
          NEW.promoter_auth_id, tstzrange(COALESCE(NEW.created, now()), NULL),
          'alta por vista de compatibilidad');

  INSERT INTO promoters_legacy (id, neighborhood_id, notes, updated,
                                user_create_id, vencimiento, cat_committee,
                                follower_id, auth_id)
  VALUES (v_id, NEW.neighborhood_id, NEW.notes, now(),
          NEW.user_create_id, NEW.vencimiento, NEW.cat_committee,
          NEW.follower_id, NEW.auth_id);

  INSERT INTO structure."change_log" (id, occurred_at, type, entity, entity_id, after_data, reason)
  VALUES (followers_compat.next_change_log_id(), now(), 'CREATE', 'position', v_position,
          jsonb_build_object('legacy_id', v_id, 'person_id', v_person,
                             'parent_position_id', v_cz),
          'alta de promotor por vista de compatibilidad');

  NEW.id := v_id;
  RETURN NEW;
END;
$$;

-- followers: FUNCTION promoters_update()
CREATE FUNCTION followers_compat.promoters_update() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'followers_compat', 'followers', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_position bigint;
  v_person   bigint;
  v_cz       bigint;
  v_sec_terr bigint;
BEGIN
  SELECT id INTO v_position FROM structure."position" WHERE legacy_id = OLD.id;
  IF v_position IS NULL THEN
    RAISE EXCEPTION 'No hay puesto para el promotor %', OLD.id;
  END IF;
  SELECT person_id INTO v_person FROM structure_compat."position_holder" WHERE position_id = v_position;

  IF v_person IS NOT NULL THEN
    UPDATE structure."person"
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
    v_cz := followers_compat.position_for_coordinator(NEW.cordinator_zone_auth_id,
              NEW.referent_auth_id, NEW.seccion_id, NEW.municipality_id,
              COALESCE(NEW.flag_alter, false));
    v_sec_terr := followers_compat.territory_for_section(NEW.seccion_id);

    UPDATE structure."position"
       SET parent_position_id = v_cz, territory_id = v_sec_terr
     WHERE id = v_position;

    INSERT INTO structure."change_log" (id, occurred_at, type, entity, entity_id, before_data, after_data, reason)
    VALUES (followers_compat.next_change_log_id(), now(), 'REORG_POSITION', 'position', v_position,
            jsonb_build_object('seccion_id', OLD.seccion_id,
                               'cordinator_zone_auth_id', OLD.cordinator_zone_auth_id),
            jsonb_build_object('seccion_id', NEW.seccion_id,
                               'cordinator_zone_auth_id', NEW.cordinator_zone_auth_id),
            'reorganización por vista de compatibilidad');
  END IF;

  UPDATE structure."position" SET goal = NEW.followers_goal WHERE id = v_position;

  IF NEW.deleted IS NOT NULL AND OLD.deleted IS NULL THEN
    UPDATE structure."position" SET valid_to = NEW.deleted WHERE id = v_position;
    UPDATE structure."assignment"
       SET validity = followers_compat.close_validity(validity, NEW.deleted),
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
$$;

-- followers: FUNCTION territory_for_municipality(bigint)
CREATE FUNCTION followers_compat.territory_for_municipality(p_mun bigint) RETURNS bigint
    LANGUAGE sql STABLE
    AS $$
  SELECT id FROM structure."territory"
   WHERE municipality_geo_id = p_mun::integer AND type = 'MUNICIPALITY'
   LIMIT 1;
$$;

-- followers: FUNCTION territory_for_section(bigint)
CREATE FUNCTION followers_compat.territory_for_section(p_section bigint) RETURNS bigint
    LANGUAGE sql STABLE
    AS $$
  SELECT id FROM structure."territory"
   WHERE section_geo_id = p_section::integer AND type = 'SECTION'
   LIMIT 1;
$$;


-- ===== tables =====

-- geography: SEQUENCE Centroides_id_0_seq
CREATE SEQUENCE geography."Centroides_id_0_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE Tabla_secciones_id_seq
CREATE SEQUENCE geography."Tabla_secciones_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;


SET default_tablespace = '';

SET default_table_access_method = heap;

-- geography: SEQUENCE colonias_x_seccion_id_seq
CREATE SEQUENCE geography.colonias_x_seccion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_colonias_tab_id_seq
CREATE SEQUENCE geography.geo_colonias_tab_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_distrito_federal_nacional_2023_id_seq
CREATE SEQUENCE geography.geo_distrito_federal_nacional_2023_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_distrito_federal_tab_2018_id_seq
CREATE SEQUENCE geography.geo_distrito_federal_tab_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_distrito_local_nacional_2023_id_seq
CREATE SEQUENCE geography.geo_distrito_local_nacional_2023_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_distrito_local_tab_2018_id_seq
CREATE SEQUENCE geography.geo_distrito_local_tab_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_distritos_federales_municipios_id_seq
CREATE SEQUENCE geography.geo_distritos_federales_municipios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_distritos_locales_municipios_id_seq
CREATE SEQUENCE geography.geo_distritos_locales_municipios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_entidades_id_seq
CREATE SEQUENCE geography.geo_entidades_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_entidades_nacional_2023_id_seq
CREATE SEQUENCE geography.geo_entidades_nacional_2023_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_label_municipios_nacional_corr_id_seq
CREATE SEQUENCE geography.geo_label_municipios_nacional_corr_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE tmp_geo_label_secciones_nacional_2023_id_seq
CREATE SEQUENCE geography.tmp_geo_label_secciones_nacional_2023_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_localidades_juntos_id_seq
CREATE SEQUENCE geography.geo_localidades_juntos_id_seq
    START WITH 999
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- geography: SEQUENCE geo_localidades_juntos_ids_seq
CREATE SEQUENCE geography.geo_localidades_juntos_ids_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_localidades_nacional_id_seq
CREATE SEQUENCE geography.geo_localidades_nacional_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_municipios_id_seq
CREATE SEQUENCE geography.geo_municipios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_municipios_nacional_2023_id_seq
CREATE SEQUENCE geography.geo_municipios_nacional_2023_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_safe_houses_id_seq
CREATE SEQUENCE geography.geo_safe_houses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- geography: SEQUENCE geo_secciones_2023_id_seq
CREATE SEQUENCE geography.geo_secciones_2023_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_secciones_2022_id_seq
CREATE SEQUENCE geography.geo_secciones_2022_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_secciones_TAB_2021_id_seq
CREATE SEQUENCE geography."geo_secciones_TAB_2021_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE geo_secciones_tab_2018_id_seq
CREATE SEQUENCE geography.geo_secciones_tab_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE label_localidades_text_utf8_id_seq
CREATE SEQUENCE geography.label_localidades_text_utf8_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE lineas_label_pruebas_id_seq
CREATE SEQUENCE geography.lineas_label_pruebas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE localidades_id_seq
CREATE SEQUENCE geography.localidades_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- geography: SEQUENCE localidades_secciones_id_seq
CREATE SEQUENCE geography.localidades_secciones_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- geography: SEQUENCE relacion_simbologia_secciones_id_seq
CREATE SEQUENCE geography.relacion_simbologia_secciones_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_distritoF_diputadosL_tab_2021_id_seq
CREATE SEQUENCE geography."t_distritoF_diputadosL_tab_2021_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_distritoL_diputadosL_tab_2021_id_seq
CREATE SEQUENCE geography."t_distritoL_diputadosL_tab_2021_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_distrito_federal_gob_tab_2018_id_seq
CREATE SEQUENCE geography.t_distrito_federal_gob_tab_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_distrito_locales_gob_tab_2018_id_seq
CREATE SEQUENCE geography.t_distrito_locales_gob_tab_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_distritof_diputadosf_tab_2021_id_seq
CREATE SEQUENCE geography.t_distritof_diputadosf_tab_2021_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_distritof_presidente_tab_2018_id_seq
CREATE SEQUENCE geography.t_distritof_presidente_tab_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_distritof_senadores_2018_id_seq
CREATE SEQUENCE geography.t_distritof_senadores_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_distritol_diputadosf_tab_2021_id_seq
CREATE SEQUENCE geography.t_distritol_diputadosf_tab_2021_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_distritol_presidente_tab_2018_id_seq
CREATE SEQUENCE geography.t_distritol_presidente_tab_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_equivalencia_localidades_secciones_id_seq
CREATE SEQUENCE geography.t_equivalencia_localidades_secciones_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_partidos_coaliciones_ganadoras_dipfed_id_seq
CREATE SEQUENCE geography.t_partidos_coaliciones_ganadoras_dipfed_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_seccion_dip_loc_ganadores_id_seq
CREATE SEQUENCE geography.t_seccion_dip_loc_ganadores_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_secciones_diputadosL_tab_2021_id_seq
CREATE SEQUENCE geography."t_secciones_diputadosL_tab_2021_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_secciones_diputadosf_tab_2021_id_seq
CREATE SEQUENCE geography.t_secciones_diputadosf_tab_2021_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_secciones_gob_tab_2018_id_seq
CREATE SEQUENCE geography.t_secciones_gob_tab_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_secciones_presidente_tab_2018_id_seq
CREATE SEQUENCE geography.t_secciones_presidente_tab_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_secciones_senadores_2018_id_seq
CREATE SEQUENCE geography.t_secciones_senadores_2018_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_secciones_tab_2018_vs_localidad_id_seq
CREATE SEQUENCE geography.t_secciones_tab_2018_vs_localidad_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE t_secciones_tab_2021_vs_localidad_id_seq
CREATE SEQUENCE geography.t_secciones_tab_2021_vs_localidad_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- geography: SEQUENCE tabla_campo_text_loc_utf8_id_seq
CREATE SEQUENCE geography.tabla_campo_text_loc_utf8_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- structure: SEQUENCE assignment_id_seq
CREATE SEQUENCE structure.assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- structure: SEQUENCE change_log_id_seq
CREATE SEQUENCE structure.change_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- structure: SEQUENCE membership_id_seq
CREATE SEQUENCE structure.membership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- structure: SEQUENCE person_id_seq
CREATE SEQUENCE structure.person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- structure: SEQUENCE position_id_seq
CREATE SEQUENCE structure.position_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- structure: SEQUENCE campaign_election_id_seq
CREATE SEQUENCE structure.campaign_election_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- structure: SEQUENCE campaign_id_seq
CREATE SEQUENCE structure.campaign_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- structure: SEQUENCE structure_id_seq
CREATE SEQUENCE structure.structure_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- structure: SEQUENCE territory_id_seq
CREATE SEQUENCE structure.territory_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- users: SEQUENCE coordinators_group_id_seq
CREATE SEQUENCE users.coordinators_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- users: SEQUENCE geo_localidades_juntos_id_seq
CREATE SEQUENCE users.geo_localidades_juntos_id_seq
    START WITH 999
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- users: SEQUENCE geo_secciones_2023_id_seq
CREATE SEQUENCE users.geo_secciones_2023_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- users: SEQUENCE menus_groups_pk_seq
CREATE SEQUENCE users.menus_groups_pk_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- users: SEQUENCE menus_pk_seq
CREATE SEQUENCE users.menus_pk_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- users: SEQUENCE menus_roles_id_seq
CREATE SEQUENCE users.menus_roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- users: SEQUENCE users_id_seq
CREATE SEQUENCE users.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- users: SEQUENCE users_municipalities_id_seq
CREATE SEQUENCE users.users_municipalities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE cat_committee_id_seq
CREATE SEQUENCE followers.cat_committee_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_table_access_method = heap;

-- followers: SEQUENCE cat_family_role_id_seq
CREATE SEQUENCE followers.cat_family_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE cat_marital_status_id_seq
CREATE SEQUENCE followers.cat_marital_status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE cat_section_id_seq
CREATE SEQUENCE followers.cat_section_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE cat_sex_id_seq
CREATE SEQUENCE followers.cat_sex_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE follower_mark_id_seq
CREATE SEQUENCE followers.follower_mark_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE followers_id_seq
CREATE SEQUENCE followers.followers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE followers_sirena_id_seq1
CREATE SEQUENCE followers.followers_sirena_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE followers_sirena_oid_seq
CREATE SEQUENCE followers.followers_sirena_oid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE meta_seccion_id_seq
CREATE SEQUENCE followers.meta_seccion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE meta_vf_id_seq
CREATE SEQUENCE followers.meta_vf_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- followers: SEQUENCE preregister_id_seq
CREATE SEQUENCE followers.preregister_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE promoters_id_seq
CREATE SEQUENCE followers.promoters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- followers: SEQUENCE update_follower_mark_id_seq
CREATE SEQUENCE followers.update_follower_mark_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- calendar: SEQUENCE calendars_id_seq
CREATE SEQUENCE calendar.calendars_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- calendar: SEQUENCE events_id_seq
CREATE SEQUENCE calendar.events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- canvas_and_walls: SEQUENCE canvas_and_walls_category_id_seq
CREATE SEQUENCE canvas_and_walls.canvas_and_walls_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- canvas_and_walls: SEQUENCE canvas_and_walls_goals_id_seq
CREATE SEQUENCE canvas_and_walls.canvas_and_walls_goals_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- canvas_and_walls: SEQUENCE canvas_and_walls_id_seq
CREATE SEQUENCE canvas_and_walls.canvas_and_walls_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- canvas_and_walls: SEQUENCE canvas_and_walls_proyects_id_seq
CREATE SEQUENCE canvas_and_walls.canvas_and_walls_proyects_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- citizen_issues: SEQUENCE geo_localidades_juntos_id_seq
CREATE SEQUENCE citizen_issues.geo_localidades_juntos_id_seq
    START WITH 999
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

SET default_table_access_method = heap;

-- citizen_issues: SEQUENCE geo_localidades_juntos_ids_seq
CREATE SEQUENCE citizen_issues.geo_localidades_juntos_ids_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- citizen_issues: SEQUENCE geo_municipios_id_seq
CREATE SEQUENCE citizen_issues.geo_municipios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- citizen_issues: SEQUENCE issues_categories_id_seq
CREATE SEQUENCE citizen_issues.issues_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- citizen_issues: SEQUENCE issues_categories_subjects_id_seq
CREATE SEQUENCE citizen_issues.issues_categories_subjects_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- citizen_issues: SEQUENCE issues_details_id_seq
CREATE SEQUENCE citizen_issues.issues_details_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- citizen_issues: SEQUENCE issues_id_seq
CREATE SEQUENCE citizen_issues.issues_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- citizen_issues: SEQUENCE issues_solutions_documents_categories_id_seq
CREATE SEQUENCE citizen_issues.issues_solutions_documents_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- citizen_issues: SEQUENCE issues_solutions_id_seq
CREATE SEQUENCE citizen_issues.issues_solutions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- citizen_issues: SEQUENCE issues_solutions_providers_id_seq
CREATE SEQUENCE citizen_issues.issues_solutions_providers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- citizen_issues: SEQUENCE issues_solutions_resources_id_seq
CREATE SEQUENCE citizen_issues.issues_solutions_resources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- citizen_issues: SEQUENCE issues_status_id_seq
CREATE SEQUENCE citizen_issues.issues_status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- citizen_issues: SEQUENCE issues_types_id_seq
CREATE SEQUENCE citizen_issues.issues_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- document_management: SEQUENCE departament_id_seq
CREATE SEQUENCE document_management.departament_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- document_management: SEQUENCE document_id_seq
CREATE SEQUENCE document_management.document_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- document_management: SEQUENCE person_id_seq
CREATE SEQUENCE document_management.person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- document_management: SEQUENCE position_id_seq
CREATE SEQUENCE document_management.position_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- document_management: SEQUENCE position_person_id_seq
CREATE SEQUENCE document_management.position_person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- document_management: SEQUENCE position_person_status_id_seq
CREATE SEQUENCE document_management.position_person_status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- document_management: SEQUENCE position_type_id_seq
CREATE SEQUENCE document_management.position_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- document_management: SEQUENCE receiver_id_seq
CREATE SEQUENCE document_management.receiver_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- document_management: SEQUENCE sender_id_seq
CREATE SEQUENCE document_management.sender_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE candidate_party_associations_id_seq
CREATE SEQUENCE elections.candidate_party_associations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE candidates_id_seq
CREATE SEQUENCE elections.candidates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE elections_federal_district_id_seq
CREATE SEQUENCE elections.elections_federal_district_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE elections_id_seq
CREATE SEQUENCE elections.elections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE elections_local_district_id_seq
CREATE SEQUENCE elections.elections_local_district_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE elections_municipality_id_seq
CREATE SEQUENCE elections.elections_municipality_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE elections_positions_id_seq
CREATE SEQUENCE elections.elections_positions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE elections_types_id_seq
CREATE SEQUENCE elections.elections_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE elections_voter_list_id_seq
CREATE SEQUENCE elections.elections_voter_list_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE elections_years_id_seq
CREATE SEQUENCE elections.elections_years_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE fast_survey_id_seq
CREATE SEQUENCE elections.fast_survey_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE political_parties_id_seq
CREATE SEQUENCE elections.political_parties_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE political_parties_type_id_seq
CREATE SEQUENCE elections.political_parties_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE polling_booths_address_type_id_seq
CREATE SEQUENCE elections.polling_booths_address_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE polling_booths_categories_types_id_seq
CREATE SEQUENCE elections.polling_booths_categories_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE polling_booths_election_association_id_seq
CREATE SEQUENCE elections.polling_booths_election_association_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE polling_booths_id_seq
CREATE SEQUENCE elections.polling_booths_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE polling_booths_issues_categories_id_seq
CREATE SEQUENCE elections.polling_booths_issues_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE polling_booths_issues_id_seq
CREATE SEQUENCE elections.polling_booths_issues_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE polling_booths_issues_sub_categories_id_seq
CREATE SEQUENCE elections.polling_booths_issues_sub_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE polling_booths_status_id_seq
CREATE SEQUENCE elections.polling_booths_status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE polling_booths_types_id_seq
CREATE SEQUENCE elections.polling_booths_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections: SEQUENCE status_validate_id_seq
CREATE SEQUENCE elections.status_validate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections_results: SEQUENCE election_result_id_seq
CREATE SEQUENCE elections_results.election_result_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections_results: SEQUENCE election_result_sheets_id_seq
CREATE SEQUENCE elections_results.election_result_sheets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections_structure: SEQUENCE cat_sex_id_seq
CREATE SEQUENCE elections_structure.cat_sex_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections_structure: SEQUENCE pollings_representatives_coordinators_id_seq
CREATE SEQUENCE elections_structure.pollings_representatives_coordinators_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections_structure: SEQUENCE pollings_representatives_id_seq
CREATE SEQUENCE elections_structure.pollings_representatives_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections_structure: SEQUENCE pollings_representatives_types_id_seq
CREATE SEQUENCE elections_structure.pollings_representatives_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections_structure: SEQUENCE routes_assignment_id_seq
CREATE SEQUENCE elections_structure.routes_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- elections_structure: SEQUENCE routes_id_seq
CREATE SEQUENCE elections_structure.routes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- electoral_history: SEQUENCE ballot_option_id_seq
CREATE SEQUENCE electoral_history.ballot_option_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- electoral_history: SEQUENCE candidacy_id_seq
CREATE SEQUENCE electoral_history.candidacy_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- electoral_history: SEQUENCE election_id_seq
CREATE SEQUENCE electoral_history.election_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- electoral_history: SEQUENCE polling_station_id_seq
CREATE SEQUENCE electoral_history.polling_station_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- electoral_history: SEQUENCE source_id_seq
CREATE SEQUENCE electoral_history.source_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- logfile: SEQUENCE goals_id_seq
CREATE SEQUENCE logfile.goals_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- logfile: SEQUENCE indicators_id_seq
CREATE SEQUENCE logfile.indicators_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- logfile: SEQUENCE instruments_id_seq
CREATE SEQUENCE logfile.instruments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- logfile: SEQUENCE logfile_id_seq
CREATE SEQUENCE logfile.logfile_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- logfile: SEQUENCE ods_id_seq
CREATE SEQUENCE logfile.ods_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE folio_seq
CREATE SEQUENCE survey.folio_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE secciones_ok_id_seq
CREATE SEQUENCE survey.secciones_ok_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- survey: SEQUENCE status_id_seq
CREATE SEQUENCE survey.status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_id_seq
CREATE SEQUENCE survey.survey_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_annex_id_seq
CREATE SEQUENCE survey.survey_annex_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_causes_of_disability_id_seq
CREATE SEQUENCE survey.survey_causes_of_disability_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_cultural_activity_id_seq
CREATE SEQUENCE survey.survey_cultural_activity_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_disability_id_seq
CREATE SEQUENCE survey.survey_disability_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_disability_information_id_seq
CREATE SEQUENCE survey.survey_disability_information_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_family_relationship_id_seq
CREATE SEQUENCE survey.survey_family_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_federal_program_id_seq
CREATE SEQUENCE survey.survey_federal_program_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_housing_id_seq
CREATE SEQUENCE survey.survey_housing_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_housing_condition_id_seq
CREATE SEQUENCE survey.survey_housing_condition_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_housing_document_id_seq
CREATE SEQUENCE survey.survey_housing_document_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_housing_material_floor_id_seq
CREATE SEQUENCE survey.survey_housing_material_floor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_housing_material_roof_id_seq
CREATE SEQUENCE survey.survey_housing_material_roof_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_housing_material_wall_id_seq
CREATE SEQUENCE survey.survey_housing_material_wall_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_housing_service_list_id_seq
CREATE SEQUENCE survey.survey_housing_service_list_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- survey: SEQUENCE survey_housing_services_id_seq
CREATE SEQUENCE survey.survey_housing_services_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_indigenous_language_id_seq
CREATE SEQUENCE survey.survey_indigenous_language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_indigenous_language_id_seq1
CREATE SEQUENCE survey.survey_indigenous_language_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_job_id_seq
CREATE SEQUENCE survey.survey_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_job_id_seq1
CREATE SEQUENCE survey.survey_job_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_localidades_id_seq
CREATE SEQUENCE survey.survey_localidades_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- survey: SEQUENCE survey_origin_of_disability_id_seq
CREATE SEQUENCE survey.survey_origin_of_disability_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_origin_of_disability_id_seq1
CREATE SEQUENCE survey.survey_origin_of_disability_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_person_id_seq
CREATE SEQUENCE survey.survey_person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_person_id_seq1
CREATE SEQUENCE survey.survey_person_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_profession_id_seq
CREATE SEQUENCE survey.survey_profession_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_profession_id_seq1
CREATE SEQUENCE survey.survey_profession_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_relationship_id_seq
CREATE SEQUENCE survey.survey_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_requesting_or_auxiliary_document_id_seq
CREATE SEQUENCE survey.survey_requesting_or_auxiliary_document_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_requesting_or_auxiliary_document_id_seq1
CREATE SEQUENCE survey.survey_requesting_or_auxiliary_document_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_section_goal_id_seq
CREATE SEQUENCE survey.survey_section_goal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_tutor_or_assistant_id_seq
CREATE SEQUENCE survey.survey_tutor_or_assistant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_tutor_or_assistant_id_seq1
CREATE SEQUENCE survey.survey_tutor_or_assistant_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_type_of_disability_id_seq
CREATE SEQUENCE survey.survey_type_of_disability_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_type_of_disability_id_seq1
CREATE SEQUENCE survey.survey_type_of_disability_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_user_cambios_prod_id_seq
CREATE SEQUENCE survey.survey_user_cambios_prod_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- survey: SEQUENCE survey_user_cambios_prod_segunda_vuelta_id_seq
CREATE SEQUENCE survey.survey_user_cambios_prod_segunda_vuelta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;

-- survey: SEQUENCE survey_user_folio_id_seq
CREATE SEQUENCE survey.survey_user_folio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_user_folio_id_seq1
CREATE SEQUENCE survey.survey_user_folio_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_user_id_seq
CREATE SEQUENCE survey.survey_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_user_id_seq1
CREATE SEQUENCE survey.survey_user_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_user_section_id_seq
CREATE SEQUENCE survey.survey_user_section_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- survey: SEQUENCE survey_user_section_id_seq1
CREATE SEQUENCE survey.survey_user_section_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- visits: SEQUENCE visited_id_seq
CREATE SEQUENCE visits.visited_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- visits: SEQUENCE visited_number_id_seq
CREATE SEQUENCE visits.visited_number_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- visits: SEQUENCE visited_people_id_seq
CREATE SEQUENCE visits.visited_people_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- geography: TABLE geo_colonias
CREATE TABLE geography.geo_colonias (
    id integer NOT NULL,
    cp_asent character varying(5),
    nom_asent character varying(100),
    tipo_asent character varying(40),
    nom_mun character varying(60),
    nom_ent character varying(50),
    nom_ciudad character varying(60),
    cp_admpost character varying(254),
    cve_ent character varying(2),
    cp_oficina character varying(10),
    c_cp character varying(5),
    c_tipo_ase character varying(5),
    cve_mun character varying(3),
    id_asenta character varying(10),
    zona_asent character varying(10),
    cve_ciudad character varying(5),
    municipio_id integer
);

-- geography: TABLE geo_cp
CREATE TABLE geography.geo_cp (
    id integer NOT NULL,
    municipio_id integer,
    codigo_postal character varying(255)
);

-- geography: TABLE geo_distritos_federales
CREATE TABLE geography.geo_distritos_federales (
    id bigint NOT NULL,
    geom public.geometry,
    cve_ent character varying(2),
    nom_ent character varying(110),
    tipo numeric,
    distrito_federal bigint,
    ent_disf character varying(20),
    entidad_id integer,
    status boolean DEFAULT true NOT NULL
);

-- geography: TABLE geo_distritos_locales
CREATE TABLE geography.geo_distritos_locales (
    id bigint NOT NULL,
    geom public.geometry,
    cve_ent character varying(2),
    nom_ent character varying(110),
    distrito_local bigint,
    ent_disl character varying(20),
    distrito_f smallint,
    ent_disf character varying(10),
    entidad_id integer
);

-- geography: TABLE geo_entidades
CREATE TABLE geography.geo_entidades (
    id integer DEFAULT nextval('geography.geo_entidades_id_seq'::regclass) NOT NULL,
    geom public.geometry,
    cve_ent character varying(2),
    nom_ent character varying(20),
    status boolean
);

-- geography: TABLE geo_localidades_juntos
CREATE TABLE geography.geo_localidades_juntos (
    municipio character varying(255),
    seccion character varying(255),
    localidad_id integer DEFAULT nextval('geography.geo_localidades_juntos_id_seq'::regclass) NOT NULL,
    localidad character varying(255),
    seccion_id integer,
    deleted timestamp(6) without time zone,
    id integer DEFAULT nextval('geography.geo_localidades_juntos_ids_seq'::regclass) NOT NULL
);

-- geography: TABLE geo_municipios
CREATE TABLE geography.geo_municipios (
    id integer DEFAULT nextval('geography.geo_municipios_id_seq'::regclass) NOT NULL,
    geom public.geometry,
    cve_ent character varying(2),
    cve_munc character varying(5),
    cve_mun character varying(3),
    nom_ent character varying(20),
    nom_mun character varying(50),
    entidad_id integer,
    morena_vote_2024 integer
);

-- geography: TABLE geo_safe_houses
CREATE TABLE geography.geo_safe_houses (
    id bigint NOT NULL,
    alias character varying(255),
    created timestamp(6) without time zone,
    external_number character varying(255),
    internal_number character varying(255),
    latitude character varying(255),
    longitude character varying(255),
    manager_name character varying(255),
    manager_phone character varying(255),
    municipality_id bigint,
    neighborhood character varying(255),
    reference character varying(255),
    seccion_id bigint,
    street character varying(255),
    updated timestamp(6) without time zone,
    user_create_id character varying(255),
    zip_code character varying(255)
);

-- geography: TABLE geo_secciones
CREATE TABLE geography.geo_secciones (
    id integer DEFAULT nextval('geography.geo_secciones_2023_id_seq'::regclass),
    geom public.geometry,
    id_seccion numeric,
    cve_ent character varying(2),
    nom_ent character varying(20),
    cve_munc character varying(5),
    nom_mun character varying(50),
    distrito_f character varying(2),
    distrito_l character varying(2),
    seccion character varying(4),
    tipo numeric,
    mun_secc character(10),
    meta bigint,
    cve_mun character varying(255),
    municipio_id integer,
    distrito_local_id integer,
    distrito_federal_id character varying(255),
    entidad_id integer,
    status boolean,
    distrito_fed bigint,
    distrito_loc bigint,
    nominal_list integer,
    goal_percentage numeric(5,2) DEFAULT 61 NOT NULL,
    nominal_list_date date
);

-- geography: TABLE localidades_secciones
CREATE TABLE geography.localidades_secciones (
    id bigint NOT NULL,
    abr_ent character varying(255),
    altitud character varying(255),
    ambito character varying(255),
    cve_carta character varying(255),
    cve_ent character varying(255),
    cve_ent2 character varying(255),
    cve_locc character varying(255),
    cve_mun character varying(255),
    cve_munc character varying(255),
    cve_munc2 character varying(255),
    distrito_fed character varying(255),
    distrito_federal_id character varying(255),
    distrito_loc character varying(255),
    distrito_local_id character varying(255),
    entidad_id character varying(255),
    id_seccion bigint,
    lat_dec character varying(255),
    latitud character varying(255),
    lon_dec character varying(255),
    longitud character varying(255),
    meta character varying(255),
    mun_secc character varying(255),
    municipio_id character varying(255),
    nom_ent character varying(255),
    nom_ent2 character varying(255),
    nom_loc character varying(255),
    nom_mun character varying(255),
    nom_mun_2 character varying(255),
    per_fem character varying(255),
    per_mas character varying(255),
    per_total character varying(255),
    plano character varying(255),
    seccion character varying(255),
    status character varying(255),
    tipo character varying(255),
    tipo1 character varying(255),
    vot_tot character varying(255)
);

-- structure: TABLE assignment
CREATE TABLE structure.assignment (
    id bigint NOT NULL,
    position_id bigint NOT NULL,
    person_id bigint NOT NULL,
    role character varying(20) DEFAULT 'HOLDER'::character varying NOT NULL,
    auth_id character varying(100),
    validity tstzrange DEFAULT tstzrange(now(), NULL::timestamp with time zone) NOT NULL,
    start_reason character varying(500),
    end_reason character varying(500),
    created_by_person_id bigint,
    created timestamp with time zone DEFAULT now() NOT NULL,
    notes character varying(500),
    created_by_auth_id character varying(100),
    updated timestamp with time zone,
    CONSTRAINT assignment_role_chk CHECK (((role)::text = ANY ((ARRAY['HOLDER'::character varying, 'SUBSTITUTE'::character varying, 'ACTING'::character varying])::text[])))
);

-- structure: TABLE person
CREATE TABLE structure.person (
    id bigint NOT NULL,
    curp character varying(20),
    ine character varying(30),
    names character varying(200),
    last_name character varying(200),
    sur_name character varying(200),
    birthdate date,
    sex character varying(20),
    cell_phone_number character varying(30),
    email character varying(200),
    street character varying(300),
    external_number character varying(60),
    internal_number character varying(60),
    neighborhood character varying(300),
    zip_code character varying(20),
    legacy_source character varying(30),
    legacy_id bigint,
    auth_id character varying(100),
    created timestamp with time zone DEFAULT now() NOT NULL,
    updated timestamp with time zone,
    birth_entity_id integer,
    residence_entity_id integer,
    residence_municipality_id integer,
    cell_phone_number_other character varying(255),
    ine_validity integer
);

-- structure: TABLE membership
CREATE TABLE structure.membership (
    id bigint NOT NULL,
    person_id bigint NOT NULL,
    structure_id bigint NOT NULL,
    position_id bigint NOT NULL,
    captured_by_assignment_id bigint,
    territory_id bigint,
    source character varying(20) DEFAULT 'WEB'::character varying NOT NULL,
    status character varying(30) DEFAULT 'ACTIVO'::character varying NOT NULL,
    validity tstzrange DEFAULT tstzrange(now(), NULL::timestamp with time zone) NOT NULL,
    vote_flag boolean DEFAULT false NOT NULL,
    vote_flag_timestamp timestamp with time zone,
    legacy_id bigint,
    captured_by_auth_id character varying(100),
    CONSTRAINT membership_source_chk CHECK (((source)::text = ANY ((ARRAY['APP'::character varying, 'WEB'::character varying, 'IMPORT'::character varying])::text[])))
);

-- structure: TABLE membership_detail
CREATE TABLE structure.membership_detail (
    membership_id bigint NOT NULL,
    family_role_id bigint,
    marital_status_id bigint,
    sex_id bigint,
    is_adult boolean,
    committee_participation character varying(255),
    inviting_others character varying(255),
    number_of_people_invited character varying(255),
    polling_rep_participation character varying(255),
    supports_movement character varying(255),
    support_whatsapp character varying(255),
    respondent_last_name character varying(255),
    respondent_sur_name character varying(255),
    latitude character varying(50),
    longitude character varying(50),
    device_id character varying(255),
    app_uuid character varying(255),
    is_in_sync boolean,
    ine_validity integer,
    section_number_ine character varying(255),
    follower_auth_id character varying(100),
    updated timestamp with time zone
);

-- structure: TABLE structure
CREATE TABLE structure.structure (
    id bigint NOT NULL,
    campaign_id bigint NOT NULL,
    name character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    owner_person_id bigint,
    valid_from timestamp with time zone DEFAULT now() NOT NULL,
    valid_to timestamp with time zone,
    created timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT structure_type_chk CHECK (((type)::text = ANY ((ARRAY['MAIN'::character varying, 'ALTERNATE'::character varying, 'ALLIED'::character varying])::text[])))
);

-- structure: TABLE territory
CREATE TABLE structure.territory (
    id bigint NOT NULL,
    type character varying(20) NOT NULL,
    parent_territory_id bigint,
    code character varying(50) NOT NULL,
    name character varying(250),
    state_geo_id integer,
    municipality_geo_id integer,
    section_geo_id integer,
    valid_from timestamp with time zone DEFAULT now() NOT NULL,
    valid_to timestamp with time zone,
    federal_district_geo_id bigint,
    local_district_geo_id bigint,
    CONSTRAINT territory_parent_chk CHECK (((((type)::text = 'STATE'::text) AND (parent_territory_id IS NULL)) OR (((type)::text <> 'STATE'::text) AND (parent_territory_id IS NOT NULL)))),
    CONSTRAINT territory_type_chk CHECK (((type)::text = ANY ((ARRAY['STATE'::character varying, 'MUNICIPALITY'::character varying, 'SECTION'::character varying, 'FRAGMENT'::character varying])::text[])))
);

-- structure: TABLE position
CREATE TABLE structure."position" (
    id bigint NOT NULL,
    structure_id bigint NOT NULL,
    level character varying(20) NOT NULL,
    parent_position_id bigint,
    territory_id bigint NOT NULL,
    code character varying(80),
    goal integer,
    child_limit integer,
    valid_from timestamp with time zone DEFAULT now() NOT NULL,
    valid_to timestamp with time zone,
    legacy_id bigint,
    section_key character varying(20),
    CONSTRAINT position_level_chk CHECK (((level)::text = ANY ((ARRAY['REFERENT'::character varying, 'ZONE_COORDINATOR'::character varying, 'PROMOTER'::character varying])::text[]))),
    CONSTRAINT position_parent_chk CHECK (((((level)::text = 'REFERENT'::text) AND (parent_position_id IS NULL)) OR ((level)::text <> 'REFERENT'::text)))
);

-- structure: TABLE change_log
CREATE TABLE structure.change_log (
    id bigint NOT NULL,
    type character varying(20) NOT NULL,
    entity character varying(40) NOT NULL,
    entity_id bigint,
    before_data jsonb,
    after_data jsonb,
    actor_person_id bigint,
    reason character varying(500),
    occurred_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT change_log_type_chk CHECK (((type)::text = ANY ((ARRAY['ROTATION'::character varying, 'TRANSFER'::character varying, 'REORG_POSITION'::character varying, 'CREATE'::character varying, 'TERMINATE'::character varying, 'CONVERSION'::character varying])::text[])))
);

-- structure: TABLE campaign
CREATE TABLE structure.campaign (
    id bigint NOT NULL,
    name character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    year integer,
    election_day date,
    territory_id bigint,
    status character varying(20) DEFAULT 'PLANNING'::character varying NOT NULL,
    valid_from timestamp with time zone DEFAULT now() NOT NULL,
    valid_to timestamp with time zone,
    CONSTRAINT campaign_status_chk CHECK (((status)::text = ANY ((ARRAY['PLANNING'::character varying, 'ACTIVE'::character varying, 'CLOSED'::character varying])::text[]))),
    CONSTRAINT campaign_type_chk CHECK (((type)::text = ANY ((ARRAY['FEDERAL'::character varying, 'STATE'::character varying, 'MUNICIPAL'::character varying, 'INTERNAL'::character varying])::text[])))
);

-- structure: TABLE campaign_election
CREATE TABLE structure.campaign_election (
    id bigint NOT NULL,
    campaign_id bigint NOT NULL,
    office character varying(20) NOT NULL,
    scope character varying(20) NOT NULL,
    seats integer NOT NULL,
    election_day date NOT NULL,
    CONSTRAINT campaign_election_office_chk CHECK (((office)::text = ANY ((ARRAY['PRESIDENT'::character varying, 'SENATOR'::character varying, 'FEDERAL_DEPUTY'::character varying, 'GOVERNOR'::character varying, 'LOCAL_DEPUTY'::character varying, 'MAYOR'::character varying])::text[]))),
    CONSTRAINT campaign_election_scope_chk CHECK (((scope)::text = ANY ((ARRAY['STATE'::character varying, 'FEDERAL_DISTRICT'::character varying, 'LOCAL_DISTRICT'::character varying, 'MUNICIPALITY'::character varying])::text[])))
);

-- users: TABLE coordinators_group
CREATE TABLE users.coordinators_group (
    id bigint NOT NULL,
    coordinator_municipality_alt_auth_id character varying(255),
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    name character varying(255),
    updated timestamp(6) without time zone
);

-- users: TABLE geo_secciones
CREATE TABLE users.geo_secciones (
    id integer NOT NULL,
    id_seccion character varying(255),
    cve_ent character varying(255),
    nom_ent character varying(255),
    cve_munc character varying(255),
    nom_mun character varying(255),
    distrito_f character varying(255),
    distrito_l character varying(255),
    seccion character varying(255),
    tipo character varying(255),
    mun_secc character varying(255),
    meta character varying(255),
    cve_mun character varying(255),
    municipio_id integer,
    distrito_local_id character varying(255),
    distrito_federal_id character varying(255),
    entidad_id character varying(255),
    status character varying(255)
);

-- users: TABLE menus
CREATE TABLE users.menus (
    pk bigint NOT NULL,
    icon character varying(255),
    id character varying(255),
    link character varying(255),
    menu_group_id bigint,
    subtitle character varying(255),
    title character varying(255),
    type character varying(255)
);

-- users: TABLE menus_groups
CREATE TABLE users.menus_groups (
    pk bigint NOT NULL,
    icon character varying(255),
    id character varying(255),
    link character varying(255),
    subtitle character varying(255),
    title character varying(255),
    type character varying(255),
    "order" integer
);

-- users: TABLE menus_roles
CREATE TABLE users.menus_roles (
    id bigint NOT NULL,
    menu_group_id bigint,
    role character varying(255)
);

-- users: TABLE promoters
CREATE TABLE users.promoters (
    id character varying(255) NOT NULL,
    curp character varying(255),
    ine character varying(255),
    last_name character varying(255),
    names character varying(255),
    sur_name character varying(255),
    cordinator_zone_auth_id character varying(255),
    referent_auth_id character varying(255),
    promoter_auth_id character varying(255),
    flag_alter character varying(255)
);

-- users: TABLE users
CREATE TABLE users.users (
    id bigint NOT NULL,
    auth_id character varying(255),
    id_google_calendar character varying(255),
    phone character varying(255),
    status character varying(255),
    token_google_calendar character varying(255),
    rejuntado_id bigint,
    auth_id_reference character varying(255),
    auth_name character varying(255),
    auth_reference_name character varying(255),
    birth_entity_id integer,
    birthdate timestamp(6) without time zone,
    cell_phone_number_other character varying(255),
    created timestamp(6) without time zone,
    curp character varying(255),
    deleted timestamp(6) without time zone,
    email character varying(255),
    external_number character varying(255),
    ine character varying(255),
    internal_number character varying(255),
    last_name character varying(255),
    names character varying(255),
    neighborhood character varying(255),
    residence_entity_id integer,
    residence_municipality_id integer,
    sex integer,
    street character varying(255),
    support_whatsapp character varying(255),
    sur_name character varying(255),
    updated timestamp(6) without time zone,
    zip_code character varying(255),
    flag_alter boolean DEFAULT false,
    coordinator_group_id bigint
);

-- users: TABLE users_municipalities
CREATE TABLE users.users_municipalities (
    id bigint NOT NULL,
    municipality_id character varying(255),
    rejuntado_id bigint,
    user_auth_id character varying(255),
    name character varying(255),
    deleted timestamp(6) without time zone
);

-- followers: TABLE cat_committee
CREATE TABLE followers.cat_committee (
    id bigint DEFAULT nextval('followers.cat_committee_id_seq'::regclass) NOT NULL,
    electoral_box character varying(255),
    name character varying(255),
    promoters_goal integer,
    reference_auth_id character varying(255),
    cat_section bigint
);

-- followers: TABLE cat_family_role
CREATE TABLE followers.cat_family_role (
    id bigint DEFAULT nextval('followers.cat_family_role_id_seq'::regclass) NOT NULL,
    nombre character varying(255)
);

-- followers: TABLE cat_marital_status
CREATE TABLE followers.cat_marital_status (
    id bigint DEFAULT nextval('followers.cat_marital_status_id_seq'::regclass) NOT NULL,
    nombre character varying(255) NOT NULL
);

-- followers: TABLE cat_section
CREATE TABLE followers.cat_section (
    id bigint DEFAULT nextval('followers.cat_section_id_seq'::regclass) NOT NULL,
    nombre character varying(255)
);

-- followers: TABLE cat_sex
CREATE TABLE followers.cat_sex (
    id bigint DEFAULT nextval('followers.cat_sex_id_seq'::regclass) NOT NULL,
    nombre character varying(255)
);

-- followers: TABLE follower_mark
CREATE TABLE followers.follower_mark (
    id bigint DEFAULT nextval('followers.follower_mark_id_seq'::regclass) NOT NULL,
    follower_id bigint,
    mark_number integer,
    political_party_id bigint,
    polling_booth_id bigint,
    mark_flag boolean DEFAULT false,
    is_synchronized boolean,
    full_name character varying(255),
    federal_district character varying(255),
    federal_district_name character varying(255),
    local_district character varying(255),
    municipality character varying(255),
    municipality_id bigint,
    seccion_number character varying(255)
);

-- followers: TABLE followers_legacy
CREATE TABLE followers.followers_legacy (
    id bigint DEFAULT nextval('followers.followers_id_seq'::regclass) NOT NULL,
    birth_entity_id integer,
    birthdate timestamp(6) without time zone,
    cell_phone_number character varying(255),
    created timestamp(6) without time zone,
    curp character varying(255),
    deleted timestamp(6) without time zone,
    email character varying(255),
    ine character varying(255),
    last_name character varying(255),
    names character varying(255),
    section_number character varying(255),
    status character varying(255),
    sur_name character varying(255),
    updated timestamp(6) without time zone,
    user_create_id character varying(255),
    cat_family_role bigint,
    cat_marital_status bigint,
    cat_sex bigint,
    cell_phone_number_other character varying(255),
    committee_participation character varying(255),
    device_id character varying(255),
    external_number character varying(255),
    internal_number character varying(255),
    inviting_others_to_participate character varying(255),
    is_adult boolean,
    is_in_sync boolean,
    latitude character varying(255),
    longitude character varying(255),
    neighborhood character varying(255),
    number_of_people_invited character varying(255),
    pollin_station_representative_participation character varying(255),
    residence_entity_id integer,
    residence_municipality_id integer,
    section_number_ine character varying(255),
    street character varying(255),
    support_whatsapp character varying(255),
    supports_movement character varying(255),
    uuid character varying(255),
    validity integer,
    zip_code character varying(255),
    auth_id character varying(255),
    promoter_auth_id character varying(255),
    promoter_alt_auth_id character varying(255),
    vote_flag boolean DEFAULT false,
    last_name_resp character varying(255),
    sur_name_resp character varying(255),
    vote_flag_timestamp timestamp(6) without time zone,
    federal_district_id bigint,
    federal_district_name character varying(255),
    local_district_id bigint,
    local_district_name character varying(255),
    municipality_id bigint,
    municipality_name character varying(255),
    section_id bigint
);

-- followers: TABLE nw_hierarchy
CREATE TABLE followers.nw_hierarchy (
    follower_id bigint,
    promoter_auth_id character varying,
    promoter_alt_auth_id character varying,
    user_create_id character varying(100),
    status character varying(30),
    vote_flag boolean,
    vote_flag_timestamp timestamp with time zone
);

-- followers: TABLE followers_sirena
CREATE TABLE followers.followers_sirena (
    cell_phone_number_other character varying(255),
    committee_participation character varying(255),
    created timestamp(6) without time zone,
    curp character varying(255),
    deleted timestamp(6) without time zone,
    electoral_district integer,
    email character varying(255),
    entity integer,
    external_number character varying(255),
    ine character varying(255),
    internal_number character varying(255),
    inviting_others_to_participate character varying(255),
    is_adult character varying(255),
    last_name character varying(255),
    latitude character varying(255),
    longitude character varying(255),
    municipality_id integer,
    municipality_name character varying(255),
    names character varying(255),
    neighborhood_id character varying(255),
    number_of_people_invited character varying(255),
    phone character varying(255),
    pollin_station_representative_participation character varying(255),
    postal_code character varying(255),
    register_date timestamp(6) without time zone,
    seccion_id integer,
    seccion_number integer,
    street character varying(255),
    support_whatsapp character varying(255),
    supports_movement character varying(255),
    sur_name character varying(255),
    updated timestamp(6) without time zone,
    usuario character varying(255),
    oid bigint DEFAULT nextval('followers.followers_sirena_oid_seq'::regclass) NOT NULL,
    promoters_id bigint,
    register_time character varying(255),
    id bigint DEFAULT nextval('followers.followers_sirena_id_seq1'::regclass) NOT NULL,
    follower_id bigint,
    curp_status character varying(255),
    curp_message character varying(255),
    curp_message_operador character varying(255),
    curp_bandera boolean
);

-- followers: TABLE geo_distritos_federales
CREATE TABLE followers.geo_distritos_federales (
    id integer,
    cve_ent integer,
    nom_ent character varying(255),
    tipo integer,
    distrito_federal integer,
    ent_disf character varying(255),
    entidad_id integer
);

-- followers: TABLE geo_distritos_locales
CREATE TABLE followers.geo_distritos_locales (
    id integer,
    cve_ent integer,
    nom_ent character varying(255),
    distrito_local integer,
    ent_disl character varying(255),
    distrito_f integer,
    ent_disf character varying(255),
    entidad_id integer
);

-- followers: TABLE geo_municipios
CREATE TABLE followers.geo_municipios (
    id integer NOT NULL,
    geom character varying(255),
    cve_ent character varying(255),
    cve_munc character varying(255),
    cve_mun character varying(255),
    nom_ent character varying(255),
    nom_mun character varying(255),
    entidad_id integer
);

-- followers: TABLE geo_secciones
CREATE TABLE followers.geo_secciones (
    id integer NOT NULL,
    id_seccion character varying(255),
    cve_ent character varying(255),
    nom_ent character varying(255),
    cve_munc character varying(255),
    nom_mun character varying(255),
    distrito_f character varying(255),
    distrito_l character varying(255),
    seccion character varying(255),
    tipo character varying(255),
    mun_secc character varying(255),
    meta character varying(255),
    cve_mun character varying(255),
    municipio_id character varying(255),
    distrito_local_id character varying(255),
    distrito_federal_id character varying(255),
    entidad_id character varying(255),
    status character varying(255)
);

-- followers: TABLE polling_booths
CREATE TABLE followers.polling_booths (
    id integer NOT NULL,
    federal_district character varying(255),
    federal_district_name character varying(255),
    local_district character varying(255),
    municipality character varying(255),
    municipality_id character varying(255),
    polling_booth_id character varying(255),
    seccion_id character varying(255),
    seccion_number character varying(255),
    state character varying(255),
    state_id character varying(255),
    polling_booth_category_id character varying(255),
    polling_booth_type_id character varying(255),
    address character varying(255),
    approved_board character varying(255),
    approved_committee character varying(255),
    voters_list character varying(255),
    voters_registry character varying(255),
    polling_booth_address_type_id character varying(255),
    polling_booth_key character varying(255),
    polling_booth_number character varying(255),
    reference_address character varying(255),
    date_status_open character varying(255),
    polling_booth_status_id character varying(255),
    latitude character varying(255),
    longitude character varying(255),
    date_closed character varying(255)
);

-- followers: TABLE preregister
CREATE TABLE followers.preregister (
    id bigint NOT NULL,
    area_identificator integer,
    created timestamp(6) without time zone,
    date character varying(255),
    deleted timestamp(6) without time zone,
    device_id character varying(255),
    ine_back character varying(255),
    ine_front character varying(255),
    is_synchronized boolean,
    latitude character varying(255),
    locality character varying(255),
    longitude character varying(255),
    municipality character varying(255),
    user_allows_water_access character varying(255),
    user_available_hectares double precision,
    user_cattle_amount double precision,
    user_civil_status character varying(255),
    user_communal_land character varying(255),
    user_community character varying(255),
    user_contact_phone character varying(255),
    user_crates_amount double precision,
    user_curp character varying(255),
    user_email character varying(255),
    user_has_crates character varying(255),
    user_has_production_unit character varying(255),
    user_has_upp character varying(255),
    user_indigenous_ethnicity character varying(255),
    user_inside_number character varying(255),
    user_last_name character varying(255),
    user_locality character varying(255),
    user_monthly_income double precision,
    user_municipality character varying(255),
    user_name character varying(255),
    user_occupation character varying(255),
    user_outside_number character varying(255),
    user_phone character varying(255),
    user_second_last_name character varying(255),
    user_sex character varying(255),
    user_street character varying(255),
    user_subscribed character varying(255),
    user_type_of_property character varying(255),
    user_upp_bigger_than character varying(255),
    user_upp_measurement double precision,
    user_upp_number double precision,
    user_water_distance double precision,
    user_water_locality character varying(255),
    user_water_municipality character varying(255),
    user_water_name character varying(255),
    user_zip_code character varying(255),
    uuid character varying(255)
);

-- followers: TABLE users_seccions
CREATE TABLE followers.users_seccions (
    id integer,
    user_auth_id character varying(255),
    rejuntado_id integer,
    seccion_id integer,
    seccion_number character varying(255),
    seccion_geo_id integer,
    meta character varying(255),
    deleted character varying(255),
    municipality_id integer,
    limit_promoters character varying(255)
);

-- calendar: TABLE calendars
CREATE TABLE calendar.calendars (
    id bigint NOT NULL,
    auth_id character varying(255),
    color character varying(255),
    title character varying(255),
    visible boolean
);

-- calendar: TABLE events
CREATE TABLE calendar.events (
    id bigint NOT NULL,
    all_day boolean,
    auth_id character varying(255),
    calendar_id integer NOT NULL,
    description character varying(255),
    end_date timestamp(6) without time zone,
    is_first_instance boolean NOT NULL,
    recurrence character varying(255),
    recurring_event_id integer NOT NULL,
    start_date timestamp(6) without time zone,
    title character varying(255)
);

-- canvas_and_walls: TABLE canvas_and_walls
CREATE TABLE canvas_and_walls.canvas_and_walls (
    id bigint NOT NULL,
    coordinate_zone_id character varying(255),
    created timestamp(6) without time zone,
    date_of_placement timestamp(6) without time zone NOT NULL,
    deleted timestamp(6) without time zone,
    device_id character varying(255),
    latitude character varying(255),
    locality_id bigint,
    locality_name character varying(255),
    longitude character varying(255),
    municipality_id bigint NOT NULL,
    municipalityname character varying(255),
    path_evidence_photo character varying(255),
    reference_auth_id character varying(255),
    seccion_id bigint NOT NULL,
    seccion_number character varying(255),
    updated timestamp(6) without time zone,
    user_created_id character varying(255),
    uuid character varying(255),
    canvas_and_wall_category_id bigint,
    canvas_and_wall_proyect_id bigint
);

-- canvas_and_walls: TABLE canvas_and_walls_category
CREATE TABLE canvas_and_walls.canvas_and_walls_category (
    id bigint NOT NULL,
    name character varying(255)
);

-- canvas_and_walls: TABLE canvas_and_walls_goals
CREATE TABLE canvas_and_walls.canvas_and_walls_goals (
    id bigint NOT NULL,
    goal integer NOT NULL,
    municipality_id bigint NOT NULL,
    reference_auth_id character varying(255) NOT NULL,
    canvas_and_wall_category_id bigint,
    canvas_and_wall_proyect_id bigint
);

-- canvas_and_walls: TABLE canvas_and_walls_proyects
CREATE TABLE canvas_and_walls.canvas_and_walls_proyects (
    id bigint NOT NULL,
    name character varying(255)
);

-- canvas_and_walls: TABLE report_canvas_and_wall_municipality_entity
CREATE TABLE canvas_and_walls.report_canvas_and_wall_municipality_entity (
    id integer NOT NULL,
    canvas_and_wall_category_id integer,
    canvas_and_wall_proyect_id integer,
    category character varying(255),
    goal integer,
    municipality_id integer,
    proyect character varying(255),
    total bigint
);

-- canvas_and_walls: TABLE report_canvas_and_wall_referrer_entity
CREATE TABLE canvas_and_walls.report_canvas_and_wall_referrer_entity (
    id integer NOT NULL,
    canvas_and_wall_category_id integer,
    canvas_and_wall_proyect_id integer,
    category character varying(255),
    "coalesce" integer,
    municipality_id integer,
    proyect character varying(255),
    reference_auth_id character varying(255),
    total bigint
);

-- canvas_and_walls: TABLE report_canvas_and_wall_zone_entity
CREATE TABLE canvas_and_walls.report_canvas_and_wall_zone_entity (
    id integer NOT NULL,
    canvas_and_wall_category_id integer,
    canvas_and_wall_proyect_id integer,
    category character varying(255),
    coordinate_zone_id character varying(255),
    municipality_id integer,
    proyect character varying(255),
    reference_auth_id character varying(255),
    section_id integer,
    total bigint
);

-- canvas_and_walls: TABLE reportpdfdto
CREATE TABLE canvas_and_walls.reportpdfdto (
    id bigint NOT NULL,
    categoria character varying(255),
    coordinador_zona character varying(255),
    entidad character varying(255),
    fecha_registro character varying(255),
    latitud character varying(255),
    localidad character varying(255),
    longitud character varying(255),
    municipio character varying(255),
    path_evidence_photo character varying(255),
    proyecto character varying(255),
    referente character varying(255),
    seccion character varying(255)
);

-- canvas_and_walls: TABLE user_entity
CREATE TABLE canvas_and_walls.user_entity (
    id character varying(36) NOT NULL,
    email character varying(255),
    email_constraint character varying(255),
    email_verified boolean DEFAULT false NOT NULL,
    enabled boolean DEFAULT false NOT NULL,
    federation_link character varying(255),
    first_name character varying(255),
    last_name character varying(255),
    realm_id character varying(255),
    username character varying(255),
    created_timestamp bigint,
    service_account_client_link character varying(255),
    not_before integer DEFAULT 0 NOT NULL
);

-- citizen_issues: TABLE geo_localidades_juntos
CREATE TABLE citizen_issues.geo_localidades_juntos (
    municipio character varying(255),
    seccion character varying(255),
    localidad_id integer DEFAULT nextval('citizen_issues.geo_localidades_juntos_id_seq'::regclass) NOT NULL,
    localidad character varying(255),
    seccion_id integer,
    deleted timestamp(6) without time zone,
    id integer NOT NULL
);

-- citizen_issues: TABLE geo_municipios
CREATE TABLE citizen_issues.geo_municipios (
    id integer,
    cve_ent integer,
    cve_munc integer,
    cve_mun integer,
    nom_ent character varying(255),
    nom_mun character varying(255),
    entidad_id integer
);

-- citizen_issues: TABLE issues
CREATE TABLE citizen_issues.issues (
    id bigint NOT NULL,
    deleted_date timestamp(6) without time zone,
    full_name character varying(255),
    invoice character varying(255),
    locality_id integer,
    municipality_id integer,
    phone character varying(255),
    registration_date timestamp(6) without time zone,
    locality_other character varying(255)
);

-- citizen_issues: TABLE issues_categories
CREATE TABLE citizen_issues.issues_categories (
    id bigint NOT NULL,
    name character varying(255),
    issue_category_id bigint,
    issue_category_subject_id bigint
);

-- citizen_issues: TABLE issues_categories_subjects
CREATE TABLE citizen_issues.issues_categories_subjects (
    id bigint NOT NULL,
    name character varying(255)
);

-- citizen_issues: TABLE issues_details
CREATE TABLE citizen_issues.issues_details (
    id bigint NOT NULL,
    comments text,
    deleted_date timestamp(6) without time zone,
    registration_date timestamp(6) without time zone,
    user_create_id character varying(255),
    issue_id bigint,
    issue_category_id bigint,
    issue_solution_id bigint,
    issue_status_id bigint,
    issue_type_id bigint,
    birt_day character varying(255),
    curp character varying(255),
    last_name character varying(255),
    name character varying(255),
    sur_name character varying(255),
    urgent boolean,
    issue_solution_resource_tmp_id bigint,
    path_file character varying(255),
    uuid character varying(255),
    issues_solutions_documents_categories bigint
);

-- citizen_issues: TABLE issues_solutions
CREATE TABLE citizen_issues.issues_solutions (
    id bigint NOT NULL,
    comments character varying(255),
    follower_id bigint,
    quantity integer,
    solution_date timestamp(6) without time zone,
    issue_solution_provider_id bigint,
    issue_solution_resource_id bigint
);

-- citizen_issues: TABLE issues_solutions_documents_categories
CREATE TABLE citizen_issues.issues_solutions_documents_categories (
    id bigint NOT NULL,
    name character varying(255)
);

-- citizen_issues: TABLE issues_solutions_providers
CREATE TABLE citizen_issues.issues_solutions_providers (
    id bigint NOT NULL,
    name character varying(255)
);

-- citizen_issues: TABLE issues_solutions_resources
CREATE TABLE citizen_issues.issues_solutions_resources (
    id bigint NOT NULL,
    name character varying(255),
    issue_category_id bigint
);

-- citizen_issues: TABLE issues_status
CREATE TABLE citizen_issues.issues_status (
    id bigint NOT NULL,
    name character varying(255)
);

-- citizen_issues: TABLE issues_types
CREATE TABLE citizen_issues.issues_types (
    id bigint NOT NULL,
    name character varying(255)
);

-- document_management: TABLE departament
CREATE TABLE document_management.departament (
    id bigint NOT NULL,
    names character varying(255)
);

-- document_management: TABLE document
CREATE TABLE document_management.document (
    id bigint NOT NULL,
    body character varying(255),
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    document_date timestamp(6) without time zone,
    expiration_date timestamp(6) without time zone,
    received_date timestamp(6) without time zone,
    subject character varying(255),
    updated timestamp(6) without time zone,
    receiver_id bigint,
    sender_id bigint
);

-- document_management: TABLE person
CREATE TABLE document_management.person (
    id bigint NOT NULL,
    address character varying(255),
    cell_phone_number character varying(255),
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    email character varying(255),
    last_name character varying(255),
    names character varying(255),
    sur_name character varying(255),
    updated timestamp(6) without time zone
);

-- document_management: TABLE position
CREATE TABLE document_management."position" (
    id bigint NOT NULL,
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    name character varying(255),
    position_key character varying(255),
    status_id integer,
    updated timestamp(6) without time zone,
    departament_id bigint,
    position_type_id bigint
);

-- document_management: TABLE position_person
CREATE TABLE document_management.position_person (
    id bigint NOT NULL,
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    updated timestamp(6) without time zone,
    person_id bigint,
    position_id bigint
);

-- document_management: TABLE position_person_status
CREATE TABLE document_management.position_person_status (
    id bigint NOT NULL,
    name character varying(255)
);

-- document_management: TABLE position_type
CREATE TABLE document_management.position_type (
    id bigint NOT NULL,
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    name character varying(255),
    updated timestamp(6) without time zone
);

-- document_management: TABLE receiver
CREATE TABLE document_management.receiver (
    id bigint NOT NULL,
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    updated timestamp(6) without time zone,
    position_person_id bigint
);

-- document_management: TABLE sender
CREATE TABLE document_management.sender (
    id bigint NOT NULL,
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    updated timestamp(6) without time zone,
    position_person_id bigint
);

-- elections: TABLE candidates_party_associations
CREATE TABLE elections.candidates_party_associations (
    id bigint NOT NULL,
    candidate_id bigint,
    political_party_id bigint,
    sort_order integer
);

-- elections: TABLE candidates
CREATE TABLE elections.candidates (
    id bigint NOT NULL,
    last_names character varying(255),
    names character varying(255),
    sur_names character varying(255),
    election_id bigint,
    candidate_path character varying(255)
);

-- elections: TABLE elections
CREATE TABLE elections.elections (
    id bigint NOT NULL,
    name character varying(255),
    status character varying(255),
    election_position_id bigint,
    election_type_id bigint,
    election_year_id bigint,
    layer_federal_district character varying(255),
    layer_local_district character varying(255),
    layer_seccion character varying(255),
    voter_list integer
);

-- elections: TABLE elections_federal_district
CREATE TABLE elections.elections_federal_district (
    id bigint NOT NULL,
    candidate_id bigint,
    election_id bigint,
    federal_district_id bigint
);

-- elections: TABLE elections_local_district
CREATE TABLE elections.elections_local_district (
    id bigint NOT NULL,
    candidate_id bigint,
    election_id bigint,
    local_district_id bigint
);

-- elections: TABLE elections_municipality
CREATE TABLE elections.elections_municipality (
    id bigint NOT NULL,
    candidate_id bigint,
    election_id bigint,
    municipality_id bigint
);

-- elections: TABLE elections_positions
CREATE TABLE elections.elections_positions (
    id bigint NOT NULL,
    name character varying(255),
    sort_order integer
);

-- elections: TABLE elections_types
CREATE TABLE elections.elections_types (
    id bigint NOT NULL,
    name character varying(255)
);

-- elections: TABLE elections_voter_list
CREATE TABLE elections.elections_voter_list (
    id bigint NOT NULL,
    election_id integer,
    user_auth_id character varying(255),
    voter_list integer
);

-- elections: TABLE elections_years
CREATE TABLE elections.elections_years (
    id bigint NOT NULL,
    name character varying(255)
);

-- elections: TABLE fast_survey
CREATE TABLE elections.fast_survey (
    id bigint NOT NULL,
    balancan character varying(255),
    canditate character varying(255),
    cardenas character varying(255),
    centla character varying(255),
    centro character varying(255),
    comalcalco character varying(255),
    cunduacan character varying(255),
    date_collected timestamp(6) without time zone,
    duration integer,
    emiliano_zapata character varying(255),
    gobernatura character varying(255),
    huimanguillo character varying(255),
    jalapa character varying(255),
    jalpa_de_mendez character varying(255),
    jonuta character varying(255),
    latitud character varying(255),
    longitude character varying(255),
    macuspana character varying(255),
    nacajuca character varying(255),
    paraiso character varying(255),
    survey_end_time timestamp(6) without time zone,
    tacotalpa character varying(255),
    teapa character varying(255),
    tenosique character varying(255),
    upload_time timestamp(6) without time zone,
    usuario character varying(255)
);

-- elections: TABLE geo_secciones
CREATE TABLE elections.geo_secciones (
    id integer,
    id_seccion integer,
    cve_ent character varying(255),
    nom_ent character varying(255),
    cve_munc character varying(255),
    nom_mun character varying(255),
    distrito_f character varying(255),
    distrito_l character varying(255),
    seccion character varying(255),
    tipo character varying(255),
    mun_secc character varying(255),
    meta character varying(255),
    cve_mun character varying(255),
    municipio_id integer,
    distrito_local_id integer,
    distrito_federal_id integer,
    entidad_id integer,
    status character varying(255)
);

-- elections: TABLE political_parties
CREATE TABLE elections.political_parties (
    id bigint NOT NULL,
    acronym character varying(255),
    logo character varying(255),
    name character varying(255),
    political_party_type_id bigint,
    softorder integer
);

-- elections: TABLE political_parties_type
CREATE TABLE elections.political_parties_type (
    id bigint NOT NULL,
    name character varying(255)
);

-- elections: TABLE polling_booths
CREATE TABLE elections.polling_booths (
    id bigint NOT NULL,
    federal_district integer,
    federal_district_name character varying(255),
    local_district integer,
    municipality character varying(255),
    municipality_id integer,
    polling_booth_id character varying(255),
    seccion_id integer,
    seccion_number integer,
    state character varying(255),
    state_id integer,
    polling_booth_category_id bigint,
    polling_booth_type_id bigint,
    address character varying(255),
    approved_board character varying(255),
    approved_committee character varying(255),
    voters_list integer,
    voters_registry integer,
    polling_booth_address_type_id bigint,
    polling_booth_key character varying(255),
    polling_booth_number text,
    reference_address character varying(255),
    date_status_open timestamp(6) without time zone,
    polling_booth_status_id bigint,
    latitude character varying(255),
    longitude character varying(255),
    date_closed timestamp(6) without time zone
);

-- elections: TABLE polling_booths_address_type
CREATE TABLE elections.polling_booths_address_type (
    id bigint NOT NULL,
    acronym character varying(255),
    name character varying(255)
);

-- elections: TABLE polling_booths_categories
CREATE TABLE elections.polling_booths_categories (
    id bigint NOT NULL,
    name character varying(255)
);

-- elections: TABLE polling_booths_election_association
CREATE TABLE elections.polling_booths_election_association (
    id bigint NOT NULL,
    polling_booth_id integer,
    election_id integer,
    is_synchronized boolean,
    path_sheet character varying(255),
    path_sheet_created timestamp(6) without time zone,
    path_sheet_updated timestamp(6) without time zone,
    is_validated_sheet integer
);

-- elections: TABLE polling_booths_issues
CREATE TABLE elections.polling_booths_issues (
    id bigint NOT NULL,
    polling_booth_id bigint,
    sub_category_id integer,
    description character varying(255),
    is_synchronized boolean,
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    updated timestamp(6) without time zone
);

-- elections: TABLE polling_booths_issues_categories
CREATE TABLE elections.polling_booths_issues_categories (
    id bigint NOT NULL,
    name character varying(255)
);

-- elections: TABLE polling_booths_issues_sub_categories
CREATE TABLE elections.polling_booths_issues_sub_categories (
    id bigint NOT NULL,
    name text,
    category_id bigint
);

-- elections: TABLE polling_booths_status
CREATE TABLE elections.polling_booths_status (
    id bigint NOT NULL,
    name character varying(255)
);

-- elections: TABLE polling_booths_types
CREATE TABLE elections.polling_booths_types (
    id bigint NOT NULL,
    acronym character varying(255),
    name character varying(255)
);

-- elections: TABLE status_validate
CREATE TABLE elections.status_validate (
    id bigint NOT NULL,
    name character varying(255)
);

-- elections_results: TABLE election_result
CREATE TABLE elections_results.election_result (
    id bigint NOT NULL,
    candidate_party_association_id bigint,
    district_federal_id bigint,
    district_federal_name character varying(255),
    district_local_id bigint,
    district_local_name character varying(255),
    election_association_id bigint,
    election_name character varying(255),
    municipality_id bigint,
    municipality_name character varying(255),
    political_party_id bigint,
    political_party_name character varying(255),
    polling_booth_description character varying(255),
    result integer,
    seccion_id bigint,
    seccion_number bigint,
    candidate_id bigint
);

-- elections_results: TABLE election_result_sheets
CREATE TABLE elections_results.election_result_sheets (
    id bigint NOT NULL,
    candidate_party_association_id bigint,
    district_federal_id bigint,
    district_federal_name character varying(255),
    district_local_id bigint,
    district_local_name character varying(255),
    election_association_id bigint,
    election_name character varying(255),
    municipality_id bigint,
    municipality_name character varying(255),
    political_party_id bigint,
    political_party_name character varying(255),
    polling_booth_description character varying(255),
    result integer,
    seccion_id bigint,
    seccion_number bigint,
    candidate_id bigint
);

-- elections_structure: TABLE cat_sex
CREATE TABLE elections_structure.cat_sex (
    id bigint NOT NULL,
    nombre character varying(255) NOT NULL
);

-- elections_structure: TABLE pollings_representatives
CREATE TABLE elections_structure.pollings_representatives (
    id bigint NOT NULL,
    birthdate timestamp(6) without time zone,
    cell_phone_number character varying(255),
    cell_phone_number_other character varying(255),
    created timestamp(6) without time zone,
    curp character varying(255),
    deleted timestamp(6) without time zone,
    email character varying(255),
    external_number character varying(255),
    ine character varying(255),
    internal_number character varying(255),
    last_name character varying(255),
    names character varying(255),
    neighborhood character varying(255),
    polling_booth_description character varying(255),
    polling_booth_id integer,
    residence_entity_id integer,
    residence_municipality_id integer,
    section_id character varying(255),
    section_number character varying(255),
    status character varying(255),
    street character varying(255),
    support_whatsapp character varying(255),
    sur_name character varying(255),
    updated timestamp(6) without time zone,
    user_create_id character varying(255),
    validity integer,
    zip_code character varying(255),
    cat_sex bigint,
    auth_id character varying(255),
    polling_representative_type_id integer
);

-- elections_structure: TABLE pollings_representatives_coordinators
CREATE TABLE elections_structure.pollings_representatives_coordinators (
    id bigint NOT NULL,
    polling_representative_general_id bigint,
    polling_representative_coordinator_id bigint
);

-- elections_structure: TABLE pollings_representatives_types
CREATE TABLE elections_structure.pollings_representatives_types (
    id bigint NOT NULL,
    name character varying(255),
    acronym character varying(255)
);

-- elections_structure: TABLE routes
CREATE TABLE elections_structure.routes (
    id bigint NOT NULL,
    key integer,
    name character varying(255),
    polling_representative_general_id bigint
);

-- elections_structure: TABLE routes_assignment
CREATE TABLE elections_structure.routes_assignment (
    id bigint NOT NULL,
    polling_booth_id bigint,
    route_id bigint
);

-- electoral_history: TABLE ballot_option
CREATE TABLE electoral_history.ballot_option (
    id integer NOT NULL,
    election_id integer NOT NULL,
    raw_label character varying(120) NOT NULL,
    kind character varying(15) NOT NULL,
    party_codes character varying(20)[] NOT NULL,
    label character varying(120) NOT NULL,
    CONSTRAINT ballot_option_kind_check CHECK (((kind)::text = ANY ((ARRAY['PARTY'::character varying, 'COMBINATION'::character varying, 'INDEPENDENT'::character varying, 'DERIVED_TOTAL'::character varying])::text[])))
);

-- electoral_history: TABLE bloc
CREATE TABLE electoral_history.bloc (
    code character varying(20) NOT NULL,
    name character varying(80) NOT NULL,
    color character(7)
);

-- electoral_history: TABLE bloc_party
CREATE TABLE electoral_history.bloc_party (
    year smallint NOT NULL,
    party_code character varying(20) NOT NULL,
    bloc_code character varying(20) NOT NULL
);

-- electoral_history: TABLE candidacy
CREATE TABLE electoral_history.candidacy (
    id integer NOT NULL,
    election_id integer NOT NULL,
    territory character varying(10),
    code character varying(40) NOT NULL,
    name character varying(160) NOT NULL,
    party_codes character varying(20)[] NOT NULL
);

-- electoral_history: TABLE election
CREATE TABLE electoral_history.election (
    id integer NOT NULL,
    year smallint NOT NULL,
    office character varying(20) NOT NULL,
    scope character varying(20) NOT NULL,
    election_date date NOT NULL,
    source_id integer NOT NULL,
    source_member text,
    CONSTRAINT election_office_check CHECK (((office)::text = ANY ((ARRAY['PRESIDENT'::character varying, 'SENATOR'::character varying, 'FEDERAL_DEPUTY'::character varying, 'GOVERNOR'::character varying, 'LOCAL_DEPUTY'::character varying, 'MAYOR'::character varying])::text[]))),
    CONSTRAINT election_scope_check CHECK (((scope)::text = ANY ((ARRAY['STATE'::character varying, 'FEDERAL_DISTRICT'::character varying, 'LOCAL_DISTRICT'::character varying, 'MUNICIPALITY'::character varying])::text[])))
);

-- electoral_history: TABLE municipality
CREATE TABLE electoral_history.municipality (
    cve smallint NOT NULL,
    name character varying(60) NOT NULL
);

-- electoral_history: TABLE polling_station
CREATE TABLE electoral_history.polling_station (
    id integer NOT NULL,
    election_id integer NOT NULL,
    station_key character varying(120) NOT NULL,
    section integer NOT NULL,
    district character varying(10) NOT NULL,
    municipality_cve smallint NOT NULL,
    station_type character varying(10) NOT NULL,
    nominal_list integer NOT NULL,
    total_votes integer NOT NULL,
    null_votes integer NOT NULL,
    non_registered_votes integer NOT NULL,
    counted boolean NOT NULL,
    source_row integer NOT NULL,
    local_district smallint,
    federal_district smallint
);

-- electoral_history: TABLE station_vote
CREATE TABLE electoral_history.station_vote (
    station_id integer NOT NULL,
    option_id integer NOT NULL,
    votes integer NOT NULL
);

-- electoral_history: TABLE official_total
CREATE TABLE electoral_history.official_total (
    election_id integer NOT NULL,
    municipality_cve smallint,
    code character varying(40) NOT NULL,
    value bigint NOT NULL,
    district character varying(10)
);

-- electoral_history: TABLE party
CREATE TABLE electoral_history.party (
    code character varying(20) NOT NULL,
    name character varying(120) NOT NULL,
    color character(7)
);

-- electoral_history: TABLE section_municipality
CREATE TABLE electoral_history.section_municipality (
    year smallint NOT NULL,
    section integer NOT NULL,
    municipality_cve smallint NOT NULL,
    local_district smallint,
    federal_district smallint
);

-- electoral_history: TABLE source
CREATE TABLE electoral_history.source (
    id integer NOT NULL,
    publisher character varying(10) NOT NULL,
    url text NOT NULL,
    file_name text NOT NULL,
    sha256 character(64) NOT NULL,
    cut_label text NOT NULL,
    downloaded_at date NOT NULL,
    notes text,
    CONSTRAINT source_publisher_check CHECK (((publisher)::text = ANY ((ARRAY['INE'::character varying, 'IEPCT'::character varying])::text[])))
);

-- logfile: TABLE goals
CREATE TABLE logfile.goals (
    id bigint NOT NULL,
    description character varying(255),
    goal_number character varying(255),
    ods_id bigint
);

-- logfile: TABLE indicators
CREATE TABLE logfile.indicators (
    id bigint NOT NULL,
    description character varying(255),
    goal_id bigint,
    indicator_number character varying(255)
);

-- logfile: TABLE instruments
CREATE TABLE logfile.instruments (
    id bigint NOT NULL,
    acronym character varying(255),
    article character varying(255),
    description character varying(255),
    indicator_id bigint,
    name character varying(255)
);

-- logfile: TABLE logfile
CREATE TABLE logfile.logfile (
    id bigint NOT NULL,
    accion_id bigint,
    action character varying(255),
    created_at timestamp(6) without time zone,
    data character varying(255),
    ip character varying(255),
    message character varying(255),
    module character varying(255),
    quantity integer,
    user_auth_id character varying(255)
);

-- logfile: TABLE ods
CREATE TABLE logfile.ods (
    id bigint NOT NULL,
    color character varying(255),
    description character varying(255),
    image character varying(255),
    objective character varying(255)
);

-- survey: TABLE localidades
CREATE TABLE survey.localidades (
    cvegeo integer,
    cve_ent integer,
    cve_mun integer,
    cve_loc integer,
    nomgeo character varying(255),
    ambito character varying(255)
);

-- survey: TABLE secciones_ok
CREATE TABLE survey.secciones_ok (
    municipio character varying(255),
    seccion bigint,
    num_viviendas integer,
    seccion_id integer,
    id integer NOT NULL
);

-- survey: TABLE status
CREATE TABLE survey.status (
    id bigint DEFAULT nextval('survey.status_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey
CREATE TABLE survey.survey (
    id bigint DEFAULT nextval('survey.survey_id_seq'::regclass) NOT NULL,
    created timestamp(6) without time zone,
    cultural_activity_participation character varying(255),
    deleted timestamp(6) without time zone,
    device_id character varying(255),
    exterior_number character varying(255),
    family_numbers character varying(255),
    folio_house character varying(255),
    housing_other character varying(255),
    interior_number character varying(255),
    knowledge_cultural_activity character varying(255),
    latitude character varying(255),
    locality character varying(255),
    locality_id integer,
    locality_other character varying(255),
    longitude character varying(255),
    municipality character varying(255),
    municipality_id integer,
    other_type_cultural_activity character varying(255),
    path_photo_facade character varying(255),
    path_photo_facade_qr character varying(255),
    path_photo_ine_adverse character varying(255),
    path_photo_ine_reverse character varying(255),
    postal_code character varying(255),
    reference character varying(255),
    status_change integer,
    street character varying(255),
    survey_date timestamp(6) without time zone,
    updated timestamp(6) without time zone,
    user_auth_id character varying(255),
    type_cultural_activity bigint,
    survey_housing_id bigint,
    survey_housing_condition_id bigint,
    survey_housing_document_id bigint,
    survey_housing_material_floor_id bigint,
    survey_housing_material_roof_id bigint,
    survey_housing_material_wall_id bigint,
    type_cultural_activity_id bigint,
    survey_user_id bigint NOT NULL,
    survey_user_folio_id bigint NOT NULL,
    section_id bigint,
    section_number bigint
);

-- survey: TABLE survey_annex
CREATE TABLE survey.survey_annex (
    id bigint DEFAULT nextval('survey.survey_annex_id_seq'::regclass) NOT NULL,
    which_certificate character varying(255),
    another_cause character varying(255),
    causes_disability integer,
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    desabililty_information integer,
    desability_origin integer,
    desability_type integer,
    ethnic_or_afromexican_group character varying(255),
    hasadisability character varying(255),
    health_certificate character varying(255),
    last_name_aux character varying(255),
    names_aux character varying(255),
    other_relation_ship character varying(255),
    other_speak character varying(255),
    phone_number character varying(255),
    relationship character varying(255),
    requesting_auxiliar character varying(255),
    requesting_document character varying(255),
    requiries_help character varying(255),
    speaks_indigenous_language character varying(255),
    sur_name_aux character varying(255),
    updated timestamp(6) without time zone,
    which_group character varying(255),
    requesting_auxiliar_curp character varying(255),
    requesting_auxiliar_ine character varying(255),
    requesting_auxiliar_minutes character varying(255),
    requesting_auxiliar_proof_address character varying(255),
    requesting_document_curp character varying(255),
    requesting_document_ine character varying(255),
    requesting_document_minutes character varying(255),
    requesting_document_proof_address character varying(255),
    survey_id bigint,
    survey_causes_of_disability_id bigint,
    survey_disability_information_id bigint,
    survey_indigenous_language_id bigint,
    survey_origin_of_disability_id bigint,
    survey_requesting_or_auxiliar_document_id bigint,
    survey_tutor_or_assistant_id bigint,
    survey_type_of_disability_id bigint,
    path_file_address_applicant character varying(255),
    path_file_address_assistant character varying(255),
    path_file_birth_certificate_applicant character varying(255),
    path_file_birth_certificate_assistant character varying(255),
    path_file_curp_applicant character varying(255),
    path_file_curp_assistant character varying(255),
    path_file_identification_applicant character varying(255),
    path_file_identification_assistant character varying(255),
    survey_family_relationship_id bigint,
    survey_person_id bigint
);

-- survey: TABLE survey_causes_of_disability
CREATE TABLE survey.survey_causes_of_disability (
    id bigint DEFAULT nextval('survey.survey_causes_of_disability_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_cultural_activity
CREATE TABLE survey.survey_cultural_activity (
    id bigint DEFAULT nextval('survey.survey_cultural_activity_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_disability
CREATE TABLE survey.survey_disability (
    id bigint DEFAULT nextval('survey.survey_disability_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_disability_information
CREATE TABLE survey.survey_disability_information (
    id bigint DEFAULT nextval('survey.survey_disability_information_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_family_relationship
CREATE TABLE survey.survey_family_relationship (
    id bigint DEFAULT nextval('survey.survey_family_relationship_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_federal_program
CREATE TABLE survey.survey_federal_program (
    id bigint DEFAULT nextval('survey.survey_federal_program_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_housing
CREATE TABLE survey.survey_housing (
    id bigint DEFAULT nextval('survey.survey_housing_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_housing_condition
CREATE TABLE survey.survey_housing_condition (
    id bigint DEFAULT nextval('survey.survey_housing_condition_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_housing_document
CREATE TABLE survey.survey_housing_document (
    id bigint DEFAULT nextval('survey.survey_housing_document_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_housing_material_floor
CREATE TABLE survey.survey_housing_material_floor (
    id bigint DEFAULT nextval('survey.survey_housing_material_floor_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_housing_material_roof
CREATE TABLE survey.survey_housing_material_roof (
    id bigint DEFAULT nextval('survey.survey_housing_material_roof_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_housing_material_wall
CREATE TABLE survey.survey_housing_material_wall (
    id bigint DEFAULT nextval('survey.survey_housing_material_wall_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_housing_service_list
CREATE TABLE survey.survey_housing_service_list (
    survey_housing_services_id bigint NOT NULL,
    survey_id bigint NOT NULL,
    id integer DEFAULT nextval('survey.survey_housing_service_list_id_seq'::regclass) NOT NULL
);

-- survey: TABLE survey_housing_services
CREATE TABLE survey.survey_housing_services (
    id bigint DEFAULT nextval('survey.survey_housing_services_id_seq'::regclass) NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_indigenous_language
CREATE TABLE survey.survey_indigenous_language (
    id bigint NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_job
CREATE TABLE survey.survey_job (
    id bigint NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_origin_of_disability
CREATE TABLE survey.survey_origin_of_disability (
    id bigint NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_person
CREATE TABLE survey.survey_person (
    id bigint NOT NULL,
    afro_mexican_group character varying(255),
    curp character varying(255),
    date_of_birth character varying(255),
    deleted character varying(255),
    ethnic_group character varying(255),
    gender character varying(255),
    identification_document character varying(255),
    income integer,
    is_disabled character varying(255),
    is_head_of_household boolean,
    landline_phone character varying(255),
    last_name character varying(255),
    mobile_phone character varying(255),
    names character varying(255),
    no_identification_document character varying(255),
    other_disabled_name character varying(255),
    other_survey_federal_program character varying(255),
    other_survey_job character varying(255),
    other_survey_profession character varying(255),
    own_business character varying(255),
    sur_name character varying(255),
    time_activity character varying(255),
    survey_disable_id bigint,
    survey_id bigint,
    survey_family_relationship_id bigint,
    survey_federal_program_id bigint,
    survey_job_id bigint,
    survey_profession_id bigint
);

-- survey: TABLE survey_profession
CREATE TABLE survey.survey_profession (
    id bigint NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_requesting_or_auxiliary_document
CREATE TABLE survey.survey_requesting_or_auxiliary_document (
    id bigint NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_section_goal
CREATE TABLE survey.survey_section_goal (
    id bigint NOT NULL,
    goal bigint,
    municipality_id bigint,
    municipality_name character varying(255),
    section_id integer,
    section_number integer,
    total_homes bigint
);

-- survey: TABLE survey_tutor_or_assistant
CREATE TABLE survey.survey_tutor_or_assistant (
    id bigint NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_type_of_disability
CREATE TABLE survey.survey_type_of_disability (
    id bigint NOT NULL,
    name character varying(255)
);

-- survey: TABLE survey_user
CREATE TABLE survey.survey_user (
    id bigint NOT NULL,
    auth_id character varying(255),
    device_id character varying(255),
    municipality_name character varying(255),
    municipaly_id bigint,
    name character varying(255),
    survey_municipality_auth_id character varying(255),
    user_name character varying(255),
    user_person_id bigint
);

-- survey: TABLE survey_user_folio
CREATE TABLE survey.survey_user_folio (
    id bigint NOT NULL,
    comment character varying(255),
    folio character varying(255),
    survey_user_id integer
);

-- survey: TABLE survey_user_section
CREATE TABLE survey.survey_user_section (
    id bigint NOT NULL,
    is_owner boolean,
    section_id integer,
    section_number integer,
    survey_user_id bigint
);

-- visits: TABLE reportpdfdto
CREATE TABLE visits.reportpdfdto (
    id bigint NOT NULL,
    categoria character varying(255),
    coordinador_zona character varying(255),
    entidad character varying(255),
    fecha_registro character varying(255),
    latitud character varying(255),
    localidad character varying(255),
    longitud character varying(255),
    municipio character varying(255),
    path_evidence_photo character varying(255),
    proyecto character varying(255),
    referente character varying(255),
    seccion character varying(255)
);

-- visits: TABLE visited
CREATE TABLE visits.visited (
    id bigint NOT NULL,
    coordinate_zone_id character varying(255),
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    locality_id bigint NOT NULL,
    municipality_id bigint NOT NULL,
    number_of_visited integer,
    reference_auth_id character varying(255),
    seccion_id bigint NOT NULL,
    updated timestamp(6) without time zone,
    user_created_id character varying(255),
    visit_date timestamp(6) without time zone NOT NULL
);

-- visits: TABLE visited_number
CREATE TABLE visits.visited_number (
    id bigint NOT NULL,
    created timestamp(6) without time zone,
    deleted timestamp(6) without time zone,
    updated timestamp(6) without time zone,
    visited_number integer NOT NULL,
    visited_people_id bigint NOT NULL
);

-- visits: TABLE visited_people
CREATE TABLE visits.visited_people (
    id bigint NOT NULL,
    adults_in_home integer,
    age_of_the_person_who_opens_the_door integer,
    deleted timestamp(6) without time zone,
    delivered_gazette boolean,
    external_number character varying(255) NOT NULL,
    is_related boolean,
    last_name character varying(255),
    latitude character varying(255) NOT NULL,
    longitude character varying(255) NOT NULL,
    names character varying(255),
    notes character varying(255),
    open_the_door boolean NOT NULL,
    phone character varying(255),
    sex_of_the_one_who_opens_the_door character varying(255),
    street character varying(255) NOT NULL,
    sur_name character varying(255),
    uuid character varying(255),
    visit_number integer,
    visited_id bigint
);


-- ===== owned =====

-- geography: SEQUENCE OWNED BY geo_safe_houses_id_seq
ALTER SEQUENCE geography.geo_safe_houses_id_seq OWNED BY geography.geo_safe_houses.id;

-- geography: SEQUENCE OWNED BY localidades_secciones_id_seq
ALTER SEQUENCE geography.localidades_secciones_id_seq OWNED BY geography.localidades_secciones.id;

-- geography: DEFAULT geo_safe_houses id
ALTER TABLE ONLY geography.geo_safe_houses ALTER COLUMN id SET DEFAULT nextval('geography.geo_safe_houses_id_seq'::regclass);

-- geography: DEFAULT localidades_secciones id
ALTER TABLE ONLY geography.localidades_secciones ALTER COLUMN id SET DEFAULT nextval('geography.localidades_secciones_id_seq'::regclass);

-- structure: SEQUENCE OWNED BY assignment_id_seq
ALTER SEQUENCE structure.assignment_id_seq OWNED BY structure.assignment.id;

-- structure: SEQUENCE OWNED BY change_log_id_seq
ALTER SEQUENCE structure.change_log_id_seq OWNED BY structure.change_log.id;

-- structure: SEQUENCE OWNED BY membership_id_seq
ALTER SEQUENCE structure.membership_id_seq OWNED BY structure.membership.id;

-- structure: SEQUENCE OWNED BY person_id_seq
ALTER SEQUENCE structure.person_id_seq OWNED BY structure.person.id;

-- structure: SEQUENCE OWNED BY position_id_seq
ALTER SEQUENCE structure.position_id_seq OWNED BY structure."position".id;

-- structure: SEQUENCE OWNED BY campaign_election_id_seq
ALTER SEQUENCE structure.campaign_election_id_seq OWNED BY structure.campaign_election.id;

-- structure: SEQUENCE OWNED BY campaign_id_seq
ALTER SEQUENCE structure.campaign_id_seq OWNED BY structure.campaign.id;

-- structure: SEQUENCE OWNED BY structure_id_seq
ALTER SEQUENCE structure.structure_id_seq OWNED BY structure.structure.id;

-- structure: SEQUENCE OWNED BY territory_id_seq
ALTER SEQUENCE structure.territory_id_seq OWNED BY structure.territory.id;

-- structure: DEFAULT assignment id
ALTER TABLE ONLY structure.assignment ALTER COLUMN id SET DEFAULT nextval('structure.assignment_id_seq'::regclass);

-- structure: DEFAULT campaign id
ALTER TABLE ONLY structure.campaign ALTER COLUMN id SET DEFAULT nextval('structure.campaign_id_seq'::regclass);

-- structure: DEFAULT campaign_election id
ALTER TABLE ONLY structure.campaign_election ALTER COLUMN id SET DEFAULT nextval('structure.campaign_election_id_seq'::regclass);

-- structure: DEFAULT change_log id
ALTER TABLE ONLY structure.change_log ALTER COLUMN id SET DEFAULT nextval('structure.change_log_id_seq'::regclass);

-- structure: DEFAULT membership id
ALTER TABLE ONLY structure.membership ALTER COLUMN id SET DEFAULT nextval('structure.membership_id_seq'::regclass);

-- structure: DEFAULT person id
ALTER TABLE ONLY structure.person ALTER COLUMN id SET DEFAULT nextval('structure.person_id_seq'::regclass);

-- structure: DEFAULT position id
ALTER TABLE ONLY structure."position" ALTER COLUMN id SET DEFAULT nextval('structure.position_id_seq'::regclass);

-- structure: DEFAULT structure id
ALTER TABLE ONLY structure.structure ALTER COLUMN id SET DEFAULT nextval('structure.structure_id_seq'::regclass);

-- structure: DEFAULT territory id
ALTER TABLE ONLY structure.territory ALTER COLUMN id SET DEFAULT nextval('structure.territory_id_seq'::regclass);

-- users: SEQUENCE OWNED BY coordinators_group_id_seq
ALTER SEQUENCE users.coordinators_group_id_seq OWNED BY users.coordinators_group.id;

-- users: SEQUENCE OWNED BY menus_groups_pk_seq
ALTER SEQUENCE users.menus_groups_pk_seq OWNED BY users.menus_groups.pk;

-- users: SEQUENCE OWNED BY menus_pk_seq
ALTER SEQUENCE users.menus_pk_seq OWNED BY users.menus.pk;

-- users: SEQUENCE OWNED BY menus_roles_id_seq
ALTER SEQUENCE users.menus_roles_id_seq OWNED BY users.menus_roles.id;

-- users: SEQUENCE OWNED BY users_id_seq
ALTER SEQUENCE users.users_id_seq OWNED BY users.users.id;

-- users: SEQUENCE OWNED BY users_municipalities_id_seq
ALTER SEQUENCE users.users_municipalities_id_seq OWNED BY users.users_municipalities.id;

-- users: DEFAULT coordinators_group id
ALTER TABLE ONLY users.coordinators_group ALTER COLUMN id SET DEFAULT nextval('users.coordinators_group_id_seq'::regclass);

-- users: DEFAULT menus pk
ALTER TABLE ONLY users.menus ALTER COLUMN pk SET DEFAULT nextval('users.menus_pk_seq'::regclass);

-- users: DEFAULT menus_groups pk
ALTER TABLE ONLY users.menus_groups ALTER COLUMN pk SET DEFAULT nextval('users.menus_groups_pk_seq'::regclass);

-- users: DEFAULT menus_roles id
ALTER TABLE ONLY users.menus_roles ALTER COLUMN id SET DEFAULT nextval('users.menus_roles_id_seq'::regclass);

-- users: DEFAULT users id
ALTER TABLE ONLY users.users ALTER COLUMN id SET DEFAULT nextval('users.users_id_seq'::regclass);

-- users: DEFAULT users_municipalities id
ALTER TABLE ONLY users.users_municipalities ALTER COLUMN id SET DEFAULT nextval('users.users_municipalities_id_seq'::regclass);

-- followers: SEQUENCE OWNED BY preregister_id_seq
ALTER SEQUENCE followers.preregister_id_seq OWNED BY followers.preregister.id;

-- followers: DEFAULT preregister id
ALTER TABLE ONLY followers.preregister ALTER COLUMN id SET DEFAULT nextval('followers.preregister_id_seq'::regclass);

-- calendar: SEQUENCE OWNED BY calendars_id_seq
ALTER SEQUENCE calendar.calendars_id_seq OWNED BY calendar.calendars.id;

-- calendar: SEQUENCE OWNED BY events_id_seq
ALTER SEQUENCE calendar.events_id_seq OWNED BY calendar.events.id;

-- calendar: DEFAULT calendars id
ALTER TABLE ONLY calendar.calendars ALTER COLUMN id SET DEFAULT nextval('calendar.calendars_id_seq'::regclass);

-- calendar: DEFAULT events id
ALTER TABLE ONLY calendar.events ALTER COLUMN id SET DEFAULT nextval('calendar.events_id_seq'::regclass);

-- canvas_and_walls: SEQUENCE OWNED BY canvas_and_walls_category_id_seq
ALTER SEQUENCE canvas_and_walls.canvas_and_walls_category_id_seq OWNED BY canvas_and_walls.canvas_and_walls_category.id;

-- canvas_and_walls: SEQUENCE OWNED BY canvas_and_walls_goals_id_seq
ALTER SEQUENCE canvas_and_walls.canvas_and_walls_goals_id_seq OWNED BY canvas_and_walls.canvas_and_walls_goals.id;

-- canvas_and_walls: SEQUENCE OWNED BY canvas_and_walls_id_seq
ALTER SEQUENCE canvas_and_walls.canvas_and_walls_id_seq OWNED BY canvas_and_walls.canvas_and_walls.id;

-- canvas_and_walls: SEQUENCE OWNED BY canvas_and_walls_proyects_id_seq
ALTER SEQUENCE canvas_and_walls.canvas_and_walls_proyects_id_seq OWNED BY canvas_and_walls.canvas_and_walls_proyects.id;

-- canvas_and_walls: DEFAULT canvas_and_walls id
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls ALTER COLUMN id SET DEFAULT nextval('canvas_and_walls.canvas_and_walls_id_seq'::regclass);

-- canvas_and_walls: DEFAULT canvas_and_walls_category id
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls_category ALTER COLUMN id SET DEFAULT nextval('canvas_and_walls.canvas_and_walls_category_id_seq'::regclass);

-- canvas_and_walls: DEFAULT canvas_and_walls_goals id
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls_goals ALTER COLUMN id SET DEFAULT nextval('canvas_and_walls.canvas_and_walls_goals_id_seq'::regclass);

-- canvas_and_walls: DEFAULT canvas_and_walls_proyects id
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls_proyects ALTER COLUMN id SET DEFAULT nextval('canvas_and_walls.canvas_and_walls_proyects_id_seq'::regclass);

-- citizen_issues: SEQUENCE OWNED BY geo_localidades_juntos_ids_seq
ALTER SEQUENCE citizen_issues.geo_localidades_juntos_ids_seq OWNED BY citizen_issues.geo_localidades_juntos.id;

-- citizen_issues: SEQUENCE OWNED BY issues_categories_id_seq
ALTER SEQUENCE citizen_issues.issues_categories_id_seq OWNED BY citizen_issues.issues_categories.id;

-- citizen_issues: SEQUENCE OWNED BY issues_categories_subjects_id_seq
ALTER SEQUENCE citizen_issues.issues_categories_subjects_id_seq OWNED BY citizen_issues.issues_categories_subjects.id;

-- citizen_issues: SEQUENCE OWNED BY issues_details_id_seq
ALTER SEQUENCE citizen_issues.issues_details_id_seq OWNED BY citizen_issues.issues_details.id;

-- citizen_issues: SEQUENCE OWNED BY issues_id_seq
ALTER SEQUENCE citizen_issues.issues_id_seq OWNED BY citizen_issues.issues.id;

-- citizen_issues: SEQUENCE OWNED BY issues_solutions_documents_categories_id_seq
ALTER SEQUENCE citizen_issues.issues_solutions_documents_categories_id_seq OWNED BY citizen_issues.issues_solutions_documents_categories.id;

-- citizen_issues: SEQUENCE OWNED BY issues_solutions_id_seq
ALTER SEQUENCE citizen_issues.issues_solutions_id_seq OWNED BY citizen_issues.issues_solutions.id;

-- citizen_issues: SEQUENCE OWNED BY issues_solutions_providers_id_seq
ALTER SEQUENCE citizen_issues.issues_solutions_providers_id_seq OWNED BY citizen_issues.issues_solutions_providers.id;

-- citizen_issues: SEQUENCE OWNED BY issues_solutions_resources_id_seq
ALTER SEQUENCE citizen_issues.issues_solutions_resources_id_seq OWNED BY citizen_issues.issues_solutions_resources.id;

-- citizen_issues: SEQUENCE OWNED BY issues_status_id_seq
ALTER SEQUENCE citizen_issues.issues_status_id_seq OWNED BY citizen_issues.issues_status.id;

-- citizen_issues: SEQUENCE OWNED BY issues_types_id_seq
ALTER SEQUENCE citizen_issues.issues_types_id_seq OWNED BY citizen_issues.issues_types.id;

-- citizen_issues: DEFAULT geo_localidades_juntos id
ALTER TABLE ONLY citizen_issues.geo_localidades_juntos ALTER COLUMN id SET DEFAULT nextval('citizen_issues.geo_localidades_juntos_ids_seq'::regclass);

-- citizen_issues: DEFAULT issues id
ALTER TABLE ONLY citizen_issues.issues ALTER COLUMN id SET DEFAULT nextval('citizen_issues.issues_id_seq'::regclass);

-- citizen_issues: DEFAULT issues_categories id
ALTER TABLE ONLY citizen_issues.issues_categories ALTER COLUMN id SET DEFAULT nextval('citizen_issues.issues_categories_id_seq'::regclass);

-- citizen_issues: DEFAULT issues_categories_subjects id
ALTER TABLE ONLY citizen_issues.issues_categories_subjects ALTER COLUMN id SET DEFAULT nextval('citizen_issues.issues_categories_subjects_id_seq'::regclass);

-- citizen_issues: DEFAULT issues_details id
ALTER TABLE ONLY citizen_issues.issues_details ALTER COLUMN id SET DEFAULT nextval('citizen_issues.issues_details_id_seq'::regclass);

-- citizen_issues: DEFAULT issues_solutions id
ALTER TABLE ONLY citizen_issues.issues_solutions ALTER COLUMN id SET DEFAULT nextval('citizen_issues.issues_solutions_id_seq'::regclass);

-- citizen_issues: DEFAULT issues_solutions_documents_categories id
ALTER TABLE ONLY citizen_issues.issues_solutions_documents_categories ALTER COLUMN id SET DEFAULT nextval('citizen_issues.issues_solutions_documents_categories_id_seq'::regclass);

-- citizen_issues: DEFAULT issues_solutions_resources id
ALTER TABLE ONLY citizen_issues.issues_solutions_resources ALTER COLUMN id SET DEFAULT nextval('citizen_issues.issues_solutions_resources_id_seq'::regclass);

-- citizen_issues: DEFAULT issues_status id
ALTER TABLE ONLY citizen_issues.issues_status ALTER COLUMN id SET DEFAULT nextval('citizen_issues.issues_status_id_seq'::regclass);

-- citizen_issues: DEFAULT issues_types id
ALTER TABLE ONLY citizen_issues.issues_types ALTER COLUMN id SET DEFAULT nextval('citizen_issues.issues_types_id_seq'::regclass);

-- document_management: SEQUENCE OWNED BY departament_id_seq
ALTER SEQUENCE document_management.departament_id_seq OWNED BY document_management.departament.id;

-- document_management: SEQUENCE OWNED BY document_id_seq
ALTER SEQUENCE document_management.document_id_seq OWNED BY document_management.document.id;

-- document_management: SEQUENCE OWNED BY person_id_seq
ALTER SEQUENCE document_management.person_id_seq OWNED BY document_management.person.id;

-- document_management: SEQUENCE OWNED BY position_id_seq
ALTER SEQUENCE document_management.position_id_seq OWNED BY document_management."position".id;

-- document_management: SEQUENCE OWNED BY position_person_id_seq
ALTER SEQUENCE document_management.position_person_id_seq OWNED BY document_management.position_person.id;

-- document_management: SEQUENCE OWNED BY position_person_status_id_seq
ALTER SEQUENCE document_management.position_person_status_id_seq OWNED BY document_management.position_person_status.id;

-- document_management: SEQUENCE OWNED BY position_type_id_seq
ALTER SEQUENCE document_management.position_type_id_seq OWNED BY document_management.position_type.id;

-- document_management: SEQUENCE OWNED BY receiver_id_seq
ALTER SEQUENCE document_management.receiver_id_seq OWNED BY document_management.receiver.id;

-- document_management: SEQUENCE OWNED BY sender_id_seq
ALTER SEQUENCE document_management.sender_id_seq OWNED BY document_management.sender.id;

-- document_management: DEFAULT departament id
ALTER TABLE ONLY document_management.departament ALTER COLUMN id SET DEFAULT nextval('document_management.departament_id_seq'::regclass);

-- document_management: DEFAULT document id
ALTER TABLE ONLY document_management.document ALTER COLUMN id SET DEFAULT nextval('document_management.document_id_seq'::regclass);

-- document_management: DEFAULT person id
ALTER TABLE ONLY document_management.person ALTER COLUMN id SET DEFAULT nextval('document_management.person_id_seq'::regclass);

-- document_management: DEFAULT position id
ALTER TABLE ONLY document_management."position" ALTER COLUMN id SET DEFAULT nextval('document_management.position_id_seq'::regclass);

-- document_management: DEFAULT position_person id
ALTER TABLE ONLY document_management.position_person ALTER COLUMN id SET DEFAULT nextval('document_management.position_person_id_seq'::regclass);

-- document_management: DEFAULT position_person_status id
ALTER TABLE ONLY document_management.position_person_status ALTER COLUMN id SET DEFAULT nextval('document_management.position_person_status_id_seq'::regclass);

-- document_management: DEFAULT position_type id
ALTER TABLE ONLY document_management.position_type ALTER COLUMN id SET DEFAULT nextval('document_management.position_type_id_seq'::regclass);

-- document_management: DEFAULT receiver id
ALTER TABLE ONLY document_management.receiver ALTER COLUMN id SET DEFAULT nextval('document_management.receiver_id_seq'::regclass);

-- document_management: DEFAULT sender id
ALTER TABLE ONLY document_management.sender ALTER COLUMN id SET DEFAULT nextval('document_management.sender_id_seq'::regclass);

-- elections: SEQUENCE OWNED BY candidate_party_associations_id_seq
ALTER SEQUENCE elections.candidate_party_associations_id_seq OWNED BY elections.candidates_party_associations.id;

-- elections: SEQUENCE OWNED BY candidates_id_seq
ALTER SEQUENCE elections.candidates_id_seq OWNED BY elections.candidates.id;

-- elections: SEQUENCE OWNED BY elections_federal_district_id_seq
ALTER SEQUENCE elections.elections_federal_district_id_seq OWNED BY elections.elections_federal_district.id;

-- elections: SEQUENCE OWNED BY elections_id_seq
ALTER SEQUENCE elections.elections_id_seq OWNED BY elections.elections.id;

-- elections: SEQUENCE OWNED BY elections_local_district_id_seq
ALTER SEQUENCE elections.elections_local_district_id_seq OWNED BY elections.elections_local_district.id;

-- elections: SEQUENCE OWNED BY elections_municipality_id_seq
ALTER SEQUENCE elections.elections_municipality_id_seq OWNED BY elections.elections_municipality.id;

-- elections: SEQUENCE OWNED BY elections_positions_id_seq
ALTER SEQUENCE elections.elections_positions_id_seq OWNED BY elections.elections_positions.id;

-- elections: SEQUENCE OWNED BY elections_types_id_seq
ALTER SEQUENCE elections.elections_types_id_seq OWNED BY elections.elections_types.id;

-- elections: SEQUENCE OWNED BY elections_voter_list_id_seq
ALTER SEQUENCE elections.elections_voter_list_id_seq OWNED BY elections.elections_voter_list.id;

-- elections: SEQUENCE OWNED BY elections_years_id_seq
ALTER SEQUENCE elections.elections_years_id_seq OWNED BY elections.elections_years.id;

-- elections: SEQUENCE OWNED BY fast_survey_id_seq
ALTER SEQUENCE elections.fast_survey_id_seq OWNED BY elections.fast_survey.id;

-- elections: SEQUENCE OWNED BY political_parties_id_seq
ALTER SEQUENCE elections.political_parties_id_seq OWNED BY elections.political_parties.id;

-- elections: SEQUENCE OWNED BY political_parties_type_id_seq
ALTER SEQUENCE elections.political_parties_type_id_seq OWNED BY elections.political_parties_type.id;

-- elections: SEQUENCE OWNED BY polling_booths_address_type_id_seq
ALTER SEQUENCE elections.polling_booths_address_type_id_seq OWNED BY elections.polling_booths_address_type.id;

-- elections: SEQUENCE OWNED BY polling_booths_categories_types_id_seq
ALTER SEQUENCE elections.polling_booths_categories_types_id_seq OWNED BY elections.polling_booths_categories.id;

-- elections: SEQUENCE OWNED BY polling_booths_election_association_id_seq
ALTER SEQUENCE elections.polling_booths_election_association_id_seq OWNED BY elections.polling_booths_election_association.id;

-- elections: SEQUENCE OWNED BY polling_booths_id_seq
ALTER SEQUENCE elections.polling_booths_id_seq OWNED BY elections.polling_booths.id;

-- elections: SEQUENCE OWNED BY polling_booths_issues_categories_id_seq
ALTER SEQUENCE elections.polling_booths_issues_categories_id_seq OWNED BY elections.polling_booths_issues_categories.id;

-- elections: SEQUENCE OWNED BY polling_booths_issues_id_seq
ALTER SEQUENCE elections.polling_booths_issues_id_seq OWNED BY elections.polling_booths_issues.id;

-- elections: SEQUENCE OWNED BY polling_booths_issues_sub_categories_id_seq
ALTER SEQUENCE elections.polling_booths_issues_sub_categories_id_seq OWNED BY elections.polling_booths_issues_sub_categories.id;

-- elections: SEQUENCE OWNED BY polling_booths_status_id_seq
ALTER SEQUENCE elections.polling_booths_status_id_seq OWNED BY elections.polling_booths_status.id;

-- elections: SEQUENCE OWNED BY polling_booths_types_id_seq
ALTER SEQUENCE elections.polling_booths_types_id_seq OWNED BY elections.polling_booths_types.id;

-- elections: SEQUENCE OWNED BY status_validate_id_seq
ALTER SEQUENCE elections.status_validate_id_seq OWNED BY elections.status_validate.id;

-- elections: DEFAULT candidates id
ALTER TABLE ONLY elections.candidates ALTER COLUMN id SET DEFAULT nextval('elections.candidates_id_seq'::regclass);

-- elections: DEFAULT candidates_party_associations id
ALTER TABLE ONLY elections.candidates_party_associations ALTER COLUMN id SET DEFAULT nextval('elections.candidate_party_associations_id_seq'::regclass);

-- elections: DEFAULT elections id
ALTER TABLE ONLY elections.elections ALTER COLUMN id SET DEFAULT nextval('elections.elections_id_seq'::regclass);

-- elections: DEFAULT elections_federal_district id
ALTER TABLE ONLY elections.elections_federal_district ALTER COLUMN id SET DEFAULT nextval('elections.elections_federal_district_id_seq'::regclass);

-- elections: DEFAULT elections_local_district id
ALTER TABLE ONLY elections.elections_local_district ALTER COLUMN id SET DEFAULT nextval('elections.elections_local_district_id_seq'::regclass);

-- elections: DEFAULT elections_municipality id
ALTER TABLE ONLY elections.elections_municipality ALTER COLUMN id SET DEFAULT nextval('elections.elections_municipality_id_seq'::regclass);

-- elections: DEFAULT elections_positions id
ALTER TABLE ONLY elections.elections_positions ALTER COLUMN id SET DEFAULT nextval('elections.elections_positions_id_seq'::regclass);

-- elections: DEFAULT elections_types id
ALTER TABLE ONLY elections.elections_types ALTER COLUMN id SET DEFAULT nextval('elections.elections_types_id_seq'::regclass);

-- elections: DEFAULT elections_voter_list id
ALTER TABLE ONLY elections.elections_voter_list ALTER COLUMN id SET DEFAULT nextval('elections.elections_voter_list_id_seq'::regclass);

-- elections: DEFAULT elections_years id
ALTER TABLE ONLY elections.elections_years ALTER COLUMN id SET DEFAULT nextval('elections.elections_years_id_seq'::regclass);

-- elections: DEFAULT fast_survey id
ALTER TABLE ONLY elections.fast_survey ALTER COLUMN id SET DEFAULT nextval('elections.fast_survey_id_seq'::regclass);

-- elections: DEFAULT political_parties id
ALTER TABLE ONLY elections.political_parties ALTER COLUMN id SET DEFAULT nextval('elections.political_parties_id_seq'::regclass);

-- elections: DEFAULT political_parties_type id
ALTER TABLE ONLY elections.political_parties_type ALTER COLUMN id SET DEFAULT nextval('elections.political_parties_type_id_seq'::regclass);

-- elections: DEFAULT polling_booths id
ALTER TABLE ONLY elections.polling_booths ALTER COLUMN id SET DEFAULT nextval('elections.polling_booths_id_seq'::regclass);

-- elections: DEFAULT polling_booths_address_type id
ALTER TABLE ONLY elections.polling_booths_address_type ALTER COLUMN id SET DEFAULT nextval('elections.polling_booths_address_type_id_seq'::regclass);

-- elections: DEFAULT polling_booths_categories id
ALTER TABLE ONLY elections.polling_booths_categories ALTER COLUMN id SET DEFAULT nextval('elections.polling_booths_categories_types_id_seq'::regclass);

-- elections: DEFAULT polling_booths_election_association id
ALTER TABLE ONLY elections.polling_booths_election_association ALTER COLUMN id SET DEFAULT nextval('elections.polling_booths_election_association_id_seq'::regclass);

-- elections: DEFAULT polling_booths_issues id
ALTER TABLE ONLY elections.polling_booths_issues ALTER COLUMN id SET DEFAULT nextval('elections.polling_booths_issues_id_seq'::regclass);

-- elections: DEFAULT polling_booths_issues_categories id
ALTER TABLE ONLY elections.polling_booths_issues_categories ALTER COLUMN id SET DEFAULT nextval('elections.polling_booths_issues_categories_id_seq'::regclass);

-- elections: DEFAULT polling_booths_status id
ALTER TABLE ONLY elections.polling_booths_status ALTER COLUMN id SET DEFAULT nextval('elections.polling_booths_status_id_seq'::regclass);

-- elections: DEFAULT polling_booths_types id
ALTER TABLE ONLY elections.polling_booths_types ALTER COLUMN id SET DEFAULT nextval('elections.polling_booths_types_id_seq'::regclass);

-- elections: DEFAULT status_validate id
ALTER TABLE ONLY elections.status_validate ALTER COLUMN id SET DEFAULT nextval('elections.status_validate_id_seq'::regclass);

-- elections_results: SEQUENCE OWNED BY election_result_id_seq
ALTER SEQUENCE elections_results.election_result_id_seq OWNED BY elections_results.election_result.id;

-- elections_results: SEQUENCE OWNED BY election_result_sheets_id_seq
ALTER SEQUENCE elections_results.election_result_sheets_id_seq OWNED BY elections_results.election_result_sheets.id;

-- elections_results: DEFAULT election_result id
ALTER TABLE ONLY elections_results.election_result ALTER COLUMN id SET DEFAULT nextval('elections_results.election_result_id_seq'::regclass);

-- elections_results: DEFAULT election_result_sheets id
ALTER TABLE ONLY elections_results.election_result_sheets ALTER COLUMN id SET DEFAULT nextval('elections_results.election_result_sheets_id_seq'::regclass);

-- elections_structure: SEQUENCE OWNED BY cat_sex_id_seq
ALTER SEQUENCE elections_structure.cat_sex_id_seq OWNED BY elections_structure.cat_sex.id;

-- elections_structure: SEQUENCE OWNED BY pollings_representatives_coordinators_id_seq
ALTER SEQUENCE elections_structure.pollings_representatives_coordinators_id_seq OWNED BY elections_structure.pollings_representatives_coordinators.id;

-- elections_structure: SEQUENCE OWNED BY pollings_representatives_id_seq
ALTER SEQUENCE elections_structure.pollings_representatives_id_seq OWNED BY elections_structure.pollings_representatives.id;

-- elections_structure: SEQUENCE OWNED BY pollings_representatives_types_id_seq
ALTER SEQUENCE elections_structure.pollings_representatives_types_id_seq OWNED BY elections_structure.pollings_representatives_types.id;

-- elections_structure: SEQUENCE OWNED BY routes_assignment_id_seq
ALTER SEQUENCE elections_structure.routes_assignment_id_seq OWNED BY elections_structure.routes_assignment.id;

-- elections_structure: SEQUENCE OWNED BY routes_id_seq
ALTER SEQUENCE elections_structure.routes_id_seq OWNED BY elections_structure.routes.id;

-- elections_structure: DEFAULT cat_sex id
ALTER TABLE ONLY elections_structure.cat_sex ALTER COLUMN id SET DEFAULT nextval('elections_structure.cat_sex_id_seq'::regclass);

-- elections_structure: DEFAULT pollings_representatives id
ALTER TABLE ONLY elections_structure.pollings_representatives ALTER COLUMN id SET DEFAULT nextval('elections_structure.pollings_representatives_id_seq'::regclass);

-- elections_structure: DEFAULT pollings_representatives_coordinators id
ALTER TABLE ONLY elections_structure.pollings_representatives_coordinators ALTER COLUMN id SET DEFAULT nextval('elections_structure.pollings_representatives_coordinators_id_seq'::regclass);

-- elections_structure: DEFAULT pollings_representatives_types id
ALTER TABLE ONLY elections_structure.pollings_representatives_types ALTER COLUMN id SET DEFAULT nextval('elections_structure.pollings_representatives_types_id_seq'::regclass);

-- elections_structure: DEFAULT routes id
ALTER TABLE ONLY elections_structure.routes ALTER COLUMN id SET DEFAULT nextval('elections_structure.routes_id_seq'::regclass);

-- elections_structure: DEFAULT routes_assignment id
ALTER TABLE ONLY elections_structure.routes_assignment ALTER COLUMN id SET DEFAULT nextval('elections_structure.routes_assignment_id_seq'::regclass);

-- electoral_history: SEQUENCE OWNED BY ballot_option_id_seq
ALTER SEQUENCE electoral_history.ballot_option_id_seq OWNED BY electoral_history.ballot_option.id;

-- electoral_history: SEQUENCE OWNED BY candidacy_id_seq
ALTER SEQUENCE electoral_history.candidacy_id_seq OWNED BY electoral_history.candidacy.id;

-- electoral_history: SEQUENCE OWNED BY election_id_seq
ALTER SEQUENCE electoral_history.election_id_seq OWNED BY electoral_history.election.id;

-- electoral_history: SEQUENCE OWNED BY polling_station_id_seq
ALTER SEQUENCE electoral_history.polling_station_id_seq OWNED BY electoral_history.polling_station.id;

-- electoral_history: SEQUENCE OWNED BY source_id_seq
ALTER SEQUENCE electoral_history.source_id_seq OWNED BY electoral_history.source.id;

-- electoral_history: DEFAULT ballot_option id
ALTER TABLE ONLY electoral_history.ballot_option ALTER COLUMN id SET DEFAULT nextval('electoral_history.ballot_option_id_seq'::regclass);

-- electoral_history: DEFAULT candidacy id
ALTER TABLE ONLY electoral_history.candidacy ALTER COLUMN id SET DEFAULT nextval('electoral_history.candidacy_id_seq'::regclass);

-- electoral_history: DEFAULT election id
ALTER TABLE ONLY electoral_history.election ALTER COLUMN id SET DEFAULT nextval('electoral_history.election_id_seq'::regclass);

-- electoral_history: DEFAULT polling_station id
ALTER TABLE ONLY electoral_history.polling_station ALTER COLUMN id SET DEFAULT nextval('electoral_history.polling_station_id_seq'::regclass);

-- electoral_history: DEFAULT source id
ALTER TABLE ONLY electoral_history.source ALTER COLUMN id SET DEFAULT nextval('electoral_history.source_id_seq'::regclass);

-- logfile: SEQUENCE OWNED BY goals_id_seq
ALTER SEQUENCE logfile.goals_id_seq OWNED BY logfile.goals.id;

-- logfile: SEQUENCE OWNED BY indicators_id_seq
ALTER SEQUENCE logfile.indicators_id_seq OWNED BY logfile.indicators.id;

-- logfile: SEQUENCE OWNED BY instruments_id_seq
ALTER SEQUENCE logfile.instruments_id_seq OWNED BY logfile.instruments.id;

-- logfile: SEQUENCE OWNED BY logfile_id_seq
ALTER SEQUENCE logfile.logfile_id_seq OWNED BY logfile.logfile.id;

-- logfile: SEQUENCE OWNED BY ods_id_seq
ALTER SEQUENCE logfile.ods_id_seq OWNED BY logfile.ods.id;

-- logfile: DEFAULT goals id
ALTER TABLE ONLY logfile.goals ALTER COLUMN id SET DEFAULT nextval('logfile.goals_id_seq'::regclass);

-- logfile: DEFAULT indicators id
ALTER TABLE ONLY logfile.indicators ALTER COLUMN id SET DEFAULT nextval('logfile.indicators_id_seq'::regclass);

-- logfile: DEFAULT instruments id
ALTER TABLE ONLY logfile.instruments ALTER COLUMN id SET DEFAULT nextval('logfile.instruments_id_seq'::regclass);

-- logfile: DEFAULT logfile id
ALTER TABLE ONLY logfile.logfile ALTER COLUMN id SET DEFAULT nextval('logfile.logfile_id_seq'::regclass);

-- logfile: DEFAULT ods id
ALTER TABLE ONLY logfile.ods ALTER COLUMN id SET DEFAULT nextval('logfile.ods_id_seq'::regclass);

-- survey: SEQUENCE OWNED BY survey_indigenous_language_id_seq1
ALTER SEQUENCE survey.survey_indigenous_language_id_seq1 OWNED BY survey.survey_indigenous_language.id;

-- survey: SEQUENCE OWNED BY survey_job_id_seq1
ALTER SEQUENCE survey.survey_job_id_seq1 OWNED BY survey.survey_job.id;

-- survey: SEQUENCE OWNED BY survey_origin_of_disability_id_seq1
ALTER SEQUENCE survey.survey_origin_of_disability_id_seq1 OWNED BY survey.survey_origin_of_disability.id;

-- survey: SEQUENCE OWNED BY survey_person_id_seq1
ALTER SEQUENCE survey.survey_person_id_seq1 OWNED BY survey.survey_person.id;

-- survey: SEQUENCE OWNED BY survey_profession_id_seq1
ALTER SEQUENCE survey.survey_profession_id_seq1 OWNED BY survey.survey_profession.id;

-- survey: SEQUENCE OWNED BY survey_requesting_or_auxiliary_document_id_seq1
ALTER SEQUENCE survey.survey_requesting_or_auxiliary_document_id_seq1 OWNED BY survey.survey_requesting_or_auxiliary_document.id;

-- survey: SEQUENCE OWNED BY survey_section_goal_id_seq
ALTER SEQUENCE survey.survey_section_goal_id_seq OWNED BY survey.survey_section_goal.id;

-- survey: SEQUENCE OWNED BY survey_tutor_or_assistant_id_seq1
ALTER SEQUENCE survey.survey_tutor_or_assistant_id_seq1 OWNED BY survey.survey_tutor_or_assistant.id;

-- survey: SEQUENCE OWNED BY survey_type_of_disability_id_seq1
ALTER SEQUENCE survey.survey_type_of_disability_id_seq1 OWNED BY survey.survey_type_of_disability.id;

-- survey: SEQUENCE OWNED BY survey_user_folio_id_seq1
ALTER SEQUENCE survey.survey_user_folio_id_seq1 OWNED BY survey.survey_user_folio.id;

-- survey: SEQUENCE OWNED BY survey_user_id_seq1
ALTER SEQUENCE survey.survey_user_id_seq1 OWNED BY survey.survey_user.id;

-- survey: SEQUENCE OWNED BY survey_user_section_id_seq1
ALTER SEQUENCE survey.survey_user_section_id_seq1 OWNED BY survey.survey_user_section.id;

-- survey: DEFAULT survey_indigenous_language id
ALTER TABLE ONLY survey.survey_indigenous_language ALTER COLUMN id SET DEFAULT nextval('survey.survey_indigenous_language_id_seq1'::regclass);

-- survey: DEFAULT survey_job id
ALTER TABLE ONLY survey.survey_job ALTER COLUMN id SET DEFAULT nextval('survey.survey_job_id_seq1'::regclass);

-- survey: DEFAULT survey_origin_of_disability id
ALTER TABLE ONLY survey.survey_origin_of_disability ALTER COLUMN id SET DEFAULT nextval('survey.survey_origin_of_disability_id_seq1'::regclass);

-- survey: DEFAULT survey_person id
ALTER TABLE ONLY survey.survey_person ALTER COLUMN id SET DEFAULT nextval('survey.survey_person_id_seq1'::regclass);

-- survey: DEFAULT survey_profession id
ALTER TABLE ONLY survey.survey_profession ALTER COLUMN id SET DEFAULT nextval('survey.survey_profession_id_seq1'::regclass);

-- survey: DEFAULT survey_requesting_or_auxiliary_document id
ALTER TABLE ONLY survey.survey_requesting_or_auxiliary_document ALTER COLUMN id SET DEFAULT nextval('survey.survey_requesting_or_auxiliary_document_id_seq1'::regclass);

-- survey: DEFAULT survey_section_goal id
ALTER TABLE ONLY survey.survey_section_goal ALTER COLUMN id SET DEFAULT nextval('survey.survey_section_goal_id_seq'::regclass);

-- survey: DEFAULT survey_tutor_or_assistant id
ALTER TABLE ONLY survey.survey_tutor_or_assistant ALTER COLUMN id SET DEFAULT nextval('survey.survey_tutor_or_assistant_id_seq1'::regclass);

-- survey: DEFAULT survey_type_of_disability id
ALTER TABLE ONLY survey.survey_type_of_disability ALTER COLUMN id SET DEFAULT nextval('survey.survey_type_of_disability_id_seq1'::regclass);

-- survey: DEFAULT survey_user id
ALTER TABLE ONLY survey.survey_user ALTER COLUMN id SET DEFAULT nextval('survey.survey_user_id_seq1'::regclass);

-- survey: DEFAULT survey_user_folio id
ALTER TABLE ONLY survey.survey_user_folio ALTER COLUMN id SET DEFAULT nextval('survey.survey_user_folio_id_seq1'::regclass);

-- survey: DEFAULT survey_user_section id
ALTER TABLE ONLY survey.survey_user_section ALTER COLUMN id SET DEFAULT nextval('survey.survey_user_section_id_seq1'::regclass);

-- visits: SEQUENCE OWNED BY visited_id_seq
ALTER SEQUENCE visits.visited_id_seq OWNED BY visits.visited.id;

-- visits: SEQUENCE OWNED BY visited_number_id_seq
ALTER SEQUENCE visits.visited_number_id_seq OWNED BY visits.visited_number.id;

-- visits: SEQUENCE OWNED BY visited_people_id_seq
ALTER SEQUENCE visits.visited_people_id_seq OWNED BY visits.visited_people.id;

-- visits: DEFAULT visited id
ALTER TABLE ONLY visits.visited ALTER COLUMN id SET DEFAULT nextval('visits.visited_id_seq'::regclass);

-- visits: DEFAULT visited_number id
ALTER TABLE ONLY visits.visited_number ALTER COLUMN id SET DEFAULT nextval('visits.visited_number_id_seq'::regclass);

-- visits: DEFAULT visited_people id
ALTER TABLE ONLY visits.visited_people ALTER COLUMN id SET DEFAULT nextval('visits.visited_people_id_seq'::regclass);


-- ===== views =====

-- structure: VIEW position_holder
CREATE VIEW structure_compat.position_holder AS
 SELECT a.position_id,
    a.id AS assignment_id,
    a.person_id,
    COALESCE(a.auth_id, p.auth_id) AS auth_id
   FROM (structure.assignment a
     JOIN structure.person p ON ((p.id = a.person_id)))
  WHERE (((a.role)::text = 'HOLDER'::text) AND (upper(a.validity) IS NULL));

-- structure: VIEW followers
CREATE VIEW structure_compat.followers AS
 SELECT m.legacy_id AS id,
    per.birth_entity_id,
    (per.birthdate)::timestamp without time zone AS birthdate,
    (per.cell_phone_number)::character varying(255) AS cell_phone_number,
    (lower(m.validity))::timestamp without time zone AS created,
    (per.curp)::character varying(255) AS curp,
    (upper(m.validity))::timestamp without time zone AS deleted,
    (per.email)::character varying(255) AS email,
    (per.ine)::character varying(255) AS ine,
    (per.last_name)::character varying(255) AS last_name,
    (per.names)::character varying(255) AS names,
    (lpad(regexp_replace((sec.code)::text, '^S-'::text, ''::text), 4, '0'::text))::character varying(255) AS section_number,
    (m.status)::character varying(255) AS status,
    (per.sur_name)::character varying(255) AS sur_name,
    (d.updated)::timestamp without time zone AS updated,
    (COALESCE(ca.auth_id, cap.auth_id, m.captured_by_auth_id))::character varying(255) AS user_create_id,
    d.family_role_id AS cat_family_role,
    d.marital_status_id AS cat_marital_status,
    d.sex_id AS cat_sex,
    per.cell_phone_number_other,
    d.committee_participation,
    d.device_id,
    (per.external_number)::character varying(255) AS external_number,
    (per.internal_number)::character varying(255) AS internal_number,
    d.inviting_others AS inviting_others_to_participate,
    d.is_adult,
    d.is_in_sync,
    (d.latitude)::character varying(255) AS latitude,
    (d.longitude)::character varying(255) AS longitude,
    (per.neighborhood)::character varying(255) AS neighborhood,
    d.number_of_people_invited,
    d.polling_rep_participation AS pollin_station_representative_participation,
    per.residence_entity_id,
    per.residence_municipality_id,
    d.section_number_ine,
    (per.street)::character varying(255) AS street,
    d.support_whatsapp,
    d.supports_movement,
    d.app_uuid AS uuid,
    d.ine_validity AS validity,
    (per.zip_code)::character varying(255) AS zip_code,
    (d.follower_auth_id)::character varying(255) AS auth_id,
    (
        CASE
            WHEN ((st.type)::text = 'MAIN'::text) THEN ph.auth_id
            ELSE NULL::character varying
        END)::character varying(255) AS promoter_auth_id,
    (
        CASE
            WHEN ((st.type)::text = 'ALTERNATE'::text) THEN ph.auth_id
            ELSE NULL::character varying
        END)::character varying(255) AS promoter_alt_auth_id,
    m.vote_flag,
    d.respondent_last_name AS last_name_resp,
    d.respondent_sur_name AS sur_name_resp,
    (m.vote_flag_timestamp)::timestamp without time zone AS vote_flag_timestamp,
    sec.federal_district_geo_id AS federal_district_id,
    (fd.distrito_federal)::character varying(255) AS federal_district_name,
    sec.local_district_geo_id AS local_district_id,
    (ld.distrito_local)::character varying(255) AS local_district_name,
    (mun.municipality_geo_id)::bigint AS municipality_id,
    (gm.nom_mun)::character varying(255) AS municipality_name,
    (sec.section_geo_id)::bigint AS section_id
   FROM (((((((((((structure.membership m
     JOIN structure.structure st ON ((st.id = m.structure_id)))
     LEFT JOIN structure.membership_detail d ON ((d.membership_id = m.id)))
     LEFT JOIN structure.person per ON ((per.id = m.person_id)))
     LEFT JOIN structure.territory sec ON ((sec.id = m.territory_id)))
     LEFT JOIN structure.territory mun ON ((mun.id = sec.parent_territory_id)))
     LEFT JOIN geography."geo_municipios" gm ON ((gm.id = mun.municipality_geo_id)))
     LEFT JOIN geography."geo_distritos_federales" fd ON ((fd.id = sec.federal_district_geo_id)))
     LEFT JOIN geography."geo_distritos_locales" ld ON ((ld.id = sec.local_district_geo_id)))
     LEFT JOIN structure_compat.position_holder ph ON ((ph.position_id = m.position_id)))
     LEFT JOIN structure.assignment ca ON ((ca.id = m.captured_by_assignment_id)))
     LEFT JOIN structure.person cap ON ((cap.id = ca.person_id)))
  WHERE ((m.legacy_id IS NOT NULL) AND (upper(m.validity) IS NULL));

-- structure: VIEW promoters
CREATE VIEW structure_compat.promoters AS
 SELECT pos.legacy_id AS id,
    (per.cell_phone_number)::character varying(255) AS cell_phone_number,
    (pos.valid_from)::timestamp without time zone AS created,
    (per.curp)::character varying(255) AS curp,
    (pos.valid_to)::timestamp without time zone AS deleted,
    (per.email)::character varying(255) AS email,
    (per.ine)::character varying(255) AS ine,
    (per.internal_number)::character varying(255) AS interior_number,
    (per.last_name)::character varying(255) AS last_name,
    mun.municipality_geo_id AS municipality_id,
    (per.names)::character varying(255) AS names,
    (per.neighborhood)::character varying(255) AS neighborhood_id,
    (hold.notes)::character varying(255) AS notes,
    (per.external_number)::character varying(255) AS outdoor_number,
    (per.zip_code)::character varying(255) AS postal_code,
    (per.street)::character varying(255) AS street,
    (per.sur_name)::character varying(255) AS sur_name,
    (hold.updated)::timestamp without time zone AS updated,
    (hold.created_by_auth_id)::character varying(255) AS user_create_id,
    per.ine_validity AS vencimiento,
    NULL::bigint AS cat_committee,
    (hold.holder_auth)::character varying(255) AS auth_id,
    NULL::bigint AS follower_id,
    sec.section_geo_id AS seccion_id,
    (structure_compat.holder_auth(pos.parent_position_id))::character varying(100) AS cordinator_zone_auth_id,
    (structure_compat.holder_auth(cz.parent_position_id))::character varying(100) AS referent_auth_id,
    (hold.holder_auth)::character varying(255) AS promoter_auth_id,
    pos.goal AS followers_goal,
    ((st.type)::text = 'ALTERNATE'::text) AS flag_alter
   FROM ((((((structure."position" pos
     JOIN structure.structure st ON ((st.id = pos.structure_id)))
     LEFT JOIN structure.territory sec ON ((sec.id = pos.territory_id)))
     LEFT JOIN structure.territory mun ON ((mun.id = sec.parent_territory_id)))
     LEFT JOIN structure."position" cz ON ((cz.id = pos.parent_position_id)))
     LEFT JOIN LATERAL ( SELECT a.person_id,
            a.notes,
            a.updated,
            a.created_by_auth_id,
            COALESCE(a.auth_id, pe.auth_id) AS holder_auth
           FROM (structure.assignment a
             JOIN structure.person pe ON ((pe.id = a.person_id)))
          WHERE ((a.position_id = pos.id) AND ((a.role)::text = 'HOLDER'::text) AND (upper(a.validity) IS NULL))
         LIMIT 1) hold ON (true))
     LEFT JOIN structure.person per ON ((per.id = hold.person_id)))
  WHERE (((pos.level)::text = 'PROMOTER'::text) AND (pos.legacy_id IS NOT NULL) AND (pos.valid_to IS NULL));

-- structure: VIEW seq_assignment
CREATE VIEW structure_compat.seq_assignment AS
 SELECT nextval('structure.assignment_id_seq'::regclass) AS id;

-- structure: VIEW seq_change_log
CREATE VIEW structure_compat.seq_change_log AS
 SELECT nextval('structure.change_log_id_seq'::regclass) AS id;

-- structure: VIEW seq_membership
CREATE VIEW structure_compat.seq_membership AS
 SELECT nextval('structure.membership_id_seq'::regclass) AS id;

-- structure: VIEW seq_person
CREATE VIEW structure_compat.seq_person AS
 SELECT nextval('structure.person_id_seq'::regclass) AS id;

-- structure: VIEW seq_position
CREATE VIEW structure_compat.seq_position AS
 SELECT nextval('structure.position_id_seq'::regclass) AS id;

-- structure: VIEW user_sections
CREATE VIEW structure_compat.user_sections AS
 SELECT structure_compat.us_id(pos.id, sec.section_geo_id) AS id,
    (h.auth_id)::character varying(500) AS user_auth_id,
    NULL::bigint AS rejuntado_id,
    sec.section_geo_id AS seccion_id,
    (lpad(regexp_replace((sec.code)::text, '^S-'::text, ''::text), 4, '0'::text))::character varying(255) AS seccion_number,
    sec.section_geo_id AS seccion_geo_id,
    pos.goal AS meta,
    NULL::timestamp without time zone AS deleted,
    mun.municipality_geo_id AS municipality_id,
    pos.child_limit AS limit_promoters,
    pos.id AS position_id,
    false AS from_municipality
   FROM (((structure."position" pos
     JOIN structure.territory sec ON (((sec.id = pos.territory_id) AND ((sec.type)::text = 'SECTION'::text))))
     LEFT JOIN structure.territory mun ON ((mun.id = sec.parent_territory_id)))
     JOIN structure_compat.position_holder h ON ((h.position_id = pos.id)))
  WHERE (((pos.level)::text = 'ZONE_COORDINATOR'::text) AND (pos.valid_to IS NULL))
UNION ALL
 SELECT structure_compat.us_id(pos.id, sec.section_geo_id) AS id,
    (h.auth_id)::character varying(500) AS user_auth_id,
    NULL::bigint AS rejuntado_id,
    NULL::integer AS seccion_id,
    (lpad(regexp_replace((sec.code)::text, '^S-'::text, ''::text), 4, '0'::text))::character varying(255) AS seccion_number,
    sec.section_geo_id AS seccion_geo_id,
    NULL::integer AS meta,
    NULL::timestamp without time zone AS deleted,
    mun.municipality_geo_id AS municipality_id,
    NULL::integer AS limit_promoters,
    pos.id AS position_id,
    true AS from_municipality
   FROM (((structure."position" pos
     JOIN structure.territory mun ON (((mun.id = pos.territory_id) AND ((mun.type)::text = 'MUNICIPALITY'::text))))
     JOIN structure.territory sec ON (((sec.parent_territory_id = mun.id) AND ((sec.type)::text = 'SECTION'::text) AND (sec.valid_to IS NULL))))
     JOIN structure_compat.position_holder h ON ((h.position_id = pos.id)))
  WHERE (((pos.level)::text = 'ZONE_COORDINATOR'::text) AND (pos.valid_to IS NULL) AND (NOT (EXISTS ( SELECT 1
           FROM (structure."position" sp
             JOIN structure_compat.position_holder sh ON ((sh.position_id = sp.id)))
          WHERE (((sp.level)::text = 'ZONE_COORDINATOR'::text) AND (sp.valid_to IS NULL) AND (sp.territory_id = sec.id) AND ((sh.auth_id)::text = (h.auth_id)::text))))));

-- structure: VIEW v_current_holder
CREATE VIEW structure.v_current_holder AS
 SELECT a.position_id,
    a.person_id,
    a.id AS assignment_id,
    p.names,
    p.last_name,
    p.sur_name,
    lower(a.validity) AS since
   FROM (structure.assignment a
     JOIN structure.person p ON ((p.id = a.person_id)))
  WHERE (((a.role)::text = 'HOLDER'::text) AND (upper(a.validity) IS NULL));

-- structure: VIEW v_person_multi_structure
CREATE VIEW structure.v_person_multi_structure AS
 SELECT membership.person_id,
    count(*) AS structures,
    array_agg(membership.structure_id) AS ids
   FROM structure.membership
  WHERE (upper(membership.validity) IS NULL)
  GROUP BY membership.person_id
 HAVING (count(*) > 1);

-- users: VIEW users_seccions
CREATE VIEW users.users_seccions AS
 SELECT us.id,
    us.user_auth_id,
    us.rejuntado_id,
    us.seccion_id,
    us.seccion_number,
    us.seccion_geo_id,
    us.meta,
    us.deleted,
    us.municipality_id,
    us.limit_promoters
   FROM structure_compat."user_sections" us;

-- followers: VIEW followers
CREATE VIEW followers.followers AS
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
   FROM structure_compat."followers" r;

-- followers: VIEW nw_hierarchy_v
CREATE VIEW followers.nw_hierarchy_v AS
 SELECT m.legacy_id AS follower_id,
        CASE
            WHEN ((s.type)::text = 'MAIN'::text) THEN h.auth_id
            ELSE NULL::character varying
        END AS promoter_auth_id,
        CASE
            WHEN ((s.type)::text = 'ALTERNATE'::text) THEN h.auth_id
            ELSE NULL::character varying
        END AS promoter_alt_auth_id,
    cap.auth_id AS user_create_id,
    m.status,
    m.vote_flag,
    m.vote_flag_timestamp
   FROM (((structure."membership" m
     JOIN structure."structure" s ON ((s.id = m.structure_id)))
     LEFT JOIN structure."assignment" h ON (((h.position_id = m.position_id) AND ((h.role)::text = 'HOLDER'::text))))
     LEFT JOIN structure."assignment" cap ON ((cap.id = m.captured_by_assignment_id)));

-- followers: VIEW followers_compat
CREATE VIEW followers.followers_compat AS
 SELECT l.id,
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
    COALESCE(n.vote_flag_timestamp, (l.vote_flag_timestamp)::timestamp with time zone) AS vote_flag_timestamp,
    l.federal_district_id,
    l.federal_district_name,
    l.local_district_id,
    l.local_district_name,
    l.municipality_id,
    l.municipality_name,
    l.section_id
   FROM (followers.followers_legacy l
     LEFT JOIN followers.nw_hierarchy_v n ON ((n.follower_id = l.id)));

-- followers: VIEW followers_compat_mat
CREATE VIEW followers.followers_compat_mat AS
 SELECT l.id,
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
    COALESCE(n.vote_flag_timestamp, (l.vote_flag_timestamp)::timestamp with time zone) AS vote_flag_timestamp,
    l.federal_district_id,
    l.federal_district_name,
    l.local_district_id,
    l.local_district_name,
    l.municipality_id,
    l.municipality_name,
    l.section_id
   FROM (followers.followers_legacy l
     LEFT JOIN followers.nw_hierarchy n ON ((n.follower_id = l.id)));

-- followers: VIEW promoters
CREATE VIEW followers.promoters AS
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
   FROM structure_compat."promoters" r;

-- electoral_history: MATERIALIZED VIEW mv_section_group
CREATE MATERIALIZED VIEW electoral_history.mv_section_group AS
 WITH votes AS (
         SELECT p.election_id,
            p.municipality_cve,
            p.local_district,
            p.federal_district,
            p.section,
            p.district,
            e.year,
            e.scope,
            o.kind,
            o.raw_label,
            o.party_codes,
            v.votes
           FROM (((electoral_history.station_vote v
             JOIN electoral_history.polling_station p ON ((p.id = v.station_id)))
             JOIN electoral_history.election e ON ((e.id = p.election_id)))
             JOIN electoral_history.ballot_option o ON ((o.id = v.option_id)))
          WHERE (((o.kind)::text <> 'DERIVED_TOTAL'::text) AND p.counted)
        )
 SELECT v.election_id,
    v.municipality_cve,
    v.local_district,
    v.federal_district,
    v.section,
    'candidacy'::character varying(10) AS group_by,
    c.code,
    sum(v.votes) AS votes
   FROM (votes v
     JOIN electoral_history.candidacy c ON (((c.election_id = v.election_id) AND (cardinality(v.party_codes) > 0) AND (v.party_codes <@ c.party_codes) AND ((c.territory IS NULL) OR (((v.scope)::text = 'MUNICIPALITY'::text) AND ((c.territory)::text = (v.municipality_cve)::text)) OR (((v.scope)::text = ANY ((ARRAY['FEDERAL_DISTRICT'::character varying, 'LOCAL_DISTRICT'::character varying])::text[])) AND ((c.territory)::text = (v.district)::text))))))
  GROUP BY v.election_id, v.municipality_cve, v.local_district, v.federal_district, v.section, c.code
UNION ALL
 SELECT v.election_id,
    v.municipality_cve,
    v.local_district,
    v.federal_district,
    v.section,
    'party'::character varying AS group_by,
    v.party_codes[1] AS code,
    sum(v.votes) AS votes
   FROM votes v
  WHERE ((v.kind)::text = 'PARTY'::text)
  GROUP BY v.election_id, v.municipality_cve, v.local_district, v.federal_district, v.section, v.party_codes[1]
UNION ALL
 SELECT v.election_id,
    v.municipality_cve,
    v.local_district,
    v.federal_district,
    v.section,
    'bloc'::character varying AS group_by,
    COALESCE(b.bloc_code, 'OTHERS'::text) AS code,
    sum(v.votes) AS votes
   FROM (votes v
     LEFT JOIN LATERAL ( SELECT min((bp.bloc_code)::text) AS bloc_code
           FROM (unnest(v.party_codes) pc(pc)
             JOIN electoral_history.bloc_party bp ON (((bp.year = v.year) AND ((bp.party_code)::text = (pc.pc)::text))))
         HAVING ((count(DISTINCT bp.bloc_code) = 1) AND (count(*) = cardinality(v.party_codes)))) b ON (true))
  GROUP BY v.election_id, v.municipality_cve, v.local_district, v.federal_district, v.section, COALESCE(b.bloc_code, 'OTHERS'::text)
  WITH NO DATA;

-- electoral_history: MATERIALIZED VIEW mv_section_turnout
CREATE MATERIALIZED VIEW electoral_history.mv_section_turnout AS
 SELECT p.election_id,
    p.municipality_cve,
    p.local_district,
    p.federal_district,
    p.section,
    count(*) FILTER (WHERE p.counted) AS stations,
    count(*) FILTER (WHERE (p.counted AND ((p.station_type)::text <> ALL ((ARRAY['B'::character varying, 'C'::character varying, 'E'::character varying])::text[])))) AS special_stations,
    sum(p.nominal_list) FILTER (WHERE ((p.station_type)::text = ANY ((ARRAY['B'::character varying, 'C'::character varying, 'E'::character varying])::text[]))) AS nominal_list,
    sum(p.total_votes) FILTER (WHERE p.counted) AS total_votes,
    sum(p.total_votes) FILTER (WHERE (p.counted AND ((p.station_type)::text = ANY ((ARRAY['B'::character varying, 'C'::character varying, 'E'::character varying])::text[])))) AS ordinary_votes,
    sum(p.null_votes) FILTER (WHERE p.counted) AS null_votes,
    sum(p.non_registered_votes) FILTER (WHERE p.counted) AS non_registered_votes
   FROM electoral_history.polling_station p
  GROUP BY p.election_id, p.municipality_cve, p.local_district, p.federal_district, p.section
  WITH NO DATA;


-- ===== constraints =====

-- geography: CONSTRAINT geo_safe_houses geo_safe_houses_pkey
ALTER TABLE ONLY geography.geo_safe_houses
    ADD CONSTRAINT geo_safe_houses_pkey PRIMARY KEY (id);

-- geography: CONSTRAINT localidades_secciones localidades_secciones_pkey
ALTER TABLE ONLY geography.localidades_secciones
    ADD CONSTRAINT localidades_secciones_pkey PRIMARY KEY (id);

-- structure: CONSTRAINT assignment assignment_pkey
ALTER TABLE ONLY structure.assignment
    ADD CONSTRAINT assignment_pkey PRIMARY KEY (id);

-- structure: CONSTRAINT assignment assignment_single_holder
ALTER TABLE ONLY structure.assignment
    ADD CONSTRAINT assignment_single_holder EXCLUDE USING gist (position_id WITH =, validity WITH &&) WHERE (((role)::text = 'HOLDER'::text));

-- structure: CONSTRAINT campaign_election campaign_election_pkey
ALTER TABLE ONLY structure.campaign_election
    ADD CONSTRAINT campaign_election_pkey PRIMARY KEY (id);

-- structure: CONSTRAINT campaign_election campaign_election_uq
ALTER TABLE ONLY structure.campaign_election
    ADD CONSTRAINT campaign_election_uq UNIQUE (campaign_id, office);

-- structure: CONSTRAINT campaign campaign_pkey
ALTER TABLE ONLY structure.campaign
    ADD CONSTRAINT campaign_pkey PRIMARY KEY (id);

-- structure: CONSTRAINT change_log change_log_pkey
ALTER TABLE ONLY structure.change_log
    ADD CONSTRAINT change_log_pkey PRIMARY KEY (id);

-- structure: CONSTRAINT membership_detail membership_detail_pkey
ALTER TABLE ONLY structure.membership_detail
    ADD CONSTRAINT membership_detail_pkey PRIMARY KEY (membership_id);

-- structure: CONSTRAINT membership membership_pkey
ALTER TABLE ONLY structure.membership
    ADD CONSTRAINT membership_pkey PRIMARY KEY (id);

-- structure: CONSTRAINT membership membership_single_per_structure
ALTER TABLE ONLY structure.membership
    ADD CONSTRAINT membership_single_per_structure EXCLUDE USING gist (person_id WITH =, structure_id WITH =, validity WITH &&);

-- structure: CONSTRAINT person person_pkey
ALTER TABLE ONLY structure.person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);

-- structure: CONSTRAINT position position_pkey
ALTER TABLE ONLY structure."position"
    ADD CONSTRAINT position_pkey PRIMARY KEY (id);

-- structure: CONSTRAINT structure structure_pkey
ALTER TABLE ONLY structure.structure
    ADD CONSTRAINT structure_pkey PRIMARY KEY (id);

-- structure: CONSTRAINT territory territory_pkey
ALTER TABLE ONLY structure.territory
    ADD CONSTRAINT territory_pkey PRIMARY KEY (id);

-- users: CONSTRAINT coordinators_group coordinators_group_pkey
ALTER TABLE ONLY users.coordinators_group
    ADD CONSTRAINT coordinators_group_pkey PRIMARY KEY (id);

-- users: CONSTRAINT geo_secciones geo_secciones_pkey
ALTER TABLE ONLY users.geo_secciones
    ADD CONSTRAINT geo_secciones_pkey PRIMARY KEY (id);

-- users: CONSTRAINT menus_groups menus_groups_pkey
ALTER TABLE ONLY users.menus_groups
    ADD CONSTRAINT menus_groups_pkey PRIMARY KEY (pk);

-- users: CONSTRAINT menus menus_pkey
ALTER TABLE ONLY users.menus
    ADD CONSTRAINT menus_pkey PRIMARY KEY (pk);

-- users: CONSTRAINT menus_roles menus_roles_pkey
ALTER TABLE ONLY users.menus_roles
    ADD CONSTRAINT menus_roles_pkey PRIMARY KEY (id);

-- users: CONSTRAINT promoters promoters_pkey
ALTER TABLE ONLY users.promoters
    ADD CONSTRAINT promoters_pkey PRIMARY KEY (id);

-- users: CONSTRAINT users users_auth_id_key
ALTER TABLE ONLY users.users
    ADD CONSTRAINT users_auth_id_key UNIQUE (auth_id);

-- users: CONSTRAINT users_municipalities users_municipalities_pkey
ALTER TABLE ONLY users.users_municipalities
    ADD CONSTRAINT users_municipalities_pkey PRIMARY KEY (id);

-- users: CONSTRAINT users users_pkey
ALTER TABLE ONLY users.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

-- followers: CONSTRAINT preregister preregister_pkey
ALTER TABLE ONLY followers.preregister
    ADD CONSTRAINT preregister_pkey PRIMARY KEY (id);

-- calendar: CONSTRAINT calendars calendars_pkey
ALTER TABLE ONLY calendar.calendars
    ADD CONSTRAINT calendars_pkey PRIMARY KEY (id);

-- calendar: CONSTRAINT events events_pkey
ALTER TABLE ONLY calendar.events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);

-- canvas_and_walls: CONSTRAINT canvas_and_walls_category canvas_and_walls_category_pkey
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls_category
    ADD CONSTRAINT canvas_and_walls_category_pkey PRIMARY KEY (id);

-- canvas_and_walls: CONSTRAINT canvas_and_walls_goals canvas_and_walls_goals_pkey
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls_goals
    ADD CONSTRAINT canvas_and_walls_goals_pkey PRIMARY KEY (id);

-- canvas_and_walls: CONSTRAINT canvas_and_walls canvas_and_walls_pkey
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls
    ADD CONSTRAINT canvas_and_walls_pkey PRIMARY KEY (id);

-- canvas_and_walls: CONSTRAINT canvas_and_walls_proyects canvas_and_walls_proyects_pkey
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls_proyects
    ADD CONSTRAINT canvas_and_walls_proyects_pkey PRIMARY KEY (id);

-- canvas_and_walls: CONSTRAINT user_entity constraint_fb
ALTER TABLE ONLY canvas_and_walls.user_entity
    ADD CONSTRAINT constraint_fb PRIMARY KEY (id);

-- canvas_and_walls: CONSTRAINT report_canvas_and_wall_municipality_entity report_canvas_and_wall_municipality_entity_pkey
ALTER TABLE ONLY canvas_and_walls.report_canvas_and_wall_municipality_entity
    ADD CONSTRAINT report_canvas_and_wall_municipality_entity_pkey PRIMARY KEY (id);

-- canvas_and_walls: CONSTRAINT report_canvas_and_wall_referrer_entity report_canvas_and_wall_referrer_entity_pkey
ALTER TABLE ONLY canvas_and_walls.report_canvas_and_wall_referrer_entity
    ADD CONSTRAINT report_canvas_and_wall_referrer_entity_pkey PRIMARY KEY (id);

-- canvas_and_walls: CONSTRAINT report_canvas_and_wall_zone_entity report_canvas_and_wall_zone_entity_pkey
ALTER TABLE ONLY canvas_and_walls.report_canvas_and_wall_zone_entity
    ADD CONSTRAINT report_canvas_and_wall_zone_entity_pkey PRIMARY KEY (id);

-- canvas_and_walls: CONSTRAINT user_entity uk_dykn684sl8up1crfei6eckhd7
ALTER TABLE ONLY canvas_and_walls.user_entity
    ADD CONSTRAINT uk_dykn684sl8up1crfei6eckhd7 UNIQUE (realm_id, email_constraint);

-- canvas_and_walls: CONSTRAINT user_entity uk_ru8tt6t700s9v50bu18ws5ha6
ALTER TABLE ONLY canvas_and_walls.user_entity
    ADD CONSTRAINT uk_ru8tt6t700s9v50bu18ws5ha6 UNIQUE (realm_id, username);

-- citizen_issues: CONSTRAINT geo_localidades_juntos geo_localidades_juntos_pkey
ALTER TABLE ONLY citizen_issues.geo_localidades_juntos
    ADD CONSTRAINT geo_localidades_juntos_pkey PRIMARY KEY (id);

-- document_management: CONSTRAINT departament departament_pkey
ALTER TABLE ONLY document_management.departament
    ADD CONSTRAINT departament_pkey PRIMARY KEY (id);

-- document_management: CONSTRAINT document document_pkey
ALTER TABLE ONLY document_management.document
    ADD CONSTRAINT document_pkey PRIMARY KEY (id);

-- document_management: CONSTRAINT person person_pkey
ALTER TABLE ONLY document_management.person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);

-- document_management: CONSTRAINT position_person position_person_pkey
ALTER TABLE ONLY document_management.position_person
    ADD CONSTRAINT position_person_pkey PRIMARY KEY (id);

-- document_management: CONSTRAINT position_person_status position_person_status_pkey
ALTER TABLE ONLY document_management.position_person_status
    ADD CONSTRAINT position_person_status_pkey PRIMARY KEY (id);

-- document_management: CONSTRAINT position position_pkey
ALTER TABLE ONLY document_management."position"
    ADD CONSTRAINT position_pkey PRIMARY KEY (id);

-- document_management: CONSTRAINT position_type position_type_pkey
ALTER TABLE ONLY document_management.position_type
    ADD CONSTRAINT position_type_pkey PRIMARY KEY (id);

-- document_management: CONSTRAINT receiver receiver_pkey
ALTER TABLE ONLY document_management.receiver
    ADD CONSTRAINT receiver_pkey PRIMARY KEY (id);

-- document_management: CONSTRAINT sender sender_pkey
ALTER TABLE ONLY document_management.sender
    ADD CONSTRAINT sender_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT candidates_party_associations candidate_party_associations_pkey
ALTER TABLE ONLY elections.candidates_party_associations
    ADD CONSTRAINT candidate_party_associations_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT candidates candidates_pkey
ALTER TABLE ONLY elections.candidates
    ADD CONSTRAINT candidates_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT elections_federal_district elections_federal_district_pkey
ALTER TABLE ONLY elections.elections_federal_district
    ADD CONSTRAINT elections_federal_district_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT elections_local_district elections_local_district_pkey
ALTER TABLE ONLY elections.elections_local_district
    ADD CONSTRAINT elections_local_district_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT elections_municipality elections_municipality_pkey
ALTER TABLE ONLY elections.elections_municipality
    ADD CONSTRAINT elections_municipality_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT elections elections_pkey
ALTER TABLE ONLY elections.elections
    ADD CONSTRAINT elections_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT elections_positions elections_positions_pkey
ALTER TABLE ONLY elections.elections_positions
    ADD CONSTRAINT elections_positions_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT elections_types elections_types_pkey
ALTER TABLE ONLY elections.elections_types
    ADD CONSTRAINT elections_types_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT elections_voter_list elections_voter_list_pkey
ALTER TABLE ONLY elections.elections_voter_list
    ADD CONSTRAINT elections_voter_list_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT elections_years elections_years_pkey
ALTER TABLE ONLY elections.elections_years
    ADD CONSTRAINT elections_years_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT fast_survey fast_survey_pkey
ALTER TABLE ONLY elections.fast_survey
    ADD CONSTRAINT fast_survey_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT political_parties political_parties_pkey
ALTER TABLE ONLY elections.political_parties
    ADD CONSTRAINT political_parties_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT political_parties_type political_parties_type_pkey
ALTER TABLE ONLY elections.political_parties_type
    ADD CONSTRAINT political_parties_type_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT polling_booths_address_type polling_booths_address_type_pkey
ALTER TABLE ONLY elections.polling_booths_address_type
    ADD CONSTRAINT polling_booths_address_type_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT polling_booths_categories polling_booths_categories_types_pkey
ALTER TABLE ONLY elections.polling_booths_categories
    ADD CONSTRAINT polling_booths_categories_types_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT polling_booths_election_association polling_booths_election_association_pkey
ALTER TABLE ONLY elections.polling_booths_election_association
    ADD CONSTRAINT polling_booths_election_association_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT polling_booths_issues_categories polling_booths_issues_categories_pkey
ALTER TABLE ONLY elections.polling_booths_issues_categories
    ADD CONSTRAINT polling_booths_issues_categories_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT polling_booths_issues polling_booths_issues_pkey
ALTER TABLE ONLY elections.polling_booths_issues
    ADD CONSTRAINT polling_booths_issues_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT polling_booths_issues_sub_categories polling_booths_issues_sub_categories_pkey
ALTER TABLE ONLY elections.polling_booths_issues_sub_categories
    ADD CONSTRAINT polling_booths_issues_sub_categories_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT polling_booths polling_booths_pkey
ALTER TABLE ONLY elections.polling_booths
    ADD CONSTRAINT polling_booths_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT polling_booths_status polling_booths_status_pkey
ALTER TABLE ONLY elections.polling_booths_status
    ADD CONSTRAINT polling_booths_status_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT polling_booths_types polling_booths_types_pkey
ALTER TABLE ONLY elections.polling_booths_types
    ADD CONSTRAINT polling_booths_types_pkey PRIMARY KEY (id);

-- elections: CONSTRAINT status_validate status_validate_pkey
ALTER TABLE ONLY elections.status_validate
    ADD CONSTRAINT status_validate_pkey PRIMARY KEY (id);

-- elections_results: CONSTRAINT election_result election_result_pkey
ALTER TABLE ONLY elections_results.election_result
    ADD CONSTRAINT election_result_pkey PRIMARY KEY (id);

-- elections_results: CONSTRAINT election_result_sheets election_result_sheets_pkey
ALTER TABLE ONLY elections_results.election_result_sheets
    ADD CONSTRAINT election_result_sheets_pkey PRIMARY KEY (id);

-- elections_structure: CONSTRAINT cat_sex cat_sex_pkey
ALTER TABLE ONLY elections_structure.cat_sex
    ADD CONSTRAINT cat_sex_pkey PRIMARY KEY (id);

-- elections_structure: CONSTRAINT pollings_representatives_coordinators pollings_representatives_coordinators_pkey
ALTER TABLE ONLY elections_structure.pollings_representatives_coordinators
    ADD CONSTRAINT pollings_representatives_coordinators_pkey PRIMARY KEY (id);

-- elections_structure: CONSTRAINT pollings_representatives pollings_representatives_pkey
ALTER TABLE ONLY elections_structure.pollings_representatives
    ADD CONSTRAINT pollings_representatives_pkey PRIMARY KEY (id);

-- elections_structure: CONSTRAINT pollings_representatives_types pollings_representatives_types_pkey
ALTER TABLE ONLY elections_structure.pollings_representatives_types
    ADD CONSTRAINT pollings_representatives_types_pkey PRIMARY KEY (id);

-- elections_structure: CONSTRAINT routes_assignment routes_assignment_pkey
ALTER TABLE ONLY elections_structure.routes_assignment
    ADD CONSTRAINT routes_assignment_pkey PRIMARY KEY (id);

-- elections_structure: CONSTRAINT routes_assignment routes_assignment_polling_booth_id_key
ALTER TABLE ONLY elections_structure.routes_assignment
    ADD CONSTRAINT routes_assignment_polling_booth_id_key UNIQUE (polling_booth_id);

-- elections_structure: CONSTRAINT routes routes_pkey
ALTER TABLE ONLY elections_structure.routes
    ADD CONSTRAINT routes_pkey PRIMARY KEY (id);

-- elections_structure: CONSTRAINT routes routes_polling_representative_general_id_key
ALTER TABLE ONLY elections_structure.routes
    ADD CONSTRAINT routes_polling_representative_general_id_key UNIQUE (polling_representative_general_id);

-- electoral_history: CONSTRAINT ballot_option ballot_option_election_id_raw_label_key
ALTER TABLE ONLY electoral_history.ballot_option
    ADD CONSTRAINT ballot_option_election_id_raw_label_key UNIQUE (election_id, raw_label);

-- electoral_history: CONSTRAINT ballot_option ballot_option_pkey
ALTER TABLE ONLY electoral_history.ballot_option
    ADD CONSTRAINT ballot_option_pkey PRIMARY KEY (id);

-- electoral_history: CONSTRAINT bloc_party bloc_party_pkey
ALTER TABLE ONLY electoral_history.bloc_party
    ADD CONSTRAINT bloc_party_pkey PRIMARY KEY (year, party_code);

-- electoral_history: CONSTRAINT bloc bloc_pkey
ALTER TABLE ONLY electoral_history.bloc
    ADD CONSTRAINT bloc_pkey PRIMARY KEY (code);

-- electoral_history: CONSTRAINT candidacy candidacy_pkey
ALTER TABLE ONLY electoral_history.candidacy
    ADD CONSTRAINT candidacy_pkey PRIMARY KEY (id);

-- electoral_history: CONSTRAINT election election_pkey
ALTER TABLE ONLY electoral_history.election
    ADD CONSTRAINT election_pkey PRIMARY KEY (id);

-- electoral_history: CONSTRAINT election election_year_office_key
ALTER TABLE ONLY electoral_history.election
    ADD CONSTRAINT election_year_office_key UNIQUE (year, office);

-- electoral_history: CONSTRAINT municipality municipality_pkey
ALTER TABLE ONLY electoral_history.municipality
    ADD CONSTRAINT municipality_pkey PRIMARY KEY (cve);

-- electoral_history: CONSTRAINT party party_pkey
ALTER TABLE ONLY electoral_history.party
    ADD CONSTRAINT party_pkey PRIMARY KEY (code);

-- electoral_history: CONSTRAINT polling_station polling_station_election_id_station_key_key
ALTER TABLE ONLY electoral_history.polling_station
    ADD CONSTRAINT polling_station_election_id_station_key_key UNIQUE (election_id, station_key);

-- electoral_history: CONSTRAINT polling_station polling_station_pkey
ALTER TABLE ONLY electoral_history.polling_station
    ADD CONSTRAINT polling_station_pkey PRIMARY KEY (id);

-- electoral_history: CONSTRAINT section_municipality section_municipality_pkey
ALTER TABLE ONLY electoral_history.section_municipality
    ADD CONSTRAINT section_municipality_pkey PRIMARY KEY (year, section);

-- electoral_history: CONSTRAINT source source_file_name_key
ALTER TABLE ONLY electoral_history.source
    ADD CONSTRAINT source_file_name_key UNIQUE (file_name);

-- electoral_history: CONSTRAINT source source_pkey
ALTER TABLE ONLY electoral_history.source
    ADD CONSTRAINT source_pkey PRIMARY KEY (id);

-- electoral_history: CONSTRAINT station_vote station_vote_pkey
ALTER TABLE ONLY electoral_history.station_vote
    ADD CONSTRAINT station_vote_pkey PRIMARY KEY (station_id, option_id);

-- logfile: CONSTRAINT goals goals_pkey
ALTER TABLE ONLY logfile.goals
    ADD CONSTRAINT goals_pkey PRIMARY KEY (id);

-- logfile: CONSTRAINT indicators indicators_pkey
ALTER TABLE ONLY logfile.indicators
    ADD CONSTRAINT indicators_pkey PRIMARY KEY (id);

-- logfile: CONSTRAINT instruments instruments_pkey
ALTER TABLE ONLY logfile.instruments
    ADD CONSTRAINT instruments_pkey PRIMARY KEY (id);

-- logfile: CONSTRAINT logfile logfile_pkey
ALTER TABLE ONLY logfile.logfile
    ADD CONSTRAINT logfile_pkey PRIMARY KEY (id);

-- logfile: CONSTRAINT ods ods_pkey
ALTER TABLE ONLY logfile.ods
    ADD CONSTRAINT ods_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_indigenous_language survey_indigenous_language_pkey
ALTER TABLE ONLY survey.survey_indigenous_language
    ADD CONSTRAINT survey_indigenous_language_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_job survey_job_pkey
ALTER TABLE ONLY survey.survey_job
    ADD CONSTRAINT survey_job_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_origin_of_disability survey_origin_of_disability_pkey
ALTER TABLE ONLY survey.survey_origin_of_disability
    ADD CONSTRAINT survey_origin_of_disability_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_person survey_person_pkey
ALTER TABLE ONLY survey.survey_person
    ADD CONSTRAINT survey_person_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_profession survey_profession_pkey
ALTER TABLE ONLY survey.survey_profession
    ADD CONSTRAINT survey_profession_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_requesting_or_auxiliary_document survey_requesting_or_auxiliary_document_pkey
ALTER TABLE ONLY survey.survey_requesting_or_auxiliary_document
    ADD CONSTRAINT survey_requesting_or_auxiliary_document_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_section_goal survey_section_goal_pkey
ALTER TABLE ONLY survey.survey_section_goal
    ADD CONSTRAINT survey_section_goal_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_tutor_or_assistant survey_tutor_or_assistant_pkey
ALTER TABLE ONLY survey.survey_tutor_or_assistant
    ADD CONSTRAINT survey_tutor_or_assistant_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_type_of_disability survey_type_of_disability_pkey
ALTER TABLE ONLY survey.survey_type_of_disability
    ADD CONSTRAINT survey_type_of_disability_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_user_folio survey_user_folio_pkey
ALTER TABLE ONLY survey.survey_user_folio
    ADD CONSTRAINT survey_user_folio_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_user survey_user_pkey
ALTER TABLE ONLY survey.survey_user
    ADD CONSTRAINT survey_user_pkey PRIMARY KEY (id);

-- survey: CONSTRAINT survey_user_section survey_user_section_pkey
ALTER TABLE ONLY survey.survey_user_section
    ADD CONSTRAINT survey_user_section_pkey PRIMARY KEY (id);

-- visits: CONSTRAINT reportpdfdto reportpdfdto_pkey
ALTER TABLE ONLY visits.reportpdfdto
    ADD CONSTRAINT reportpdfdto_pkey PRIMARY KEY (id);

-- visits: CONSTRAINT visited_number visited_number_pkey
ALTER TABLE ONLY visits.visited_number
    ADD CONSTRAINT visited_number_pkey PRIMARY KEY (id);

-- visits: CONSTRAINT visited_people visited_people_pkey
ALTER TABLE ONLY visits.visited_people
    ADD CONSTRAINT visited_people_pkey PRIMARY KEY (id);

-- visits: CONSTRAINT visited visited_pkey
ALTER TABLE ONLY visits.visited
    ADD CONSTRAINT visited_pkey PRIMARY KEY (id);


-- ===== indexes =====

-- structure: INDEX assignment_auth_id_idx
CREATE INDEX assignment_auth_id_idx ON structure.assignment USING btree (auth_id) WHERE (auth_id IS NOT NULL);

-- structure: INDEX assignment_created_by_auth_idx
CREATE INDEX assignment_created_by_auth_idx ON structure.assignment USING btree (created_by_auth_id) WHERE (created_by_auth_id IS NOT NULL);

-- structure: INDEX assignment_current_holder_idx
CREATE INDEX assignment_current_holder_idx ON structure.assignment USING btree (position_id, person_id) WHERE (((role)::text = 'HOLDER'::text) AND (upper(validity) IS NULL));

-- structure: INDEX assignment_person_id_role_idx
CREATE INDEX assignment_person_id_role_idx ON structure.assignment USING btree (person_id, role);

-- structure: INDEX assignment_person_idx
CREATE INDEX assignment_person_idx ON structure.assignment USING btree (person_id);

-- structure: INDEX assignment_position_idx
CREATE INDEX assignment_position_idx ON structure.assignment USING btree (position_id);

-- structure: INDEX assignment_validity_idx
CREATE INDEX assignment_validity_idx ON structure.assignment USING gist (validity);

-- structure: INDEX campaign_one_active_uq
CREATE UNIQUE INDEX campaign_one_active_uq ON structure.campaign USING btree (COALESCE(territory_id, (0)::bigint)) WHERE ((status)::text = 'ACTIVE'::text);

-- structure: INDEX change_log_date_idx
CREATE INDEX change_log_date_idx ON structure.change_log USING btree (occurred_at DESC);

-- structure: INDEX change_log_entity_idx
CREATE INDEX change_log_entity_idx ON structure.change_log USING btree (entity, entity_id);

-- structure: INDEX membership_captured_auth_idx
CREATE INDEX membership_captured_auth_idx ON structure.membership USING btree (captured_by_auth_id) WHERE (captured_by_auth_id IS NOT NULL);

-- structure: INDEX membership_captured_idx
CREATE INDEX membership_captured_idx ON structure.membership USING btree (captured_by_assignment_id);

-- structure: INDEX membership_detail_auth_idx
CREATE INDEX membership_detail_auth_idx ON structure.membership_detail USING btree (follower_auth_id) WHERE (follower_auth_id IS NOT NULL);

-- structure: INDEX membership_legacy_idx
CREATE INDEX membership_legacy_idx ON structure.membership USING btree (legacy_id);

-- structure: INDEX membership_legacy_uq
CREATE UNIQUE INDEX membership_legacy_uq ON structure.membership USING btree (legacy_id) WHERE (legacy_id IS NOT NULL);

-- structure: INDEX membership_person_idx
CREATE INDEX membership_person_idx ON structure.membership USING btree (person_id);

-- structure: INDEX membership_position_idx
CREATE INDEX membership_position_idx ON structure.membership USING btree (position_id);

-- structure: INDEX membership_structure_idx
CREATE INDEX membership_structure_idx ON structure.membership USING btree (structure_id);

-- structure: INDEX membership_territory_idx
CREATE INDEX membership_territory_idx ON structure.membership USING btree (territory_id);

-- structure: INDEX person_auth_id_uq
CREATE UNIQUE INDEX person_auth_id_uq ON structure.person USING btree (auth_id) WHERE (auth_id IS NOT NULL);

-- structure: INDEX person_curp_uq
CREATE UNIQUE INDEX person_curp_uq ON structure.person USING btree (curp) WHERE (curp IS NOT NULL);

-- structure: INDEX person_ine_idx
CREATE INDEX person_ine_idx ON structure.person USING btree (ine) WHERE (ine IS NOT NULL);

-- structure: INDEX person_legacy_idx
CREATE INDEX person_legacy_idx ON structure.person USING btree (legacy_source, legacy_id);

-- structure: INDEX person_name_idx
CREATE INDEX person_name_idx ON structure.person USING btree (last_name, sur_name, names);

-- structure: INDEX position_legacy_uq
CREATE UNIQUE INDEX position_legacy_uq ON structure."position" USING btree (legacy_id) WHERE (legacy_id IS NOT NULL);

-- structure: INDEX position_level_idx
CREATE INDEX position_level_idx ON structure."position" USING btree (level) WHERE (valid_to IS NULL);

-- structure: INDEX position_parent_idx
CREATE INDEX position_parent_idx ON structure."position" USING btree (parent_position_id);

-- structure: INDEX position_section_key_uq
CREATE UNIQUE INDEX position_section_key_uq ON structure."position" USING btree (structure_id, section_key) WHERE (section_key IS NOT NULL);

-- structure: INDEX position_structure_idx
CREATE INDEX position_structure_idx ON structure."position" USING btree (structure_id);

-- structure: INDEX position_territory_idx
CREATE INDEX position_territory_idx ON structure."position" USING btree (territory_id);

-- structure: INDEX structure_campaign_idx
CREATE INDEX structure_campaign_idx ON structure.structure USING btree (campaign_id);

-- structure: INDEX territory_code_uq
CREATE UNIQUE INDEX territory_code_uq ON structure.territory USING btree (code) WHERE (valid_to IS NULL);

-- structure: INDEX territory_parent_idx
CREATE INDEX territory_parent_idx ON structure.territory USING btree (parent_territory_id);

-- structure: INDEX territory_section_idx
CREATE INDEX territory_section_idx ON structure.territory USING btree (section_geo_id) WHERE (section_geo_id IS NOT NULL);

-- users: INDEX menus_roles_group_role_uq
CREATE UNIQUE INDEX menus_roles_group_role_uq ON users.menus_roles USING btree (menu_group_id, role);

-- users: INDEX users_auth_id_idx
CREATE INDEX users_auth_id_idx ON users.users USING hash (auth_id);

-- users: INDEX users_municipalities_user_auth_id_idx
CREATE INDEX users_municipalities_user_auth_id_idx ON users.users_municipalities USING hash (user_auth_id);

-- followers: INDEX followers_legacy_id_uq
CREATE UNIQUE INDEX followers_legacy_id_uq ON followers.followers_legacy USING btree (id);

-- followers: INDEX nw_hierarchy_follower_id_idx
CREATE UNIQUE INDEX nw_hierarchy_follower_id_idx ON followers.nw_hierarchy USING btree (follower_id);

-- canvas_and_walls: INDEX idx_user_email
CREATE INDEX idx_user_email ON canvas_and_walls.user_entity USING btree (email);

-- canvas_and_walls: INDEX idx_user_service_account
CREATE INDEX idx_user_service_account ON canvas_and_walls.user_entity USING btree (realm_id, service_account_client_link);

-- elections: INDEX candidates_id_election_id_idx
CREATE INDEX candidates_id_election_id_idx ON elections.candidates USING btree (id, election_id);

-- elections: INDEX candidates_party_associations_candidate_id_idx
CREATE INDEX candidates_party_associations_candidate_id_idx ON elections.candidates_party_associations USING hash (candidate_id);

-- elections: INDEX candidates_party_associations_id_candidate_id_political_par_idx
CREATE INDEX candidates_party_associations_id_candidate_id_political_par_idx ON elections.candidates_party_associations USING btree (id, candidate_id, political_party_id);

-- elections: INDEX candidates_party_associations_political_party_id_idx
CREATE INDEX candidates_party_associations_political_party_id_idx ON elections.candidates_party_associations USING hash (political_party_id);

-- elections: INDEX elections_federal_district_candidate_id_idx
CREATE INDEX elections_federal_district_candidate_id_idx ON elections.elections_federal_district USING hash (candidate_id);

-- elections: INDEX elections_federal_district_election_id_idx
CREATE INDEX elections_federal_district_election_id_idx ON elections.elections_federal_district USING hash (election_id);

-- elections: INDEX elections_federal_district_federal_district_id_idx
CREATE INDEX elections_federal_district_federal_district_id_idx ON elections.elections_federal_district USING hash (federal_district_id);

-- elections: INDEX elections_id_idx
CREATE INDEX elections_id_idx ON elections.elections USING btree (id);

-- elections: INDEX elections_local_district_candidate_id_idx
CREATE INDEX elections_local_district_candidate_id_idx ON elections.elections_local_district USING hash (candidate_id);

-- elections: INDEX elections_local_district_election_id_idx
CREATE INDEX elections_local_district_election_id_idx ON elections.elections_local_district USING hash (election_id);

-- elections: INDEX elections_local_district_local_district_id_idx
CREATE INDEX elections_local_district_local_district_id_idx ON elections.elections_local_district USING hash (local_district_id);

-- elections: INDEX elections_municipality_candidate_id_idx
CREATE INDEX elections_municipality_candidate_id_idx ON elections.elections_municipality USING hash (candidate_id);

-- elections: INDEX elections_municipality_election_id_idx
CREATE INDEX elections_municipality_election_id_idx ON elections.elections_municipality USING hash (election_id);

-- elections: INDEX elections_municipality_municipality_id_idx
CREATE INDEX elections_municipality_municipality_id_idx ON elections.elections_municipality USING hash (municipality_id);

-- elections: INDEX elections_voter_list_election_id_idx
CREATE INDEX elections_voter_list_election_id_idx ON elections.elections_voter_list USING hash (election_id);

-- elections: INDEX elections_voter_list_user_auth_id_idx
CREATE INDEX elections_voter_list_user_auth_id_idx ON elections.elections_voter_list USING hash (user_auth_id);

-- elections: INDEX political_parties_id_idx
CREATE INDEX political_parties_id_idx ON elections.political_parties USING btree (id);

-- elections: INDEX political_parties_political_party_type_id_idx
CREATE INDEX political_parties_political_party_type_id_idx ON elections.political_parties USING hash (political_party_type_id);

-- elections: INDEX polling_booths_election_assoc_id_polling_booth_id_election__idx
CREATE INDEX polling_booths_election_assoc_id_polling_booth_id_election__idx ON elections.polling_booths_election_association USING btree (id, polling_booth_id, election_id);

-- elections: INDEX polling_booths_election_association_election_id_idx
CREATE INDEX polling_booths_election_association_election_id_idx ON elections.polling_booths_election_association USING hash (election_id);

-- elections: INDEX polling_booths_election_association_polling_booth_id_idx
CREATE INDEX polling_booths_election_association_polling_booth_id_idx ON elections.polling_booths_election_association USING hash (polling_booth_id);

-- elections: INDEX polling_booths_federal_district_idx
CREATE INDEX polling_booths_federal_district_idx ON elections.polling_booths USING hash (federal_district);

-- elections: INDEX polling_booths_id_idx
CREATE INDEX polling_booths_id_idx ON elections.polling_booths USING btree (id);

-- elections: INDEX polling_booths_issues_polling_booth_id_idx
CREATE INDEX polling_booths_issues_polling_booth_id_idx ON elections.polling_booths_issues USING hash (polling_booth_id);

-- elections: INDEX polling_booths_issues_sub_category_id_idx
CREATE INDEX polling_booths_issues_sub_category_id_idx ON elections.polling_booths_issues USING hash (sub_category_id);

-- elections: INDEX polling_booths_local_district_idx
CREATE INDEX polling_booths_local_district_idx ON elections.polling_booths USING hash (local_district);

-- elections: INDEX polling_booths_municipality_id_idx
CREATE INDEX polling_booths_municipality_id_idx ON elections.polling_booths USING hash (municipality_id);

-- elections: INDEX polling_booths_seccion_number_idx
CREATE INDEX polling_booths_seccion_number_idx ON elections.polling_booths USING hash (seccion_number);

-- elections_results: INDEX election_result_candidate_id_idx
CREATE INDEX election_result_candidate_id_idx ON elections_results.election_result USING hash (candidate_id);

-- elections_results: INDEX election_result_candidate_party_association_id_idx
CREATE INDEX election_result_candidate_party_association_id_idx ON elections_results.election_result USING hash (candidate_party_association_id);

-- elections_results: INDEX election_result_election_association_id_idx
CREATE INDEX election_result_election_association_id_idx ON elections_results.election_result USING hash (election_association_id);

-- elections_results: INDEX election_result_political_party_id_idx
CREATE INDEX election_result_political_party_id_idx ON elections_results.election_result USING hash (political_party_id);

-- elections_results: INDEX election_result_sheets_candidate_id_idx
CREATE INDEX election_result_sheets_candidate_id_idx ON elections_results.election_result_sheets USING hash (candidate_id);

-- elections_results: INDEX election_result_sheets_candidate_party_association_id_idx
CREATE INDEX election_result_sheets_candidate_party_association_id_idx ON elections_results.election_result_sheets USING hash (candidate_party_association_id);

-- elections_results: INDEX election_result_sheets_election_association_id_idx
CREATE INDEX election_result_sheets_election_association_id_idx ON elections_results.election_result_sheets USING hash (election_association_id);

-- elections_results: INDEX election_result_sheets_political_party_id_idx
CREATE INDEX election_result_sheets_political_party_id_idx ON elections_results.election_result_sheets USING hash (political_party_id);

-- electoral_history: INDEX candidacy_uq
CREATE UNIQUE INDEX candidacy_uq ON electoral_history.candidacy USING btree (election_id, COALESCE(territory, ''::character varying), code);

-- electoral_history: INDEX mv_section_group_uq
CREATE UNIQUE INDEX mv_section_group_uq ON electoral_history.mv_section_group USING btree (election_id, municipality_cve, COALESCE((local_district)::integer, 0), COALESCE((federal_district)::integer, 0), section, group_by, code);

-- electoral_history: INDEX mv_section_turnout_uq
CREATE UNIQUE INDEX mv_section_turnout_uq ON electoral_history.mv_section_turnout USING btree (election_id, municipality_cve, COALESCE((local_district)::integer, 0), COALESCE((federal_district)::integer, 0), section);

-- electoral_history: INDEX official_total_territory_uq
CREATE UNIQUE INDEX official_total_territory_uq ON electoral_history.official_total USING btree (election_id, COALESCE((municipality_cve)::integer, '-1'::integer), COALESCE(district, ''::character varying), code);

-- electoral_history: INDEX polling_station_mun_idx
CREATE INDEX polling_station_mun_idx ON electoral_history.polling_station USING btree (election_id, municipality_cve);

-- electoral_history: INDEX polling_station_section_idx
CREATE INDEX polling_station_section_idx ON electoral_history.polling_station USING btree (election_id, section);


-- ===== fks =====

-- structure: FK CONSTRAINT assignment assignment_created_by_person_id_fkey
ALTER TABLE ONLY structure.assignment
    ADD CONSTRAINT assignment_created_by_person_id_fkey FOREIGN KEY (created_by_person_id) REFERENCES structure.person(id);

-- structure: FK CONSTRAINT assignment assignment_person_id_fkey
ALTER TABLE ONLY structure.assignment
    ADD CONSTRAINT assignment_person_id_fkey FOREIGN KEY (person_id) REFERENCES structure.person(id);

-- structure: FK CONSTRAINT assignment assignment_position_id_fkey
ALTER TABLE ONLY structure.assignment
    ADD CONSTRAINT assignment_position_id_fkey FOREIGN KEY (position_id) REFERENCES structure."position"(id);

-- structure: FK CONSTRAINT campaign_election campaign_election_campaign_id_fkey
ALTER TABLE ONLY structure.campaign_election
    ADD CONSTRAINT campaign_election_campaign_id_fkey FOREIGN KEY (campaign_id) REFERENCES structure.campaign(id);

-- structure: FK CONSTRAINT campaign campaign_territory_id_fkey
ALTER TABLE ONLY structure.campaign
    ADD CONSTRAINT campaign_territory_id_fkey FOREIGN KEY (territory_id) REFERENCES structure.territory(id);

-- structure: FK CONSTRAINT change_log change_log_actor_person_id_fkey
ALTER TABLE ONLY structure.change_log
    ADD CONSTRAINT change_log_actor_person_id_fkey FOREIGN KEY (actor_person_id) REFERENCES structure.person(id);

-- structure: FK CONSTRAINT membership membership_captured_by_assignment_id_fkey
ALTER TABLE ONLY structure.membership
    ADD CONSTRAINT membership_captured_by_assignment_id_fkey FOREIGN KEY (captured_by_assignment_id) REFERENCES structure.assignment(id);

-- structure: FK CONSTRAINT membership_detail membership_detail_membership_id_fkey
ALTER TABLE ONLY structure.membership_detail
    ADD CONSTRAINT membership_detail_membership_id_fkey FOREIGN KEY (membership_id) REFERENCES structure.membership(id) ON DELETE CASCADE;

-- structure: FK CONSTRAINT membership membership_person_id_fkey
ALTER TABLE ONLY structure.membership
    ADD CONSTRAINT membership_person_id_fkey FOREIGN KEY (person_id) REFERENCES structure.person(id);

-- structure: FK CONSTRAINT membership membership_position_id_fkey
ALTER TABLE ONLY structure.membership
    ADD CONSTRAINT membership_position_id_fkey FOREIGN KEY (position_id) REFERENCES structure."position"(id);

-- structure: FK CONSTRAINT membership membership_structure_id_fkey
ALTER TABLE ONLY structure.membership
    ADD CONSTRAINT membership_structure_id_fkey FOREIGN KEY (structure_id) REFERENCES structure.structure(id);

-- structure: FK CONSTRAINT membership membership_territory_id_fkey
ALTER TABLE ONLY structure.membership
    ADD CONSTRAINT membership_territory_id_fkey FOREIGN KEY (territory_id) REFERENCES structure.territory(id);

-- structure: FK CONSTRAINT position position_parent_position_id_fkey
ALTER TABLE ONLY structure."position"
    ADD CONSTRAINT position_parent_position_id_fkey FOREIGN KEY (parent_position_id) REFERENCES structure."position"(id);

-- structure: FK CONSTRAINT position position_structure_id_fkey
ALTER TABLE ONLY structure."position"
    ADD CONSTRAINT position_structure_id_fkey FOREIGN KEY (structure_id) REFERENCES structure.structure(id);

-- structure: FK CONSTRAINT position position_territory_id_fkey
ALTER TABLE ONLY structure."position"
    ADD CONSTRAINT position_territory_id_fkey FOREIGN KEY (territory_id) REFERENCES structure.territory(id);

-- structure: FK CONSTRAINT structure structure_campaign_id_fkey
ALTER TABLE ONLY structure.structure
    ADD CONSTRAINT structure_campaign_id_fkey FOREIGN KEY (campaign_id) REFERENCES structure.campaign(id);

-- structure: FK CONSTRAINT structure structure_owner_person_id_fkey
ALTER TABLE ONLY structure.structure
    ADD CONSTRAINT structure_owner_person_id_fkey FOREIGN KEY (owner_person_id) REFERENCES structure.person(id);

-- structure: FK CONSTRAINT territory territory_parent_territory_id_fkey
ALTER TABLE ONLY structure.territory
    ADD CONSTRAINT territory_parent_territory_id_fkey FOREIGN KEY (parent_territory_id) REFERENCES structure.territory(id);

-- users: FK CONSTRAINT users fk79f8hf37ce774da8dwmwb4bvn
ALTER TABLE ONLY users.users
    ADD CONSTRAINT fk79f8hf37ce774da8dwmwb4bvn FOREIGN KEY (coordinator_group_id) REFERENCES users.coordinators_group(id);

-- canvas_and_walls: FK CONSTRAINT canvas_and_walls fk2cneix8sotrhj3y5cfcefak76
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls
    ADD CONSTRAINT fk2cneix8sotrhj3y5cfcefak76 FOREIGN KEY (canvas_and_wall_proyect_id) REFERENCES canvas_and_walls.canvas_and_walls_proyects(id);

-- canvas_and_walls: FK CONSTRAINT canvas_and_walls_goals fkgebfbg9bdetqvig30d9uk2352
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls_goals
    ADD CONSTRAINT fkgebfbg9bdetqvig30d9uk2352 FOREIGN KEY (canvas_and_wall_category_id) REFERENCES canvas_and_walls.canvas_and_walls_category(id);

-- canvas_and_walls: FK CONSTRAINT canvas_and_walls_goals fkoj68qarvfgp4usgc4a3yl3j1n
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls_goals
    ADD CONSTRAINT fkoj68qarvfgp4usgc4a3yl3j1n FOREIGN KEY (canvas_and_wall_proyect_id) REFERENCES canvas_and_walls.canvas_and_walls_proyects(id);

-- canvas_and_walls: FK CONSTRAINT canvas_and_walls fkonfsfqw074nnr2nell189wglv
ALTER TABLE ONLY canvas_and_walls.canvas_and_walls
    ADD CONSTRAINT fkonfsfqw074nnr2nell189wglv FOREIGN KEY (canvas_and_wall_category_id) REFERENCES canvas_and_walls.canvas_and_walls_category(id);

-- document_management: FK CONSTRAINT position_person fk4v7hq7jpmgyntroffujdcl7x4
ALTER TABLE ONLY document_management.position_person
    ADD CONSTRAINT fk4v7hq7jpmgyntroffujdcl7x4 FOREIGN KEY (person_id) REFERENCES document_management.person(id);

-- document_management: FK CONSTRAINT receiver fk8l787ev25pm6ojfmjsycd1fmo
ALTER TABLE ONLY document_management.receiver
    ADD CONSTRAINT fk8l787ev25pm6ojfmjsycd1fmo FOREIGN KEY (position_person_id) REFERENCES document_management.position_person(id);

-- document_management: FK CONSTRAINT position fk9xyxt0dp0dbscxnmc9c77hp4s
ALTER TABLE ONLY document_management."position"
    ADD CONSTRAINT fk9xyxt0dp0dbscxnmc9c77hp4s FOREIGN KEY (position_type_id) REFERENCES document_management.position_type(id);

-- document_management: FK CONSTRAINT document fkbfuj24i2nk4c9cph8nn0cxo8s
ALTER TABLE ONLY document_management.document
    ADD CONSTRAINT fkbfuj24i2nk4c9cph8nn0cxo8s FOREIGN KEY (receiver_id) REFERENCES document_management.receiver(id);

-- document_management: FK CONSTRAINT sender fkoa82ogr66kymrg8w6omc3tn8u
ALTER TABLE ONLY document_management.sender
    ADD CONSTRAINT fkoa82ogr66kymrg8w6omc3tn8u FOREIGN KEY (position_person_id) REFERENCES document_management.position_person(id);

-- document_management: FK CONSTRAINT position fkr8j84p16u8qky5y5clbote9my
ALTER TABLE ONLY document_management."position"
    ADD CONSTRAINT fkr8j84p16u8qky5y5clbote9my FOREIGN KEY (departament_id) REFERENCES document_management.departament(id);

-- document_management: FK CONSTRAINT document fkrwo9sf4lrasgp8mg4wyedjbvj
ALTER TABLE ONLY document_management.document
    ADD CONSTRAINT fkrwo9sf4lrasgp8mg4wyedjbvj FOREIGN KEY (sender_id) REFERENCES document_management.sender(id);

-- document_management: FK CONSTRAINT position_person fksh2ksykdeki82jbtu2de6pyct
ALTER TABLE ONLY document_management.position_person
    ADD CONSTRAINT fksh2ksykdeki82jbtu2de6pyct FOREIGN KEY (position_id) REFERENCES document_management."position"(id);

-- elections: FK CONSTRAINT polling_booths fk70s57sm5x8p7jniw4r6d3wfut
ALTER TABLE ONLY elections.polling_booths
    ADD CONSTRAINT fk70s57sm5x8p7jniw4r6d3wfut FOREIGN KEY (polling_booth_type_id) REFERENCES elections.polling_booths_types(id);

-- elections: FK CONSTRAINT polling_booths_issues fkaj31y5ipa39e84t5g8vx0egwh
ALTER TABLE ONLY elections.polling_booths_issues
    ADD CONSTRAINT fkaj31y5ipa39e84t5g8vx0egwh FOREIGN KEY (sub_category_id) REFERENCES elections.polling_booths_issues_sub_categories(id);

-- elections: FK CONSTRAINT elections fkbkkcwen2iwc29fje7yow15hrw
ALTER TABLE ONLY elections.elections
    ADD CONSTRAINT fkbkkcwen2iwc29fje7yow15hrw FOREIGN KEY (election_year_id) REFERENCES elections.elections_years(id);

-- elections: FK CONSTRAINT polling_booths fkjyd5lgixrwvf7vxluiee1gg8o
ALTER TABLE ONLY elections.polling_booths
    ADD CONSTRAINT fkjyd5lgixrwvf7vxluiee1gg8o FOREIGN KEY (polling_booth_status_id) REFERENCES elections.polling_booths_status(id);

-- elections: FK CONSTRAINT elections fkkmmoalui11shgk0es8mt44co3
ALTER TABLE ONLY elections.elections
    ADD CONSTRAINT fkkmmoalui11shgk0es8mt44co3 FOREIGN KEY (election_position_id) REFERENCES elections.elections_positions(id);

-- elections: FK CONSTRAINT political_parties fkkq5yd69n0uqfl63p2x829bbuu
ALTER TABLE ONLY elections.political_parties
    ADD CONSTRAINT fkkq5yd69n0uqfl63p2x829bbuu FOREIGN KEY (political_party_type_id) REFERENCES elections.political_parties_type(id);

-- elections: FK CONSTRAINT polling_booths_issues_sub_categories fkm2ex6adty88jr3dq1g8nehinr
ALTER TABLE ONLY elections.polling_booths_issues_sub_categories
    ADD CONSTRAINT fkm2ex6adty88jr3dq1g8nehinr FOREIGN KEY (category_id) REFERENCES elections.polling_booths_issues_categories(id);

-- elections: FK CONSTRAINT polling_booths fkmdenw4qnddwo46h242w4bicwx
ALTER TABLE ONLY elections.polling_booths
    ADD CONSTRAINT fkmdenw4qnddwo46h242w4bicwx FOREIGN KEY (polling_booth_category_id) REFERENCES elections.polling_booths_categories(id);

-- elections: FK CONSTRAINT candidates fkmdu3yssghrl36wth97l9b9a9d
ALTER TABLE ONLY elections.candidates
    ADD CONSTRAINT fkmdu3yssghrl36wth97l9b9a9d FOREIGN KEY (election_id) REFERENCES elections.elections(id);

-- elections: FK CONSTRAINT polling_booths fkn4l427fifeq4s898h37cf45nq
ALTER TABLE ONLY elections.polling_booths
    ADD CONSTRAINT fkn4l427fifeq4s898h37cf45nq FOREIGN KEY (polling_booth_address_type_id) REFERENCES elections.polling_booths_address_type(id);

-- elections: FK CONSTRAINT candidates_party_associations fkp5rwy8ncv2e6nybnyaf94avkk
ALTER TABLE ONLY elections.candidates_party_associations
    ADD CONSTRAINT fkp5rwy8ncv2e6nybnyaf94avkk FOREIGN KEY (candidate_id) REFERENCES elections.candidates(id);

-- elections: FK CONSTRAINT polling_booths_election_association fkqcbwsysrljxm50c94bu3051w6
ALTER TABLE ONLY elections.polling_booths_election_association
    ADD CONSTRAINT fkqcbwsysrljxm50c94bu3051w6 FOREIGN KEY (election_id) REFERENCES elections.elections(id);

-- elections: FK CONSTRAINT candidates_party_associations fkrhwnb62pwljl7oseiac3grgva
ALTER TABLE ONLY elections.candidates_party_associations
    ADD CONSTRAINT fkrhwnb62pwljl7oseiac3grgva FOREIGN KEY (political_party_id) REFERENCES elections.political_parties(id);

-- elections: FK CONSTRAINT elections fkry13xod05eo2brf0d654rk71v
ALTER TABLE ONLY elections.elections
    ADD CONSTRAINT fkry13xod05eo2brf0d654rk71v FOREIGN KEY (election_type_id) REFERENCES elections.elections_types(id);

-- elections_structure: FK CONSTRAINT routes fk96hyk4kvqpigf0mbq366gwd9m
ALTER TABLE ONLY elections_structure.routes
    ADD CONSTRAINT fk96hyk4kvqpigf0mbq366gwd9m FOREIGN KEY (polling_representative_general_id) REFERENCES elections_structure.pollings_representatives(id);

-- elections_structure: FK CONSTRAINT pollings_representatives_coordinators fkgemm54rxkvyxnkq7g7yjmj1ff
ALTER TABLE ONLY elections_structure.pollings_representatives_coordinators
    ADD CONSTRAINT fkgemm54rxkvyxnkq7g7yjmj1ff FOREIGN KEY (polling_representative_coordinator_id) REFERENCES elections_structure.pollings_representatives(id);

-- elections_structure: FK CONSTRAINT pollings_representatives_coordinators fkhr21tsaxmm16597a56bbbuoj9
ALTER TABLE ONLY elections_structure.pollings_representatives_coordinators
    ADD CONSTRAINT fkhr21tsaxmm16597a56bbbuoj9 FOREIGN KEY (polling_representative_general_id) REFERENCES elections_structure.pollings_representatives(id);

-- elections_structure: FK CONSTRAINT routes_assignment fkiv9bsooa4cx0yxrg4d7ss391p
ALTER TABLE ONLY elections_structure.routes_assignment
    ADD CONSTRAINT fkiv9bsooa4cx0yxrg4d7ss391p FOREIGN KEY (route_id) REFERENCES elections_structure.routes(id);

-- elections_structure: FK CONSTRAINT pollings_representatives fkte3gg27cdtfmxvpv9xcdae8is
ALTER TABLE ONLY elections_structure.pollings_representatives
    ADD CONSTRAINT fkte3gg27cdtfmxvpv9xcdae8is FOREIGN KEY (cat_sex) REFERENCES elections_structure.cat_sex(id);

-- elections_structure: FK CONSTRAINT pollings_representatives pollings_representatives_polling_representative_type_id_fkey
ALTER TABLE ONLY elections_structure.pollings_representatives
    ADD CONSTRAINT pollings_representatives_polling_representative_type_id_fkey FOREIGN KEY (polling_representative_type_id) REFERENCES elections_structure.pollings_representatives_types(id);

-- electoral_history: FK CONSTRAINT ballot_option ballot_option_election_id_fkey
ALTER TABLE ONLY electoral_history.ballot_option
    ADD CONSTRAINT ballot_option_election_id_fkey FOREIGN KEY (election_id) REFERENCES electoral_history.election(id);

-- electoral_history: FK CONSTRAINT bloc_party bloc_party_bloc_code_fkey
ALTER TABLE ONLY electoral_history.bloc_party
    ADD CONSTRAINT bloc_party_bloc_code_fkey FOREIGN KEY (bloc_code) REFERENCES electoral_history.bloc(code);

-- electoral_history: FK CONSTRAINT bloc_party bloc_party_party_code_fkey
ALTER TABLE ONLY electoral_history.bloc_party
    ADD CONSTRAINT bloc_party_party_code_fkey FOREIGN KEY (party_code) REFERENCES electoral_history.party(code);

-- electoral_history: FK CONSTRAINT candidacy candidacy_election_id_fkey
ALTER TABLE ONLY electoral_history.candidacy
    ADD CONSTRAINT candidacy_election_id_fkey FOREIGN KEY (election_id) REFERENCES electoral_history.election(id);

-- electoral_history: FK CONSTRAINT election election_source_id_fkey
ALTER TABLE ONLY electoral_history.election
    ADD CONSTRAINT election_source_id_fkey FOREIGN KEY (source_id) REFERENCES electoral_history.source(id);

-- electoral_history: FK CONSTRAINT official_total official_total_election_id_fkey
ALTER TABLE ONLY electoral_history.official_total
    ADD CONSTRAINT official_total_election_id_fkey FOREIGN KEY (election_id) REFERENCES electoral_history.election(id);

-- electoral_history: FK CONSTRAINT official_total official_total_municipality_cve_fkey
ALTER TABLE ONLY electoral_history.official_total
    ADD CONSTRAINT official_total_municipality_cve_fkey FOREIGN KEY (municipality_cve) REFERENCES electoral_history.municipality(cve);

-- electoral_history: FK CONSTRAINT polling_station polling_station_election_id_fkey
ALTER TABLE ONLY electoral_history.polling_station
    ADD CONSTRAINT polling_station_election_id_fkey FOREIGN KEY (election_id) REFERENCES electoral_history.election(id);

-- electoral_history: FK CONSTRAINT polling_station polling_station_municipality_cve_fkey
ALTER TABLE ONLY electoral_history.polling_station
    ADD CONSTRAINT polling_station_municipality_cve_fkey FOREIGN KEY (municipality_cve) REFERENCES electoral_history.municipality(cve);

-- electoral_history: FK CONSTRAINT section_municipality section_municipality_municipality_cve_fkey
ALTER TABLE ONLY electoral_history.section_municipality
    ADD CONSTRAINT section_municipality_municipality_cve_fkey FOREIGN KEY (municipality_cve) REFERENCES electoral_history.municipality(cve);

-- electoral_history: FK CONSTRAINT station_vote station_vote_option_id_fkey
ALTER TABLE ONLY electoral_history.station_vote
    ADD CONSTRAINT station_vote_option_id_fkey FOREIGN KEY (option_id) REFERENCES electoral_history.ballot_option(id);

-- electoral_history: FK CONSTRAINT station_vote station_vote_station_id_fkey
ALTER TABLE ONLY electoral_history.station_vote
    ADD CONSTRAINT station_vote_station_id_fkey FOREIGN KEY (station_id) REFERENCES electoral_history.polling_station(id) ON DELETE CASCADE;

-- survey: FK CONSTRAINT survey_person fk3epy6udnslyqacoge467plfci
ALTER TABLE ONLY survey.survey_person
    ADD CONSTRAINT fk3epy6udnslyqacoge467plfci FOREIGN KEY (survey_profession_id) REFERENCES survey.survey_profession(id);

-- survey: FK CONSTRAINT survey_person fkfkqi5epvcw67ygd4nbde7m9rf
ALTER TABLE ONLY survey.survey_person
    ADD CONSTRAINT fkfkqi5epvcw67ygd4nbde7m9rf FOREIGN KEY (survey_job_id) REFERENCES survey.survey_job(id);

-- survey: FK CONSTRAINT survey_annex fkowrksi0x2bioabvlfqnqpiobp
ALTER TABLE ONLY survey.survey_annex
    ADD CONSTRAINT fkowrksi0x2bioabvlfqnqpiobp FOREIGN KEY (survey_requesting_or_auxiliar_document_id) REFERENCES survey.survey_requesting_or_auxiliary_document(id);

-- visits: FK CONSTRAINT visited_people fk41mbvsluhwnmsq8ypsjhf4ckx
ALTER TABLE ONLY visits.visited_people
    ADD CONSTRAINT fk41mbvsluhwnmsq8ypsjhf4ckx FOREIGN KEY (visited_id) REFERENCES visits.visited(id);


-- ===== triggers =====

-- structure: TRIGGER assignment campaign_closed_guard_trg
CREATE TRIGGER campaign_closed_guard_trg BEFORE INSERT OR DELETE OR UPDATE ON structure.assignment FOR EACH ROW EXECUTE FUNCTION structure.campaign_closed_guard();

-- structure: TRIGGER membership campaign_closed_guard_trg
CREATE TRIGGER campaign_closed_guard_trg BEFORE INSERT OR DELETE OR UPDATE ON structure.membership FOR EACH ROW EXECUTE FUNCTION structure.campaign_closed_guard();

-- structure: TRIGGER position campaign_closed_guard_trg
CREATE TRIGGER campaign_closed_guard_trg BEFORE INSERT OR DELETE OR UPDATE ON structure."position" FOR EACH ROW EXECUTE FUNCTION structure.campaign_closed_guard();

-- structure: TRIGGER membership membership_protect_lineage_trg
CREATE TRIGGER membership_protect_lineage_trg BEFORE UPDATE ON structure.membership FOR EACH ROW EXECUTE FUNCTION structure.membership_protect_lineage();

-- structure: TRIGGER membership membership_same_structure_trg
CREATE TRIGGER membership_same_structure_trg BEFORE INSERT OR UPDATE OF structure_id, position_id ON structure.membership FOR EACH ROW EXECUTE FUNCTION structure.membership_same_structure();

-- structure: TRIGGER position position_section_key_trg
CREATE TRIGGER position_section_key_trg BEFORE INSERT OR UPDATE OF section_key ON structure."position" FOR EACH ROW EXECUTE FUNCTION structure.position_section_key();

-- structure: TRIGGER position position_validate_hierarchy_trg
CREATE TRIGGER position_validate_hierarchy_trg BEFORE INSERT OR UPDATE OF parent_position_id, territory_id, level, structure_id ON structure."position" FOR EACH ROW EXECUTE FUNCTION structure.position_validate_hierarchy();

-- users: TRIGGER users_seccions users_seccions_delete_trg
CREATE TRIGGER users_seccions_delete_trg INSTEAD OF DELETE ON users.users_seccions FOR EACH ROW EXECUTE FUNCTION users_compat.user_sections_delete();

-- users: TRIGGER users_seccions users_seccions_insert_trg
CREATE TRIGGER users_seccions_insert_trg INSTEAD OF INSERT ON users.users_seccions FOR EACH ROW EXECUTE FUNCTION users_compat.user_sections_insert();

-- users: TRIGGER users_seccions users_seccions_update_trg
CREATE TRIGGER users_seccions_update_trg INSTEAD OF UPDATE ON users.users_seccions FOR EACH ROW EXECUTE FUNCTION users_compat.user_sections_update();

-- followers: TRIGGER followers followers_delete_trg
CREATE TRIGGER followers_delete_trg INSTEAD OF DELETE ON followers.followers FOR EACH ROW EXECUTE FUNCTION followers_compat.followers_delete();

-- followers: TRIGGER followers followers_insert_trg
CREATE TRIGGER followers_insert_trg INSTEAD OF INSERT ON followers.followers FOR EACH ROW EXECUTE FUNCTION followers_compat.followers_insert();

-- followers: TRIGGER followers followers_update_trg
CREATE TRIGGER followers_update_trg INSTEAD OF UPDATE ON followers.followers FOR EACH ROW EXECUTE FUNCTION followers_compat.followers_update();

-- followers: TRIGGER promoters promoters_delete_trg
CREATE TRIGGER promoters_delete_trg INSTEAD OF DELETE ON followers.promoters FOR EACH ROW EXECUTE FUNCTION followers_compat.promoters_delete();

-- followers: TRIGGER promoters promoters_insert_trg
CREATE TRIGGER promoters_insert_trg INSTEAD OF INSERT ON followers.promoters FOR EACH ROW EXECUTE FUNCTION followers_compat.promoters_insert();

-- followers: TRIGGER promoters promoters_update_trg
CREATE TRIGGER promoters_update_trg INSTEAD OF UPDATE ON followers.promoters FOR EACH ROW EXECUTE FUNCTION followers_compat.promoters_update();


-- ===== comments =====

-- structure: COMMENT COLUMN assignment.created_by_auth_id
COMMENT ON COLUMN structure.assignment.created_by_auth_id IS 'Cuenta que dio de alta el nombramiento, tal como venía. Cruda, porque puede no tener persona.';

-- structure: COMMENT COLUMN person.ine_validity
COMMENT ON COLUMN structure.person.ine_validity IS 'Año de vigencia de la credencial de elector. El dato de la persona, no el de una captura.';

-- structure: COMMENT TABLE membership_detail
COMMENT ON TABLE structure.membership_detail IS 'El expediente de captura del convencido: respuestas del levantamiento y rastro de cómo se levantó. Aparte de `membership` para que la adhesión siga siendo persona + estructura + puesto + vigencia.';

-- structure: COMMENT COLUMN territory.federal_district_geo_id
COMMENT ON COLUMN structure.territory.federal_district_geo_id IS 'Referencia al distrito federal del catálogo de geography. Nunca su nombre: geography es el dueño.';

-- structure: COMMENT COLUMN territory.local_district_geo_id
COMMENT ON COLUMN structure.territory.local_district_geo_id IS 'Referencia al distrito local del catálogo de geography.';

-- followers: COMMENT VIEW followers
COMMENT ON VIEW followers.followers IS 'Vista de compatibilidad (fase 3). Lee ÚNICAMENTE msvc_dash_structure; ya no toca followers_legacy. Escribe por triggers INSTEAD OF.';

-- followers: COMMENT TABLE followers_legacy
COMMENT ON TABLE followers.followers_legacy IS 'Tabla original de convencidos, congelada en la fase 3. Conserva las columnas que el modelo nuevo no posee (distritos, catalogos, uuid, coordenadas): la vista `followers` las pega por id. Las columnas estructurales de las filas anteriores al corte quedan como estaban y ya no se leen.';

-- followers: COMMENT VIEW promoters
COMMENT ON VIEW followers.promoters IS 'Vista de compatibilidad (fase 3). Lee ÚNICAMENTE msvc_dash_structure; ya no toca promoters_legacy. Escribe por triggers INSTEAD OF.';
