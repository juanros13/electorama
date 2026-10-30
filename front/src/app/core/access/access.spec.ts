import { describe, expect, it } from 'vitest';
import {
  ROLE_VIEWS,
  ROLES,
  VIEWS,
  viewsForRole,
  viewsForRoles,
} from './access';

describe('viewsForRoles', () => {
  it('le da a ADMIN todo el catálogo', () => {
    expect(viewsForRoles(['ADMIN'])).toEqual([...VIEWS]);
  });

  it('no le da nada a un rol sin vistas asignadas', () => {
    expect(viewsForRoles(['CORDINATOR_ZONE'])).toEqual([]);
  });

  it('no le da nada a un rol desconocido', () => {
    // Un rol que no está en el mapa no hereda permisos por descuido.
    expect(viewsForRoles(['ROL_QUE_NO_EXISTE'])).toEqual([]);
  });

  it('no le da nada a una sesión sin roles', () => {
    expect(viewsForRoles([])).toEqual([]);
  });

  it('suma las vistas cuando alguien trae varios roles', () => {
    // ADMIN manda: si uno de los roles abre todo, el resto no resta.
    expect(viewsForRoles(['CORDINATOR_ZONE', 'ADMIN'])).toEqual([...VIEWS]);
  });

  it('no repite una vista que abran dos roles a la vez', () => {
    const ids = viewsForRoles(['ADMIN', 'ADMIN']).map((v) => v.id);
    expect(new Set(ids).size).toBe(ids.length);
  });

  it('conserva las erratas de los roles del realm', () => {
    // CORDINATOR_* va sin la primera O y ISSUES_SUPEVISOR sin la R, tal como
    // están en Keycloak. "Corregirlas" aquí dejaría a esa gente sin menú.
    expect(ROLE_VIEWS).toHaveProperty('CORDINATOR_ZONE');
    expect(ROLE_VIEWS).toHaveProperty('CORDINATOR_MUNICIPAL');
    expect(ROLE_VIEWS).toHaveProperty('ISSUES_SUPEVISOR');
    expect(ROLE_VIEWS).not.toHaveProperty('COORDINATOR_ZONE');
  });

  it('cada vista del catálogo apunta a una ruta del admin', () => {
    for (const view of VIEWS) {
      expect(view.route.startsWith('/admin/')).toBe(true);
    }
  });

  it('ROLES trae todos los roles del mapa', () => {
    expect(ROLES.length).toBe(Object.keys(ROLE_VIEWS).length);
    expect(ROLES).toContain('ADMIN');
  });

  it('viewsForRole es la vista de un solo rol', () => {
    expect(viewsForRole('ADMIN')).toEqual([...VIEWS]);
    expect(viewsForRole('CORDINATOR_ZONE')).toEqual([]);
  });

  it('cada vista tiene id, etiqueta e icono', () => {
    for (const view of VIEWS) {
      expect(view.id).toBeTruthy();
      expect(view.label).toBeTruthy();
      expect(view.icon).toBeTruthy();
    }
  });
});
