-- =====================================================================
-- Electorama — msvc-structure
-- V1: esquema inicial (fase 1 de la migración)
--
-- Modelo de estructura territorial con puestos y vigencias.
-- El esqueleto son POSITIONS; las personas los ocupan por ASSIGNMENTS con
-- vigencia; los convencidos (MEMBERSHIPS) cuelgan del puesto, no de la persona.
--
-- Identificadores en inglés, como el resto de los microservicios.
-- Comentarios en español, también como el resto del repo.
--
--   createdb -U postgres msvc_dash_structure
--   psql -U postgres -d msvc_dash_structure -f V1__initial_schema.sql
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS btree_gist;   -- necesaria para el EXCLUDE de vigencias
CREATE EXTENSION IF NOT EXISTS unaccent;     -- buscar personas por nombre sin acentos

-- ---------------------------------------------------------------------
-- 1. person — el ser humano, una sola vez
--    Hoy está triplicada entre users.users, followers.followers y
--    followers.promoters (más una cuarta copia en pollings_representatives).
-- ---------------------------------------------------------------------
CREATE TABLE person (
    id                  BIGSERIAL PRIMARY KEY,
    -- Anchos tomados de los máximos reales del modelo viejo, con holgura.
    -- La CURP llega a 19 caracteres en los datos (debería ser 18): hay basura.
    curp                VARCHAR(20),
    ine                 VARCHAR(30),
    -- Nullable a propósito: 3,408 cuentas de operador y 96 convencidos no
    -- tienen nombre en la base (su perfil vive en Keycloak). Forzar NOT NULL
    -- obligaría a inventar datos; se prefiere que el hueco quede visible.
    names               VARCHAR(200),
    last_name           VARCHAR(200),
    sur_name            VARCHAR(200),
    birthdate           DATE,
    sex                 VARCHAR(20),
    cell_phone_number   VARCHAR(30),
    email               VARCHAR(200),
    street              VARCHAR(300),
    external_number     VARCHAR(60),
    internal_number     VARCHAR(60),
    neighborhood        VARCHAR(300),
    zip_code            VARCHAR(20),
    -- trazabilidad al modelo viejo, para poder reconciliar después del backfill
    legacy_source       VARCHAR(30),
    legacy_id           BIGINT,
    auth_id             VARCHAR(100),   -- UUID de Keycloak, sólo si es operador
    created             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated             TIMESTAMPTZ
);

-- La CURP sí es única: es la llave de deduplicación del backfill.
CREATE UNIQUE INDEX person_curp_uq ON person (curp) WHERE curp IS NOT NULL;
-- El INE NO se puede declarar único: en los datos reales hay 53,674 credenciales
-- repetidas entre personas con CURP distinta. Se indexa para búsqueda y se
-- reporta como calidad de datos, pero no se impone la restricción.
CREATE INDEX person_ine_idx ON person (ine) WHERE ine IS NOT NULL;
CREATE UNIQUE INDEX person_auth_id_uq ON person (auth_id) WHERE auth_id IS NOT NULL;
CREATE INDEX person_legacy_idx ON person (legacy_source, legacy_id);
CREATE INDEX person_name_idx ON person (last_name, sur_name, names);

-- ---------------------------------------------------------------------
-- 2. territory — el territorio, árbol propio y subdivisible
--    REFERENCIA el catálogo de geography por section_geo_id / municipality_geo_id.
--    NUNCA lo copia: geography es el dueño de geo_*.
-- ---------------------------------------------------------------------
CREATE TABLE territory (
    id                      BIGSERIAL PRIMARY KEY,
    type                    VARCHAR(20) NOT NULL,
    parent_territory_id     BIGINT REFERENCES territory (id),
    code                    VARCHAR(50) NOT NULL,   -- "30301", "30301-a"
    name                    VARCHAR(250),
    -- punteros al catálogo del INE que vive en msvc_dash_geografia
    state_geo_id            INTEGER,
    municipality_geo_id     INTEGER,
    section_geo_id          INTEGER,
    valid_from              TIMESTAMPTZ NOT NULL DEFAULT now(),
    valid_to                TIMESTAMPTZ,
    CONSTRAINT territory_type_chk
        CHECK (type IN ('STATE','MUNICIPALITY','SECTION','FRAGMENT')),
    -- un fragmento siempre cuelga de algo; un estado nunca
    CONSTRAINT territory_parent_chk
        CHECK ((type = 'STATE' AND parent_territory_id IS NULL)
            OR (type <> 'STATE' AND parent_territory_id IS NOT NULL))
);

