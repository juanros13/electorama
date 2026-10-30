-- =====================================================================
-- Electorama — msvc-structure
-- V3d: el corte de `users_seccions` (fase 3b) sobre msvc_dash_user
--
-- Renombra `users_seccions` a `users_seccions_legacy` y la sustituye por
-- una vista del mismo nombre que lee el modelo nuevo por postgres_fdw, con
-- triggers INSTEAD OF para escribir. `msvc-users` sigue funcionando sin
-- tocar una línea: la entidad `UserSeccionEntity`, los tres endpoints de
-- ADMIN y las consultas nativas de los facades de metas.
--
-- Requiere V3c ya ejecutado en msvc_dash_structure.
-- REVERSIBLE: V3dR__rollback_user_sections.sql deshace todo esto.
--
-- LA GRANULARIDAD. En el modelo nuevo un nombramiento es UN puesto; aquí es
-- una fila por sección. El puesto sobre una sección da 1 fila con su meta;
-- el puesto sobre un municipio (el nombramiento de la rama alterna, V2d) se
-- expande en una fila por sección del catálogo. Los ids son sintéticos:
-- `(position_id << 20) | section_geo_id`, para que `PUT /updateUserSection/{id}`
-- y `DELETE /{id}` puedan volver del id al par (puesto, sección).
--
-- ESCRIBIR SOBRE UNA FILA EXPANDIDA MATERIALIZA EL PUESTO. Ponerle meta a
-- una sección de un nombramiento municipal, o quitársela, significa que el
-- ámbito ya dejó de ser el municipio completo: entonces el puesto municipal
-- se sustituye por uno por sección —la inversa de V2d— y los promotores se
-- recuelgan cada uno en el suyo. Cuesta ~130 inserciones, y sólo lo dispara
-- una acción de ADMIN.
--
-- La lógica está escrita dos veces a propósito: aquí contra las tablas
-- foráneas, y en `compat` del lado del modelo (V3c) para que el servicio de
-- msvc-structure la use en la fase 4. postgres_fdw no puede invocar una
-- función del otro lado dentro de la misma transacción, y partir la
-- transacción es peor que repetir el código de una capa que la fase 5 borra.
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 \
--        -v fdw_user=postgres -v fdw_password=… -f V3d__compat_user_sections.sql
-- =====================================================================

\set ON_ERROR_STOP on

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

SET compat.fdw_host = :'fdw_host';
SET compat.fdw_port = :'fdw_port';
SET compat.fdw_user = :'fdw_user';
SET compat.fdw_pw   = :'fdw_password';

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_foreign_server WHERE srvname = 'srv_structure') THEN
    EXECUTE format(
      'CREATE SERVER srv_structure FOREIGN DATA WRAPPER postgres_fdw
         OPTIONS (host %L, port %L, dbname %L)',
      current_setting('compat.fdw_host'),
      current_setting('compat.fdw_port'),
      'msvc_dash_structure');
  END IF;
END $$;

DO $$
DECLARE o text;
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

-- la vista de forma vieja: la expansión del municipio se resuelve del otro lado
IMPORT FOREIGN SCHEMA compat
  LIMIT TO (user_sections, position_holder,
            seq_person, seq_position, seq_assignment)
  FROM SERVER srv_structure INTO str;

-- las tablas reales: es donde escriben los triggers
IMPORT FOREIGN SCHEMA public
  LIMIT TO (person, territory, structure, position, assignment)
  FROM SERVER srv_structure INTO str;

-- una vista remota no tiene ctid: postgres_fdw no puede hacerle UPDATE ni
-- DELETE. Se marca para que el error salga al planear y no a mitad de una
-- transacción de producción.
ALTER FOREIGN TABLE str.user_sections   OPTIONS (ADD updatable 'false');
ALTER FOREIGN TABLE str.position_holder OPTIONS (ADD updatable 'false');

CREATE SCHEMA IF NOT EXISTS compat;

