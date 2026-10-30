/** Utilidades de CURP (sin moment). */

/** Fecha de nacimiento (YYYY-MM-DD) a partir de la CURP, o null si inválida. */
export function getBirthdateFromCurp(curp: string): string | null {
  if (!curp || curp.length !== 18) {
    return null;
  }
  const yy = parseInt(curp.substring(4, 6), 10);
  const mm = parseInt(curp.substring(6, 8), 10);
  const dd = parseInt(curp.substring(8, 10), 10);
  if (Number.isNaN(yy) || Number.isNaN(mm) || Number.isNaN(dd)) {
    return null;
  }
  const currentYY = new Date().getFullYear() % 100;
  const year = yy <= currentYY - 19 ? 2000 + yy : 1900 + yy;
  const pad = (n: number) => n.toString().padStart(2, '0');
  return `${year}-${pad(mm)}-${pad(dd)}`;
}

/** Sexo desde la CURP: 'HOMBRE' | 'MUJER' | null. */
export function getSexFromCurp(curp: string): 'HOMBRE' | 'MUJER' | null {
  if (!curp || curp.length !== 18) {
    return null;
  }
  // Posición 10 (0-index): H = hombre, M = mujer.
  return curp.charAt(10).toUpperCase() === 'H' ? 'HOMBRE' : 'MUJER';
}
