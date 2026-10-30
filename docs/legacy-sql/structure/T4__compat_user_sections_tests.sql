-- =====================================================================
-- Pruebas de la vista de compatibilidad `users_seccions` (fase 3b).
-- Todo corre dentro de una transacción y termina en ROLLBACK: no deja
-- datos ni del lado viejo ni del nuevo (postgres_fdw aborta también la
-- transacción remota). Las secuencias sí avanzan, que es inofensivo.
--
--   psql -U postgres -d msvc_dash_user -f T4__compat_user_sections_tests.sql
-- =====================================================================
\set ON_ERROR_STOP on
\pset pager off
BEGIN;

-- --- datos de apoyo -------------------------------------------------
-- una fila que viene de un puesto sobre la SECCIÓN, con meta
SELECT id AS t_sec_id, user_auth_id AS t_sec_auth, seccion_geo_id AS t_sec_geo,
       meta AS t_sec_meta, municipality_id AS t_sec_mun
FROM users_seccions
WHERE meta IS NOT NULL AND seccion_id IS NOT NULL
ORDER BY id LIMIT 1
\gset

-- una fila que viene de un puesto MUNICIPAL (rama alterna)
SELECT id AS t_mun_id, user_auth_id AS t_mun_auth, seccion_geo_id AS t_mun_geo,
       municipality_id AS t_mun_mun
FROM users_seccions
WHERE seccion_id IS NULL
ORDER BY id LIMIT 1
\gset

\echo ''
\echo '### 1. lectura: las dos formas de fila'
SELECT :t_sec_meta IS NOT NULL   AS seccion_trae_meta,
       (SELECT count(*) FROM users_seccions WHERE user_auth_id = :'t_mun_auth') AS filas_del_municipal,
       (SELECT count(DISTINCT municipality_id) FROM users_seccions
         WHERE user_auth_id = :'t_mun_auth')                                   AS municipios_del_municipal;

\echo ''
\echo '### 2. cambio de meta en una fila de sección'
UPDATE users_seccions SET meta = 999, limit_promoters = 7 WHERE id = :t_sec_id;

SELECT meta, limit_promoters FROM users_seccions WHERE id = :t_sec_id;

SELECT (SELECT goal FROM str.position WHERE id = compat.us_position(:t_sec_id)) AS meta_en_el_puesto;

\echo ''
\echo '### 3. cambio de meta en una fila expandida: crea el puesto de la sección'
SELECT count(*) AS cz_antes FROM str.position
 WHERE level = 'ZONE_COORDINATOR' AND valid_to IS NULL
   AND code LIKE '%|' || :'t_mun_auth'
\gset alt_

UPDATE users_seccions SET meta = 123 WHERE id = :t_mun_id;

SELECT (SELECT count(*) FROM str.position
         WHERE level = 'ZONE_COORDINATOR' AND valid_to IS NULL
           AND code LIKE '%|' || :'t_mun_auth') = :alt_cz_antes + 1 AS creo_un_solo_puesto,
       (SELECT count(*) FROM users_seccions
         WHERE user_auth_id = :'t_mun_auth' AND seccion_geo_id = :t_mun_geo) = 1 AS sin_duplicar,
       (SELECT meta FROM users_seccions
         WHERE user_auth_id = :'t_mun_auth' AND seccion_geo_id = :t_mun_geo) = 123 AS meta_aplicada,
       (SELECT valid_to IS NULL FROM str.position WHERE id = compat.us_position(:t_mun_id))
                                                                                 AS municipal_sigue_vigente;

\echo ''
\echo '### 4. alta de una sección que ya está cubierta: no crea nada'
SELECT count(*) AS pos_antes FROM str.position
 WHERE level = 'ZONE_COORDINATOR' AND valid_to IS NULL
   AND code LIKE '%|' || :'t_sec_auth'
\gset sec_

INSERT INTO users_seccions (user_auth_id, seccion_number, seccion_geo_id, municipality_id, meta)
VALUES (:'t_sec_auth', '0001', :t_sec_geo, :t_sec_mun, 555);

