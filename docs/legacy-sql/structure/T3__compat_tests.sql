-- =====================================================================
-- Pruebas de las vistas de compatibilidad de la fase 3.
-- Todo corre dentro de una transacción y termina en ROLLBACK: no deja
-- datos ni del lado viejo ni del nuevo (postgres_fdw aborta también la
-- transacción remota). Las secuencias sí avanzan, que es inofensivo.
--
--   psql -U postgres -d msvc_dash_followers -f T3__compat_tests.sql
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;

-- --- datos de apoyo: un promotor real y su sección ------------------
SELECT promoter_auth_id AS t_prom, seccion_id AS t_sec, cordinator_zone_auth_id AS t_cz,
       referent_auth_id AS t_ref, municipality_id AS t_mun
FROM promoters
WHERE promoter_auth_id IS NOT NULL AND seccion_id IS NOT NULL
  AND cordinator_zone_auth_id IS NOT NULL AND referent_auth_id IS NOT NULL
ORDER BY id LIMIT 1
\gset

-- otro promotor distinto, para probar el traslado
SELECT promoter_auth_id AS t_prom2
FROM promoters
WHERE promoter_auth_id IS NOT NULL AND promoter_auth_id <> :'t_prom'
ORDER BY id LIMIT 1
\gset

\echo ''
\echo '### 1. alta de convencido por la vista'
INSERT INTO followers (names, last_name, sur_name, curp, ine, cell_phone_number,
                       status, promoter_auth_id, section_id, user_create_id,
                       device_id, uuid, latitude)
VALUES ('T3_PRUEBA', 'APELLIDO', 'SEGUNDO', 'XTST900101HTCXXX01', 'XTSTINE0001',
        '9990000000', 'ACTIVO', :'t_prom', :t_sec, :'t_cz', 'DEV-T3', 'uuid-t3', '17.98')
RETURNING id AS t_fol
\gset

SELECT :t_fol AS id_asignado;

\echo '--- se lee por la vista con los datos de ambos lados ---'
SELECT id, names, curp, status, promoter_auth_id IS NOT NULL AS tiene_promotor,
       section_id = :t_sec AS seccion_ok,
       device_id, uuid, latitude
FROM followers WHERE id = :t_fol;

\echo '--- la membresía existe en el modelo nuevo y con linaje APP ---'
SELECT m.legacy_id, m.source, m.status, upper(m.validity) IS NULL AS vigente,
       p.curp AS persona_curp
FROM str.membership m JOIN str.person p ON p.id = m.person_id
WHERE m.legacy_id = :t_fol;

\echo ''
\echo '### 2. edición: nombre, status y marca de voto'
UPDATE followers
   SET names = 'T3_EDITADO', status = 'INACTIVO', vote_flag = true,
       vote_flag_timestamp = now(), longitude = '-92.93'
 WHERE id = :t_fol;

SELECT names, status, vote_flag, longitude FROM followers WHERE id = :t_fol;

\echo ''
\echo '### 3. traslado a otro promotor: cambia el puesto, NO el linaje'
SELECT position_id AS t_pos_antes, captured_by_assignment_id AS t_lin_antes
FROM str.membership WHERE legacy_id = :t_fol
\gset

UPDATE followers SET promoter_auth_id = :'t_prom2' WHERE id = :t_fol;

SELECT (position_id <> :t_pos_antes)                      AS cambio_de_puesto,
       (captured_by_assignment_id::text
          IS NOT DISTINCT FROM nullif(:'t_lin_antes','')) AS linaje_intacto
FROM str.membership WHERE legacy_id = :t_fol;

\echo '--- quedó registrado como TRANSFER en la bitácora ---'
SELECT type, entity, entity_id FROM str.change_log
WHERE entity = 'membership' AND entity_id = :t_fol ORDER BY id;

\echo ''
\echo '### 4. baja: desaparece de la vista, la historia se queda'
DELETE FROM followers WHERE id = :t_fol;

SELECT (SELECT count(*) FROM followers WHERE id = :t_fol)                    AS en_la_vista,
       (SELECT count(*) FROM str.membership WHERE legacy_id = :t_fol)        AS en_el_modelo,
       (SELECT upper(validity) IS NOT NULL FROM str.membership
         WHERE legacy_id = :t_fol)                                           AS vigencia_cerrada;

\echo ''
\echo '### 5. alta de promotor por la vista'
INSERT INTO promoters (names, last_name, sur_name, curp, ine, cell_phone_number,
                       street, municipality_id, seccion_id,
                       cordinator_zone_auth_id, referent_auth_id, promoter_auth_id,
                       followers_goal, flag_alter, notes)
VALUES ('T3_PROMOTOR', 'APELLIDO', 'SEGUNDO', 'XTST900202HTCXXX02', 'XTSTINE0002',
        '9990000001', 'CALLE PRUEBA', :t_mun, :t_sec,
        :'t_cz', :'t_ref', 'authid-t3-promotor', 150, false, 'nota de prueba')
RETURNING id AS t_new_prom
\gset

SELECT id, names, seccion_id = :t_sec AS seccion_ok, followers_goal, notes,
       cordinator_zone_auth_id = :'t_cz' AS cz_ok,
       referent_auth_id = :'t_ref'       AS ref_ok
FROM promoters WHERE id = :t_new_prom;

\echo '--- puesto y asignación creados en el modelo nuevo ---'
SELECT p.level, p.goal, p.valid_to IS NULL AS puesto_vigente,
       (SELECT count(*) FROM str.assignment a
         WHERE a.position_id = p.id AND upper(a.validity) IS NULL) AS titulares
FROM str.position p WHERE p.legacy_id = :t_new_prom;

