-- Electorama: configuración de campaña y niveles de estructura por campaña.
--
-- Hasta aquí el organigrama tenía tres niveles fijos (REFERENT > ZONE_COORDINATOR > PROMOTER) en un
-- CHECK y en el rango del trigger de jerarquía. Ahora cada campaña define sus niveles en
-- structure.campaign_level: cuántos, con qué nombre, dónde se colocan, su meta y su rol de Keycloak.
--
-- Un nivel puede llevar una "función" heredada (REFERENT, ZONE_COORDINATOR o PROMOTER). La función se
-- sigue guardando en position.level, así que las claves de sección, la captura, los reportes y las
-- vistas compat siguen funcionando para esos niveles sin cambios. Un nivel sin función sólo agrega
-- jerarquía (p.ej. un coordinador estatal arriba del municipal).

SET LOCAL search_path TO structure, public;

-- ─────────────────────────────────────────────── campaña: fechas y descripción

ALTER TABLE structure.campaign
    ADD COLUMN start_date date,
    ADD COLUMN end_date date,
    ADD COLUMN description text,
    ADD CONSTRAINT campaign_dates_chk
        CHECK (start_date IS NULL OR end_date IS NULL OR end_date >= start_date);

-- ─────────────────────────────────────────────── niveles por campaña

CREATE SEQUENCE structure.campaign_level_id_seq;

CREATE TABLE structure.campaign_level (
    id                  bigint PRIMARY KEY DEFAULT nextval('structure.campaign_level_id_seq'),
    campaign_id         bigint NOT NULL REFERENCES structure.campaign (id),
    -- 1 es la cúspide; un puesto cuelga de uno de rango menor
    rank                smallint NOT NULL CHECK (rank >= 1),
    code                varchar(40) NOT NULL CHECK (code ~ '^[A-Z][A-Z0-9_]*$'),
    name_singular       varchar(80) NOT NULL,
    name_plural         varchar(80) NOT NULL,
    -- dónde se coloca el puesto; NULL = donde diga quien lo crea (dentro del territorio del padre)
    territory_type      varchar(20)
        CHECK (territory_type IN ('STATE', 'MUNICIPALITY', 'SECTION', 'FRAGMENT')),
    -- meta con la que nace cada puesto del nivel; también reparte las vacantes del nivel de arriba
    default_goal        integer CHECK (default_goal >= 0),
    default_child_limit integer CHECK (default_child_limit >= 0),
    level_function      varchar(20)
        CHECK (level_function IN ('REFERENT', 'ZONE_COORDINATOR', 'PROMOTER')),
    -- rol del realm que recibe el titular; el alterno es para las estructuras ALTERNATE
    role_name           varchar(80) NOT NULL CHECK (role_name ~ '^[A-Z][A-Z0-9_]*$'),
    alternate_role_name varchar(80) CHECK (alternate_role_name ~ '^[A-Z][A-Z0-9_]*$'),
    created             timestamptz NOT NULL DEFAULT now(),
    -- diferible: al reordenar se intercambian rangos dentro de la misma transacción
    CONSTRAINT campaign_level_rank_uq UNIQUE (campaign_id, rank) DEFERRABLE INITIALLY IMMEDIATE,
    CONSTRAINT campaign_level_code_uq UNIQUE (campaign_id, code)
);
ALTER SEQUENCE structure.campaign_level_id_seq OWNED BY structure.campaign_level.id;

CREATE UNIQUE INDEX campaign_level_function_uq
    ON structure.campaign_level (campaign_id, level_function) WHERE level_function IS NOT NULL;

COMMENT ON TABLE structure.campaign_level IS
    'Niveles del organigrama de cada campaña (V3). level_function liga el nivel con la lógica heredada '
    'de REFERENT / ZONE_COORDINATOR / PROMOTER (claves de sección, captura, reportes).';

-- Las campañas que ya existen se quedan con sus tres niveles, sus nombres de pantalla y sus roles.
INSERT INTO structure.campaign_level
    (campaign_id, rank, code, name_singular, name_plural, territory_type, default_goal,
     level_function, role_name, alternate_role_name)
SELECT c.id, v.rank, v.code, v.singular, v.plural, v.territory_type, v.goal,
       v.fn, v.role_name, v.alt_role
  FROM structure.campaign c
 CROSS JOIN (VALUES
     (1, 'COORDINADOR', 'Coordinador', 'Coordinadores', 'MUNICIPALITY', NULL::int,
      'REFERENT', 'CORDINATOR_MUNICIPAL', 'CORDINATOR_MUNICIPAL_ALT'),
     -- en la alterna hay promotores sobre municipio (V2d): el territorio lo decide quien crea
     (2, 'PROMOTOR', 'Promotor', 'Promotores', NULL, 200,
      'ZONE_COORDINATOR', 'CORDINATOR_ZONE', 'CORDINATOR_ZONE_ALT'),
     (3, 'MOVILIZADOR', 'Movilizador', 'Movilizadores', 'SECTION', 20,
      'PROMOTER', 'PROMOTER', NULL)
 ) AS v (rank, code, singular, plural, territory_type, goal, fn, role_name, alt_role)
 WHERE NOT EXISTS (SELECT 1 FROM structure.campaign_level l WHERE l.campaign_id = c.id);

