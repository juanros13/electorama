-- =====================================================================
-- Electorama — msvc-users
-- V5: siembra la entrada del análisis de simpatizantes 2024
--
-- Los convencidos de la campaña 2024 (cerrada, sólo consulta) contra el
-- resultado oficial: `/admin/structure/supporters-2024`, que junta
-- msvc-structure, msvc-geography y msvc-electoral-history. Después del
-- histórico electoral (17). Sólo ADMIN por ahora.
--
-- Es IDEMPOTENTE: si la entrada ya existe (mismo `link`), no la duplica.
-- Requiere V1 (que adelantó las secuencias).
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V5__menu_supporters_2024.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO menus_groups (id, title, subtitle, icon, link, "order", type)
SELECT 'structure.supporters2024', 'Simpatizantes 2024', 'Análisis de la campaña cerrada: convencidos contra votos',
       'heart-handshake', '/admin/structure/supporters-2024', 18, 'basic'
WHERE NOT EXISTS (SELECT 1 FROM menus_groups g WHERE g.link = '/admin/structure/supporters-2024');

INSERT INTO menus_roles (menu_group_id, role)
SELECT g.pk, 'ADMIN'
FROM menus_groups g
WHERE g.link = '/admin/structure/supporters-2024'
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
