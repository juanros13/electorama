-- Electorama: territorios de cualquier estado en structure.territory.
--
-- El código de una sección es su número ("S-0815") y se repite entre estados,
-- así que la unicidad pasa a ser por estado. Para eso municipios y secciones
-- también llevan `state_geo_id` (hasta ahora sólo el STATE lo tenía).
-- Los territorios de los demás estados se cargan con
-- infra/db/reference/territory-nacional.sql.

-- se hereda del padre, nivel por nivel (estado > municipio > sección > fragmento)
DO $$
BEGIN
  LOOP
    UPDATE structure.territory t
       SET state_geo_id = p.state_geo_id
      FROM structure.territory p
     WHERE p.id = t.parent_territory_id
       AND t.state_geo_id IS NULL
       AND p.state_geo_id IS NOT NULL;
    EXIT WHEN NOT FOUND;
  END LOOP;
END $$;

ALTER TABLE structure.territory
  ADD CONSTRAINT territory_state_chk CHECK (state_geo_id IS NOT NULL) NOT VALID;
ALTER TABLE structure.territory VALIDATE CONSTRAINT territory_state_chk;

DROP INDEX structure.territory_code_uq;
CREATE UNIQUE INDEX territory_code_uq ON structure.territory (state_geo_id, code) WHERE valid_to IS NULL;