-- ─────────────────────────────────────────────── el puesto apunta a su nivel

ALTER TABLE structure."position"
    ADD COLUMN level_id bigint REFERENCES structure.campaign_level (id),
    ALTER COLUMN level DROP NOT NULL,
    -- la raíz ya no es siempre REFERENT: la decide el rango del nivel (trigger de jerarquía)
    DROP CONSTRAINT position_parent_chk;

-- position_level_chk se queda: si hay función, es una de las tres.

-- Los puestos existentes toman el nivel de su campaña con la misma función. Sin triggers: la guarda
-- de campaña cerrada bloquearía 2024 y la jerarquía nueva no debe revalidar datos históricos.
ALTER TABLE structure."position" DISABLE TRIGGER USER;
UPDATE structure."position" p
   SET level_id = l.id
  FROM structure.structure s
  JOIN structure.campaign_level l ON l.campaign_id = s.campaign_id
 WHERE s.id = p.structure_id
   AND l.level_function = p.level
   AND p.level_id IS NULL;
ALTER TABLE structure."position" ENABLE TRIGGER USER;

ALTER TABLE structure."position" ALTER COLUMN level_id SET NOT NULL;
CREATE INDEX position_level_id_idx ON structure."position" (level_id) WHERE valid_to IS NULL;

-- ─────────────────────────────────────────────── triggers

-- Antes que los demás triggers del puesto (se disparan en orden alfabético): resuelve el nivel.
-- Quien inserte sólo con la función (las funciones compat) recibe el nivel de su campaña.
CREATE FUNCTION structure.position_resolve_level() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
    v_campaign bigint;
    lvl        campaign_level;
BEGIN
    SELECT s.campaign_id INTO v_campaign FROM structure s WHERE s.id = NEW.structure_id;

    IF NEW.level_id IS NULL THEN
        IF NEW.level IS NULL THEN
            RAISE EXCEPTION 'el puesto necesita un nivel' USING ERRCODE = 'not_null_violation';
        END IF;
        SELECT l.id INTO NEW.level_id
          FROM campaign_level l
         WHERE l.campaign_id = v_campaign AND l.level_function = NEW.level;
        IF NEW.level_id IS NULL THEN
            RAISE EXCEPTION 'la campaña % no tiene un nivel con la función %', v_campaign, NEW.level
                USING ERRCODE = 'check_violation';
        END IF;
    END IF;

    SELECT * INTO lvl FROM campaign_level WHERE id = NEW.level_id;
    IF lvl.campaign_id <> v_campaign THEN
        RAISE EXCEPTION 'el nivel % es de otra campaña que la estructura %', NEW.level_id, NEW.structure_id
            USING ERRCODE = 'check_violation';
    END IF;
    -- la función la manda el nivel, no quien escribe
    NEW.level := lvl.level_function;
    RETURN NEW;
END;
$$;

CREATE TRIGGER position_00_resolve_level_trg
    BEFORE INSERT OR UPDATE OF level_id, level, structure_id ON structure."position"
    FOR EACH ROW EXECUTE FUNCTION structure.position_resolve_level();

-- La jerarquía con el rango del nivel en vez del CASE fijo de tres niveles.
CREATE OR REPLACE FUNCTION structure.position_validate_hierarchy() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
DECLARE
    lvl         campaign_level;
    parent      RECORD;
    parent_lvl  campaign_level;
    v_top_rank  smallint;
    v_ttype     text;
