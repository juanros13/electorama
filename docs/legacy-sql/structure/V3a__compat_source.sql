-- =====================================================================
-- Electorama — msvc-structure
-- V3a: fuente de las vistas de compatibilidad (fase 3, lado nuevo)
--
-- Publica el modelo nuevo con la FORMA del modelo viejo, en el esquema
-- `compat`. La base legacy monta una sola tabla foránea sobre cada una de
-- estas vistas, así que el join pesado se resuelve aquí y postgres_fdw
-- hace un único viaje en vez de uno por fila.
--
-- Sólo expone las columnas que el modelo nuevo POSEE. Las que existían
-- únicamente en `followers`/`promoters` (distritos, catálogos, uuid,
-- coordenadas…) se quedan en `*_legacy` y las pega la vista del otro lado.
--
-- Se ejecuta DESPUÉS de V2, y hay que volver a ejecutarlo si V2 se repite,
-- porque V2 trunca `position` y con ella `legacy_id`.
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V3a__compat_source.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. legacy_id en position
--    El backfill dejó la referencia al promotor viejo dentro de `code`
--    ('PROM|1234'). Para las vistas hace falta como columna: parsear la
--    cadena en cada consulta impide el índice, y el propio V2 advierte de
--    no reconstruir mapas a partir del código.
-- ---------------------------------------------------------------------
ALTER TABLE position ADD COLUMN IF NOT EXISTS legacy_id BIGINT;

UPDATE position
   SET legacy_id = split_part(code, '|', 2)::bigint
 WHERE code LIKE 'PROM|%'
   AND legacy_id IS DISTINCT FROM split_part(code, '|', 2)::bigint;

CREATE UNIQUE INDEX IF NOT EXISTS position_legacy_uq
    ON position (legacy_id) WHERE legacy_id IS NOT NULL;

-- `membership.legacy_id` ya existe; el índice único hace de guardia contra
-- que dos membresías reclamen el mismo id de convencido viejo.
CREATE UNIQUE INDEX IF NOT EXISTS membership_legacy_uq
    ON membership (legacy_id) WHERE legacy_id IS NOT NULL;

CREATE SCHEMA IF NOT EXISTS compat;

-- ---------------------------------------------------------------------
-- 2. titular vigente de cada puesto
--    v_current_holder ya hace esto, pero sin `auth_id`, que es justo la
--    llave con la que habla el modelo viejo.
-- ---------------------------------------------------------------------
-- Sin este índice, «titular vigente» es un recorrido secuencial de
-- assignment y el planificador estima 288 filas donde hay 57,601.
CREATE INDEX IF NOT EXISTS assignment_current_holder_idx
    ON assignment (position_id, person_id)
 WHERE role = 'HOLDER' AND upper(validity) IS NULL;

-- El authId de Keycloak vive en la ASIGNACION, no en la persona.
--
-- Es la diferencia entre «quien es este humano» y «con que cuenta ocupaba
-- este puesto». Una persona tiene varias cuentas de Keycloak —
-- `person.auth_id` es unico, asi que al deduplicar por CURP sobrevive una
-- sola— pero `assignment.auth_id` guarda la que traia cada nombramiento.
-- Contra el modelo viejo: assignment.auth_id acierta en 49,355 de 49,355
-- promotores; person.auth_id, en 47,944.
--
-- El COALESCE cubre las asignaciones creadas sin authId explicito.
CREATE OR REPLACE VIEW compat.position_holder AS
SELECT a.position_id,
       a.id                            AS assignment_id,
       a.person_id,
       COALESCE(a.auth_id, p.auth_id)  AS auth_id
FROM assignment a
JOIN person p ON p.id = a.person_id
WHERE a.role = 'HOLDER' AND upper(a.validity) IS NULL;

-- El authId del titular de un puesto, como función.
--
-- `compat.promoters` necesita tres titulares por fila: el del promotor, el
-- de su coordinador de zona y el del referente. Resueltos con JOIN contra
-- position_holder, el planificador encadena bucles anidados con un join
-- filter sobre un Materialize y la consulta pasa de 0.3 s a casi 2 min.
-- Como función es una búsqueda por índice por fila: predecible y acotada.
--
-- Los nombres van calificados: postgres_fdw se conecta con search_path en
-- sólo `pg_catalog`, así que un `assignment` a secas no existe para él. En
-- las vistas no hace falta porque guardan OIDs, pero el cuerpo de una
-- función se vuelve a analizar en cada ejecución.
CREATE OR REPLACE FUNCTION compat.holder_auth(p_position bigint)
RETURNS varchar AS $$
  SELECT COALESCE(a.auth_id, p.auth_id)
  FROM public.assignment a
  JOIN public.person p ON p.id = a.person_id
  WHERE a.position_id = p_position
    AND a.role = 'HOLDER'
    AND upper(a.validity) IS NULL
  LIMIT 1;
