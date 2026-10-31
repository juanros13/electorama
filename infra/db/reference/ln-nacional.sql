-- Lista nominal oficial del INE y techo de meta en los 32 estados.
-- Es G5 (docs/legacy-sql/geography) para todo el país:
--
--   nominal_list       LN de la sección (DERFE, "LISTA NOMINAL NACIONAL — TOTAL")
--   nominal_list_date  corte de donde salió
--   meta               round(nominal_list × goal_percentage / 100); el % de cada
--                      sección no se toca
--
-- Sólo secciones vigentes; el número de sección es único dentro del estado.
-- Es IDEMPOTENTE. El archivo sale de ln-nacional.py:
--
--   python infra/db/reference/ln-nacional.py <xlsx> ln_nacional.txt
--   psql -d electorama -v ON_ERROR_STOP=1 -v cut=2025-10-31 \
--        -f infra/db/reference/ln-nacional.sql < ln_nacional.txt

DROP TABLE IF EXISTS geography_staging.stg_ln_nacional;
CREATE TABLE geography_staging.stg_ln_nacional (entidad int, df int, mun int, seccion int, ln int);
\copy geography_staging.stg_ln_nacional from pstdin with (format csv, delimiter '|')

BEGIN;

UPDATE geography.geo_secciones s
   SET nominal_list      = l.ln,
       nominal_list_date = :'cut'
  FROM geography_staging.stg_ln_nacional l
 WHERE s.entidad_id = l.entidad
   AND s.status
   AND s.seccion::int = l.seccion
   AND (s.nominal_list IS DISTINCT FROM l.ln OR s.nominal_list_date IS DISTINCT FROM :'cut'::date);

UPDATE geography.geo_secciones
   SET meta = round(nominal_list * goal_percentage / 100)
 WHERE nominal_list IS NOT NULL
   AND meta IS DISTINCT FROM round(nominal_list * goal_percentage / 100);

COMMIT;

-- verificación por estado
SELECT l.entidad,
       count(*)                                    AS filas_fuente,
       count(s.id)                                 AS con_seccion_vigente,
       count(*) - count(s.id)                      AS fuente_sin_seccion,
       sum(l.ln)                                   AS ln_fuente,
       sum(s.meta)                                 AS meta_total
  FROM geography_staging.stg_ln_nacional l
  LEFT JOIN geography.geo_secciones s
         ON s.entidad_id = l.entidad AND s.status AND s.seccion::int = l.seccion
 GROUP BY l.entidad
 ORDER BY l.entidad;
