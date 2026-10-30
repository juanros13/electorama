import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { FolioRangeCompare, SurveyDto, SurveyFolioPage, SurveyUser } from './surveys.types';

@Injectable({ providedIn: 'root' })
export class SurveysService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return this.appConfig.config.url_surveys;
  }
  private get summary(): string {
    return this.appConfig.config.url_survey_summary;
  }
  private get cedula(): string {
    return this.appConfig.config.url_cedula;
  }

  getSurveyById(id: string): Observable<SurveyDto> {
    return this.http.get<SurveyDto>(`${this.base}/${id}`);
  }

  /** Carga única de la cédula (PDF/imagen) de un censo. */
  uploadSurveyCedula(file: File, surveyId: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', 'cedulaDocument');
    formData.append('surveyId', surveyId);
    return this.http.post<any>(`${this.cedula}/uploadCedulaDocument`, formData);
  }

  /** Folios de censos por capturar (paginado server-side). */
  getSurveysFolios(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    search: string
  ): Observable<SurveyFolioPage> {
    let url = `${this.base}/listSurveyFolio?pageSize=${pageSize}&sortBy=${sort}&orderBy=${order}&pageNo=${page}`;
    if (search) {
      url += `&search=${search}`;
    }
    return this.http.get<SurveyFolioPage>(url);
  }

  /** Folios de censos ya capturados (lista completa). */
  getSurveysFoliosComplete(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    search: string
  ): Observable<SurveyFolioPage> {
    let url = `${this.base}/listSurveyFolioComplete?pageSize=${pageSize}&sortBy=${sort}&orderBy=${order}&pageNo=${page}`;
    if (search) {
      url += `&search=${search}`;
    }
    return this.http.get<SurveyFolioPage>(url);
  }

  deleteSurvey(survey: SurveyDto): Observable<SurveyDto> {
    return this.http.delete<SurveyDto>(`${this.base}/survey/${survey.id}`);
  }

  /** Foto (descarga) desde Dropbox. */
  getPhoto(path: string): Observable<Blob> {
    return this.http.get(`${this.base}/dropboxDownload?path=${path}`, { responseType: 'blob' });
  }

  /** Previsualización de imagen (servicio de summary). */
  getSurveySummary(path: string): Observable<Blob> {
    return this.http.get(`${this.summary}/dropboxDownload?path=${path}`, { responseType: 'blob' });
  }

  // --- asignación de secciones / folios (admin) ---
  getUserMunicipality(): Observable<SurveyUser[]> {
    return this.http.get<SurveyUser[]>(`${this.base}/surveyUser/getUsersMunicipality`);
  }

  /** Folios permitidos de un usuario (para acotar y validar el rango a mover). */
  getFoliosCompareByUser(userId: number): Observable<FolioRangeCompare[]> {
    return this.http.post<FolioRangeCompare[]>(`${this.base}/foliosAvailables`, { userId });
  }

  /** Mueve un rango de folios a un usuario. */
  assignFolios(min: number, max: number, surveyUserId: number): Observable<any> {
    return this.http.post<any>(`${this.base}/surveyUser/assignFolio`, {
      surveyUserId,
      folioStart: Number(min),
      folioEnd: Number(max),
    });
  }

  /** Integra (mueve) una sección de un usuario a otro. */
  assignSection(sectionOutId: number, userOutAuthId: string, userInAuthId: string): Observable<any> {
    return this.http.post<any>(`${this.base}/surveyUser/assignSection`, {
      surveyUserAuthOuthId: userOutAuthId,
      surveyUserAuthInId: userInAuthId,
      sectionOutId,
    });
  }

  /** Elimina una sección/id asignada a un usuario. */
  deleteSection(id: number): Observable<any> {
    return this.http.delete<any>(`${this.base}/surveyUser/deleteSection/${id}`);
  }
}
