-- =====================================================================
-- Electorama — msvc-users
-- V4: siembra la entrada del histórico electoral
--
-- Resultados oficiales 2018, 2021 y 2024 por municipio y sección
-- (`/admin/electoral-history/dashboard`, msvc-electoral-history). Después del catálogo (15).
-- Sólo ADMIN por ahora.
--
-- Es IDEMPOTENTE: si la entrada ya existe (mismo `link`), no la duplica.
-- Requiere V1 (que adelantó las secuencias).
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V4__menu_electoral_history.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO menus_groups (id, title, subtitle, icon, link, "order", type)
SELECT 'electoralHistory.dashboard', 'Histórico electoral', 'Cómo votó cada municipio en 2018, 2021 y 2024',
       'vote', '/admin/electoral-history/dashboard', 17, 'basic'
WHERE NOT EXISTS (SELECT 1 FROM menus_groups g WHERE g.link = '/admin/electoral-history/dashboard');

INSERT INTO menus_roles (menu_group_id, role)
SELECT g.pk, 'ADMIN'
FROM menus_groups g
WHERE g.link = '/admin/electoral-history/dashboard'
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
