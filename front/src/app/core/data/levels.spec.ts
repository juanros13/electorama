import { describe, expect, it } from 'vitest';
import { functionName, levelByFunction, LevelLike, levelName, nextLevel, previousLevel } from './levels';

const FOUR: LevelLike[] = [
  { rank: 1, nameSingular: 'Coordinador estatal', namePlural: 'Coordinadores estatales', function: null },
  { rank: 2, nameSingular: 'Coordinador municipal', namePlural: 'Coordinadores municipales', function: 'REFERENT' },
  { rank: 3, nameSingular: 'Promotor', namePlural: 'Promotores', function: 'ZONE_COORDINATOR' },
  { rank: 4, nameSingular: 'Activista', namePlural: 'Activistas', function: 'PROMOTER' },
];

describe('niveles por campaña', () => {
  it('el siguiente y el anterior por rango, sin importar el orden de la lista', () => {
    const shuffled = [FOUR[2], FOUR[0], FOUR[3], FOUR[1]];
    expect(nextLevel(shuffled, 1)?.nameSingular).toBe('Coordinador municipal');
    expect(nextLevel(shuffled, 4)).toBeNull();
    expect(previousLevel(shuffled, 3)?.nameSingular).toBe('Coordinador municipal');
    expect(previousLevel(shuffled, 1)).toBeNull();
  });

  it('nombra una función como la llama la campaña', () => {
    expect(functionName(FOUR, 'PROMOTER')).toBe('Activista');
    expect(functionName(FOUR, 'REFERENT', true)).toBe('Coordinadores municipales');
    expect(levelByFunction(FOUR, 'ZONE_COORDINATOR')?.rank).toBe(3);
  });

  it('sin campaña cargada usa los nombres de respaldo', () => {
    expect(functionName([], 'PROMOTER')).toBe(levelName('PROMOTER'));
    expect(functionName([], 'ZONE_COORDINATOR', true)).toBe('Promotores');
    expect(functionName(FOUR, null)).toBe('');
  });
});