$$ LANGUAGE sql STABLE;

-- ---------------------------------------------------------------------
-- 3. compat.promoters
--    Un promotor viejo = un puesto PROMOTER con legacy_id. Los puestos
--    POOL|* y UNPLACED|* no salen: son invención de la migración para no
--    perder convencidos huérfanos, no promotores reales.
--
--    `deleted` sale siempre NULL porque la vista filtra puestos vigentes.
--    Es fiel al dato: las 49,355 filas de `promoters` tienen `deleted`
--    nulo — la baja del servicio viejo es un DELETE físico.
-- ---------------------------------------------------------------------
-- Se tira y se recrea en vez de REPLACE porque la columna `auth_id` salió
-- de aquí: en el modelo viejo es un duplicado a medio llenar de
-- `promoter_auth_id` (23,239 filas de 49,355, y 23,231 de ellas idénticas),
-- o sea que no es un dato que el modelo nuevo posea. Lo pega la vista del
-- otro lado desde `promoters_legacy`.
DROP VIEW IF EXISTS compat.promoters;
CREATE VIEW compat.promoters AS
SELECT pos.legacy_id                                  AS id,
       per.cell_phone_number::varchar(255)            AS cell_phone_number,
       pos.valid_from::timestamp                      AS created,
       per.curp::varchar(255)                         AS curp,
       pos.valid_to::timestamp                        AS deleted,
       per.email::varchar(255)                        AS email,
       per.ine::varchar(255)                          AS ine,
       per.internal_number::varchar(255)              AS interior_number,
       per.last_name::varchar(255)                    AS last_name,
       mun.municipality_geo_id                        AS municipality_id,
       per.names::varchar(255)                        AS names,
       per.external_number::varchar(255)              AS outdoor_number,
       per.zip_code::varchar(255)                     AS postal_code,
       per.street::varchar(255)                       AS street,
       per.sur_name::varchar(255)                     AS sur_name,
       sec.section_geo_id                             AS seccion_id,
       -- el coordinador de zona es el titular del puesto padre; el
       -- referente, el del abuelo
       compat.holder_auth(pos.parent_position_id)::varchar(100) AS cordinator_zone_auth_id,
       compat.holder_auth(cz.parent_position_id)::varchar(100)  AS referent_auth_id,
       per.holder_auth::varchar(255)                  AS promoter_auth_id,
       pos.goal                                       AS followers_goal,
       (st.type = 'ALTERNATE')                        AS flag_alter
FROM position pos
JOIN structure st  ON st.id  = pos.structure_id
JOIN territory sec ON sec.id = pos.territory_id
LEFT JOIN territory mun ON mun.id = sec.parent_territory_id
LEFT JOIN position cz   ON cz.id = pos.parent_position_id
-- el titular del propio puesto, con LATERAL para que sea una búsqueda por
-- índice por fila y no una reordenación de joins
LEFT JOIN LATERAL (
    SELECT p.*, COALESCE(a.auth_id, p.auth_id) AS holder_auth
    FROM assignment a
    JOIN person p ON p.id = a.person_id
    WHERE a.position_id = pos.id
      AND a.role = 'HOLDER'
      AND upper(a.validity) IS NULL
    LIMIT 1) per ON true
WHERE pos.level = 'PROMOTER'
  AND pos.legacy_id IS NOT NULL
  AND pos.valid_to IS NULL;

