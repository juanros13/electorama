/**
 * Qué ve cada rol.
 *
 * El catálogo de vistas vive aquí, no en la base: mientras se rehace el front
 * sobre el modelo de estructura, mover una vista de rol tiene que costar una
 * línea y un recargado, no un despliegue de `msvc-users`. Cuando la estructura
 * de vistas se asiente, esto se mueve al backend —que es quien debe mandar en
 * permisos— y este archivo se queda como el mapa por defecto.
 *
 * NO es una capa de seguridad. El front esconde lo que no toca; quien decide de
 * verdad es cada microservicio con el rol del token. Esconder un botón no
 * protege un endpoint.
 */

/** Las vistas que existen hoy. Se van agregando conforme se rehacen. */
export type ViewId =
  | 'structure.dashboard'
  | 'structure.organization'
  | 'structure.capture'
  | 'structure.supporters'
  | 'structure.territory'
  | 'structure.campaigns'
  | 'structure.supporters2024'
  | 'electoralHistory.dashboard'
  | 'electoralHistory.mySection'
  | 'access.matrix';

export interface AccessView {
  id: ViewId;
  /** Lo que se lee en el menú. */
  label: string;
  route: string;
  /** Nombre del icono en el set de Lucide registrado en `core/icons`. */
  icon: string;
  description?: string;
}

export const VIEWS: readonly AccessView[] = [
  {
    id: 'structure.dashboard',
    label: 'Tablero de campaña',
    route: '/admin/structure/dashboard',
    icon: 'layout-dashboard',
    description: 'Meta, estructura y avance por municipio',
  },
  {
    id: 'structure.organization',
    label: 'Organigrama',
    route: '/admin/structure/organization',
    icon: 'network',
    description: 'Puestos, titulares y vigencias',
  },
  {
    id: 'structure.capture',
    label: 'Capturar convencidos',
    route: '/admin/structure/capture',
    icon: 'user-plus',
    description: 'Registra simpatizantes en tu puesto o tu rama',
  },
  {
    id: 'structure.supporters',
    label: 'Mis convencidos',
    route: '/admin/structure/supporters',
    icon: 'users',
    description: 'Tu meta, tu avance y tus convencidos',
  },
  {
    id: 'structure.territory',
    label: 'Catálogo territorial',
    route: '/admin/structure/territory',
    icon: 'map',
    description: 'Secciones, municipios y distritos',
  },
  {
    id: 'structure.campaigns',
    label: 'Campañas',
    route: '/admin/structure/campaigns',
    icon: 'flag',
    description: 'Datos, cargos, estructuras y niveles',
  },
  {
    id: 'structure.supporters2024',
    label: 'Simpatizantes 2024',
    route: '/admin/structure/supporters-2024',
    icon: 'heart-handshake',
    description: 'Análisis de la campaña cerrada: convencidos contra votos',
  },
  {
    id: 'electoralHistory.dashboard',
    label: 'Histórico electoral',
    route: '/admin/electoral-history/dashboard',
    icon: 'vote',
    description: 'Cómo votó cada municipio en 2018, 2021 y 2024',
  },
  {
    id: 'electoralHistory.mySection',
    label: 'Histórico de mi sección',
    route: '/admin/electoral-history/my-section',
    icon: 'vote',
    description: 'Cómo votó tu sección en 2018, 2021 y 2024',
  },
  {
    id: 'access.matrix',
    label: 'Accesos',
    route: '/admin/access',
    icon: 'shield-check',
    description: 'Qué ve cada rol',
  },
] as const;

/**
 * Rol de Keycloak → vistas que ve.
 *
 * Los nombres van tal como están en el realm, con sus erratas congeladas
 * (`CORDINATOR_*` sin la primera O, `ISSUES_SUPEVISOR` sin la R): corregirlas
 * aquí rompería la autorización real.
 *
 * `'*'` es todo el catálogo. Los roles con lista vacía existen a propósito: son
 * los que todavía no tienen vista rehecha, y dejarlos escritos hace visible lo
 * que falta en vez de esconderlo detrás de un mapa incompleto.
 */
export const ROLE_VIEWS: Record<string, readonly ViewId[] | '*'> = {
  ADMIN: '*',

  // Quien ocupa un puesto (referente, promotor o movilizador): su rama del
  // organigrama, que msvc-structure acota por su nombramiento (nombra en
  // vacantes, deja vacante, cambia contraseñas debajo de él; no crea estructura
  // ni toca metas), y el histórico electoral de su sección (el tablero estatal es
  // sólo ADMIN). Tablero y simpatizantes, cuando
  // sepan filtrar a su municipio.
  CORDINATOR_MUNICIPAL: ['structure.organization', 'structure.supporters', 'structure.capture', 'electoralHistory.mySection'],
  CORDINATOR_MUNICIPAL_ALT: ['structure.organization', 'structure.supporters', 'structure.capture', 'electoralHistory.mySection'],
  CORDINATOR_ZONE: ['structure.organization', 'structure.supporters', 'structure.capture', 'electoralHistory.mySection'],
  CORDINATOR_ZONE_ALT: ['structure.organization', 'structure.supporters', 'structure.capture', 'electoralHistory.mySection'],
  PROMOTER: ['structure.organization', 'structure.supporters', 'structure.capture', 'electoralHistory.mySection'],

  // Seguimiento y operación: sus vistas están por rehacer.
  MUNICIPALITY_TRACKER: [],
  LOCAL_DISTRICT_TRACKER: [],
  FEDERAL_DISTRICT_TRACKER: [],
  SAFE_HOUSE_OPERATOR: [],
  ISSUES_SUPEVISOR: [],
  SOLUTION_TRACKING: [],
  CITIZEN_ISSUER: [],
  SURVEY_MUNICIPALITY: [],
  ADMIN_SURVEY: [],
};

/** Las vistas que le tocan a un conjunto de roles, sin repetir. */
export function viewsForRoles(roles: readonly string[]): AccessView[] {
  if (roles.some((role) => ROLE_VIEWS[role] === '*')) {
    return [...VIEWS];
  }
  const allowed = new Set<ViewId>();
  for (const role of roles) {
    const views = ROLE_VIEWS[role];
    if (Array.isArray(views)) {
      views.forEach((id) => allowed.add(id));
    }
  }
  return VIEWS.filter((view) => allowed.has(view.id));
}

/** Los roles del realm, en el orden en que están escritos en el mapa. */
export const ROLES: readonly string[] = Object.keys(ROLE_VIEWS);

/** Qué vistas abre un rol concreto. Vacío si no abre ninguna. */
export function viewsForRole(role: string): AccessView[] {
  return viewsForRoles([role]);
}