CREATE UNIQUE INDEX territory_code_uq ON territory (code) WHERE valid_to IS NULL;
CREATE INDEX territory_parent_idx  ON territory (parent_territory_id);
CREATE INDEX territory_section_idx ON territory (section_geo_id) WHERE section_geo_id IS NOT NULL;

-- ---------------------------------------------------------------------
-- 3. campaign — el contenedor que permite reutilizar el modelo
-- ---------------------------------------------------------------------
CREATE TABLE campaign (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    year            INTEGER,
    election_day    DATE,
    territory_id    BIGINT REFERENCES territory (id),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PLANNING',
    valid_from      TIMESTAMPTZ NOT NULL DEFAULT now(),
    valid_to        TIMESTAMPTZ,
    CONSTRAINT campaign_type_chk
        CHECK (type IN ('FEDERAL','STATE','MUNICIPAL','INTERNAL')),
    CONSTRAINT campaign_status_chk
        CHECK (status IN ('PLANNING','ACTIVE','CLOSED'))
);

-- ---------------------------------------------------------------------
-- 4. structure — reemplaza al booleano promoters.flag_alter
-- ---------------------------------------------------------------------
CREATE TABLE structure (
    id                  BIGSERIAL PRIMARY KEY,
    campaign_id         BIGINT NOT NULL REFERENCES campaign (id),
    name                VARCHAR(200) NOT NULL,
    type                VARCHAR(20)  NOT NULL,
    owner_person_id     BIGINT REFERENCES person (id),
    valid_from          TIMESTAMPTZ NOT NULL DEFAULT now(),
    valid_to            TIMESTAMPTZ,
    created             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT structure_type_chk
        CHECK (type IN ('MAIN','ALTERNATE','ALLIED'))
);

CREATE INDEX structure_campaign_idx ON structure (campaign_id);

-- ---------------------------------------------------------------------
-- 5. position — el esqueleto estable, lo que NO se mueve
--    parent_position_id = ORGANIGRAMA · territory_id = TERRITORIO
--    Son dos árboles independientes: por eso la sección 30301 se puede
--    repartir entre dos coordinadores de zona distintos.
-- ---------------------------------------------------------------------
CREATE TABLE position (
    id                  BIGSERIAL PRIMARY KEY,
    structure_id        BIGINT NOT NULL REFERENCES structure (id),
    level               VARCHAR(20) NOT NULL,
    parent_position_id  BIGINT REFERENCES position (id),
    territory_id        BIGINT NOT NULL REFERENCES territory (id),
    code                VARCHAR(80),
    goal                INTEGER,
    child_limit         INTEGER,
    valid_from          TIMESTAMPTZ NOT NULL DEFAULT now(),
    valid_to            TIMESTAMPTZ,
    CONSTRAINT position_level_chk
        CHECK (level IN ('REFERENT','ZONE_COORDINATOR','PROMOTER')),
    -- el REFERENT es raíz; los demás siempre cuelgan de alguien
    CONSTRAINT position_parent_chk
        CHECK ((level = 'REFERENT' AND parent_position_id IS NULL)
            OR (level <> 'REFERENT'))
);

CREATE INDEX position_structure_idx ON position (structure_id);
CREATE INDEX position_parent_idx    ON position (parent_position_id);
CREATE INDEX position_territory_idx ON position (territory_id);
CREATE INDEX position_level_idx     ON position (level) WHERE valid_to IS NULL;