-- ---------------------------------------------------------------------
-- 1. ids y vigencias
--    postgres_fdw manda TODAS las columnas al insertar en una tabla
--    foránea, así que los DEFAULT del otro lado nunca se aplican: el id de
--    cada secuencia y toda columna NOT NULL DEFAULT hay que pasarlos a
--    mano. Por eso `position.valid_from`, `assignment.created`,
--    `assignment.role`, `assignment.validity` y `person.created` van
--    explícitos en cada INSERT de este archivo.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION compat.next_person_id()     RETURNS bigint AS $fn$ SELECT id FROM str.seq_person     $fn$ LANGUAGE sql;
CREATE OR REPLACE FUNCTION compat.next_position_id()   RETURNS bigint AS $fn$ SELECT id FROM str.seq_position   $fn$ LANGUAGE sql;
CREATE OR REPLACE FUNCTION compat.next_assignment_id() RETURNS bigint AS $fn$ SELECT id FROM str.seq_assignment $fn$ LANGUAGE sql;

-- Cerrar una vigencia NO puede usar now(): dentro de una transacción es
-- constante, así que dar de baja algo creado en la misma produce el rango
-- vacío, cuyo upper() es NULL — y la fila se seguiría leyendo como vigente.
CREATE OR REPLACE FUNCTION compat.close_validity(p_range tstzrange, p_at timestamptz DEFAULT NULL)
RETURNS tstzrange AS $fn$
  SELECT tstzrange(lower(p_range),
                   GREATEST(COALESCE(p_at, clock_timestamp()),
                            lower(p_range) + interval '1 microsecond'));
$fn$ LANGUAGE sql VOLATILE;

-- el id sintético de la vista: (puesto << 20) | sección
CREATE OR REPLACE FUNCTION compat.us_id(p_position bigint, p_section integer)
RETURNS bigint AS $fn$ SELECT (p_position << 20) | p_section::bigint $fn$ LANGUAGE sql IMMUTABLE;
CREATE OR REPLACE FUNCTION compat.us_position(p_id bigint)
RETURNS bigint AS $fn$ SELECT p_id >> 20 $fn$ LANGUAGE sql IMMUTABLE;
CREATE OR REPLACE FUNCTION compat.us_section(p_id bigint)
RETURNS integer AS $fn$ SELECT (p_id & 1048575)::integer $fn$ LANGUAGE sql IMMUTABLE;

-- ---------------------------------------------------------------------
-- 2. materializar un nombramiento municipal en puestos de sección
--    Devuelve el puesto que quedó sobre la sección pedida.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION compat.materialize_municipal(p_position bigint, p_section integer)
RETURNS bigint AS $$
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
    FROM str.position p
   WHERE p.id = p_position AND p.level = 'ZONE_COORDINATOR' AND p.valid_to IS NULL;

  IF v_mun_terr IS NULL THEN
    RAISE EXCEPTION 'el puesto % no es un coordinador vigente', p_position;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM str.territory t
                  WHERE t.id = v_mun_terr AND t.type = 'MUNICIPALITY') THEN
    RETURN p_position;   -- ya está a nivel sección
  END IF;

  SELECT a.auth_id, a.person_id INTO v_auth, v_person
    FROM str.assignment a
   WHERE a.position_id = p_position AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
   LIMIT 1;

  FOR r IN
    SELECT sec.id AS territory_id, sec.code, sec.section_geo_id
    FROM str.territory sec
    WHERE sec.parent_territory_id = v_mun_terr
      AND sec.type = 'SECTION'
      AND sec.valid_to IS NULL
  LOOP
    -- si ya hubiera un puesto de ese coordinador en la sección, se respeta
    SELECT p.id INTO v_target
      FROM str.position p
     WHERE p.structure_id = v_structure
       AND p.level = 'ZONE_COORDINATOR'
       AND p.territory_id = r.territory_id
       AND p.valid_to IS NULL
       AND p.code = 'CZ|' || r.code || '|' || v_auth
     LIMIT 1;

    IF v_target IS NULL THEN
      v_target := compat.next_position_id();
      INSERT INTO str.position (id, structure_id, level, parent_position_id,
                                territory_id, code, valid_from)
      VALUES (v_target, v_structure, 'ZONE_COORDINATOR', v_parent,
              r.territory_id, 'CZ|' || r.code || '|' || v_auth, v_from);

      IF v_person IS NOT NULL THEN
        INSERT INTO str.assignment (id, created, position_id, person_id, role,
                                    auth_id, validity, start_reason)
        VALUES (compat.next_assignment_id(), now(), v_target, v_person, 'HOLDER',
                v_auth, tstzrange(v_from, NULL),
                'materialización del nombramiento municipal');
      END IF;
    END IF;

    -- los promotores de esa sección pasan a colgar del puesto de la sección
    UPDATE str.position pr
       SET parent_position_id = v_target
     WHERE pr.parent_position_id = p_position
       AND pr.level = 'PROMOTER'
       AND pr.territory_id = r.territory_id;
  END LOOP;

  -- el municipal se cierra
  UPDATE str.assignment a
     SET validity = compat.close_validity(a.validity, v_at),
         end_reason = 'materialización del nombramiento municipal'
   WHERE a.position_id = p_position AND upper(a.validity) IS NULL;

  UPDATE str.position p
     SET valid_to = GREATEST(v_at, p.valid_from + interval '1 microsecond')
   WHERE p.id = p_position AND p.valid_to IS NULL;

  SELECT p.id INTO v_target
    FROM str.position p
    JOIN str.territory t ON t.id = p.territory_id
   WHERE p.structure_id = v_structure
     AND p.level = 'ZONE_COORDINATOR'
     AND p.valid_to IS NULL
     AND t.section_geo_id = p_section
     AND p.code = 'CZ|' || t.code || '|' || v_auth
   LIMIT 1;

  RETURN v_target;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- 2b. la meta de UNA sección de un nombramiento municipal
