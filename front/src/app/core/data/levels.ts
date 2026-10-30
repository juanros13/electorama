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
 * Sólo cambia lo que se ve. Los códigos, la base y los roles de Keycloak siguen
 * igual: renombrarlos obligaría a migrar datos y romper reportes, y `PROMOTER`
 * cambiaría de significado a medio camino. Cualquier texto nuevo que nombre un
 * nivel sale de aquí, para que la pantalla nunca diga dos nombres para lo mismo.
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