-- ---------------------------------------------------------------------
-- 6. assignment — la persona ocupa el puesto, con vigencia
-- ---------------------------------------------------------------------
CREATE TABLE assignment (
    id                      BIGSERIAL PRIMARY KEY,
    position_id             BIGINT NOT NULL REFERENCES position (id),
    person_id               BIGINT NOT NULL REFERENCES person (id),
    role                    VARCHAR(20) NOT NULL DEFAULT 'HOLDER',
    -- Cuenta de Keycloak con la que esta persona ocupa ESTE puesto.
    -- Vive aquí y no en `person` porque un mismo humano puede tener varias
    -- cuentas (p.ej. fue dado de alta dos veces como promotor). Sin esto no se
    -- puede reconstruir con qué cuenta capturó a cada convencido.
    auth_id                 VARCHAR(100),
    -- TSTZRANGE no tiene mapeo directo en JPA. El DEFAULT permite que el
    -- servicio inserte una asignación abierta sin tocar el rango; cerrarla
    -- (rotar) sí es una operación de rango y va por consulta nativa.
    validity                TSTZRANGE NOT NULL DEFAULT tstzrange(now(), NULL),
    start_reason            VARCHAR(500),
    end_reason              VARCHAR(500),
    created_by_person_id    BIGINT REFERENCES person (id),
    created                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT assignment_role_chk
        CHECK (role IN ('HOLDER','SUBSTITUTE','ACTING')),
    -- un puesto no puede tener dos titulares al mismo tiempo
    CONSTRAINT assignment_single_holder
        EXCLUDE USING gist (position_id WITH =, validity WITH &&) WHERE (role = 'HOLDER')
);

CREATE INDEX assignment_position_idx ON assignment (position_id);
CREATE INDEX assignment_person_idx   ON assignment (person_id);
CREATE INDEX assignment_validity_idx ON assignment USING gist (validity);
CREATE INDEX assignment_auth_id_idx  ON assignment (auth_id) WHERE auth_id IS NOT NULL;

-- ---------------------------------------------------------------------
-- 7. membership — el convencido
--    position_id                = responsabilidad ACTUAL (cambia con un traslado)
--    captured_by_assignment_id  = linaje, INMUTABLE (nunca se toca, ni en la baja)
-- ---------------------------------------------------------------------
CREATE TABLE membership (
    id                          BIGSERIAL PRIMARY KEY,
    person_id                   BIGINT NOT NULL REFERENCES person (id),
    structure_id                BIGINT NOT NULL REFERENCES structure (id),
    position_id                 BIGINT NOT NULL REFERENCES position (id),
    captured_by_assignment_id   BIGINT REFERENCES assignment (id),
    -- El authId crudo de quien capturó, además del puntero a la asignación.
    -- No es redundante: 18,026 convencidos fueron capturados por cuentas que
    -- nunca ocuparon un puesto (administradores, cargas masivas), así que no
    -- hay asignación a la que apuntar. Sin esta columna ese dato se pierde y
    -- `user_create_id` sale nulo por la vista de compatibilidad.
    captured_by_auth_id         VARCHAR(100),
    territory_id                BIGINT REFERENCES territory (id),   -- dónde vive
    source                      VARCHAR(20) NOT NULL DEFAULT 'WEB',
    status                      VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    validity                    TSTZRANGE NOT NULL DEFAULT tstzrange(now(), NULL),
    vote_flag                   BOOLEAN NOT NULL DEFAULT false,
    vote_flag_timestamp         TIMESTAMPTZ,
    legacy_id                   BIGINT,
    CONSTRAINT membership_source_chk
        CHECK (source IN ('APP','WEB','IMPORT')),
    -- una persona no puede estar dos veces en la misma estructura a la vez;
    -- SÍ puede estar en varias estructuras distintas (y eso es consultable)
    CONSTRAINT membership_single_per_structure
        EXCLUDE USING gist (person_id WITH =, structure_id WITH =, validity WITH &&)
);

