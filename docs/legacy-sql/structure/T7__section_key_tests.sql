-- =====================================================================
-- T7 · Pruebas de V7 (clave por sección). Todo dentro de una transacción
-- que se deshace al final: no deja datos.
-- =====================================================================

\set ON_ERROR_STOP on

BEGIN;

CREATE TEMP TABLE t7 (caso text, esperado text, obtenido text) ON COMMIT DROP;

DO $$
DECLARE
  v_main      bigint := (SELECT s.id FROM structure s JOIN campaign c ON c.id = s.campaign_id
                          WHERE c.status = 'ACTIVE' AND s.type = 'MAIN');
  v_alt       bigint := (SELECT s.id FROM structure s JOIN campaign c ON c.id = s.campaign_id
                          WHERE c.status = 'ACTIVE' AND s.type = 'ALTERNATE');
  v_sec       bigint := (SELECT id FROM territory WHERE code = 'S-0233' AND type = 'SECTION'
                          AND valid_to IS NULL);
  v_sec_alt   bigint;
  v_code_alt  text;
  v_ref_main  bigint;
  v_ref_alt   bigint;
  v_prev      int;
  v_p1        bigint;
  v_p2        bigint;
  v_m         bigint;
  v_key       text;
BEGIN
  SELECT id INTO v_ref_main FROM position
   WHERE structure_id = v_main AND level = 'REFERENT' AND valid_to IS NULL ORDER BY id LIMIT 1;
  SELECT id INTO v_ref_alt FROM position
   WHERE structure_id = v_alt AND level = 'REFERENT' AND valid_to IS NULL ORDER BY id LIMIT 1;
  SELECT coalesce(max(substring(section_key FROM 'A([0-9]+)$')::int), 0) INTO v_prev
    FROM position WHERE structure_id = v_main AND section_key LIKE 'S-0233A%'
     AND level = 'ZONE_COORDINATOR';

  -- 1. promotor nuevo en la principal: el siguiente consecutivo
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, goal)
  VALUES (v_main, 'ZONE_COORDINATOR', v_ref_main, v_sec, 200) RETURNING id INTO v_p1;
  INSERT INTO t7 SELECT 'promotor principal', 'S-0233A' || (v_prev + 1), section_key
    FROM position WHERE id = v_p1;

  -- 2. se cierra y se crea otro: no reutiliza el número
  UPDATE position SET valid_to = now() WHERE id = v_p1;
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, goal)
  VALUES (v_main, 'ZONE_COORDINATOR', v_ref_main, v_sec, 200) RETURNING id INTO v_p2;
  INSERT INTO t7 SELECT 'no reutiliza el cerrado', 'S-0233A' || (v_prev + 2), section_key
    FROM position WHERE id = v_p2;

  -- 3. la alterna lleva B y su propio consecutivo (en una sección del
  --    municipio de su referente: la jerarquía de territorios se valida)
  SELECT t.id, t.code INTO v_sec_alt, v_code_alt
    FROM territory t
   WHERE t.type = 'SECTION' AND t.valid_to IS NULL
     AND t.parent_territory_id = (SELECT territory_id FROM position WHERE id = v_ref_alt)
     AND NOT EXISTS (SELECT 1 FROM position p WHERE p.structure_id = v_alt AND p.territory_id = t.id)
   ORDER BY t.code LIMIT 1;
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, goal)
  VALUES (v_alt, 'ZONE_COORDINATOR', v_ref_alt, v_sec_alt, 200) RETURNING id INTO v_p1;
  INSERT INTO t7 SELECT 'promotor alterna', v_code_alt || 'B1', section_key FROM position WHERE id = v_p1;

  -- 4. movilizadores: heredan la clave del promotor
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, goal)
  VALUES (v_main, 'PROMOTER', v_p2, v_sec, 20) RETURNING id INTO v_m;
  INSERT INTO t7 SELECT 'movilizador 1', 'S-0233A' || (v_prev + 2) || '-01', section_key
    FROM position WHERE id = v_m;
  INSERT INTO position (structure_id, level, parent_position_id, territory_id, goal)
  VALUES (v_main, 'PROMOTER', v_p2, v_sec, 20) RETURNING id INTO v_m;
  INSERT INTO t7 SELECT 'movilizador 2', 'S-0233A' || (v_prev + 2) || '-02', section_key
    FROM position WHERE id = v_m;

  -- 5. el referente no lleva clave
  INSERT INTO t7 SELECT 'referente sin clave', NULL, section_key FROM position WHERE id = v_ref_main;

  -- 6. la clave no se cambia
  BEGIN
    UPDATE position SET section_key = 'S-9999A1' WHERE id = v_p2;
    v_key := 'se dejó cambiar';
  EXCEPTION WHEN check_violation THEN
    v_key := 'rechazado';
  END;
  INSERT INTO t7 VALUES ('clave inmutable', 'rechazado', v_key);
END $$;

SELECT caso, esperado, obtenido,
       CASE WHEN esperado IS NOT DISTINCT FROM obtenido THEN 'OK' ELSE 'FALLA' END AS resultado
  FROM t7;

ROLLBACK;
