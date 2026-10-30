-- =====================================================================
-- V7 · Clave legible del puesto por sección: S-0233A1, S-0233A1-01
--
--   promotor    (ZONE_COORDINATOR)  S-0233A1     sección + letra + consecutivo
--   movilizador (PROMOTER)          S-0233A1-01  clave del promotor + consecutivo
--
--   La letra sale de la estructura: A la principal, B la alterna. El
--   consecutivo es por sección y estructura (promotor) o por promotor
--   (movilizador), y NO se reutiliza: los puestos se cierran con valid_to y
--   nunca se borran, así que el siguiente es el máximo histórico + 1. Si se
--   cierra S-0233A2, la clave sigue siendo de ese puesto en el historial.
--
--   Es una columna NUEVA y no un cambio de `code`: las funciones de compat
--   (V3c/V3d, materialize_municipal) buscan los puestos por
--   code = 'CZ|' || sección || '|' || cuenta, y reescribirlo haría que
--   msvc-users duplicara los promotores.
--
--   La pone la base con un trigger BEFORE INSERT, venga de donde venga el
--   alta (BuildService, compat, psql). El backfill es sólo de la campaña
--   2027: la 2024 está cerrada y se queda sin clave.
--
--   Idempotente: se puede volver a correr. Rollback: V7R.
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

ALTER TABLE position ADD COLUMN IF NOT EXISTS section_key VARCHAR(20);

-- única por estructura: otra campaña (2030) vuelve a tener su S-0233A1
CREATE UNIQUE INDEX IF NOT EXISTS position_section_key_uq
    ON position (structure_id, section_key) WHERE section_key IS NOT NULL;

-- ---------------------------------------------------------------------
-- La clave que le toca a un puesto nuevo. NULL si no aplica: referentes,
-- promotores que no están sobre una sección, movilizadores de un promotor
-- sin clave.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION position_next_section_key(
    p_structure bigint, p_level text, p_parent bigint, p_territory bigint)
RETURNS varchar AS $$
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
$$ LANGUAGE plpgsql VOLATILE;

-- ---------------------------------------------------------------------
-- Alta: la pone la base. Cambio: la clave no se mueve una vez puesta,
-- aunque el puesto cambie de sección o de promotor (identifica al puesto).
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION position_section_key() RETURNS trigger AS $$
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
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS position_section_key_trg ON position;
CREATE TRIGGER position_section_key_trg
  BEFORE INSERT OR UPDATE OF section_key ON position
  FOR EACH ROW EXECUTE FUNCTION position_section_key();

-- ---------------------------------------------------------------------
-- Backfill de 2027, en orden de alta (id) para que A1 sea el más viejo.
-- Incluye los puestos cerrados: así su número queda ocupado.
-- Primero promotores, después movilizadores (heredan la del promotor).
-- ---------------------------------------------------------------------
WITH numbered AS (
  SELECT p.id,
         t.code
         || CASE s.type WHEN 'MAIN' THEN 'A' ELSE 'B' END
         || row_number() OVER (PARTITION BY p.structure_id, t.code ORDER BY p.id) AS k
    FROM position p
    JOIN structure s ON s.id = p.structure_id
    JOIN campaign c  ON c.id = s.campaign_id AND c.status <> 'CLOSED'
    JOIN territory t ON t.id = p.territory_id AND t.type = 'SECTION'
   WHERE p.level = 'ZONE_COORDINATOR'
     AND s.type IN ('MAIN', 'ALTERNATE')
     AND p.section_key IS NULL)
UPDATE position p SET section_key = n.k FROM numbered n WHERE p.id = n.id;

WITH numbered AS (
  SELECT c.id,
         par.section_key || '-'
         || lpad(row_number() OVER (PARTITION BY c.parent_position_id ORDER BY c.id)::text, 2, '0') AS k
    FROM position c
    JOIN position par ON par.id = c.parent_position_id AND par.section_key IS NOT NULL
    JOIN structure s  ON s.id = c.structure_id
    JOIN campaign cp  ON cp.id = s.campaign_id AND cp.status <> 'CLOSED'
   WHERE c.level = 'PROMOTER'
     AND c.section_key IS NULL)
UPDATE position p SET section_key = n.k FROM numbered n WHERE p.id = n.id;

COMMIT;

-- verificación rápida
SELECT s.type, p.level, count(*) AS puestos, count(p.section_key) AS con_clave,
       min(p.section_key) AS ejemplo
  FROM position p
  JOIN structure s ON s.id = p.structure_id
  JOIN campaign c  ON c.id = s.campaign_id AND c.status <> 'CLOSED'
 WHERE p.level <> 'REFERENT'
 GROUP BY 1, 2 ORDER BY 1, 2;
