// Tipos para el anexo de datos extendidos de miembros (discapacidad / adulto mayor).

export interface CatalogItem {
  id: number;
  name: string;
}

/** Documentos a subir por solicitante y por auxiliar/tutor. */
export interface MemberDocuments {
  identificationApplicantFile: File;
  birthCertificateApplicantFile: File;
  curpApplicantFile: File;
  addressApplicantFile: File;
  identificationAssistantFile: File;
  birthCertificateAssistantFile: File;
  curpAssistantFile: File;
  addressAssistantFile: File;
}

/** Anexo de un miembro (datos extendidos). */
export interface SurveyAnnex {
  surveyPersonId: number;
  indigenousLanguage?: CatalogItem | null;
  disabilityType?: CatalogItem | null;
  originOfDisability?: CatalogItem | null;
  causeOfDisability?: CatalogItem | null;
  disabilityInformation?: CatalogItem | null;
  requestingDocument?: CatalogItem | null;
  tutorOrAssistant?: CatalogItem | null;
  relationship?: CatalogItem | null;
  [key: string]: any;
}
