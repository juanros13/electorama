-- Electorama: las vistas compat del flujo 2024 (users.users_seccions,
-- followers.followers, followers.promoters) escriben en la campaña ACTIVE
-- que contiene la sección.
--
-- Hasta V6 elegían "la primera estructura MAIN/ALTERNATE" (ORDER BY id LIMIT 1),
-- que es la de Tabasco 2024: cerrada, así que campaign_closed_guard rechazaba
-- toda alta, y en otro estado habría caído en Tabasco. Las búsquedas de
-- "¿ya tiene puesto?" se limitan ahora a esa estructura.
--
-- Además, desde V3 sólo la cúspide puede ir sin padre: un referente que no es
-- la cúspide (hay un nivel arriba, p.ej. coordinación estatal) o un
-- coordinador sin referente conocido cuelgan del único puesto vigente del
-- nivel inmediato superior que contiene su territorio (compat_parent).

-- la estructura MAIN/ALTERNATE de la campaña activa más reciente que contiene
-- la sección; NULL si no hay
CREATE FUNCTION structure.active_structure(p_sec_terr bigint, p_alternate boolean) RETURNS bigint
    LANGUAGE sql STABLE
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
  SELECT s.id
    FROM campaign c
    JOIN structure s ON s.campaign_id = c.id
                    AND s.type = CASE WHEN p_alternate THEN 'ALTERNATE' ELSE 'MAIN' END
   WHERE c.id = (SELECT c2.id FROM campaign c2
                  WHERE c2.status = 'ACTIVE'
                    AND territory_contained_in(p_sec_terr, c2.territory_id)
                  ORDER BY c2.id DESC LIMIT 1)
   ORDER BY s.id LIMIT 1;
$$;

-- igual, pero sin campaña o sin esa rama es un error
CREATE FUNCTION structure.require_active_structure(p_sec_terr bigint, p_alternate boolean) RETURNS bigint
    LANGUAGE plpgsql STABLE
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_campaign  bigint;
  v_structure bigint;
BEGIN
  SELECT c.id INTO v_campaign FROM campaign c
   WHERE c.status = 'ACTIVE' AND territory_contained_in(p_sec_terr, c.territory_id)
   ORDER BY c.id DESC LIMIT 1;
  IF v_campaign IS NULL THEN
    RAISE EXCEPTION 'no hay campaña activa para la sección (territorio %)', p_sec_terr
      USING ERRCODE = 'check_violation';
  END IF;
  v_structure := active_structure(p_sec_terr, p_alternate);
  IF v_structure IS NULL THEN
    RAISE EXCEPTION 'la campaña % no tiene estructura %', v_campaign,
      CASE WHEN p_alternate THEN 'Alterna' ELSE 'Principal' END
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN v_structure;
END;
$$;

-- el padre de un puesto nuevo de la función p_function sobre p_territory: NULL
-- si ese nivel es la cúspide; si no, el único puesto vigente del nivel
-- inmediato superior que contiene el territorio
CREATE FUNCTION structure.compat_parent(p_structure bigint, p_function text, p_territory bigint) RETURNS bigint
    LANGUAGE plpgsql STABLE
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
  v_campaign bigint;
  v_rank     smallint;
  v_above    smallint;
  v_ids      bigint[];
BEGIN
  SELECT l.campaign_id, l.rank INTO v_campaign, v_rank
    FROM structure s JOIN campaign_level l ON l.campaign_id = s.campaign_id
   WHERE s.id = p_structure AND l.level_function = p_function;
  IF v_rank IS NULL THEN
    RAISE EXCEPTION 'la campaña de la estructura % no tiene un nivel con función %', p_structure, p_function
      USING ERRCODE = 'check_violation';
  END IF;

  SELECT max(l.rank) INTO v_above FROM campaign_level l
   WHERE l.campaign_id = v_campaign AND l.rank < v_rank;
  IF v_above IS NULL THEN
    RETURN NULL;
  END IF;

  SELECT array_agg(p.id) INTO v_ids
    FROM position p JOIN campaign_level l ON l.id = p.level_id
   WHERE p.structure_id = p_structure AND p.valid_to IS NULL
     AND l.rank = v_above
     AND territory_contained_in(p_territory, p.territory_id);
  IF coalesce(array_length(v_ids, 1), 0) <> 1 THEN
    RAISE EXCEPTION 'no se sabe de qué puesto superior colgar el % sobre el territorio % (% candidatos)',
      p_function, p_territory, coalesce(array_length(v_ids, 1), 0)
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN v_ids[1];
END;
$$;


