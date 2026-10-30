import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CatalogItem, MemberDocuments, SurveyAnnex } from './extended.types';

/**
 * Capa de datos del anexo de datos extendidos de miembros (discapacidad /
 * adulto mayor): catálogos, alta de anexo y carga de documentos.
 */
@Injectable({ providedIn: 'root' })
export class ExtendedService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get catalogs(): string {
    return this.appConfig.config.url_survey_catalogs;
  }
  private get surveys(): string {
    return this.appConfig.config.url_surveys;
  }

  // --- catálogos ---
  getDisability(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getDisability`);
  }
  getRelationship(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getRelationship`);
  }
  getIndigenousLanguages(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getAllIndigenousLanguages`);
  }
  getOriginsOfDisability(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getAllOriginsOfDisability`);
  }
  getDisabilityInformation(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getAllDisabilityInformation`);
  }
  getTypesOfDisability(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getAllTypesOfDisability`);
  }
  getCausesOfDisability(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getAllCausesOfDisability`);
  }
  getTutorsOrAssistants(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getAllTutorsOrAssistants`);
  }
  getRequestingOrAuxiliaryDocuments(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getAllRequestingOrAuxiliaryDocuments`);
  }

  // --- escritura ---
  /** Alta de anexos de miembros. */
  saveSurveyAnnex(annexes: SurveyAnnex[]): Observable<any> {
    return this.http.post<any>(`${this.surveys}/surveyAnnex`, { surveyAnnexList: annexes });
  }

  /** Actualiza un anexo existente (por id). */
  updateSurveyAnnex(annex: SurveyAnnex, id: number): Observable<any> {
    return this.http.put<any>(`${this.surveys}/surveyAnnex/${id}`, annex);
  }

  /** Carga de documentos (solicitante + auxiliar) de un miembro; sube los presentes. */
  uploadMemberDocuments(
    docs: Partial<MemberDocuments>,
    surveyPersonId: string,
    surveyId: string
  ): Observable<any> {
    const formData = new FormData();
    formData.append('surveyPersonId', surveyPersonId);
    formData.append('surveyId', surveyId);
    for (const [key, file] of Object.entries(docs)) {
      if (file instanceof File) {
        formData.append(key, file);
      }
    }
    return this.http.post<any>(`${this.surveys}/surveyAnnex/uploadFileAll`, formData);
  }

  /** ¿Tiene al menos un documento para subir? */
  hasAnyDocument(docs: Partial<MemberDocuments>): boolean {
    return Object.values(docs).some((f) => f instanceof File);
  }
}
