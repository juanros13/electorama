import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CatalogItem } from './extended.types';
import { CurpResponse } from './surveys.types';

/**
 * Capa de datos compartida para la captura/edición del censo (surveys-form y
 * editsurvey): catálogos de hogar/persona, secciones, y actualización de
 * encuesta / persona. La validación CURP por API se añadirá con surveys-form
 * (requiere su propia URL de servicio).
 */
@Injectable({ providedIn: 'root' })
export class CaptureService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get surveys(): string {
    return this.appConfig.config.url_surveys;
  }
  private get catalogs(): string {
    return this.appConfig.config.url_survey_catalogs;
  }
  private get sections(): string {
    return this.appConfig.config.url_survey_sections;
  }
  private get personEntity(): string {
    return this.appConfig.config.url_personEntity;
  }
  private get validacion(): string {
    return this.appConfig.config.url_validacion;
  }

  // --- catálogos de persona ---
  getSurveyProfession(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.surveys}/catalogs/getSurveyProfession`);
  }
  getSurveyJob(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.surveys}/catalogs/getSurveyJob`);
  }
  getActivityCatalog(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getSurveyCulturalActivity`);
  }
  getFederalProgram(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getSurveyFederalProgram`);
  }

  /** Parentesco / relación familiar. */
  getRelationship(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getRelationship`);
  }

  /** Tipos de discapacidad. */
  getDisability(): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getDisability`);
  }

  /** Catálogos de vivienda: '' (tipo), 'Condition', 'Document', 'MaterialFloor/Roof/Wall'. */
  getHousingCatalogs(kind: string): Observable<CatalogItem[]> {
    return this.http.get<CatalogItem[]>(`${this.catalogs}/getSurveyHousing${kind}`);
  }

  /**
   * Validación de CURP contra el servicio externo (directo, sin proxy). Devuelve
   * datos oficiales (fecha/sexo) y banderas de coincidencia de nombre.
   */
  validateCurp(
    curp: string,
    name: string,
    lastName: string,
    surName: string
  ): Observable<CurpResponse> {
    return this.http.post<CurpResponse>(this.validacion, {
      curp,
      nombre: name,
      apellido_paterno: lastName,
      apellido_materno: surName,
    });
  }

  /** Localidades de una sección (para el selector de localidad en captura). */
  getLocalitySections(sectionId: number): Observable<any> {
    return this.http.get<any>(`${this.sections}/localitiesSection/localitiesSect/${sectionId}`);
  }

  // --- escritura ---
  updateSurvey(survey: any, surveyId: number | string): Observable<any> {
    return this.http.put<any>(`${this.surveys}/${surveyId}`, survey);
  }

  updatePersonEntity(person: any, surveyId: number | string): Observable<any> {
    return this.http.put<any>(`${this.personEntity}/${surveyId}`, person);
  }

  deletePersonEntity(person: any, surveyId: number | string): Observable<any> {
    return this.http.put<any>(`${this.personEntity}/${surveyId}`, person);
  }
}
