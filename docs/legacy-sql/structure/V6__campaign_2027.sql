-- =====================================================================
-- Electorama — msvc-structure
-- V6: la campaña 2027 y el cierre de 2024
--
-- Hasta aquí todo colgaba de «Tabasco 2024» y ninguna consulta filtraba por
-- campaña. Decisión del usuario (28-sep-2026):
--   · se abre «Tabasco 2027» (ACTIVE) con estructuras Principal y Alterna
--     VACÍAS: la estructura 2027 se arma desde cero;
--   · 2024 se queda completo como análisis: sus puestos, titulares y
--     convencidos no se borran ni se cierran, pero ya no se pueden escribir
--     (trigger campaign_closed_guard) y los servicios filtran por campaña;
--   · el catálogo de elecciones 2027 (campaign_election) dice qué se elige.
--
-- La captura heredada (vistas de compatibilidad de V3b/V3c/V3d en
-- msvc_dash_followers y msvc_dash_user) resuelve la estructura a partir de
-- puestos que ya existen, todos de 2024: con el candado, esa escritura se
-- rechaza con un error claro en vez de caer en silencio en 2024. La captura
-- 2027 va por los endpoints nuevos de msvc-structure.
--
-- Es IDEMPOTENTE. Requiere V1..V4d.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V6__campaign_2027.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Una sola campaña ACTIVE por territorio
-- ---------------------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS campaign_one_active_uq
    ON campaign (coalesce(territory_id, 0))
 WHERE status = 'ACTIVE';

-- ---------------------------------------------------------------------
-- 2. Qué se elige en cada campaña
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS campaign_election (
    id              BIGSERIAL PRIMARY KEY,
    campaign_id     BIGINT      NOT NULL REFERENCES campaign (id),
    -- mismos códigos que msvc-electoral-history (election.office / scope)
    office          VARCHAR(20) NOT NULL,
    scope           VARCHAR(20) NOT NULL,
    -- cuántos cargos: 17 ayuntamientos, 21 distritos locales, 6 federales
    seats           INTEGER     NOT NULL,
    election_day    DATE        NOT NULL,
    CONSTRAINT campaign_election_office_chk
        CHECK (office IN ('PRESIDENT','SENATOR','FEDERAL_DEPUTY','GOVERNOR','LOCAL_DEPUTY','MAYOR')),
    CONSTRAINT campaign_election_scope_chk
        CHECK (scope IN ('STATE','FEDERAL_DISTRICT','LOCAL_DISTRICT','MUNICIPALITY')),
    CONSTRAINT campaign_election_uq UNIQUE (campaign_id, office)
);

-- ---------------------------------------------------------------------
-- 3. Tabasco 2027 con sus estructuras vacías
--    Jornada: primer domingo de junio de 2027 (6-jun), federal y local.
-- ---------------------------------------------------------------------
INSERT INTO campaign (name, type, year, election_day, status, territory_id)
SELECT 'Tabasco 2027', 'STATE', 2027, DATE '2027-06-06', 'ACTIVE', t.id
  FROM territory t
 WHERE t.type = 'STATE'
   AND NOT EXISTS (SELECT 1 FROM campaign WHERE name = 'Tabasco 2027');

INSERT INTO structure (campaign_id, name, type)
SELECT c.id, x.name, x.type
  FROM campaign c
 CROSS JOIN (VALUES ('Principal', 'MAIN'), ('Alterna', 'ALTERNATE')) AS x (name, type)
 WHERE c.name = 'Tabasco 2027'
   AND NOT EXISTS (SELECT 1 FROM structure s WHERE s.campaign_id = c.id AND s.type = x.type);

INSERT INTO campaign_election (campaign_id, office, scope, seats, election_day)
SELECT c.id, x.office, x.scope, x.seats, c.election_day
  FROM campaign c
 CROSS JOIN (VALUES ('MAYOR',          'MUNICIPALITY',     17),
                    ('LOCAL_DEPUTY',   'LOCAL_DISTRICT',   21),
                    ('FEDERAL_DEPUTY', 'FEDERAL_DISTRICT',  6)) AS x (office, scope, seats)
 WHERE c.name = 'Tabasco 2027'
ON CONFLICT (campaign_id, office) DO UPDATE
   SET scope = EXCLUDED.scope, seats = EXCLUDED.seats, election_day = EXCLUDED.election_day;

-- 2024 ya estaba CLOSED desde V2; se deja explícito y con su fecha de jornada
UPDATE campaign
   SET status = 'CLOSED', election_day = coalesce(election_day, DATE '2024-06-02')
 WHERE name = 'Tabasco 2024';

-- ---------------------------------------------------------------------
-- 4. Una campaña cerrada es de sólo lectura
--    Cubre puestos, titulares y convencidos; la persona es compartida entre
--    campañas y se puede seguir actualizando.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION campaign_closed_guard() RETURNS trigger AS $$
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
$$ LANGUAGE plpgsql SET search_path = public, pg_catalog;

DROP TRIGGER IF EXISTS campaign_closed_guard_trg ON position;
CREATE TRIGGER campaign_closed_guard_trg
  BEFORE INSERT OR UPDATE OR DELETE ON position
  FOR EACH ROW EXECUTE FUNCTION campaign_closed_guard();

DROP TRIGGER IF EXISTS campaign_closed_guard_trg ON assignment;
CREATE TRIGGER campaign_closed_guard_trg
  BEFORE INSERT OR UPDATE OR DELETE ON assignment
  FOR EACH ROW EXECUTE FUNCTION campaign_closed_guard();

DROP TRIGGER IF EXISTS campaign_closed_guard_trg ON membership;
CREATE TRIGGER campaign_closed_guard_trg
  BEFORE INSERT OR UPDATE OR DELETE ON membership
  FOR EACH ROW EXECUTE FUNCTION campaign_closed_guard();

-- ---------------------------------------------------------------------
-- 5. Un convencido vive en la estructura de su puesto
--    Sin esto se podría anclar un convencido 2027 a un puesto 2024.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION membership_same_structure() RETURNS trigger AS $$
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
$$ LANGUAGE plpgsql SET search_path = public, pg_catalog;

DROP TRIGGER IF EXISTS membership_same_structure_trg ON membership;
CREATE TRIGGER membership_same_structure_trg
  BEFORE INSERT OR UPDATE OF structure_id, position_id ON membership
  FOR EACH ROW EXECUTE FUNCTION membership_same_structure();

-- ---------------------------------------------------------------------
-- verificación
-- ---------------------------------------------------------------------
SELECT c.id, c.name, c.year, c.status, c.election_day,
       (SELECT string_agg(s.id || ':' || s.type, ', ' ORDER BY s.id) FROM structure s WHERE s.campaign_id = c.id) AS estructuras,
       (SELECT string_agg(e.office || ' ×' || e.seats, ', ' ORDER BY e.office) FROM campaign_election e WHERE e.campaign_id = c.id) AS elecciones
  FROM campaign c
 ORDER BY c.year;

COMMIT;
