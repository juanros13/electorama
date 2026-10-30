/** Los cargos con histórico cargado; así los nombra msvc-electoral-history. */
export type Office =
  | 'PRESIDENT'
  | 'SENATOR'
  | 'FEDERAL_DEPUTY'
  | 'GOVERNOR'
  | 'LOCAL_DEPUTY'
  | 'MAYOR';

/**
 * Cómo se agrupan los votos:
 *  - candidacy: la coalición con que se compitió (el resultado real)
 *  - party: sólo votos de un emblema; las combinaciones no se reparten
 *  - bloc: el bloque analítico del año (4T, PAN-PRI-PRD, MC, otros)
 */
export type GroupBy = 'candidacy' | 'party' | 'bloc';

/** Cómo se reparte el estado en el reporte. */
export type TerritoryLevel = 'MUNICIPALITY' | 'LOCAL_DISTRICT' | 'FEDERAL_DISTRICT';

export interface ElectionSummary {
  id: number;
  year: number;
  office: Office;
  scope: 'STATE' | 'FEDERAL_DISTRICT' | 'LOCAL_DISTRICT' | 'MUNICIPALITY';
  electionDate: string;
  publisher: 'INE' | 'IEPCT';
  sourceUrl: string;
  sourceFile: string;
  /** La fecha de corte tal como la publica el órgano electoral. */
  cutLabel: string;
}

export interface GroupVotes {
  code: string;
  label: string;
  color: string | null;
  votes: number;
  /** Sobre la votación total emitida (con nulos y no registrados). */
  pct: number;
  /** Sobre la votación válida. */
  validPct: number;
  /**
   * Quién encabezó la candidatura en ese territorio. Sólo por candidatura y en
   * el territorio de la contienda (o si es estatal); el IEPCT sólo publica
   * los nombres de las independientes.
   */
  candidate: string | null;
}

export interface TerritoryResult {
  /** 0 = estatal; el municipio, el número de distrito o el de sección. */
  code: number;
  name: string;
  stations: number;
  specialStations: number;
  /** LN de las casillas ordinarias: la base de la participación. */
  nominalList: number;
  totalVotes: number;
  /** % de participación; null si no hay LN (voto anticipado, extranjero). */
  turnout: number | null;
  nullVotes: number;
  nonRegisteredVotes: number;
  /** De mayor a menor votación. */
  groups: GroupVotes[];
  winner: string | null;
  runnerUp: string | null;
  /** Puntos porcentuales entre el primero y el segundo. */
  marginPp: number | null;
}

export interface TerritoryReport {
  election: ElectionSummary;
  groupBy: GroupBy;
  level: TerritoryLevel;
  /** Si la fuente trae los nombres de las personas candidatas. */
  candidatesAvailable: boolean;
  /** Incluye lo que no es de ningún territorio (anticipado, prisión, extranjero). */
  state: TerritoryResult;
  territories: TerritoryResult[];
}

export interface SectionReport {
  election: ElectionSummary;
  groupBy: GroupBy;
  level: TerritoryLevel;
  territory: TerritoryResult;
  sections: TerritoryResult[];
}

/** Un territorio en una elección, para compararlo con las demás del cargo. */
export interface TrendPoint {
  year: number;
  totalVotes: number;
  turnout: number | null;
  /** En la agrupación pedida (por defecto, bloque). */
  groups: GroupVotes[];
  /** La candidatura que ganó ahí: siempre el resultado real, por candidatura. */
  winner: GroupVotes | null;
  marginPp: number | null;
}

export interface TrendRow {
  /** 0 = estatal; el municipio o el número de distrito. */
  code: number;
  name: string;
  points: TrendPoint[];
}

export interface TrendReport {
  office: Office;
  groupBy: GroupBy;
  level: TerritoryLevel;
  /** En orden cronológico. */
  elections: ElectionSummary[];
  state: TrendRow;
  territories: TrendRow[];
}

// ─────────────────────────── análisis para dirección

/** Qué tan en riesgo está un territorio para el bloque 4T. */
export type RiskStatus = 'LOST' | 'CLOSE' | 'COMPETITIVE' | 'SAFE';