-- ---------------------------------------------------------------------
-- 4. compat.followers
--    Sólo membresías vigentes: cerrar la vigencia hace desaparecer la fila
--    de la vista, que es exactamente lo que el modelo viejo veía al borrar
--    físicamente — pero aquí el histórico se conserva.
--
--    `municipality_id` se deriva del municipio de la sección en vez de
--    arrastrar la copia denormalizada: se comprobó que coinciden en las
--    1,067,949 filas. Así los reportes por municipio filtran del lado
--    remoto en vez de traerse un millón de filas por la conexión.
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW compat.followers AS
SELECT m.legacy_id                                    AS id,
       per.birthdate::timestamp                       AS birthdate,
       per.cell_phone_number::varchar(255)            AS cell_phone_number,
       lower(m.validity)::timestamp                   AS created,
       per.curp::varchar(255)                         AS curp,
       upper(m.validity)::timestamp                   AS deleted,
       per.email::varchar(255)                        AS email,
       per.ine::varchar(255)                          AS ine,
       per.last_name::varchar(255)                    AS last_name,
       per.names::varchar(255)                        AS names,
       m.status::varchar(255)                         AS status,
       per.sur_name::varchar(255)                     AS sur_name,
       -- el authId de quien capturó: el de su nombramiento si lo tuvo, y si
       -- no el que quedó guardado crudo en la membresía
       COALESCE(ca.auth_id, cap.auth_id,
                m.captured_by_auth_id)::varchar(255)  AS user_create_id,
       per.external_number::varchar(255)              AS external_number,
       per.internal_number::varchar(255)              AS internal_number,
       per.neighborhood::varchar(255)                 AS neighborhood,
       per.street::varchar(255)                       AS street,
       per.zip_code::varchar(255)                     AS zip_code,
       -- en el modelo viejo el convencido de la rama alterna trae
       -- promoter_auth_id nulo y promoter_alt_auth_id lleno; aquí la rama
       -- es la estructura, así que el par se reconstruye desde ella
       (CASE WHEN st.type = 'MAIN'      THEN ph.auth_id END)::varchar(255) AS promoter_auth_id,
       (CASE WHEN st.type = 'ALTERNATE' THEN ph.auth_id END)::varchar(255) AS promoter_alt_auth_id,
       m.vote_flag                                    AS vote_flag,
       m.vote_flag_timestamp::timestamp               AS vote_flag_timestamp,
       mun.municipality_geo_id::bigint                AS municipality_id,
       sec.section_geo_id::bigint                     AS section_id
FROM membership m
JOIN structure st ON st.id = m.structure_id
LEFT JOIN person per    ON per.id = m.person_id
LEFT JOIN territory sec ON sec.id = m.territory_id
LEFT JOIN territory mun ON mun.id = sec.parent_territory_id
LEFT JOIN compat.position_holder ph ON ph.position_id = m.position_id
-- el linaje apunta a la ASIGNACIÓN que capturó; de ahí sale la persona
LEFT JOIN assignment ca ON ca.id = m.captured_by_assignment_id
LEFT JOIN person cap    ON cap.id = ca.person_id
WHERE m.legacy_id IS NOT NULL
  AND upper(m.validity) IS NULL;

-- ---------------------------------------------------------------------
-- 5. repartidores de id
--    Al insertar en una tabla foránea, postgres_fdw manda TODAS las
--    columnas, así que el `id` viaja explícitamente como NULL y el DEFAULT
--    de la secuencia remota nunca llega a aplicarse. La base legacy pide
--    aquí el id antes de insertar.
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW compat.seq_person     AS SELECT nextval('person_id_seq')     AS id;
CREATE OR REPLACE VIEW compat.seq_position   AS SELECT nextval('position_id_seq')   AS id;
CREATE OR REPLACE VIEW compat.seq_assignment AS SELECT nextval('assignment_id_seq') AS id;
CREATE OR REPLACE VIEW compat.seq_membership AS SELECT nextval('membership_id_seq') AS id;
CREATE OR REPLACE VIEW compat.seq_change_log AS SELECT nextval('change_log_id_seq') AS id;

-- ---------------------------------------------------------------------
-- 6. search_path de los triggers del modelo
--    postgres_fdw se conecta con search_path en sólo `pg_catalog`. Las
--    funciones de trigger de V1 nombran las tablas sin calificar, así que
--    un INSERT que llegue por FDW falla con «no existe la relación
--    position». Fijarles el search_path las hace independientes de quién
--    las invoque. (En V1 ya van declaradas así; esto arregla una base
--    creada antes.)
-- ---------------------------------------------------------------------
ALTER FUNCTION public.position_validate_hierarchy()          SET search_path = public, pg_catalog;
ALTER FUNCTION public.membership_protect_lineage()           SET search_path = public, pg_catalog;
ALTER FUNCTION public.territory_contained_in(bigint, bigint) SET search_path = public, pg_catalog;

ANALYZE position;
ANALYZE membership;

-- ---------------------------------------------------------------------
-- 5. contraste contra el modelo viejo (debe cuadrar con leg.*)
-- ---------------------------------------------------------------------
\echo '--- compat.promoters vs leg.promoters ---'
SELECT (SELECT count(*) FROM compat.promoters) AS compat,
       (SELECT count(*) FROM leg.promoters)    AS legacy;

\echo '--- compat.followers vs leg.followers menos los duplicados descartados ---'
SELECT (SELECT count(*) FROM compat.followers)    AS compat,
       (SELECT count(*) FROM leg.followers)       AS legacy,
       (SELECT count(*) FROM migration_duplicate) AS descartados;
