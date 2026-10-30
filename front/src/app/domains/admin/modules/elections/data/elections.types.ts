export interface ElectionsType {
  id: number;
  name: string;
}

export interface ElectionsPosition {
  id: number;
  name: string;
  sortOrder: number;
}

export interface ResultList {
  candidateId: number;
  politicalPartyName: string;
  result: number;
  softorder: number;
}

export interface ElectionResult {
  candidateId: number;
  electionName: string;
  electionPositionName: string;
  names: string;
  lastNames: string;
  surNames: string;
  totalVotes: number;
  candidatePath: string;
  electionId: number;
  resultList: ResultList[];
  candidatePathBase64?: Blob;
}

export interface VoterListReview {
  id: number;
  electionId: number;
  total: number;
  progress: number;
}

export interface PoliticalPartyResultSheet {
  electionResultSheets: ElectionResult[];
  success: string;
  voters_list: VoterListReview[];
}

/** Resultado agrupado por posición electoral (para render por cards). */
export interface ElectionResultBySheet {
  length: number;
  totalSheets: number;
  totalVotes: number;
  electionPositionName: string;
  dataElectionResult: ElectionResult[];
  progressSheet: number;
}

export interface VotersList {
  id: number;
  userAuthId: string;
  electionId: number;
  voterList: number;
}

export interface PoliticalPartyResult {
  success: string;
  electionResult: ElectionResult[];
  voters_list: VotersList[];
}

/** Resultado agrupado por posición (PREP/participación). */
export interface ElectionResultBySection {
  length: number;
  totalVotes: number;
  electionPositionName: string;
  dataElectionResult: ElectionResult[];
  voterList: number;
}

/** Casilla en el listado de validación de actas. */
export interface PollingBoothResult {
  id: number;
  electionName: string;
  electionYear: string;
  electionType: string;
  state: string;
  federalDistrict: number;
  localDistrict: number;
  sectionNumber: number;
  pollingBoothsId: number;
  pollingBoothName: string;
  status: string;
  municipality: string;
  pollingBoothsElectionAssociationId: number;
  votersList: number;
}

/** Resultado por partido dentro de un acta. */
export interface ElectionResultDtoList {
  id: number;
  softorder: number;
  result: number | null;
  politicalPartyId: number;
  politicalPartyName: string;
}

/** Respuesta de resultados de una asociación (la API la devuelve como array). */
export interface ElectionResultByIdResponse {
  id: any;
  pollingBoothId: number;
  pathSheet: string;
  electionResultDtoList: ElectionResultDtoList[];
}

/** Totales de avance de validación. */
export interface ValideSheet {
  totalDigitalValidate: number;
  totalFisicalValidate: number;
  totalSheets: number;
}

/** Fila del flujo de votación (avance por casilla). */
export interface VotingFlow {
  id: number;
  electionType: string;
  federalDistrict: number;
  localDistrict: number;
  categoryName: string;
  sectionNumber: number;
  pollingBoothName: string;
  pollingBoothsId: number;
  progressVote?: number;
  votersList: number;
  municipality: string;
}

/** Totales de progreso de votación. */
export interface ProgressVote {
  voterList: number;
  progressVote: number;
}

/** Casilla con su estatus de apertura. */
export interface PollingBoothsStatusList {
  id: number;
  state: string;
  municipality: string;
  federalDistrict: number;
  localDistrict: number;
  sectionNumber: number;
  pollingBoothId: number;
  pollingBoothName: string;
  status: string;
}

/** Conteo de casillas abiertas/cerradas. */
export interface PollingBoothCount {
  totalOpen: number;
  totalClose: number;
}

/** Catálogo de estatus de casilla. */
export interface ElectionStatus {
  id: number;
  name: string;
}

/** Marcaje de votantes por municipio/distrito. */
export interface FollowerMarkVoter {
  municipalityId: any;
  municipality: any;
  numVoters: number;
  numVotersPending: number;
  federalDistrict: any;
  federalDistrictName: any;
  localDistrict: number;
  seccionNumber: any;
}