/** Un municipio (ayuntamiento) o distrito local (diputación) visto desde el bloque 4T. */
export interface TerritoryRisk {
  code: number;
  name: string;
  status: RiskStatus;
  winner: GroupVotes | null;
  /** La mejor candidatura del bloque 4T: MORENA o un aliado que fue aparte. */
  best4t: GroupVotes | null;
  /** La mejor fuera del bloque 4T. */
  rival: GroupVotes | null;
  /** best4t − rival en pp; negativo si se perdió. */
  marginPp: number | null;
  bloc4tPct: number | null;
  /** El bloque 4T en la elección de la marca (gubernatura o presidencia). */
  brand4tPct: number | null;
  /** Cuánto se quedó el candidato local debajo de la marca. */
  brandGapPp: number | null;
  previous4tPct: number | null;
  change4tPp: number | null;
  turnout: number | null;
  nominalList: number;
  /** Se perdió, pero el bloque 4T sumado superaba al ganador. */
  alliesSplit: boolean;
  /** Ganó un aliado compitiendo contra MORENA. */
  allyWinner: boolean;
  /** El segundo lugar fue otra candidatura del bloque 4T. */
  allyRunnerUp: boolean;
}

/** Una sección a vigilar del ayuntamiento. */
export interface SectionRisk {
  section: number;
  municipality: number;
  municipalityName: string;
  localDistrict: number | null;
  federalDistrict: number | null;
  nominalList: number;
  turnout: number | null;
  status: RiskStatus;
  best4tLabel: string;
  best4tPct: number;
  rivalLabel: string;
  rivalPct: number;
  marginPp: number;
  /** Negativo = los votos que faltaron para ganar la sección. */
  marginVotes: number;
  bloc4tPct: number | null;
  brand4tPct: number | null;
  brandGapPp: number | null;
}

export interface RivalTrend {
  code: string;
  label: string;
  color: string | null;
  points: { year: number; votes: number; pct: number }[];
}

export interface AnalysisKpis {
  brand4tPct: number;
  mayor4tPct: number;
  brandGapPp: number;
  mayoralties: number;
  mayoraltiesLost: number;
  mayoraltiesClose: number;
  localDistricts: number;
  localDistrictsLost: number;
  sections: number;
  sectionsLost: number;
  sectionsCompetitive: number;
  abstention: number;
  turnout: number | null;
}

export interface AnalysisReport {
  year: number;
  mayor: ElectionSummary;
  localDeputy: ElectionSummary;
  brand: ElectionSummary;
  previousYear: number | null;
  /** Frases armadas con los datos, de la más a la menos importante. */
  findings: string[];
  kpis: AnalysisKpis;
  municipalities: TerritoryRisk[];
  localDistricts: TerritoryRisk[];
  rivals: RivalTrend[];
  sections: SectionRisk[];
}

/** Una sección con sus votos por bloque, para cruzarla con otra fuente. */
export interface SectionTotals {
  section: number;
  municipality: number;
  localDistrict: number | null;
  federalDistrict: number | null;
  nominalList: number;
  totalVotes: number;
  /** Votos de casillas ordinarias / su LN. */
  turnout: number | null;
  /** MORENA_ALLIES, PAN_PRI_PRD, MC, OTHERS. */
  blocVotes: Record<string, number>;
}

/** Una sección en todas las elecciones cargadas (GET /v1/sections/{section}/history). */
export interface SectionHistory {
  section: number;
  /** Clave del municipio (1..17). */
  municipality: number | null;
  /** De la más reciente a la más antigua. */
  elections: SectionElection[];
}

export interface SectionElection {
  year: number;
  office: Office;
  electionDate: string;
  nominalList: number;
  totalVotes: number;
  /** Votos de casillas ordinarias / LN; null sin LN. */
  turnout: number | null;
  /** De mayor a menor votación. */
  groups: GroupVotes[];
  winner: string | null;
  /** Ventaja del primero sobre el segundo, en puntos de la votación total. */
  marginPct: number | null;
}
