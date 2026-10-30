-- =====================================================================
-- Electorama — msvc-geography
-- G5: lista nominal oficial del INE y techo de meta como % de la LN
--
-- El techo de cada sección (`geo_secciones.meta`) venía de una regla vieja,
-- ~33 % de la LN, mientras las metas de los coordinadores de zona ya estaban
-- en ~62 %. La campaña fija la META 2027 como LN × 61 %
-- ("estructura estatal.xlsx"), así que el techo pasa a calcularse:
--
--   nominal_list       LN de la sección, del INE (fuente oficial)
--   nominal_list_date  fecha del corte del INE de donde salió
--   goal_percentage    % de la LN que es meta; 61 por default, editable
--                      por sección desde el catálogo territorial
--   meta               round(nominal_list × goal_percentage / 100)
--
-- La fuente es la estadística de Padrón y Lista Nominal por sección que la
-- DERFE publica en datos abiertos (ine.mx/transparencia/datos-abiertos,
-- "Estadística de Padrón Electoral y Lista Nominal del Electorado"):
--
--   DatosAbiertos-derfe-pdln_edms_sexo_20251031_Nal_Ext.xlsx   corte 31-oct-2025
--   ine_ln_27_20251031.txt   seccion|distrito_f|municipio_ine|ln   1,191 filas
--                            Tabasco, "LISTA NOMINAL NACIONAL — TOTAL"
--
-- El corte ya viene en el marco 2026: trae las 1,191 secciones vigentes,
-- incluidas la 1133 y las 1204-1215. El Excel de la campaña usaba otro corte
-- en el marco 2024 (-0.4 % en total, 0 % de mediana por sección); se deja
-- el del INE como oficial.
--
-- Para un corte nuevo: extraer Tabasco igual, cambiar el nombre del archivo
-- y :cut, y volver a correr. El % de cada sección no se toca.
--
-- Requiere G1 (esquema `staging`). Es IDEMPOTENTE: recalcula desde la fuente
-- cada vez, y el respaldo del techo anterior sólo se toma la primera vez.
-- Se ejecuta desde la carpeta de los archivos:
--
--   cd C:\Users\juanr\geo-2026
--   psql -U postgres -d msvc_dash_geografia -v ON_ERROR_STOP=1 -f G5__nominal_list_2027.sql
-- =====================================================================

\set ON_ERROR_STOP on
\set cut '2025-10-31'

CREATE SCHEMA IF NOT EXISTS staging;

DROP TABLE IF EXISTS staging.stg_ln_2027_27, staging.stg_ine_ln_27;
CREATE TABLE staging.stg_ine_ln_27 (seccion int, df int, mun_ine int, ln int);
\copy staging.stg_ine_ln_27 from 'ine_ln_27_20251031.txt' with (format csv, delimiter '|')

BEGIN;

CREATE TABLE IF NOT EXISTS geo_secciones_meta_2026_09_prebackup AS
SELECT id, meta FROM geo_secciones WHERE entidad_id = 27;

ALTER TABLE geo_secciones ADD COLUMN IF NOT EXISTS nominal_list integer;
ALTER TABLE geo_secciones ADD COLUMN IF NOT EXISTS nominal_list_date date;
ALTER TABLE geo_secciones ADD COLUMN IF NOT EXISTS goal_percentage numeric(5,2) NOT NULL DEFAULT 61;

-- la LN del INE, en secciones vigentes; el número de sección es único en el estado
UPDATE geo_secciones s
   SET nominal_list      = l.ln,
       nominal_list_date = :'cut'
  FROM staging.stg_ine_ln_27 l
 WHERE s.entidad_id = 27
   AND s.status
   AND s.seccion::int = l.seccion
   AND (s.nominal_list IS DISTINCT FROM l.ln OR s.nominal_list_date IS DISTINCT FROM :'cut'::date);

UPDATE geo_secciones
   SET meta = round(nominal_list * goal_percentage / 100)
 WHERE entidad_id = 27
   AND nominal_list IS NOT NULL
   AND meta IS DISTINCT FROM round(nominal_list * goal_percentage / 100);

-- verificación
SELECT (SELECT count(*) FROM staging.stg_ine_ln_27) AS filas_fuente,
       (SELECT count(*) FROM geo_secciones WHERE entidad_id = 27 AND status AND nominal_list IS NOT NULL) AS vigentes_con_ln,
       (SELECT count(*) FROM geo_secciones WHERE entidad_id = 27 AND status AND nominal_list IS NULL) AS vigentes_sin_ln,
       (SELECT count(*) FROM staging.stg_ine_ln_27 l
         WHERE NOT EXISTS (SELECT 1 FROM geo_secciones s
                            WHERE s.entidad_id = 27 AND s.status AND s.seccion::int = l.seccion)) AS fuente_sin_seccion_vigente,
       (SELECT sum(nominal_list) FROM geo_secciones WHERE entidad_id = 27 AND status) AS ln_total,
       (SELECT sum(meta) FROM geo_secciones WHERE entidad_id = 27 AND status) AS meta_total;

COMMIT;
