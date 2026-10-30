-- =====================================================================
-- Electorama — msvc-structure
-- V2e: normaliza el código de las secciones a 'S-%04d'
--
-- El paso 3 de V2 construye el código de la sección desde el catálogo sin
-- rellenar con ceros, así que 14 de las 1,199 secciones quedaron como
-- 'S-560' en vez de 'S-0560'. El resto sí trae los cuatro dígitos.
--
-- Importa por dos razones:
--   1. La vista de compatibilidad de `users_seccions` deriva
--      `seccion_number` del código, y el modelo viejo lo guarda con
--      `String.format("%04d", …)`. Sin rellenar, 14 secciones reportarían
--      un número distinto al que el front espera.
--   2. Los códigos de puesto llevan el del territorio dentro
--      ('CZ|S-560|<authId>'). Si el territorio se renombra y el puesto no,
--      los chequeos de idempotencia de V2c y V2d —que reconstruyen el
--      código para preguntar si el puesto ya existe— dejarían de encontrarlo
--      y crearían duplicados en la siguiente corrida.
--
-- Los códigos son etiquetas: todos los joins del modelo van por id, así que
-- renombrarlos no mueve ni una relación.
--
-- Es IDEMPOTENTE. Se ejecuta después de V2 (da igual antes o después de
-- V2b/V2c/V2d, pero cuanto antes menos códigos hay que corregir).
--
--   psql -U postgres -d msvc_dash_structure -v ON_ERROR_STOP=1 -f V2e__section_code_padding.sql
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

CREATE TEMP TABLE fix_code AS
SELECT t.id,
       t.code                                                        AS old_code,
       'S-' || lpad(regexp_replace(t.code, '^S-', ''), 4, '0')       AS new_code
FROM territory t
WHERE t.type = 'SECTION'
  AND t.code <> 'S-' || lpad(regexp_replace(t.code, '^S-', ''), 4, '0');

\echo '--- secciones por renombrar ---'
SELECT count(*) AS secciones FROM fix_code;

-- Si el código nuevo ya existiera vigente en otra fila, `territory_code_uq`
-- lo impediría. Se comprueba explícitamente para que el error se explique.
DO $$
DECLARE v_dup int;
BEGIN
  SELECT count(*) INTO v_dup
  FROM fix_code f
  JOIN territory t ON t.code = f.new_code AND t.valid_to IS NULL AND t.id <> f.id;
  IF v_dup > 0 THEN
    RAISE EXCEPTION 'hay % códigos que colisionarían al rellenar; revisar a mano', v_dup;
  END IF;
END $$;

-- los puestos que llevan el código viejo dentro del suyo
UPDATE position p
   SET code = replace(p.code, '|' || f.old_code || '|', '|' || f.new_code || '|')
  FROM fix_code f
 WHERE p.code LIKE '%|' || f.old_code || '|%';

UPDATE territory t SET code = f.new_code FROM fix_code f WHERE t.id = f.id;

COMMIT;

\echo '--- secciones con código sin rellenar, después (debería ser 0) ---'
SELECT count(*) AS sin_rellenar
FROM territory t
WHERE t.type = 'SECTION'
  AND t.code <> 'S-' || lpad(regexp_replace(t.code, '^S-', ''), 4, '0');
