-- =====================================================================
-- Electorama — msvc-structure
-- V3c: la fuente de la vista `users_seccions` (fase 3b)
--
-- Se ejecuta en msvc_dash_structure y añade a `compat` lo que el lado de
-- msvc_dash_user necesita leer. El corte en sí va en V3d.
--
-- LA GRANULARIDAD NO ES 1:1, y ahí está todo el asunto. En el modelo nuevo
-- un nombramiento de coordinador es UN puesto; en `users_seccions` es una
-- fila por sección. Dos casos:
--
--   · puesto sobre una SECCIÓN (rama principal) → 1 fila, con su meta
--   · puesto sobre un MUNICIPIO (rama alterna)  → N filas, una por sección
--     del municipio según el catálogo, sin meta
--
-- El segundo caso es exactamente lo que hacía `UserService.java:1147`:
-- copiar todas las secciones del municipio en el alta. La diferencia es que
-- ahora la expansión se calcula al leer, así que **la copia ya no se queda
-- atrás cuando el catálogo cambia** — que es justo lo que le pasó a 221 de
-- los 489 coordinadores alternos.
--
-- EL `id`. La vista necesita uno estable para que `PUT /updateUserSection/{id}`
-- y `DELETE /{id}` sigan funcionando, y no puede salir de una secuencia
-- porque la fila no existe: se calcula. Se codifica el par
-- (puesto, sección) en un bigint:
--
--     id = (position_id << 20) | section_geo_id
--
-- 2^20 = 1,048,576 y el id de sección más alto del catálogo es 74,048, así
-- que caben con holgura; con 59,398 puestos el id máximo queda en ~6.2e10,
-- lejos del techo de bigint. Es reversible, que es lo que permite a los
-- triggers saber qué puesto y qué sección tocar.
--
-- Es IDEMPOTENTE (CREATE OR REPLACE). Después de V2e, antes de V3d.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V3c__compat_source_sections.sql
-- =====================================================================

\set ON_ERROR_STOP on

-- ---------------------------------------------------------------------
-- 1. codificar y decodificar el id sintético
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION compat.us_id(p_position bigint, p_section integer)
RETURNS bigint AS $$
  SELECT (p_position << 20) | p_section::bigint;
$$ LANGUAGE sql IMMUTABLE;

CREATE OR REPLACE FUNCTION compat.us_position(p_id bigint)
RETURNS bigint AS $$
  SELECT p_id >> 20;
$$ LANGUAGE sql IMMUTABLE;

CREATE OR REPLACE FUNCTION compat.us_section(p_id bigint)
RETURNS integer AS $$
  SELECT (p_id & 1048575)::integer;
$$ LANGUAGE sql IMMUTABLE;

-- ---------------------------------------------------------------------
-- 2. compat.user_sections
--
-- `deleted` sale siempre NULL: la vista sólo muestra puestos vigentes, y
-- cerrar la vigencia hace desaparecer la fila. Es lo que el servicio viejo
-- veía, porque su baja es un UPDATE de `deleted` y luego filtra por nulo.
--
-- `rejuntado_id` sale NULL. El modelo nuevo no lo posee: son 4,476 filas
-- con un valor que no lee nadie —ni `UserSeccionEntity`, que no mapea la
-- columna, ni las consultas nativas de los facades de metas—. Lo mismo que
-- se hizo con `promoters.auth_id`.
--
-- `seccion_id` reproduce el patrón del modelo viejo: lleno en las filas de
-- la rama principal e igual a `seccion_geo_id`, NULL en las de la alterna
-- (el alta masiva nunca lo rellenaba: 24,101 filas). Ninguna consulta lo
-- usa —todas van por `seccion_geo_id`—, así que se conserva por fidelidad,
-- no por utilidad.
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW compat.user_sections AS
-- 2a. nombramiento sobre una sección: una fila, con su meta
SELECT compat.us_id(pos.id, sec.section_geo_id)                      AS id,
       h.auth_id::varchar(500)                                       AS user_auth_id,
       NULL::bigint                                                  AS rejuntado_id,
       sec.section_geo_id                                            AS seccion_id,
       lpad(regexp_replace(sec.code, '^S-', ''), 4, '0')::varchar(255) AS seccion_number,
       sec.section_geo_id                                            AS seccion_geo_id,
       pos.goal                                                      AS meta,
       NULL::timestamp                                               AS deleted,
       mun.municipality_geo_id                                       AS municipality_id,
       pos.child_limit                                               AS limit_promoters,
       pos.id                                                        AS position_id,
       false                                                         AS from_municipality
