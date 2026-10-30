-- =====================================================================
-- Electorama — msvc-users
-- V2: siembra la entrada del catálogo territorial
--
-- La pantalla de secciones, municipios y distritos del front nuevo
-- (`/admin/structure/territory`). Va entre Organigrama (10) y Accesos (20).
-- Sólo ADMIN: es donde se edita el techo de meta de cada sección.
--
-- Es IDEMPOTENTE: si la entrada ya existe (mismo `link`), no la duplica.
-- Requiere V1 (que adelantó las secuencias).
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V2__menu_territory.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO menus_groups (id, title, subtitle, icon, link, "order", type)
SELECT 'structure.territory', 'Catálogo territorial', 'Secciones, municipios y distritos',
       'map', '/admin/structure/territory', 15, 'basic'
WHERE NOT EXISTS (SELECT 1 FROM menus_groups g WHERE g.link = '/admin/structure/territory');

INSERT INTO menus_roles (menu_group_id, role)
SELECT g.pk, 'ADMIN'
FROM menus_groups g
WHERE g.link = '/admin/structure/territory'
  AND NOT EXISTS (
        SELECT 1 FROM menus_roles r
         WHERE r.menu_group_id = g.pk AND r.role = 'ADMIN');

COMMIT;

\echo '--- el menú del front nuevo ---'
SELECT g.pk, g.title, g.link, g."order",
       string_agg(r.role, ', ' ORDER BY r.role) AS roles
FROM menus_groups g
LEFT JOIN menus_roles r ON r.menu_group_id = g.pk
WHERE g.link LIKE '/admin/%'
GROUP BY g.pk, g.title, g.link, g."order"
ORDER BY g."order";
