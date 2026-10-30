-- =====================================================================
-- V7R · Deshace V7: quita el trigger, las funciones y la columna
-- section_key de position. No toca `code` ni ningún otro dato.
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

DROP TRIGGER IF EXISTS position_section_key_trg ON position;
DROP FUNCTION IF EXISTS position_section_key();
DROP FUNCTION IF EXISTS position_next_section_key(bigint, text, bigint, bigint);
DROP INDEX IF EXISTS position_section_key_uq;
ALTER TABLE position DROP COLUMN IF EXISTS section_key;

COMMIT;
