import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

/** Los convencidos de una campaña en una sección de residencia. */
export interface SupporterSection {
  sectionGeoId: number;
  main: number;
  alternate: number;
  /** Con la marca «votó» (las dos estructuras). */
  voted: number;
  firstCaptured: string | null;
  lastCaptured: string | null;
}

/** Un coordinador de zona de la estructura principal y lo que rindió su rama. */
export interface SupporterCoordinator {
  positionId: number;
  code: string;
  territoryType: 'SECTION' | 'MUNICIPALITY' | string;
  sectionGeoId: number | null;
  municipalityGeoId: number | null;
  holderName: string | null;
  goal: number | null;
  promoters: number;
  members: number;
  voted: number;
}

/** Una barra del perfil de la encuesta de captura. */
export interface SupporterProfileRow {
  dimension: string;
  /** "" = no se contestó. */
  value: string;
  count: number;
}

/**
 * El análisis de simpatizantes de una campaña (msvc-structure,
 * `/reports/supporters`). Todo agregado y de sólo lectura; el front lo pide
 * con la campaña cerrada (2024) para cruzarlo con el resultado oficial.
 */
@Injectable({ providedIn: 'root' })
export class SupportersService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return `${this.appConfig.config.url_structure}/reports/supporters`;
  }

  getSections(campaignId: number): Observable<SupporterSection[]> {
    return this.http.get<SupporterSection[]>(`${this.base}/sections`, { params: params(campaignId) });
  }

  getCoordinators(campaignId: number): Observable<SupporterCoordinator[]> {
    return this.http.get<SupporterCoordinator[]>(`${this.base}/coordinators`, {
      params: params(campaignId),
    });
  }

  getProfile(campaignId: number): Observable<SupporterProfileRow[]> {
    return this.http.get<SupporterProfileRow[]>(`${this.base}/profile`, { params: params(campaignId) });
  }
}

function params(campaignId: number): HttpParams {
  return new HttpParams().set('campaignId', campaignId);
}
