/**
 * Cómo se llama cada nivel de la estructura en pantalla.
 *
 * Los nombres siguen al Excel de la campaña ("estructura estatal.xlsx"), que no
 * coincide con los códigos del sistema:
 *
 *   código interno     rol de Keycloak    se muestra como
 *   REFERENT           CORDINATOR_MUNICIPAL  Coordinador
 *   ZONE_COORDINATOR   CORDINATOR_ZONE       Promotor     (1 por cada 200 de meta)
 *   PROMOTER           PROMOTER              Movilizador  (1 por cada 20 de meta)
 *
 * Desde V3 cada campaña configura sus niveles (cuántos, cómo se llaman, su
 * meta y su rol) en msvc-structure. Estos códigos quedan como FUNCIONES: un
 * nivel configurado puede llevar una para conservar la lógica que depende de
 * ella. Los nombres de abajo son el respaldo cuando no hay campaña cargada; el
 * nombre real de un nivel sale de la campaña (`functionName`, `LevelCatalog`)
 * o del propio puesto (`PositionSummary.levelName`).
 */
export type StructureLevel = 'REFERENT' | 'ZONE_COORDINATOR' | 'PROMOTER';

export interface LevelName {
  one: string;
  many: string;
}

export const LEVEL_NAMES: Record<StructureLevel, LevelName> = {
  REFERENT: { one: 'Coordinador', many: 'Coordinadores' },
  ZONE_COORDINATOR: { one: 'Promotor', many: 'Promotores' },
  PROMOTER: { one: 'Movilizador', many: 'Movilizadores' },
};

export const levelName = (level: StructureLevel, plural = false): string =>
  plural ? LEVEL_NAMES[level].many : LEVEL_NAMES[level].one;

/** Lo que se necesita de un nivel configurado para nombrarlo y ordenarlo. */
export interface LevelLike {
  rank: number;
  nameSingular: string;
  namePlural: string;
  function: StructureLevel | null;
}

/** El nivel de la campaña que lleva esa función, si lo hay. */
export const levelByFunction = <T extends LevelLike>(
  levels: readonly T[],
  fn: StructureLevel | null | undefined
): T | null => (fn ? (levels.find((l) => l.function === fn) ?? null) : null);

/** El nombre de una función según la campaña; si no la configura, el de respaldo. */
export const functionName = (
  levels: readonly LevelLike[],
  fn: StructureLevel | null | undefined,
  plural = false
): string => {
  if (!fn) {
    return '';
  }
  const level = levelByFunction(levels, fn);
  return level ? (plural ? level.namePlural : level.nameSingular) : levelName(fn, plural);
};

/** El nivel inmediato inferior a `rank`; null en el último. */
export const nextLevel = <T extends { rank: number }>(levels: readonly T[], rank: number): T | null =>
  levels.filter((l) => l.rank > rank).sort((a, b) => a.rank - b.rank)[0] ?? null;

/** El nivel inmediato superior a `rank`; null en la cúspide. */
export const previousLevel = <T extends { rank: number }>(levels: readonly T[], rank: number): T | null =>
  levels.filter((l) => l.rank < rank).sort((a, b) => b.rank - a.rank)[0] ?? null;