--
--     Poner meta a una sección no rompe el nombramiento municipal: crea un
--     puesto sobre esa sección, que convive con el municipal bajo la regla
--     de precedencia de `compat.user_sections` (donde hay puesto de
--     sección, la fila sale de él y el municipio no la repite). Cuesta una
--     inserción en vez de las ~130 de la materialización, y es lo que ya
--     había en el dato: 8 filas de la rama alterna con meta propia (V2f).
--
--     La materialización completa se reserva para la BAJA, que es lo único
--     que no se puede expresar como excepción: quitarle una sección a quien
--     cubre el municipio exige enumerar las que sí cubre.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION compat.us_override(p_id bigint)
RETURNS bigint AS $$
DECLARE
  v_pos       bigint := compat.us_position(p_id);
  v_sec       integer := compat.us_section(p_id);
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
    FROM str.position p
   WHERE p.id = v_pos AND p.level = 'ZONE_COORDINATOR' AND p.valid_to IS NULL;

  IF v_structure IS NULL THEN
    RAISE EXCEPTION 'el puesto % no es un coordinador vigente', v_pos;
  END IF;

  -- ya está a nivel sección: la meta va directa al puesto
  IF EXISTS (SELECT 1 FROM str.territory t
              JOIN str.position p ON p.territory_id = t.id
             WHERE p.id = v_pos AND t.type = 'SECTION') THEN
    RETURN v_pos;
  END IF;

  SELECT a.auth_id, a.person_id INTO v_auth, v_person
    FROM str.assignment a
   WHERE a.position_id = v_pos AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
   LIMIT 1;

  SELECT t.id, 'CZ|' || t.code || '|' || v_auth INTO v_sec_terr, v_code
    FROM str.territory t
   WHERE t.section_geo_id = v_sec AND t.type = 'SECTION' AND t.valid_to IS NULL
   LIMIT 1;

  IF v_sec_terr IS NULL THEN
    RAISE EXCEPTION 'la sección % no existe en el catálogo de territorios', v_sec;
  END IF;

  SELECT p.id INTO v_new FROM str.position p
   WHERE p.structure_id = v_structure
     AND p.level = 'ZONE_COORDINATOR'
     AND p.territory_id = v_sec_terr
     AND p.valid_to IS NULL
     AND p.code = v_code
   LIMIT 1;

  IF v_new IS NULL THEN
    v_new := compat.next_position_id();
    INSERT INTO str.position (id, structure_id, level, parent_position_id,
                              territory_id, code, valid_from)
    VALUES (v_new, v_structure, 'ZONE_COORDINATOR', v_parent, v_sec_terr, v_code, v_from);

    IF v_person IS NOT NULL THEN
      INSERT INTO str.assignment (id, created, position_id, person_id, role,
                                  auth_id, validity, start_reason)
      VALUES (compat.next_assignment_id(), now(), v_new, v_person, 'HOLDER',
              v_auth, tstzrange(v_from, NULL),
              'meta propia sobre una sección del nombramiento municipal');
    END IF;

    -- los promotores de esa sección pasan al puesto específico
    UPDATE str.position pr
       SET parent_position_id = v_new
     WHERE pr.parent_position_id = v_pos
       AND pr.level = 'PROMOTER'
       AND pr.territory_id = v_sec_terr;
  END IF;

  RETURN v_new;
