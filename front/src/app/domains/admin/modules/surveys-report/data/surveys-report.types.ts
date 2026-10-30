// Subconjunto portado de tipos de reportes de censo. Se ampliará por vista.

export interface DashSurveyCard {
  id: number;
  municipalityName: string;
  municipalityId: number;
  goalHolmes: number;
  formCaptured: number;
  formToCapture: number;
}

export interface DashboardSurveyReport {
  success: string;
  getDashboardMunicipality: DashSurveyCard[];
}

export interface GetSurveyReportMunicipality {
  id: number;
  municipalityName: string;
  municipalityId: number;
  section: number;
  goalHolmes: number;
  folioCaptured: number;
  formCaptured: number;
}

export interface SurveyMainReport {
  success: string;
  getSurveyReportMunicipality: GetSurveyReportMunicipality[];
}

export interface GetSurveyReportCoordinator {
  id: number;
  municipalityName: string;
  municipalityId: number;
  coordinatorName: string;
  coordinatorAuthId: string;
  goalHolmes: number;
  folioCaptured: number;
  formCaptured: number;
}

export interface SurveyCoordinatorReport {
  success: string;
  getSurveyReportCoordinatorMunicipality: GetSurveyReportCoordinator[];
}

export interface ReportByMunList {
  id: number;
  municipalityId: number;
  municipalityName: string;
  sectionNumber: number;
  goalHolmes: number;
  folioCaptured: number;
  formCaptured: number;
  authId: string;
  name: string;
}

export interface ReportByMun {
  success: string;
  getSurveyReportCoordinatorMunicipality: ReportByMunList[];
}

export interface HousingData {
  id: number;
  name: string;
}

export interface SurveyPerson {
  id: number;
  names: string;
  lastName: string;
  surName: string;
  curp: string;
  gender: string;
  dateOfBirth: string;
  mobilePhone: string;
  surveyAnnex?: any[];
}

export interface CompleteSurvey {
  id: number;
  surveyUserFolio: { folio: string };
  folioHouse: string;
  surveyDate: string;
  latitude: string;
  longitude: string;
  surveyUser: { name: string } | null;
  deviceId: string;
  pathPhotoFacadeQr: string;
  pathPhotoIneAdverse: string;
  pathPhotoIneReverse: string;
  municipality: string;
  locality: string;
  localityOther: string;
  street: string;
  exteriorNumber: string;
  interiorNumber: string;
  postalCode: string;
  reference: string;
  surveyPersonEntityList: SurveyPerson[];
  familyNumbers: string;
  housingOther: string;
  surveyHousing: HousingData | null;
  surveyHousingCondition: HousingData | null;
  surveyHousingDocument: HousingData | null;
  surveyHousingMaterialWall: HousingData | null;
  surveyHousingMaterialRoof: HousingData | null;
  surveyHousingMaterialFloor: HousingData | null;
  surveyHousingServicesEntities: HousingData[] | null;
  culturalActivityParticipation: string;
  knowledgeCulturalActivity: string;
  surveyCulturalActivity: HousingData | null;
  otherTypeCulturalActivity: string;
  created: string;
  updated: string;
}

export interface ServerSurvey {
  id: number;
  surveyUserFolio: { folio: string } | null;
  folioHouse: string;
  surveyDate: string;
  sectionNumber?: number;
  statusChange?: number;
}

export interface ReportSurveysByServer {
  id: number;
  name: string;
  authId: string;
  municipalityName: string;
  survey: ServerSurvey[];
}

export interface GetSurveyReportMunicipalityLocation {
  id: number;
  surveyUserId: number;
  latitude: string;
  longitude: string;
}

export interface SurveyHousesLocation {
  getSurveyReportMunicipality: GetSurveyReportMunicipalityLocation[];
}

export interface MunStat {
  id: number;
  municipalityId: number;
  municipalityName: string;
  stateServer: number;
  goalHolmes: number;
  folioCaptured: number;
  formCaptured: number;
  homesToBeCensuced: number;
}

export interface GetStatsByMun {
  getSurveyReportMunicipalityId: MunStat[];
}
