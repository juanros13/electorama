/**
 * Las reglas de la campaña ("estructura estatal.xlsx") para leer la
 * estructura contra la meta. Se usan en el catálogo y en el tablero.
 *
 *   promotor     (ZONE_COORDINATOR)  1 por cada 200 de meta
 *   movilizador  (PROMOTER)          1 por cada 20 de meta
 *
 * La meta de una sección es su LN × su % de meta (61 por default, G5).
 */
export const META_PER_PROMOTER = 200;
export const META_PER_MOBILIZER = 20;
export const DEFAULT_GOAL_PERCENTAGE = 61;

/** Promotores (coordinadores de zona) que pide la campaña para una meta. */
export const requiredPromoters = (meta: number | null): number =>
  meta ? Math.ceil(meta / META_PER_PROMOTER) : 0;

/** Movilizadores (promotores) que pide la campaña para una meta. */
export const requiredMobilizers = (meta: number | null): number =>
  meta ? Math.ceil(meta / META_PER_MOBILIZER) : 0;

/** `have / need` como fracción; 0 cuando no se pide nada. */
export const coverage = (have: number, need: number): number =>
  need ? have / need : 0;

/** Color de "tengo / necesito": completo, cerca (80 %+) o lejos. */
export function coverageClass(have: number, need: number): string {
  if (!need) {
    return 'text-neutral-400';
  }
  return have >= need
    ? 'text-emerald-600 dark:text-emerald-400'
    : have >= need * 0.8
      ? 'text-amber-600 dark:text-amber-400'
      : 'text-red-600 dark:text-red-400';
}

/** El mismo criterio para el fondo de una barra de avance. */
export function coverageBarClass(have: number, need: number): string {
  if (!need) {
    return 'bg-neutral-300 dark:bg-neutral-700';
  }
  return have >= need
    ? 'bg-emerald-500'
    : have >= need * 0.8
      ? 'bg-amber-500'
      : 'bg-red-500';
}