END;
$$ LANGUAGE plpgsql;

-- si la fila viene de un puesto municipal, materializa y devuelve el puesto
-- de la sección; si no, devuelve el mismo puesto
CREATE OR REPLACE FUNCTION compat.us_resolve(p_id bigint)
RETURNS bigint AS $$
DECLARE
  v_pos bigint := compat.us_position(p_id);
  v_sec integer := compat.us_section(p_id);
BEGIN
  IF EXISTS (SELECT 1 FROM str.position p
              JOIN str.territory t ON t.id = p.territory_id
             WHERE p.id = v_pos AND t.type = 'MUNICIPALITY') THEN
    RETURN compat.materialize_municipal(v_pos, v_sec);
  END IF;
  RETURN v_pos;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- 3. el corte
-- ---------------------------------------------------------------------
BEGIN;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'users_seccions_legacy') THEN
    RAISE EXCEPTION 'ya existe users_seccions_legacy: el corte ya se hizo';
  END IF;
END $$;

ALTER TABLE users_seccions RENAME TO users_seccions_legacy;

-- La tabla vieja no tenía llave primaria ni índices más allá del que creó la
-- migración. Se le deja uno por authId para que el rollback y las
-- comparaciones de fidelidad no sean recorridos secuenciales.
CREATE INDEX IF NOT EXISTS users_seccions_legacy_auth_idx
    ON users_seccions_legacy (user_auth_id, seccion_geo_id);

-- ---------------------------------------------------------------------
-- 4. la vista, con la forma exacta de la tabla vieja
-- ---------------------------------------------------------------------
CREATE VIEW users_seccions AS
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
FROM str.user_sections us;

