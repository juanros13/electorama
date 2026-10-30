-- =====================================================================
-- Electorama — msvc-geography
-- G3: distritos federales al marco INE 2023 (vigente en 2026) y DL de Tabasco
--
-- `geo_distritos_federales` seguía en la distritación 2017 (303 filas); el
-- INE 2026 usa la de 2023 (300). El texto `geo_secciones.distrito_f` ya
-- estaba al día, pero `distrito_federal_id` —el que usan las consultas— se
-- quedó apuntando al distrito 2017: 12,404 secciones activas en otro DF.
--
-- El id de un DF es FK lógica en structure (`territory.federal_district_geo_id`),
-- followers, users (`users_federal_district`) y elections. Por eso el id se
-- conserva como identidad de (entidad, número) y no se recrea el catálogo:
--
--   · 7 DF que ya no existen (CDMX 23-24, Gro 9, Edomex 41, Mich 12,
--     Tamps 9, Ver 20) quedan con `status = false`. Nadie los referencia.
--   · 4 DF nuevos (Coah 8, NL 13-14, Pue 16) se insertan sin geometría.
--   · `distrito_federal_id` de cada sección activa se recalcula de `distrito_f`.
--
-- Distritos locales: sólo hay fuente INE para Tabasco. Se corrigen sus 9
-- secciones (las 8 nuevas de 2026 quedaron en DL 9 y la 1133 con id del DL 1).
-- En el resto del país `distrito_local_id` no concuerda con `distrito_l` en
-- ~20k secciones y no hay con qué decidir cuál es el bueno: no se toca.
--
-- `distrito_fed` / `distrito_loc` sólo estaban llenas en las 4,765 secciones
-- de junio, y `/seccion/findSectionsByFederalDistrict` filtra por ellas: en
-- Tabasco devolvía 12 secciones. Se llenan desde el texto en todas.
--
-- ⚠ La geometría de los DF sigue siendo la de 2017 en los estados redistritados.
--
-- Después de correrlo, resincronizar `territory` en msvc_dash_structure
-- (V4 §1 es idempotente; ver G4).
--
-- Es IDEMPOTENTE. Requiere G1.
--
--   psql -U postgres -d msvc_dash_geografia -v ON_ERROR_STOP=1 -f G3__ine_2026_districts.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

CREATE TABLE IF NOT EXISTS geo_distritos_federales_2026_09_prebackup AS
SELECT id, cve_ent, nom_ent, tipo, distrito_federal, ent_disf, entidad_id FROM geo_distritos_federales;

CREATE TABLE IF NOT EXISTS geo_secciones_dist_2026_09_prebackup AS
SELECT id, distrito_f, distrito_l, distrito_federal_id, distrito_local_id, distrito_fed, distrito_loc
  FROM geo_secciones;

-- ---------------------------------------------------------------------
-- 1. el catálogo de DF
-- ---------------------------------------------------------------------
ALTER TABLE geo_distritos_federales ADD COLUMN IF NOT EXISTS status boolean NOT NULL DEFAULT true;

UPDATE geo_distritos_federales d
   SET status = EXISTS (SELECT 1 FROM staging.stg_ine_secc i
                         WHERE i.ent = d.entidad_id AND i.df = d.distrito_federal);

-- el id no tiene secuencia
INSERT INTO geo_distritos_federales (id, cve_ent, nom_ent, distrito_federal, ent_disf, entidad_id, status)
SELECT (SELECT max(id) FROM geo_distritos_federales) + row_number() OVER (ORDER BY n.ent, n.df),
       lpad(n.ent::text, 2, '0'), e.nom_ent, n.df, n.ent || '_' || n.df, n.ent, true
  FROM (SELECT DISTINCT ent, df FROM staging.stg_ine_secc) n
  JOIN geo_entidades e ON e.id = n.ent
 WHERE NOT EXISTS (SELECT 1 FROM geo_distritos_federales d
                    WHERE d.entidad_id = n.ent AND d.distrito_federal = n.df);

-- ---------------------------------------------------------------------
-- 2. el DL de Tabasco, de la fuente INE
-- ---------------------------------------------------------------------
UPDATE geo_secciones s
   SET distrito_l        = lpad(i.dl::text, 2, '0'),
       distrito_local_id = dl.id
  FROM staging.stg_ine_secc_dl_27 i
  JOIN geo_distritos_locales dl ON dl.entidad_id = i.ent AND dl.distrito_local = i.dl
 WHERE s.status
   AND s.entidad_id = i.ent
   AND s.seccion::int = i.seccion
   AND (s.distrito_l IS DISTINCT FROM lpad(i.dl::text, 2, '0') OR s.distrito_local_id IS DISTINCT FROM dl.id);

-- ---------------------------------------------------------------------
-- 3. las columnas derivadas de cada sección activa
-- ---------------------------------------------------------------------
UPDATE geo_secciones s
   SET distrito_federal_id = d.id::text
  FROM geo_distritos_federales d
 WHERE s.status
   AND d.entidad_id = s.entidad_id
   AND d.distrito_federal = s.distrito_f::int
   AND s.distrito_federal_id IS DISTINCT FROM d.id::text;

UPDATE geo_secciones
   SET distrito_fed = distrito_f::bigint
 WHERE status AND distrito_f IS NOT NULL AND distrito_fed IS DISTINCT FROM distrito_f::bigint;

UPDATE geo_secciones
   SET distrito_loc = distrito_l::bigint
 WHERE status AND distrito_l IS NOT NULL AND distrito_loc IS DISTINCT FROM distrito_l::bigint;

-- verificación
SELECT (SELECT count(*) FROM geo_distritos_federales WHERE status) AS df_vigentes,
       (SELECT count(*) FROM geo_secciones s
          JOIN staging.stg_ine_secc i ON i.ent = s.entidad_id AND i.seccion = s.seccion::int
          JOIN geo_distritos_federales d ON d.id::text = s.distrito_federal_id
         WHERE s.status AND (d.distrito_federal <> i.df OR NOT d.status)) AS secciones_df_mal,
       (SELECT count(*) FROM geo_secciones s
          JOIN staging.stg_ine_secc_dl_27 i ON i.ent = s.entidad_id AND i.seccion = s.seccion::int
          JOIN geo_distritos_locales dl ON dl.id = s.distrito_local_id
         WHERE s.status AND dl.distrito_local <> i.dl) AS tabasco_dl_mal,
       (SELECT count(*) FROM geo_secciones WHERE status AND entidad_id = 27 AND distrito_fed IS NULL) AS tabasco_sin_distrito_fed;

COMMIT;