BEGIN
    SELECT * INTO lvl FROM campaign_level WHERE id = NEW.level_id;

    -- 0. el puesto va sobre el tipo de territorio de su nivel (al crearlo o al moverlo)
    IF lvl.territory_type IS NOT NULL
       AND (TG_OP = 'INSERT' OR NEW.territory_id IS DISTINCT FROM OLD.territory_id
            OR NEW.level_id IS DISTINCT FROM OLD.level_id) THEN
        SELECT t.type INTO v_ttype FROM territory t WHERE t.id = NEW.territory_id;
        IF v_ttype IS DISTINCT FROM lvl.territory_type THEN
            RAISE EXCEPTION 'Un puesto de % va sobre un territorio %, no %',
                lvl.name_singular, lvl.territory_type, v_ttype
                USING ERRCODE = 'check_violation';
        END IF;
    END IF;

    IF NEW.parent_position_id IS NULL THEN
        -- sólo el nivel de la cúspide va sin padre (al crearlo o al descolgarlo)
        IF TG_OP = 'INSERT' OR OLD.parent_position_id IS NOT NULL THEN
            SELECT min(l.rank) INTO v_top_rank FROM campaign_level l WHERE l.campaign_id = lvl.campaign_id;
            IF lvl.rank <> v_top_rank THEN
                RAISE EXCEPTION 'Un puesto de % necesita un puesto superior del que colgar', lvl.name_singular
                    USING ERRCODE = 'check_violation';
            END IF;
        END IF;
        RETURN NEW;
    END IF;

    SELECT * INTO parent FROM position WHERE id = NEW.parent_position_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'El puesto padre % no existe', NEW.parent_position_id;
    END IF;
    SELECT * INTO parent_lvl FROM campaign_level WHERE id = parent.level_id;

    -- 1. el padre debe ser de un nivel superior
    IF parent_lvl.rank >= lvl.rank THEN
        RAISE EXCEPTION 'Un puesto de % no puede colgar de un puesto de % (el padre debe ser de nivel superior)',
            lvl.name_singular, parent_lvl.name_singular;
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

DROP TRIGGER position_validate_hierarchy_trg ON structure."position";
CREATE TRIGGER position_validate_hierarchy_trg
    BEFORE INSERT OR UPDATE OF parent_position_id, territory_id, level, level_id, structure_id
    ON structure."position"
    FOR EACH ROW EXECUTE FUNCTION structure.position_validate_hierarchy();

-- Un nivel con puestos ya no se borra ni cambia de forma; nombres, metas y roles sí se editan.
CREATE FUNCTION structure.campaign_level_in_use_guard() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'structure', 'public', 'pg_catalog'
    AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM position p WHERE p.level_id = OLD.id) THEN
        RETURN coalesce(NEW, OLD);
    END IF;
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'El nivel % ya tiene puestos: no se puede quitar', OLD.name_singular
            USING ERRCODE = 'check_violation';
    END IF;
    IF NEW.campaign_id <> OLD.campaign_id
       OR NEW.rank <> OLD.rank
       OR NEW.territory_type IS DISTINCT FROM OLD.territory_type
       OR NEW.level_function IS DISTINCT FROM OLD.level_function THEN
        RAISE EXCEPTION 'El nivel % ya tiene puestos: sólo se cambian nombre, meta, tope y roles',
            OLD.name_singular
            USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER campaign_level_in_use_guard_trg
    BEFORE UPDATE OR DELETE ON structure.campaign_level
    FOR EACH ROW EXECUTE FUNCTION structure.campaign_level_in_use_guard();

-- ─────────────────────────────────────────────── users: registro de roles de estructura

-- msvc-users sólo da y quita roles de estructura y respeta su antigüedad (sólo se administra lo de
-- abajo). Antes la lista estaba fija en código; ahora la llena msvc-structure al guardar niveles.
CREATE TABLE users.structure_role (
    role        varchar(80) PRIMARY KEY CHECK (role ~ '^[A-Z][A-Z0-9_]*$'),
    -- mayor es más alto: la cúspide de una campaña de N niveles tiene N
    seniority   smallint NOT NULL CHECK (seniority >= 1),
    campaign_id bigint,
    description varchar(200),
    created     timestamptz NOT NULL DEFAULT now()
);

INSERT INTO users.structure_role (role, seniority, description) VALUES
    ('CORDINATOR_MUNICIPAL', 3, 'Coordinador (función REFERENT)'),
    ('CORDINATOR_MUNICIPAL_ALT', 3, 'Coordinador, estructura alterna'),
    ('CORDINATOR_ZONE', 2, 'Promotor (función ZONE_COORDINATOR)'),
    ('CORDINATOR_ZONE_ALT', 2, 'Promotor, estructura alterna'),
    ('PROMOTER', 1, 'Movilizador (función PROMOTER)');

-- ─────────────────────────────────────────────── menú: la pantalla de configuración

INSERT INTO users.menus_groups (id, title, subtitle, type, icon, link, "order")
SELECT 'structure.campaigns', 'Campañas', 'Datos, cargos, estructuras y niveles', 'basic', 'flag',
       '/admin/structure/campaigns', 9
 WHERE NOT EXISTS (SELECT 1 FROM users.menus_groups WHERE link = '/admin/structure/campaigns');

INSERT INTO users.menus_roles (menu_group_id, role)
SELECT g.pk, 'ADMIN' FROM users.menus_groups g
 WHERE g.link = '/admin/structure/campaigns'
   AND NOT EXISTS (SELECT 1 FROM users.menus_roles r WHERE r.menu_group_id = g.pk AND r.role = 'ADMIN');
