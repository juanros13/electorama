-- =====================================================================
-- Electorama — msvc-geography
-- G2: municipios al marco INE 2026
--
-- Contra el SIGE 2026 el catálogo tenía 2,469 municipios y el INE 2,477:
--
--   · 5 cambiaron de nombre con la misma clave (Solidaridad → Playa del
--     Carmen, Jonacatepec → Jonacatepec de Leandro Valle, San Blas Atempa,
--     San Martín de los Cansecos y Villa Hidalgo con su nombre heroico o
--     completo). Se renombran: el id no cambia.
--   · 8 son nuevos: San Felipe (BC), Ñuu Savi, Las Vigas, San Nicolás y
--     Santa Cruz del Rincón (Gro), Hueyapan (Mor), Eldorado y Juan José
--     Ríos (Sin). Se insertan sin geometría —no hay shapefile nacional—.
--   · 156 secciones activas pasan a esos municipios nuevos (más 2 de CDMX
--     que el catálogo tenía en la alcaldía equivocada). Ninguna tiene meta.
--
-- La clave de municipio del INE coincide con la de INEGI (`cve_mun`) en
-- todo el país; los dos "San Juan/San Pedro Mixtepec" de Oaxaca parecen
-- cruzados sólo porque el nombre se repite. Por eso el cruce es por
-- (entidad, clave) y el nombre sólo sirve para detectar los renombrados.
--
-- No toca `meta` ni ids existentes. Respalda antes en
-- `geo_municipios_2026_09_prebackup` y `geo_secciones_mun_2026_09_prebackup`.
--
-- Es IDEMPOTENTE. Requiere G1.
--
--   psql -U postgres -d msvc_dash_geografia -v ON_ERROR_STOP=1 -f G2__ine_2026_municipalities.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

CREATE TABLE IF NOT EXISTS geo_municipios_2026_09_prebackup AS
SELECT id, cve_ent, cve_munc, cve_mun, nom_ent, nom_mun, entidad_id FROM geo_municipios;

CREATE TABLE IF NOT EXISTS geo_secciones_mun_2026_09_prebackup AS
SELECT id, municipio_id, cve_munc, cve_mun, nom_mun, mun_secc FROM geo_secciones;

-- 1. renombrados: la clave existe en los dos lados pero el nombre no coincide con ninguno
UPDATE geo_municipios m
   SET nom_mun = i.nom
  FROM staging.stg_ine_mun i
 WHERE i.ent = m.entidad_id
   AND i.mun = m.cve_mun::int
   AND staging.norm(i.nom) <> staging.norm(m.nom_mun)
   AND NOT EXISTS (SELECT 1 FROM staging.stg_ine_mun o
                    WHERE o.ent = m.entidad_id AND staging.norm(o.nom) = staging.norm(m.nom_mun));

-- 2. nuevos: la secuencia estaba en 1 con max(id)=2469
SELECT setval('geo_municipios_id_seq', (SELECT max(id) FROM geo_municipios));

INSERT INTO geo_municipios (cve_ent, cve_munc, cve_mun, nom_ent, nom_mun, entidad_id)
SELECT lpad(i.ent::text, 2, '0'),
       lpad(i.ent::text, 2, '0') || lpad(i.mun::text, 3, '0'),
       lpad(i.mun::text, 3, '0'),
       e.nom_ent,
       i.nom,
       i.ent
  FROM staging.stg_ine_mun i
  JOIN geo_entidades e ON e.id = i.ent
 WHERE NOT EXISTS (SELECT 1 FROM geo_municipios m
                    WHERE m.entidad_id = i.ent AND m.cve_mun::int = i.mun)
 ORDER BY i.ent, i.mun;

-- 3. secciones activas cuyo municipio INE no es el del catálogo
UPDATE geo_secciones s
   SET municipio_id = m.id,
       cve_munc     = m.cve_munc,
       cve_mun      = m.cve_mun,
       nom_mun      = m.nom_mun,
       mun_secc     = CASE WHEN s.mun_secc IS NULL THEN NULL ELSE m.cve_munc || '_' || s.seccion END
  FROM staging.stg_ine_secc i
  JOIN geo_municipios m ON m.entidad_id = i.ent AND m.cve_mun::int = i.mun
 WHERE s.status
   AND i.ent = s.entidad_id
   AND i.seccion = s.seccion::int
   AND s.municipio_id IS DISTINCT FROM m.id;

-- 4. el nombre desnormalizado de las secciones de los renombrados
UPDATE geo_secciones s
   SET nom_mun = m.nom_mun
  FROM geo_municipios m
 WHERE m.id = s.municipio_id
   AND s.nom_mun IS DISTINCT FROM m.nom_mun
   AND s.municipio_id IN (SELECT b.id FROM geo_municipios_2026_09_prebackup b
                            JOIN geo_municipios a ON a.id = b.id
                           WHERE a.nom_mun <> b.nom_mun);

-- verificación: 0 municipios INE sin su fila y 0 secciones activas en otro municipio
SELECT (SELECT count(*) FROM geo_municipios) AS municipios,
       (SELECT count(*) FROM staging.stg_ine_mun i
         WHERE NOT EXISTS (SELECT 1 FROM geo_municipios m
                            WHERE m.entidad_id = i.ent AND m.cve_mun::int = i.mun)) AS ine_sin_fila,
       (SELECT count(*) FROM geo_secciones s
          JOIN staging.stg_ine_secc i ON i.ent = s.entidad_id AND i.seccion = s.seccion::int
          JOIN geo_municipios m ON m.id = s.municipio_id
         WHERE s.status AND m.cve_mun::int <> i.mun) AS secciones_en_otro_municipio;

COMMIT;
