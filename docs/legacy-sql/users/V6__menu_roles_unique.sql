-- =====================================================================
-- Electorama — msvc-users
-- V6: un rol aparece una sola vez por entrada de menú
--
-- La pantalla de accesos reemplaza los roles de una entrada borrando e
-- insertando; sin esta regla, dos guardados simultáneos podían dejar el mismo
-- rol repetido. Si hay repetidos se quitan antes de crear el índice.
--
-- Es IDEMPOTENTE.
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V6__menu_roles_unique.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

DELETE FROM menus_roles r
 USING menus_roles d
 WHERE r.menu_group_id = d.menu_group_id
   AND r.role = d.role
   AND r.id > d.id;

CREATE UNIQUE INDEX IF NOT EXISTS menus_roles_group_role_uq
    ON menus_roles (menu_group_id, role);

COMMIT;