CREATE OR REPLACE FUNCTION structure_compat.user_section_add(p_auth character varying, p_section integer, p_municipality integer, p_alternate boolean DEFAULT false, p_meta integer DEFAULT NULL::integer, p_limit integer DEFAULT NULL::integer) RETURNS bigint
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

  v_structure := structure.require_active_structure(v_sec_terr, p_alternate);

  -- ¿ya está cubierta, por el puesto de sección o por el municipal?
  SELECT p.id INTO v_pos
    FROM assignment a
    JOIN position p ON p.id = a.position_id
   WHERE a.auth_id = p_auth
     AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
     AND p.level = 'ZONE_COORDINATOR'
     AND p.structure_id = v_structure
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
  -- no se sabe quién es el referente, se busca con compat_parent.
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
      INSERT INTO position (structure_id, level, parent_position_id, territory_id, code, valid_from)
      VALUES (v_structure, 'REFERENT', structure.compat_parent(v_structure, 'REFERENT', v_mun_terr),
              v_mun_terr,
              'REF|' || (SELECT code FROM territory WHERE id = v_mun_terr) || '|' || v_ref_auth,
              now())
      RETURNING id INTO v_parent;

      INSERT INTO assignment (position_id, person_id, role, auth_id, validity, start_reason)
      SELECT v_parent, pe.id, 'HOLDER', v_ref_auth, tstzrange(now(), NULL),
             'alta por vista de compatibilidad (users_seccions)'
      FROM person pe WHERE pe.auth_id = v_ref_auth;
    END IF;
  END IF;
  v_parent := COALESCE(v_parent, structure.compat_parent(v_structure, 'ZONE_COORDINATOR', v_sec_terr));

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


CREATE OR REPLACE FUNCTION users_compat.user_sections_insert() RETURNS trigger
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

  v_structure := structure.require_active_structure(v_sec_terr, v_alternate);

  -- ¿ya está cubierta? el puesto de sección gana al municipal
  SELECT p.id INTO v_pos
    FROM structure."assignment" a
    JOIN structure."position" p ON p.id = a.position_id
   WHERE a.auth_id = NEW.user_auth_id
     AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
     AND p.level = 'ZONE_COORDINATOR'
     AND p.structure_id = v_structure
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
  -- existe todavía, se crea; y si no se sabe quién es el referente, se
  -- busca con compat_parent.
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
      INSERT INTO structure."position" (id, structure_id, level, parent_position_id, territory_id,
                                        code, valid_from)
      VALUES (v_parent, v_structure, 'REFERENT',
              structure.compat_parent(v_structure, 'REFERENT', v_mun_terr), v_mun_terr,
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
  v_parent := COALESCE(v_parent, structure.compat_parent(v_structure, 'ZONE_COORDINATOR', v_sec_terr));

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


CREATE OR REPLACE FUNCTION followers_compat.position_for_coordinator(p_cz_auth character varying, p_ref_auth character varying, p_section bigint, p_municipality bigint, p_alternate boolean) RETURNS bigint
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
  v_sec_terr := followers_compat.territory_for_section(p_section);
  IF v_sec_terr IS NULL THEN
    RAISE EXCEPTION 'La sección % no existe en el catálogo de territorios', p_section;
  END IF;
  SELECT parent_territory_id INTO v_mun_terr FROM structure."territory" WHERE id = v_sec_terr;
  -- si la sección quedó colgando sin municipio, se usa el que trae el alta
  v_mun_terr := COALESCE(v_mun_terr, followers_compat.territory_for_municipality(p_municipality));

  v_structure := structure.require_active_structure(v_sec_terr, p_alternate);

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
    INSERT INTO structure."position" (id, structure_id, level, parent_position_id, territory_id, code, valid_from)
    VALUES (v_ref, v_structure, 'REFERENT',
            structure.compat_parent(v_structure, 'REFERENT', v_mun_terr), v_mun_terr,
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
  v_ref := COALESCE(v_ref, structure.compat_parent(v_structure, 'ZONE_COORDINATOR', v_sec_terr));

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


-- el puesto de promotor se busca sólo en la campaña activa de la sección (o,
-- sin sección, en las campañas activas); si no hay, el POOL como antes
CREATE OR REPLACE FUNCTION followers_compat.position_for_promoter(p_auth character varying, p_alternate boolean, p_section bigint) RETURNS bigint
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE
  v_id        bigint;
  v_structure bigint;
BEGIN
  IF p_auth IS NOT NULL THEN
    IF p_section IS NOT NULL THEN
      v_structure := structure.active_structure(followers_compat.territory_for_section(p_section), p_alternate);
    END IF;

    SELECT p.id INTO v_id
      FROM structure_compat."position_holder" h
      JOIN structure."position"  p ON p.id = h.position_id
      JOIN structure."structure" s ON s.id = p.structure_id
      JOIN structure."campaign"  c ON c.id = s.campaign_id
     WHERE h.auth_id = p_auth
       AND p.level = 'PROMOTER'
       AND p.valid_to IS NULL
       AND s.type = CASE WHEN p_alternate THEN 'ALTERNATE' ELSE 'MAIN' END
       AND c.status = 'ACTIVE'
       AND (v_structure IS NULL OR s.id = v_structure)
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