-- ---------------------------------------------------------------------
-- 5. escritura
-- ---------------------------------------------------------------------
-- 5a. alta: `PUT /{authIdCoorZona}/{seccionId}/{seccionNumber}/{municipalityId}`
--     Si la sección ya la cubre un puesto de ese coordinador —el de sección
--     o el municipal— no se crea nada: la fila ya está en la vista. El
--     modelo viejo sí duplicaba en ese caso.
CREATE OR REPLACE FUNCTION compat.user_sections_insert() RETURNS TRIGGER AS $$
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

  SELECT t.id INTO v_sec_terr FROM str.territory t
   WHERE t.section_geo_id = NEW.seccion_geo_id::integer
     AND t.type = 'SECTION' AND t.valid_to IS NULL LIMIT 1;
  IF v_sec_terr IS NULL THEN
    RAISE EXCEPTION 'la sección % no existe en el catálogo de territorios', NEW.seccion_geo_id;
  END IF;

  SELECT t.parent_territory_id INTO v_mun_terr FROM str.territory t WHERE t.id = v_sec_terr;
  v_mun_terr := COALESCE(v_mun_terr,
                         (SELECT t.id FROM str.territory t
                           WHERE t.municipality_geo_id = NEW.municipality_id::integer
                             AND t.type = 'MUNICIPALITY' LIMIT 1));

  -- la rama sale de `users`, que vive en esta misma base
  SELECT COALESCE(u.flag_alter, false) INTO v_alternate
    FROM users u WHERE u.auth_id = NEW.user_auth_id LIMIT 1;
  v_alternate := COALESCE(v_alternate, false);

  SELECT s.id INTO v_structure FROM str.structure s
   WHERE s.type = CASE WHEN v_alternate THEN 'ALTERNATE' ELSE 'MAIN' END
   ORDER BY s.id LIMIT 1;

  -- ¿ya está cubierta? el puesto de sección gana al municipal
  SELECT p.id INTO v_pos
    FROM str.assignment a
    JOIN str.position p ON p.id = a.position_id
   WHERE a.auth_id = NEW.user_auth_id
     AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
     AND p.level = 'ZONE_COORDINATOR'
     AND p.valid_to IS NULL
     AND p.territory_id IN (v_sec_terr, v_mun_terr)
   ORDER BY (p.territory_id = v_sec_terr) DESC
   LIMIT 1;

  IF v_pos IS NOT NULL THEN
    IF NEW.meta IS NOT NULL
       AND EXISTS (SELECT 1 FROM str.position p
                    WHERE p.id = v_pos AND p.territory_id = v_sec_terr) THEN
      UPDATE str.position
         SET goal = NEW.meta,
             child_limit = COALESCE(NEW.limit_promoters, child_limit)
       WHERE id = v_pos;
    END IF;
    NEW.id := compat.us_id(v_pos, NEW.seccion_geo_id::integer);
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
    FROM str.assignment a
    JOIN str.position p  ON p.id = a.position_id
    JOIN str.position_holder rh ON rh.position_id = p.parent_position_id
   WHERE a.auth_id = NEW.user_auth_id
     AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
     AND p.level = 'ZONE_COORDINATOR'
     AND p.structure_id = v_structure
   ORDER BY p.id DESC LIMIT 1;

  IF v_ref_auth IS NOT NULL THEN
    SELECT p.id INTO v_parent
      FROM str.position p
      JOIN str.position_holder h ON h.position_id = p.id
     WHERE p.level = 'REFERENT'
       AND p.structure_id = v_structure
       AND p.territory_id = v_mun_terr
       AND p.valid_to IS NULL
       AND h.auth_id = v_ref_auth
     LIMIT 1;

    IF v_parent IS NULL THEN
      v_parent := compat.next_position_id();
      INSERT INTO str.position (id, structure_id, level, territory_id, code, valid_from)
      VALUES (v_parent, v_structure, 'REFERENT', v_mun_terr,
              'REF|' || (SELECT t.code FROM str.territory t WHERE t.id = v_mun_terr)
                     || '|' || v_ref_auth,
              now());

      SELECT p.id INTO v_person FROM str.person p WHERE p.auth_id = v_ref_auth LIMIT 1;
      IF v_person IS NOT NULL THEN
        INSERT INTO str.assignment (id, created, position_id, person_id, role,
                                    auth_id, validity, start_reason)
        VALUES (compat.next_assignment_id(), now(), v_parent, v_person, 'HOLDER',
                v_ref_auth, tstzrange(now(), NULL),
                'alta por vista de compatibilidad (users_seccions)');
      END IF;
      v_person := NULL;
    END IF;
  END IF;

  SELECT 'CZ|' || t.code || '|' || NEW.user_auth_id INTO v_code
    FROM str.territory t WHERE t.id = v_sec_terr;

  v_pos := compat.next_position_id();
  INSERT INTO str.position (id, structure_id, level, parent_position_id, territory_id,
                            code, goal, child_limit, valid_from)
  VALUES (v_pos, v_structure, 'ZONE_COORDINATOR', v_parent, v_sec_terr,
          v_code, NEW.meta, NEW.limit_promoters, now());

  SELECT p.id INTO v_person FROM str.person p WHERE p.auth_id = NEW.user_auth_id LIMIT 1;
  IF v_person IS NULL THEN
    v_person := compat.next_person_id();
    INSERT INTO str.person (id, auth_id, legacy_source, created)
    VALUES (v_person, NEW.user_auth_id, 'keycloak', now());
  END IF;

  INSERT INTO str.assignment (id, created, position_id, person_id, role,
                              auth_id, validity, start_reason)
  VALUES (compat.next_assignment_id(), now(), v_pos, v_person, 'HOLDER',
          NEW.user_auth_id, tstzrange(now(), NULL),
          'alta por vista de compatibilidad (users_seccions)');

  NEW.id := compat.us_id(v_pos, NEW.seccion_geo_id::integer);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 5b. edición: la meta, el límite de promotores y la baja lógica
--     `DELETE /{id}` del controlador no borra: pone `deleted` y guarda. Por
--     eso el UPDATE con `deleted` no nulo cierra el nombramiento.
CREATE OR REPLACE FUNCTION compat.user_sections_update() RETURNS TRIGGER AS $$
DECLARE
  v_pos bigint;
  v_at  timestamptz := clock_timestamp();
