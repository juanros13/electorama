-- =====================================================================
-- Electorama — msvc-users
-- V8: la captura de convencidos en el front
--
-- `/admin/structure/capture`: el movilizador (y el promotor y el referente,
-- en su rama) registra convencidos con los mismos datos que la app. Guarda
-- directo en msvc-structure (POST /memberships), que valida la rama.
-- Después del organigrama (10).
--
-- Es IDEMPOTENTE. Requiere V1 (que adelantó las secuencias).
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V8__menu_capture.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO menus_groups (id, title, subtitle, icon, link, "order", type)
SELECT 'structure.capture', 'Capturar convencidos', 'Registra simpatizantes en tu puesto o tu rama',
       'user-plus', '/admin/structure/capture', 12, 'basic'
WHERE NOT EXISTS (SELECT 1 FROM menus_groups g WHERE g.link = '/admin/structure/capture');

INSERT INTO menus_roles (menu_group_id, role)
SELECT g.pk, r.role
FROM menus_groups g
CROSS JOIN (VALUES ('ADMIN'), ('CORDINATOR_MUNICIPAL'), ('CORDINATOR_MUNICIPAL_ALT'),
                   ('CORDINATOR_ZONE'), ('CORDINATOR_ZONE_ALT'), ('PROMOTER')) AS r(role)
WHERE g.link = '/admin/structure/capture'
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
