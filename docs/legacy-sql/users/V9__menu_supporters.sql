-- =====================================================================
-- Electorama — msvc-users
-- V9: el listado de convencidos (Mis convencidos)
--
-- `/admin/structure/supporters`: la meta y el avance arriba, y los convencidos
-- del puesto y de lo que cuelga de él, con búsqueda y filtros (msvc-structure
-- /memberships/search y /summary, que validan la rama). Antes de la captura.
--
-- Es IDEMPOTENTE. Requiere V1 (que adelantó las secuencias).
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V9__menu_supporters.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO menus_groups (id, title, subtitle, icon, link, "order", type)
SELECT 'structure.supporters', 'Mis convencidos', 'Tu meta, tu avance y tus convencidos',
       'users', '/admin/structure/supporters', 11, 'basic'
WHERE NOT EXISTS (SELECT 1 FROM menus_groups g WHERE g.link = '/admin/structure/supporters');

INSERT INTO menus_roles (menu_group_id, role)
SELECT g.pk, r.role
FROM menus_groups g
CROSS JOIN (VALUES ('ADMIN'), ('CORDINATOR_MUNICIPAL'), ('CORDINATOR_MUNICIPAL_ALT'),
                   ('CORDINATOR_ZONE'), ('CORDINATOR_ZONE_ALT'), ('PROMOTER')) AS r(role)
WHERE g.link = '/admin/structure/supporters'
  AND NOT EXISTS (
        SELECT 1 FROM menus_roles x
         WHERE x.menu_group_id = g.pk AND x.role = r.role);

COMMIT;

\echo '--- el menú del front nuevo ---'
SELECT g.pk, g.title, g.link, g."order",
       string_agg(r.role, ', ' ORDER BY r.role) AS roles
FROM menus_groups g
LEFT JOIN menus_roles r ON r.menu_group_id = g.pk
WHERE g.link LIKE '/admin/%'
GROUP BY g.pk, g.title, g.link, g."order"
ORDER BY g."order";
