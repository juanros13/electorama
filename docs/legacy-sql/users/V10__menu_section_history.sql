-- =====================================================================
-- Electorama — msvc-users
-- V10: el histórico electoral de la sección para la estructura
--
-- `/admin/electoral-history/my-section`: cómo votó la sección de quien
-- trabaja la estructura (movilizador, promotor, referente) en 2018, 2021 y
-- 2024 (msvc-electoral-history GET /v1/sections/{section}/history).
--
-- El tablero estatal (`/admin/electoral-history/dashboard`) se queda sólo
-- para ADMIN: sus endpoints son sólo ADMIN y a la estructura le daba 403.
--
-- Es IDEMPOTENTE. Requiere V1 (que adelantó las secuencias) y V7.
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V10__menu_section_history.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO menus_groups (id, title, subtitle, icon, link, "order", type)
SELECT 'electoralHistory.mySection', 'Histórico de mi sección',
       'Cómo votó tu sección en 2018, 2021 y 2024',
       'vote', '/admin/electoral-history/my-section', 17, 'basic'
WHERE NOT EXISTS (
  SELECT 1 FROM menus_groups g WHERE g.link = '/admin/electoral-history/my-section');

INSERT INTO menus_roles (menu_group_id, role)
SELECT g.pk, r.role
FROM menus_groups g
CROSS JOIN (VALUES ('CORDINATOR_MUNICIPAL'), ('CORDINATOR_MUNICIPAL_ALT'),
                   ('CORDINATOR_ZONE'), ('CORDINATOR_ZONE_ALT'), ('PROMOTER')) AS r(role)
WHERE g.link = '/admin/electoral-history/my-section'
  AND NOT EXISTS (
        SELECT 1 FROM menus_roles x
         WHERE x.menu_group_id = g.pk AND x.role = r.role);

-- el tablero estatal, sólo ADMIN (V7 se lo había dado a la estructura)
DELETE FROM menus_roles r
 USING menus_groups g
 WHERE r.menu_group_id = g.pk
   AND g.link = '/admin/electoral-history/dashboard'
   AND r.role IN ('CORDINATOR_MUNICIPAL', 'CORDINATOR_MUNICIPAL_ALT',
                  'CORDINATOR_ZONE', 'CORDINATOR_ZONE_ALT', 'PROMOTER');

COMMIT;

\echo '--- el menú del front nuevo ---'
SELECT g.pk, g.title, g.link, g."order",
       string_agg(r.role, ', ' ORDER BY r.role) AS roles
FROM menus_groups g
LEFT JOIN menus_roles r ON r.menu_group_id = g.pk
WHERE g.link LIKE '/admin/%'
GROUP BY g.pk, g.title, g.link, g."order"
ORDER BY g."order";
