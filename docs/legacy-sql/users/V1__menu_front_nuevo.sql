-- =====================================================================
-- Electorama — msvc-users
-- V1: siembra el menú del front nuevo
--
-- El menú vive en `menus_groups` + `menus_roles` desde siempre, pero sus 29
-- entradas apuntan al front anterior. Las rutas de esos módulos salieron de
-- circulación mientras se rehacen, así que hoy ninguna entrada de la base
-- lleva a una ruta que exista: el front las descarta al pintar el menú y la
-- pantalla de accesos las marca como enlace roto.
--
-- Esto agrega las dos del front nuevo. Lo demás se queda como está: borrar las
-- viejas es una decisión de la operación, y ahora se puede tomar desde la
-- pantalla en vez de con un DELETE a mano.
--
-- Es IDEMPOTENTE: si la entrada ya existe (mismo `link`), no la duplica.
--
--   psql -U postgres -d msvc_dash_user -v ON_ERROR_STOP=1 -f V1__menu_front_nuevo.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

-- ---------------------------------------------------------------------
-- 0. la secuencia, que viene desfasada
--    Las 29 entradas se cargaron con su `pk` explícito, así que la secuencia
--    se quedó en el arranque y el primer INSERT choca con una llave que ya
--    existe. Se adelanta al máximo actual.
-- ---------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('menus_groups', 'pk'),
              GREATEST((SELECT COALESCE(max(pk), 0) FROM menus_groups), 1));
SELECT setval(pg_get_serial_sequence('menus_roles', 'id'),
              GREATEST((SELECT COALESCE(max(id), 0) FROM menus_roles), 1));

-- ---------------------------------------------------------------------
-- 1. las entradas
--    `order` va de 10 en 10 para poder meter cosas en medio sin renumerar.
-- ---------------------------------------------------------------------
INSERT INTO menus_groups (id, title, subtitle, icon, link, "order", type)
SELECT v.id, v.title, v.subtitle, v.icon, v.link, v.orden, v.tipo
FROM (VALUES
        ('structure.organization', 'Organigrama', 'Puestos, titulares y vigencias',
         'network', '/admin/structure/organization', 10, 'basic'),
        ('access.matrix', 'Accesos', 'Qué ve cada rol',
         'shield-check', '/admin/access', 20, 'basic')
     ) AS v(id, title, subtitle, icon, link, orden, tipo)
WHERE NOT EXISTS (SELECT 1 FROM menus_groups g WHERE g.link = v.link);

-- ---------------------------------------------------------------------
-- 2. quién las ve
--    Sólo ADMIN por ahora. Los demás roles se abren desde la pantalla,
--    conforme cada vista aprenda a acotarse al ámbito de quien la mira.
-- ---------------------------------------------------------------------
INSERT INTO menus_roles (menu_group_id, role)
SELECT g.pk, 'ADMIN'
FROM menus_groups g
WHERE g.link IN ('/admin/structure/organization', '/admin/access')
  AND NOT EXISTS (
        SELECT 1 FROM menus_roles r
         WHERE r.menu_group_id = g.pk AND r.role = 'ADMIN');

COMMIT;

-- =====================================================================
-- verificación
-- =====================================================================
\echo '--- las entradas del front nuevo ---'
SELECT g.pk, g.title, g.link, g."order",
       string_agg(r.role, ', ' ORDER BY r.role) AS roles
FROM menus_groups g
LEFT JOIN menus_roles r ON r.menu_group_id = g.pk
WHERE g.link IN ('/admin/structure/organization', '/admin/access')
GROUP BY g.pk, g.title, g.link, g."order"
ORDER BY g."order";

\echo '--- cuántas entradas apuntan a rutas del front nuevo, y cuántas no ---'
SELECT count(*) FILTER (WHERE link IN ('/admin/structure/organization', '/admin/access')) AS vivas,
       count(*) FILTER (WHERE link IS NULL
                           OR link NOT IN ('/admin/structure/organization', '/admin/access')) AS por_revisar
FROM menus_groups;
