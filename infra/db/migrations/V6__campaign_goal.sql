-- Electorama: % de meta por campaña.
--
-- La meta de cada sección es LN × % (G5) y el % vive en geography por sección.
-- La campaña guarda el suyo y, al aplicarlo (PATCH /api/geography/catalog/goal),
-- se copia a todas las secciones de su territorio; después se puede seguir
-- ajustando sección por sección.

ALTER TABLE structure.campaign
  ADD COLUMN goal_percentage numeric(5,2) NOT NULL DEFAULT 61
  CONSTRAINT campaign_goal_percentage_chk CHECK (goal_percentage > 0 AND goal_percentage <= 100);
