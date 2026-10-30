-- Revierte la fase 3: quita la vista y devuelve la tabla a su nombre.
-- No toca datos: `followers_legacy` nunca dejó de ser la fuente real.
BEGIN;
DROP VIEW IF EXISTS followers;
DROP VIEW IF EXISTS nw_hierarchy_v;
ALTER TABLE followers_legacy RENAME TO followers;
COMMIT;
