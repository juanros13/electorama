import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DatesRequest,
  ReportAllVisitsByAdmin,
  ReportCanvasResumeByAdmin,
  ReportMetaByAdmin,
} from './dashboard.types';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  getReportAllVisitByAdmin(): Observable<ReportAllVisitsByAdmin> {
    return this.http.get<ReportAllVisitsByAdmin>(
      `${this.appConfig.config.url_visits}/reportVisitedDashboard/reportTotalVisit`
    );
  }

  getReportMetaByAdmin(): Observable<ReportMetaByAdmin> {
    return this.http.get<ReportMetaByAdmin>(
      `${this.appConfig.config.url_user}/reportMeta/reportTotalMeta`
    );
  }

  getReportAllCanvasResumeByAdmin(): Observable<ReportCanvasResumeByAdmin[]> {
    return this.http.get<ReportCanvasResumeByAdmin[]>(
      `${this.appConfig.config.url_canvas_anda_walls}/reportCanvasAndWall/reportCanvasAndWallAllResume`
    );
  }

  // Series de tiempo (para las gráficas apexcharts, pendientes de portar).
  getReportVisitsByTime(
    startDate: string,
    endDate: string
  ): Observable<ReportMetaByAdmin[]> {
    const body: DatesRequest = { startDate, endDate };
    return this.http.post<ReportMetaByAdmin[]>(
      `${this.appConfig.config.url_visits}/reportVisitedDashboard/reportTotalVisitByTime`,
      body
    );
  }

  getReportCanvasAndWallsByTime(
    startDate: string,
    endDate: string,
    id: number | string
  ): Observable<ReportMetaByAdmin[]> {
    const body: DatesRequest = { startDate, endDate };
    return this.http.post<ReportMetaByAdmin[]>(
      `${this.appConfig.config.url_canvas_anda_walls}/reportCanvasAndWall/reportCanvasAndWallAllByTime/${id}`,
      body
    );
  }
}
