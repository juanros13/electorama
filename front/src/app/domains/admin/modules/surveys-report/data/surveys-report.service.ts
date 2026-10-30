import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import {
  CompleteSurvey,
  DashboardSurveyReport,
  DashSurveyCard,
  GetStatsByMun,
  GetSurveyReportCoordinator,
  GetSurveyReportMunicipality,
  GetSurveyReportMunicipalityLocation,
  MunStat,
  ReportByMun,
  ReportByMunList,
  ReportSurveysByServer,
  SurveyCoordinatorReport,
  SurveyHousesLocation,
  SurveyMainReport,
} from './surveys-report.types';

@Injectable({ providedIn: 'root' })
export class SurveysReportService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get surveys(): string {
    return this.appConfig.config.url_surveys;
  }
  private get coordinators(): string {
    return this.appConfig.config.url_coordinators;
  }
  private get report(): string {
    return this.appConfig.config.url_survey_report;
  }
  private get summary(): string {
    return this.appConfig.config.url_survey_summary;
  }

  /** Tarjetas de progreso por municipio (dashboard). */
  getDashboardReport(): Observable<DashSurveyCard[]> {
    return this.http
      .get<DashboardSurveyReport>(`${this.surveys}/surveyReport/getDashboardMunicipality`)
      .pipe(map((r) => r.getDashboardMunicipality));
  }

  /** Reporte por municipio (con secciones). */
  getSurveyMainReport(): Observable<GetSurveyReportMunicipality[]> {
    return this.http
      .get<SurveyMainReport>(`${this.surveys}/surveyReport/getSurveyReportMunicipality`)
      .pipe(map((r) => r.getSurveyReportMunicipality));
  }

  /** Reporte estatal por municipio/servidor (lista completa, sin paginar). */
  getReportByMunList(): Observable<ReportByMunList[]> {
    return this.http
      .get<ReportByMun>(`${this.surveys}/surveyReport/getSurveyReportCoordinatorMunicipality`)
      .pipe(map((r) => r.getSurveyReportCoordinatorMunicipality));
  }

  /** Reporte por coordinador de municipio. */
  getSurveyCoordinators(): Observable<GetSurveyReportCoordinator[]> {
    return this.http
      .get<SurveyCoordinatorReport>(`${this.coordinators}/surveyReport/getSurveyReportCoordinatorMunicipality`)
      .pipe(map((r) => r.getSurveyReportCoordinatorMunicipality));
  }

  /** Reporte general admin/coordinador (paginado). */
  getGeneralReport(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    search: string
  ): Observable<any> {
    let url = `${this.report}/getSurveyReportAdminCoordinator?pageSize=${pageSize}&sortBy=${sort}&orderBy=${order}&pageNo=${page}`;
    if (search) {
      url += `&search=${search}`;
    }
    return this.http.get<any>(url).pipe(map((r) => r.getSurveyReportAdminCoordinator));
  }

  /** Censo completo (resumen de solo lectura). */
  getCompleteSurvey(surveyId: string): Observable<CompleteSurvey> {
    return this.http.get<CompleteSurvey>(`${this.surveys}/${surveyId}`);
  }

  /** Censos capturados por un servidor estatal. */
  getSurveysByServer(authId: string): Observable<ReportSurveysByServer> {
    return this.http.get<ReportSurveysByServer>(`${this.surveys}/surveyUser/getStateServer/${authId}`);
  }

  getSurveySummary(path: string): Observable<Blob> {
    return this.http.get(`${this.summary}/dropboxDownload?path=${path}`, { responseType: 'blob' });
  }

  /** Ubicaciones (lat/lng) de casas censadas de un municipio. */
  getMunProgressDetail(idMun: string): Observable<GetSurveyReportMunicipalityLocation[]> {
    return this.http
      .get<SurveyHousesLocation>(`${this.surveys}/surveyReport/getCoordinateMunicipality/${idMun}`)
      .pipe(map((r) => r.getSurveyReportMunicipality));
  }

  /** Estadísticas de avance de un municipio. */
  getSurveyMunicipalityDetail(idMun: string): Observable<MunStat[]> {
    return this.http
      .get<GetStatsByMun>(`${this.surveys}/surveyReport/getSurveyReportMunicipalityId/${idMun}`)
      .pipe(map((r) => r.getSurveyReportMunicipalityId));
  }
}
