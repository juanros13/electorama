-- =====================================================================
-- Electorama — msvc-users
-- V3: siembra la entrada del tablero de campaña
--
-- La tabla por municipio de "estructura estatal.xlsx" más el avance real
-- (`/admin/structure/dashboard`). Va primero, antes de Organigrama (10).
-- Sólo ADMIN por ahora.
--
-- Es IDEMPOTENTE: si la entrada ya existe (mismo `link`), no la duplica.
-- Requiere V1 (que adelantó las secuencias).
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V3__menu_dashboard.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO menus_groups (id, title, subtitle, icon, link, "order", type)
SELECT 'structure.dashboard', 'Tablero de campaña', 'Meta, estructura y avance por municipio',
       'layout-dashboard', '/admin/structure/dashboard', 5, 'basic'
WHERE NOT EXISTS (SELECT 1 FROM menus_groups g WHERE g.link = '/admin/structure/dashboard');

INSERT INTO menus_roles (menu_group_id, role)
SELECT g.pk, 'ADMIN'
FROM menus_groups g
WHERE g.link = '/admin/structure/dashboard'
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