FROM position pos
JOIN territory sec        ON sec.id = pos.territory_id AND sec.type = 'SECTION'
LEFT JOIN territory mun   ON mun.id = sec.parent_territory_id
JOIN compat.position_holder h ON h.position_id = pos.id
WHERE pos.level = 'ZONE_COORDINATOR'
  AND pos.valid_to IS NULL

UNION ALL

-- 2b. nombramiento sobre un municipio: una fila por sección del catálogo
SELECT compat.us_id(pos.id, sec.section_geo_id)                      AS id,
       h.auth_id::varchar(500)                                       AS user_auth_id,
       NULL::bigint                                                  AS rejuntado_id,
       NULL::integer                                                 AS seccion_id,
       lpad(regexp_replace(sec.code, '^S-', ''), 4, '0')::varchar(255) AS seccion_number,
       sec.section_geo_id                                            AS seccion_geo_id,
       NULL::integer                                                 AS meta,
       NULL::timestamp                                               AS deleted,
       mun.municipality_geo_id                                       AS municipality_id,
       NULL::integer                                                 AS limit_promoters,
       pos.id                                                        AS position_id,
       true                                                          AS from_municipality
FROM position pos
JOIN territory mun        ON mun.id = pos.territory_id AND mun.type = 'MUNICIPALITY'
JOIN territory sec        ON sec.parent_territory_id = mun.id AND sec.type = 'SECTION'
                         AND sec.valid_to IS NULL
JOIN compat.position_holder h ON h.position_id = pos.id
WHERE pos.level = 'ZONE_COORDINATOR'
  AND pos.valid_to IS NULL
  -- PRECEDENCIA: el puesto de sección gana al municipal. Si el mismo
  -- coordinador tiene un puesto sobre esa sección concreta, la fila sale de
  -- 2a con su meta y aquí no se repite. Así el modelo expresa «cubre el
  -- municipio, y en esta sección tiene meta propia» sin duplicar la fila y
  -- sin tener que romper el nombramiento municipal en 130 pedazos. Es la
  -- misma regla que usan `position_for_coordinator` al dar de alta un
  -- promotor y el trigger de alta de la vista.
  --
  -- La precedencia cruza las estructuras a propósito. Hay 10 personas que
  -- coordinan en las dos ramas: puesto de sección en la principal y
  -- nombramiento municipal en la alterna. La tabla vieja no podía
  -- representarlo —`users.flag_alter` es uno por usuario— así que tenía una
  -- sola fila por pareja (coordinador, sección), y la vista mantiene ese
  -- contrato: gana la del puesto más específico.
  AND NOT EXISTS (
        SELECT 1
        FROM position sp
        JOIN compat.position_holder sh ON sh.position_id = sp.id
        WHERE sp.level = 'ZONE_COORDINATOR'
          AND sp.valid_to IS NULL
          AND sp.territory_id = sec.id
          AND sh.auth_id = h.auth_id);

-- ---------------------------------------------------------------------
-- 3. materializar un nombramiento municipal
--
-- Un puesto municipal dice «este coordinador cubre el municipio completo».
-- En cuanto la operación toca UNA sección —le pone meta, o se la quita— eso
-- deja de ser cierto: el ámbito ya no es uniforme. Entonces el puesto
-- municipal se sustituye por un puesto por sección (la inversa de V2d) y
-- los promotores se recuelgan cada uno en el suyo.
--
-- Vive aquí, del lado del modelo, y no en los triggers del otro lado,
-- porque toca cinco tablas y postgres_fdw no sabe hacerlo en una ida.
-- Devuelve el id del puesto que quedó sobre la sección pedida.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION compat.materialize_municipal_position(
    p_position bigint, p_section integer)