BEGIN
  IF NEW.deleted IS NOT NULL AND OLD.deleted IS NULL THEN
    v_pos := compat.us_resolve(OLD.id);

    IF EXISTS (SELECT 1 FROM str.position ch
                WHERE ch.parent_position_id = v_pos AND ch.valid_to IS NULL) THEN
      RAISE EXCEPTION 'el coordinador todavía tiene promotores en esa sección: reubícalos antes de quitarla';
    END IF;

    UPDATE str.assignment a
       SET validity = compat.close_validity(a.validity, v_at),
           end_reason = 'baja por vista de compatibilidad (users_seccions)'
     WHERE a.position_id = v_pos AND upper(a.validity) IS NULL;

    UPDATE str.position p
       SET valid_to = GREATEST(v_at, p.valid_from + interval '1 microsecond')
     WHERE p.id = v_pos AND p.valid_to IS NULL;

    RETURN NEW;
  END IF;

  IF NEW.meta IS DISTINCT FROM OLD.meta
     OR NEW.limit_promoters IS DISTINCT FROM OLD.limit_promoters THEN
    -- la meta se pone como excepción sobre la sección, sin romper el
    -- nombramiento municipal
    v_pos := compat.us_override(OLD.id);
    UPDATE str.position
       SET goal        = NEW.meta,
           child_limit = NEW.limit_promoters
     WHERE id = v_pos;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 5c. baja física: el servicio no la usa, pero la vista no puede quedar a
--     medias. Cierra el nombramiento, igual que la baja lógica.
CREATE OR REPLACE FUNCTION compat.user_sections_delete() RETURNS TRIGGER AS $$
DECLARE
  v_pos bigint := compat.us_resolve(OLD.id);
  v_at  timestamptz := clock_timestamp();
BEGIN
  IF EXISTS (SELECT 1 FROM str.position ch
              WHERE ch.parent_position_id = v_pos AND ch.valid_to IS NULL) THEN
    RAISE EXCEPTION 'el coordinador todavía tiene promotores en esa sección: reubícalos antes de quitarla';
  END IF;

  UPDATE str.assignment a
     SET validity = compat.close_validity(a.validity, v_at),
         end_reason = 'baja por vista de compatibilidad (users_seccions)'
   WHERE a.position_id = v_pos AND upper(a.validity) IS NULL;

  UPDATE str.position p
     SET valid_to = GREATEST(v_at, p.valid_from + interval '1 microsecond')
   WHERE p.id = v_pos AND p.valid_to IS NULL;

  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER users_seccions_insert_trg INSTEAD OF INSERT ON users_seccions
  FOR EACH ROW EXECUTE FUNCTION compat.user_sections_insert();
CREATE TRIGGER users_seccions_update_trg INSTEAD OF UPDATE ON users_seccions
  FOR EACH ROW EXECUTE FUNCTION compat.user_sections_update();
CREATE TRIGGER users_seccions_delete_trg INSTEAD OF DELETE ON users_seccions
  FOR EACH ROW EXECUTE FUNCTION compat.user_sections_delete();

COMMIT;

-- =====================================================================
-- verificación
-- =====================================================================
\echo '--- filas: vista contra tabla congelada ---'
SELECT (SELECT count(*) FROM users_seccions)                              AS vista,
       (SELECT count(*) FROM users_seccions_legacy WHERE deleted IS NULL) AS legacy_vivas;

\echo '--- metas: la suma tiene que coincidir ---'
SELECT (SELECT sum(meta) FROM users_seccions)                              AS vista,
       (SELECT sum(meta) FROM users_seccions_legacy WHERE deleted IS NULL) AS legacy;

\echo '--- fidelidad por pareja (coordinador, sección) ---'
SELECT count(*)                                                        AS pares_legacy,
       count(v.id)                                                     AS encontrados_en_vista,
       count(*) FILTER (WHERE v.id IS NULL)                            AS faltan,
       count(*) FILTER (WHERE v.meta IS DISTINCT FROM l.meta)           AS dif_meta,
       count(*) FILTER (WHERE v.municipality_id IS DISTINCT FROM l.municipality_id) AS dif_municipio,
       count(*) FILTER (WHERE v.seccion_number IS DISTINCT FROM l.seccion_number)   AS dif_numero
FROM (SELECT DISTINCT user_auth_id, seccion_geo_id, meta, municipality_id, seccion_number
        FROM users_seccions_legacy WHERE deleted IS NULL) l
LEFT JOIN users_seccions v ON v.user_auth_id = l.user_auth_id
                          AND v.seccion_geo_id = l.seccion_geo_id;

\echo '--- filas que la vista añade y la tabla vieja no tenía ---'
SELECT count(*) AS solo_en_la_vista
FROM users_seccions v
WHERE NOT EXISTS (SELECT 1 FROM users_seccions_legacy l
                   WHERE l.deleted IS NULL
                     AND l.user_auth_id = v.user_auth_id
                     AND l.seccion_geo_id = v.seccion_geo_id);
