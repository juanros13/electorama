/**
 * El modelo de estructura con puestos y vigencias (msvc-structure).
 *
 * Es otro modelo, no una versión nueva del de `structure.types.ts`: aquí la
 * persona no *es* coordinadora, sino que **ocupa** un puesto durante una
 * vigencia. Por eso el puesto y su titular son cosas separadas, y por eso el
 * historial existe.
 */

/** Los tres niveles del organigrama. */
export type PositionLevel = 'REFERENT' | 'ZONE_COORDINATOR' | 'PROMOTER';

/** Territorio del puesto. El organigrama y el territorio son árboles distintos. */
export type TerritoryType = 'STATE' | 'MUNICIPALITY' | 'SECTION' | 'FRAGMENT';

/** Un puesto con su titular vigente y sus dos conteos. */
export interface PositionSummary {
  id: number;
  level: PositionLevel;
  code: string | null;
  /** Clave legible por sección (V7): S-0233A1 el promotor, S-0233A1-01 el movilizador. */
  sectionKey: string | null;
  goal: number | null;
  childLimit: number | null;
  parentId: number | null;
  structureId: number;

  territoryId: number;
  territoryType: TerritoryType;
  territoryCode: string | null;
  sectionGeoId: number | null;
  municipalityGeoId: number | null;

  /** Cuenta con la que se ocupa el puesto; vive en el nombramiento, no en la persona. */
  holderAuthId: string | null;
  holderPersonId: number | null;
  holderName: string | null;

  childCount: number;
  membershipCount: number;
}

/** Un nombramiento del historial: quién ocupó el puesto y entre qué fechas. */
export interface AssignmentHistory {
  id: number;
  personId: number;
  authId: string | null;
  personName: string | null;
  role: 'HOLDER' | 'SUBSTITUTE' | 'ACTING';
  validFrom: string;
  /** Nulo mientras el nombramiento sigue vigente. */
  validTo: string | null;
  startReason: string | null;
  endReason: string | null;
}

/** Avance del subárbol de un puesto contra su meta. */
export interface PositionProgress {
  positionId: number;
  goal: number;
  memberships: number;
}

/** Una sección que cubre un coordinador, y desde qué puesto la cubre. */
export interface TerritoryScope {
  territoryId: number;
  territoryCode: string;
  sectionGeoId: number | null;
  municipalityGeoId: number | null;
  positionId: number;
  goal: number | null;
  childLimit: number | null;
  /** `true` cuando la sección la cubre un nombramiento MUNICIPAL, no uno de sección. */
  fromMunicipality: boolean;
}

/**
 * La estructura principal de un municipio (msvc-structure). Es la parte real
 * del tablero; lo requerido sale del catálogo de geography.
 */
export interface MunicipalitySummary {
  municipalityGeoId: number;
  /** Coordinadores de zona: se muestran como "promotores". */
  promoters: number;
  /** Promotores: se muestran como "movilizadores". */
  mobilizers: number;
  /** Suma de las metas de los coordinadores de zona. */
  goal: number;
  /** Convencidos vigentes y activos. */
  members: number;
}

/**
 * Un puesto vigente de la estructura principal de un municipio (desglose del
 * tablero): el árbol se arma con `parentId`.
 */
export interface MunicipalityPosition {
  id: number;
  parentId: number | null;
  level: PositionLevel;
  sectionGeoId: number | null;
  sectionKey: string | null;
  goal: number | null;
  /** null: vacante. */
  holderName: string | null;
  /** Una persona puede ocupar varios puestos de promotor (varias secciones). */
  holderPersonId: number | null;
  /** Convencidos vigentes y activos anclados directamente al puesto. */
  members: number;
  membersLast7: number;
  lastCapture: string | null;
}

/**
 * Una campaña (msvc-structure, `/campaigns`). La ACTIVE es la que se opera
 * (2027); una CLOSED (2024) sólo se consulta: la base rechaza escribirla.
 */
export interface CampaignSummary {
  id: number;
  name: string;
  type: string;
  year: number;
  electionDay: string | null;
  status: 'PLANNING' | 'ACTIVE' | 'CLOSED';
  structures: { id: number; name: string; type: 'MAIN' | 'ALTERNATE' | 'ALLIED' }[];
  /** Qué se elige: MAYOR ×17, LOCAL_DEPUTY ×21, FEDERAL_DEPUTY ×6. */
  elections: { office: string; scope: string; seats: number; electionDay: string }[];
}

// ─────────────────────────── armar la estructura (msvc-structure, sólo ADMIN)

/** Un puesto nuevo: referente sobre municipio, promotor sobre sección, movilizador bajo promotor. */
export interface CreatePositionRequest {
  structureId?: number;
  level: PositionLevel;
  parentPositionId?: number;
  sectionGeoId?: number;
  municipalityGeoId?: number;
  goal?: number | null;
  childLimit?: number | null;
}

/** Cuántos puestos pide la meta en una sección, cuántos había y cuántos se crearon. */
export interface VacancyResult {
  sectionGeoId: number | null;
  needed: number;
  existing: number;
  created: number;
}

export type AccountMode = 'CREATE' | 'LINK' | 'NONE';

export interface AppointRequest {
  person: {
    personId?: number | null;
    names?: string;
    lastName?: string;
    surName?: string;
    curp?: string;
    ine?: string;
    phone?: string;
    email?: string;
  };
  account: { mode: AccountMode; email?: string; password?: string; authId?: string };
  reason?: string;
  /** Con LINK: confirma dejar vacantes sus otros puestos de la misma rama. */
  replaceInBranch?: boolean;
  /** En un promotor: ocupa también los demás puestos vacantes de promotor de la sección. */
  wholeSection?: boolean;
}

/** Un puesto vacante de promotor de la misma sección (para «toda la sección»). */
export interface SectionSeat {
  positionId: number;
  sectionKey: string | null;
  goal: number | null;
}

/** Al ligar una cuenta que ya ocupa otros puestos en la misma rama (409 BRANCH_CONFLICT). */
export interface LinkPlan {
  roleToGrant: string;
  vacates: { positionId: number; level: PositionLevel; label: string }[];
  rolesToRevoke: string[];
}

export interface Appointed {
  positionId: number;
  personId: number;
  authId: string | null;
  accountCreated: boolean;
  role: string;
  /** Con LINK: los puestos de la misma rama que quedaron vacantes. */
  vacatedPositions: number[];
  /** Con LINK: los roles que se le quitaron a la cuenta. */
  revokedRoles: string[];
  /** Con «toda la sección»: los demás puestos de la sección que también ocupó. */
  alsoAppointed: number[];
}

/** Una persona del padrón de msvc-structure (para buscar a quién nombrar). */
export interface PersonLite {
  id: number;
  names: string | null;
  lastName: string | null;
  surName: string | null;
  curp: string | null;
  email: string | null;
  cellPhoneNumber: string | null;
  authId: string | null;
}

/** Un día de la actividad de un puesto (msvc-structure): convencidos capturados ese día. */
export interface DailyActivity {
  /** ISO, en hora de México: "2024-03-26". */
  day: string;
  captured: number;
}

/** La cuenta de Keycloak del titular, como la devuelve msvc-users (`/v2/accounts/{authId}`). */
export interface AccountSummary {
  authId: string;
  username: string;
  email: string | null;
  firstName: string | null;
  lastName: string | null;
  enabled: boolean;
  /** Alta de la cuenta, epoch en milisegundos. */
  created: number | null;
  /** Sólo los de estructura y ADMIN, con las erratas del realm (CORDINATOR_ZONE…). */
  roles: string[];
}