CREATE INDEX membership_position_idx  ON membership (position_id);
CREATE INDEX membership_person_idx    ON membership (person_id);
CREATE INDEX membership_structure_idx ON membership (structure_id);
CREATE INDEX membership_captured_idx  ON membership (captured_by_assignment_id);
CREATE INDEX membership_captured_auth_idx ON membership (captured_by_auth_id)
    WHERE captured_by_auth_id IS NOT NULL;
CREATE INDEX membership_territory_idx ON membership (territory_id);
CREATE INDEX membership_legacy_idx    ON membership (legacy_id);

-- ---------------------------------------------------------------------
-- 8. change_log — bitácora append-only, escrita por el backend
--    Sustituye al logfile que hoy escribe el navegador con texto libre.
-- ---------------------------------------------------------------------
CREATE TABLE change_log (
    id                  BIGSERIAL PRIMARY KEY,
    type                VARCHAR(20) NOT NULL,
    entity              VARCHAR(40) NOT NULL,
    entity_id           BIGINT,
    before_data         JSONB,
    after_data          JSONB,
    actor_person_id     BIGINT REFERENCES person (id),
    reason              VARCHAR(500),
    occurred_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT change_log_type_chk
        CHECK (type IN ('ROTATION','TRANSFER','REORG_POSITION','CREATE','TERMINATE','CONVERSION'))
);

CREATE INDEX change_log_entity_idx ON change_log (entity, entity_id);
CREATE INDEX change_log_date_idx   ON change_log (occurred_at DESC);

-- =====================================================================
-- Reglas duras del organigrama (las que no se pueden expresar con CHECK)
-- =====================================================================

-- ¿es `candidate` igual o descendiente de `ancestor` en el árbol de territorios?
CREATE OR REPLACE FUNCTION territory_contained_in(candidate BIGINT, ancestor BIGINT)
RETURNS BOOLEAN AS $$
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
$$ LANGUAGE plpgsql STABLE
   -- el FDW invoca estas funciones con search_path = pg_catalog a secas
   SET search_path = public, pg_catalog;

CREATE OR REPLACE FUNCTION position_validate_hierarchy() RETURNS TRIGGER AS $$
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
$$ LANGUAGE plpgsql SET search_path = public, pg_catalog;

CREATE TRIGGER position_validate_hierarchy_trg
    BEFORE INSERT OR UPDATE OF parent_position_id, territory_id, level, structure_id ON position
    FOR EACH ROW EXECUTE FUNCTION position_validate_hierarchy();

-- el linaje de una membresía es inmutable: captured_by_assignment_id no se reescribe
CREATE OR REPLACE FUNCTION membership_protect_lineage() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.captured_by_assignment_id IS NOT NULL
       AND NEW.captured_by_assignment_id IS DISTINCT FROM OLD.captured_by_assignment_id THEN
        RAISE EXCEPTION 'captured_by_assignment_id es inmutable (membership %): era %, se intentó poner %',
            OLD.id, OLD.captured_by_assignment_id, NEW.captured_by_assignment_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SET search_path = public, pg_catalog;

CREATE TRIGGER membership_protect_lineage_trg
    BEFORE UPDATE ON membership
    FOR EACH ROW EXECUTE FUNCTION membership_protect_lineage();

-- =====================================================================
-- Vistas de conveniencia (estado vigente)
-- =====================================================================

CREATE VIEW v_current_holder AS
SELECT a.position_id, a.person_id, a.id AS assignment_id,
       p.names, p.last_name, p.sur_name,
       lower(a.validity) AS since
FROM assignment a
JOIN person p ON p.id = a.person_id
WHERE a.role = 'HOLDER' AND upper(a.validity) IS NULL;

-- personas presentes en más de una estructura a la vez (duplicidad explícita)
CREATE VIEW v_person_multi_structure AS
SELECT person_id, count(*) AS structures, array_agg(structure_id) AS ids
FROM membership
WHERE upper(validity) IS NULL
GROUP BY person_id
HAVING count(*) > 1;