SELECT (SELECT count(*) FROM str.position
         WHERE level = 'ZONE_COORDINATOR' AND valid_to IS NULL
           AND code LIKE '%|' || :'t_sec_auth') = :sec_pos_antes AS no_creo_puesto,
       (SELECT meta FROM users_seccions WHERE id = :t_sec_id) = 555 AS aplico_la_meta;

\echo ''
\echo '### 5. alta de una sección nueva para ese coordinador'
SELECT t.section_geo_id AS t_free_geo
FROM str.territory t
WHERE t.type = 'SECTION' AND t.valid_to IS NULL
  AND NOT EXISTS (SELECT 1 FROM users_seccions v
                   WHERE v.user_auth_id = :'t_sec_auth' AND v.seccion_geo_id = t.section_geo_id)
ORDER BY t.id LIMIT 1
\gset

INSERT INTO users_seccions (user_auth_id, seccion_number, seccion_geo_id, municipality_id, meta)
VALUES (:'t_sec_auth', '9999', :t_free_geo, :t_sec_mun, 42)
RETURNING id AS t_new_id
\gset

SELECT (SELECT meta FROM users_seccions WHERE id = :t_new_id) = 42            AS meta_ok,
       (SELECT seccion_geo_id FROM users_seccions WHERE id = :t_new_id) = :t_free_geo AS seccion_ok,
       (SELECT count(*) FROM str.assignment a
         WHERE a.position_id = compat.us_position(:t_new_id)
           AND a.role = 'HOLDER' AND upper(a.validity) IS NULL) AS titulares;

\echo ''
\echo '### 6. baja lógica de una sección sin promotores: desaparece de la vista'
UPDATE users_seccions SET deleted = now() WHERE id = :t_new_id;

SELECT (SELECT count(*) FROM users_seccions WHERE id = :t_new_id) AS en_la_vista,
       (SELECT valid_to IS NOT NULL FROM str.position WHERE id = compat.us_position(:t_new_id))
                                                                 AS puesto_cerrado,
       (SELECT count(*) FROM str.assignment a
         WHERE a.position_id = compat.us_position(:t_new_id)
           AND upper(a.validity) IS NULL)                        AS titulares_vigentes;

\echo ''
\echo '### 7. baja de una sección con promotores: se niega'
SELECT v.id AS t_busy_id
FROM users_seccions v
WHERE EXISTS (SELECT 1 FROM str.position pr
               WHERE pr.parent_position_id = compat.us_position(v.id)
                 AND pr.level = 'PROMOTER' AND pr.valid_to IS NULL)
ORDER BY v.id LIMIT 1
\gset

SAVEPOINT antes_de_la_baja;
\set ON_ERROR_STOP off
UPDATE users_seccions SET deleted = now() WHERE id = :t_busy_id;
\set ON_ERROR_STOP on
ROLLBACK TO SAVEPOINT antes_de_la_baja;

SELECT count(*) = 1 AS sigue_en_la_vista FROM users_seccions WHERE id = :t_busy_id;

\echo ''
\echo '### 8. baja de una fila expandida: materializa y quita sólo esa sección'
SELECT id AS t_mun2_id, user_auth_id AS t_mun2_auth, seccion_geo_id AS t_mun2_geo
FROM users_seccions
WHERE seccion_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM str.position pr
                   WHERE pr.parent_position_id = compat.us_position(users_seccions.id)
                     AND pr.level = 'PROMOTER' AND pr.valid_to IS NULL)
ORDER BY id DESC LIMIT 1
\gset

SELECT count(*) AS filas_antes FROM users_seccions WHERE user_auth_id = :'t_mun2_auth'
\gset mun2_

UPDATE users_seccions SET deleted = now() WHERE id = :t_mun2_id;

SELECT (SELECT count(*) FROM users_seccions
         WHERE user_auth_id = :'t_mun2_auth' AND seccion_geo_id = :t_mun2_geo) = 0
                                                             AS la_seccion_desaparecio,
       (SELECT count(*) FROM users_seccions
         WHERE user_auth_id = :'t_mun2_auth') = :mun2_filas_antes - 1
                                                             AS el_resto_del_municipio_sigue;

ROLLBACK;
\echo ''
\echo '=== pruebas terminadas, transacción revertida ==='
