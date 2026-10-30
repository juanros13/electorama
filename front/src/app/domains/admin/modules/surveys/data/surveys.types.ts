// Subconjunto portado de los tipos de encuestas (Censo). Se ampliará por vista.

export interface SurveyUserFolio {
  id: number;
  folio: string;
  surveyUserId: number;
  comment: any;
}

export interface SurveyUserSection {
  id: number;
  surveyUserId: number;
  sectionId: number;
  sectionNumber: number;
  isOwner: boolean;
}

export interface SurveyUser {
  id: number;
  deviceId: string;
  name: string;
  authId: string;
  userName: string;
  municipalyId?: number;
  municipalityName?: string;
  surveyUserSection?: SurveyUserSection[];
}

/** Folio permitido para un usuario (validación de rangos). */
export interface FolioRangeCompare {
  id: number;
  folio: string;
  surveyUserId: number;
}

export interface SurveyDto {
  id: number;
  surveyUserFolio: SurveyUserFolio;
  folioHouse: string;
  surveyDate: string;
  latitude: string;
  longitude: string;
  surveyUser: SurveyUser;
  deviceId: string;
  pathPhotoIneAdverse: string;
  pathPhotoIneReverse: string;
  pathPhotoFacade: string;
  pathPhotoFacadeQr: string;
  municipalityId?: number;
  municipality?: string;
  localityId: number;
  locality: string;
  localityOther?: string;
  street: string;
  exteriorNumber: string;
  interiorNumber: string;
  postalCode: string;
  reference: string;
  familyNumbers: string;
  created: string;
  updated: string;
  deleted: string | null;
  statusChange: number;
  sectionId?: number;
  sectionNumber?: number;
  pathFileCedula?: string | null;
  surveyPersonEntityList?: SurveyPerson[];
}

export interface SurveyPerson {
  id: number;
  names: string;
  lastName: string;
  surName: string;
  dateOfBirth: string;
  gender: string;
  isDisabled?: string;
}

/** Página server-side de folios. */
export interface SurveyFolioPage {
  content: SurveyDto[];
  totalElements: number;
}

/** Respuesta del servicio externo de validación de CURP. */
export interface CurpResponse {
  Fecha?: string;
  apellidoPaterno?: boolean;
  apellidoMaterno?: boolean;
  nombre?: boolean;
  sexo?: number;
  error?: string;
}
