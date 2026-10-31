/**
 * El modelo de estructura con puestos y vigencias (msvc-structure).
 *
 * Es otro modelo, no una versión nueva del de `structure.types.ts`: aquí la
 * persona no *es* coordinadora, sino que **ocupa** un puesto durante una
 * vigencia. Por eso el puesto y su titular son cosas separadas, y por eso el
 * historial existe.
 */

/**
 * Las tres funciones heredadas del organigrama. Desde V3 cada campaña define sus
 * niveles (`CampaignLevel`); un nivel puede llevar una de estas funciones para
 * conservar la lógica que depende de ella (claves de sección, captura, reportes).
 */
export type PositionLevel = 'REFERENT' | 'ZONE_COORDINATOR' | 'PROMOTER';

/** Territorio del puesto. El organigrama y el territorio son árboles distintos. */
export type TerritoryType = 'STATE' | 'MUNICIPALITY' | 'SECTION' | 'FRAGMENT';

/** Un puesto con su titular vigente y sus dos conteos. */
export interface PositionSummary {
  id: number;
  /** La función heredada del nivel; null en un nivel sin función. */
  level: PositionLevel | null;
  /** El nivel de su campaña: id, rango (1 = cúspide) y nombre en pantalla. */
  levelId: number;
  levelRank: number;
  levelName: string;
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
export type CampaignStatus = 'PLANNING' | 'ACTIVE' | 'CLOSED';
export type CampaignType = 'FEDERAL' | 'STATE' | 'MUNICIPAL' | 'INTERNAL';
export type StructureType = 'MAIN' | 'ALTERNATE' | 'ALLIED';

/**
 * Un nivel del organigrama de una campaña (V3). `positions` cuenta sus puestos:
 * con alguno ya no cambian rango, territorio ni función.
 */
export interface CampaignLevel {
  id: number;
  rank: number;
  code: string;
  nameSingular: string;
  namePlural: string;
  /** Dónde va el puesto; null = donde diga quien lo crea. */
  territoryType: TerritoryType | null;
  /** Meta con la que nace cada puesto; también reparte las vacantes del nivel de arriba. */
  defaultGoal: number | null;
  defaultChildLimit: number | null;
  function: PositionLevel | null;
  roleName: string;
  alternateRoleName: string | null;
  positions: number;
}

export interface CampaignElection {
  office: string;
  scope: string;
  seats: number;
  electionDay: string;
}

export interface CampaignSummary {
  id: number;
  name: string;
  type: CampaignType;
  year: number | null;
  electionDay: string | null;
  status: CampaignStatus;
  territoryId: number | null;
  territoryName: string | null;
  /** Clave INE del estado de su territorio (geography); null si no tiene territorio. */
  stateGeoId: number | null;
  /** El municipio de geography, si la campaña es municipal. */
  municipalityGeoId: number | null;
  /** % de la LN que es meta en cada sección. */
  goalPercentage: number;
  startDate: string | null;
  endDate: string | null;
  description: string | null;
  structures: { id: number; name: string; type: StructureType }[];
  /** Qué se elige: MAYOR ×17, LOCAL_DEPUTY ×21, FEDERAL_DEPUTY ×6. */
  elections: CampaignElection[];
  /** Los niveles del organigrama, de la cúspide hacia abajo. */
  levels: CampaignLevel[];
}

// ─────────────────────────── configurar la campaña (msvc-structure, sólo ADMIN)

export interface CampaignData {
  name: string;
  type: CampaignType;
  year?: number | null;
  electionDay?: string | null;
  territoryId?: number | null;
  startDate?: string | null;
  endDate?: string | null;
  description?: string | null;
  /** % de la LN que es meta en cada sección; 61 si no se manda. */
  goalPercentage?: number | null;
}

/** Un nivel a guardar; `id` null es uno nuevo y `roleName` null toma `<CAMPAÑA>_<CODIGO>`. */
export interface LevelData {
  id?: number | null;
  code?: string | null;
  nameSingular: string;
  namePlural?: string | null;
  territoryType?: TerritoryType | null;
  defaultGoal?: number | null;
  defaultChildLimit?: number | null;
  function?: PositionLevel | null;
  roleName?: string | null;
  alternateRoleName?: string | null;
}

/** Estados y municipios que pueden ser el alcance de una campaña. */
export interface TerritoryOption {
  id: number;
  type: 'STATE' | 'MUNICIPALITY';
  code: string;
  name: string;
  parentId: number | null;
}

// ─────────────────────────── armar la estructura (msvc-structure, sólo ADMIN)

/**
 * Un puesto nuevo. El nivel va por `levelId` (V3) o por su función `level`; el
 * territorio por `territoryId` o por municipio/sección del catálogo de geography.
 */
export interface CreatePositionRequest {
  structureId?: number;
  level?: PositionLevel;
  levelId?: number;
  territoryId?: number;
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
