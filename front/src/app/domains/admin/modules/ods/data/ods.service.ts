import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Goal, Indicator, Instrument, Objective } from './ods.types';

/**
 * Capa de datos del módulo ODS. Los catálogos viven en el microservicio de logs
 * (`url_log/ods/...`), como en el A15.
 */
@Injectable({ providedIn: 'root' })
export class OdsService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return `${this.appConfig.config.url_log}/ods`;
  }

  getOds(): Observable<Objective[]> {
    return this.http.get<Objective[]>(this.base);
  }
  getOdsById(odsId: string): Observable<Objective> {
    return this.http.get<Objective>(`${this.base}/${odsId}`);
  }

  getGoalsByOdsId(odsId: string): Observable<Goal[]> {
    return this.http.get<Goal[]>(`${this.base}/goals/byOdsId/${odsId}`);
  }
  getGoalById(goalId: string): Observable<Goal> {
    return this.http.get<Goal>(`${this.base}/goals/${goalId}`);
  }

  getIndicatorsByGoalId(goalId: string): Observable<Indicator[]> {
    return this.http.get<Indicator[]>(`${this.base}/indicators/byGoalId/${goalId}`);
  }
  getIndicatorById(indicatorId: string): Observable<Indicator> {
    return this.http.get<Indicator>(`${this.base}/indicators/${indicatorId}`);
  }

  getInstrumentsByIndicatorId(indicatorId: string): Observable<Instrument[]> {
    return this.http.get<Instrument[]>(`${this.base}/instruments/byIndicatorsId/${indicatorId}`);
  }
}