RETURNS bigint AS $$
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
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- 4. alta de un puesto de coordinador sobre una sección
--    Es la operación de `PUT /{authId}/{seccionId}/{seccionNumber}/{munId}`.
--    Si la sección ya la cubre un puesto del mismo coordinador —el propio de
--    sección o el municipal— no se crea nada: la fila ya existe en la vista.
-- ---------------------------------------------------------------------
--    La rama (principal o alterna) la pasa el trigger: sale de
--    `users.flag_alter`, que vive en msvc_dash_user y no en este modelo.
CREATE OR REPLACE FUNCTION compat.user_section_add(
    p_auth varchar, p_section integer, p_municipality integer,
    p_alternate boolean DEFAULT false,
    p_meta integer DEFAULT NULL, p_limit integer DEFAULT NULL)
RETURNS bigint AS $$
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
    RETURN compat.us_id(v_pos, p_section);
  END IF;

  -- El puesto de referente sobre el municipio DE ESA SECCIÓN, con el mismo
  -- referente del que ya cuelga el coordinador. `position_validate_hierarchy`
  -- exige que el territorio del hijo esté contenido en el del padre, así que
  -- no vale reutilizar el padre de otro municipio. Si no existe, se crea; si
  -- no se sabe quién es el referente, el puesto queda sin padre.
  SELECT rh.auth_id INTO v_ref_auth
    FROM assignment a
    JOIN position p ON p.id = a.position_id
    JOIN compat.position_holder rh ON rh.position_id = p.parent_position_id
   WHERE a.auth_id = p_auth AND a.role = 'HOLDER' AND upper(a.validity) IS NULL
     AND p.level = 'ZONE_COORDINATOR' AND p.structure_id = v_structure
   ORDER BY p.id DESC LIMIT 1;

  IF v_ref_auth IS NOT NULL THEN
    SELECT p.id INTO v_parent
      FROM position p
      JOIN compat.position_holder h ON h.position_id = p.id
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

  RETURN compat.us_id(v_pos, p_section);
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- 5. cambio de meta y baja, por el id sintético
--    Si la fila viene de un puesto municipal, primero se materializa.
-- ---------------------------------------------------------------------
-- Poner meta a UNA sección de un nombramiento municipal no lo rompe: crea un
-- puesto sobre esa sección, que convive con el municipal bajo la regla de
-- precedencia de la vista. Una inserción en vez de ~130.
CREATE OR REPLACE FUNCTION compat.user_section_override(p_id bigint)
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
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION compat.user_section_update(
    p_id bigint, p_meta integer, p_limit integer)
RETURNS void AS $$
DECLARE
  v_pos bigint := compat.user_section_override(p_id);
BEGIN
  UPDATE position
     SET goal        = COALESCE(p_meta,  goal),
         child_limit = COALESCE(p_limit, child_limit)
   WHERE id = v_pos;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION compat.user_section_close(p_id bigint)
RETURNS void AS $$
DECLARE
  v_pos bigint := compat.us_position(p_id);
  v_sec integer := compat.us_section(p_id);
  v_at  timestamptz := clock_timestamp();
BEGIN
  IF EXISTS (SELECT 1 FROM position p JOIN territory t ON t.id = p.territory_id
              WHERE p.id = v_pos AND t.type = 'MUNICIPALITY') THEN
    v_pos := compat.materialize_municipal_position(v_pos, v_sec);
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
$$ LANGUAGE plpgsql;

\echo '--- filas que servirá la vista ---'
SELECT count(*) AS filas,
       count(*) FILTER (WHERE NOT from_municipality) AS de_seccion,
       count(*) FILTER (WHERE from_municipality)     AS de_municipio,
       count(meta) AS con_meta, sum(meta) AS suma_meta
FROM compat.user_sections;
