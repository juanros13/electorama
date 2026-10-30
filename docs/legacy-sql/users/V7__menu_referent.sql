-- =====================================================================
-- Electorama — msvc-users
-- V7: la estructura entra al organigrama y al histórico electoral
--
-- Referente, promotor y movilizador (CORDINATOR_MUNICIPAL, CORDINATOR_ZONE,
-- sus _ALT y PROMOTER) ven:
--   /admin/structure/organization       su rama: sus puestos y lo que cuelga
--                                       de ellos (msvc-structure la acota por
--                                       su nombramiento vigente)
--   /admin/electoral-history/dashboard  información de todo el estado
--
-- En su rama nombran en vacantes, dejan vacantes los puestos de abajo y
-- cambian contraseñas de esos titulares; no crean estructura ni tocan metas.
-- Lo validan msvc-structure (BranchGuard) y msvc-users (AccountAccess).
-- El tablero y simpatizantes 2024 llegan cuando filtren a su municipio.
--
-- Es IDEMPOTENTE. Requiere que existan las entradas (V1 y V4).
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V7__menu_referent.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO menus_roles (menu_group_id, role)
SELECT g.pk, r.role
FROM menus_groups g
CROSS JOIN (VALUES ('CORDINATOR_MUNICIPAL'), ('CORDINATOR_MUNICIPAL_ALT'),
                   ('CORDINATOR_ZONE'), ('CORDINATOR_ZONE_ALT'), ('PROMOTER')) AS r(role)
WHERE g.link IN ('/admin/structure/organization', '/admin/electoral-history/dashboard')
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