\echo ''
\echo '### 6. cambio de meta'
UPDATE promoters SET followers_goal = 320 WHERE id = :t_new_prom;
SELECT followers_goal FROM promoters WHERE id = :t_new_prom;

\echo ''
\echo '### 7. baja de promotor: se cierra el puesto, los convencidos siguen'
SELECT count(*) AS convencidos_del_promotor_original
FROM followers WHERE promoter_auth_id = :'t_prom';

DELETE FROM promoters WHERE id = :t_new_prom;

SELECT (SELECT count(*) FROM promoters WHERE id = :t_new_prom)                    AS en_la_vista,
       (SELECT valid_to IS NOT NULL FROM str.position WHERE legacy_id = :t_new_prom) AS puesto_cerrado,
       (SELECT count(*) FROM str.assignment a JOIN str.position p ON p.id = a.position_id
         WHERE p.legacy_id = :t_new_prom AND upper(a.validity) IS NULL)           AS titulares_vigentes;

\echo ''
\echo '### 8. rotación: el convencido NO se mueve cuando rota el titular'
\echo '--- (se hace directo sobre el modelo nuevo; es lo que la fase 4 usará) ---'
SELECT p.id AS t_rot_pos
FROM str.position p
WHERE p.level = 'PROMOTER' AND p.legacy_id IS NOT NULL AND p.valid_to IS NULL
  AND EXISTS (SELECT 1 FROM str.membership m WHERE m.position_id = p.id)
ORDER BY p.id LIMIT 1
\gset

SELECT count(*) AS convencidos_antes FROM str.membership WHERE position_id = :t_rot_pos
\gset conv_

UPDATE str.assignment
   SET validity = compat.close_validity(validity), end_reason = 'prueba de rotación'
 WHERE position_id = :t_rot_pos AND role = 'HOLDER' AND upper(validity) IS NULL;

-- el id y `created` van explícitos por la misma razón que en los triggers:
-- postgres_fdw manda todas las columnas y el DEFAULT remoto no se aplica
INSERT INTO str.assignment (id, created, position_id, person_id, role, validity, start_reason)
SELECT compat.next_assignment_id(), now(), :t_rot_pos, id, 'HOLDER',
       tstzrange(clock_timestamp(), NULL), 'prueba de rotación'
FROM str.person WHERE curp IS NOT NULL ORDER BY id LIMIT 1;

SELECT :conv_convencidos_antes AS antes,
       (SELECT count(*) FROM str.membership WHERE position_id = :t_rot_pos) AS despues,
       ((SELECT count(*) FROM str.membership WHERE position_id = :t_rot_pos)
          = :conv_convencidos_antes) AS ninguno_se_movio;

\echo ''
\echo '### 9. rama alterna: el alta cuelga del puesto MUNICIPAL, no crea uno de sección'
-- El nombramiento de la rama alterna es municipal (V2d). Un alta por la
-- vista tiene que reutilizarlo: si creara un puesto de sección, el mismo
-- coordinador acabaría con dos y las metas se contarían doble.
SELECT h.auth_id                AS t_alt_cz,
       p.id                     AS t_alt_pos,
       (SELECT hr.auth_id FROM str.position_holder hr
         WHERE hr.position_id = p.parent_position_id LIMIT 1) AS t_alt_ref,
       t.municipality_geo_id    AS t_alt_mun,
       (SELECT s.section_geo_id FROM str.territory s
         WHERE s.parent_territory_id = t.id AND s.type = 'SECTION'
         ORDER BY s.id LIMIT 1) AS t_alt_sec
FROM str.position p
JOIN str.territory t      ON t.id = p.territory_id AND t.type = 'MUNICIPALITY'
JOIN str.position_holder h ON h.position_id = p.id
WHERE p.level = 'ZONE_COORDINATOR' AND p.structure_id = 2 AND p.valid_to IS NULL
ORDER BY p.id LIMIT 1
\gset

SELECT count(*) AS cz_antes FROM str.position
 WHERE level = 'ZONE_COORDINATOR' AND structure_id = 2 AND valid_to IS NULL
   AND code LIKE '%|' || :'t_alt_cz'
\gset alt_

INSERT INTO promoters (names, last_name, sur_name, curp, ine, cell_phone_number,
                       street, municipality_id, seccion_id,
                       cordinator_zone_auth_id, referent_auth_id, promoter_auth_id,
                       followers_goal, flag_alter, notes)
VALUES ('T3_PROM_ALT', 'APELLIDO', 'SEGUNDO', 'XTST900303HTCXXX03', 'XTSTINE0003',
        '9990000002', 'CALLE PRUEBA', :t_alt_mun, :t_alt_sec,
        :'t_alt_cz', :'t_alt_ref', 'authid-t3-promotor-alt', 80, true, 'alta rama alterna')
RETURNING id AS t_new_alt
\gset

SELECT (SELECT parent_position_id FROM str.position WHERE legacy_id = :t_new_alt)
         = :t_alt_pos                                        AS cuelga_del_municipal,
       (SELECT count(*) FROM str.position
         WHERE level = 'ZONE_COORDINATOR' AND structure_id = 2 AND valid_to IS NULL
           AND code LIKE '%|' || :'t_alt_cz') = :alt_cz_antes AS no_creo_puesto_nuevo,
       (SELECT seccion_id FROM promoters WHERE id = :t_new_alt) = :t_alt_sec
                                                              AS seccion_ok,
       (SELECT cordinator_zone_auth_id FROM promoters WHERE id = :t_new_alt) = :'t_alt_cz'
                                                              AS cz_ok;

ROLLBACK;
\echo ''
\echo '=== pruebas terminadas, transacción revertida ==='
