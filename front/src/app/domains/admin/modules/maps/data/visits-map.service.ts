import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class VisitsMapService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  /** Puntos de visitas para el mapa. */
  getReportVisitMap(): Observable<any[]> {
    return this.http.post<any[]>(
      `${this.appConfig.config.url_visits}/reportVisited/reportVisitedMap`,
      {}
    );
  }
}
