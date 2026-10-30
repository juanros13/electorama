import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DailyActivity,
  Appointed,
  AppointRequest,
  AssignmentHistory,
  CampaignSummary,
  CreatePositionRequest,
  PersonLite,
  VacancyResult,
  MunicipalityPosition,
  MunicipalitySummary,
  PositionProgress,
  PositionSummary,
  TerritoryScope,
  SectionSeat,
} from './organization.types';
import { SectionGoal, SectionOverview } from './territory.types';

/**
 * El modelo de estructura con puestos y vigencias.
 *
 * Va a `msvc-structure`, que es el dueño del organigrama. Convive con
 * `StructureService`, que sigue hablando con `msvc-users` para las altas: son
 * dos modelos a la vez mientras dure la migración, y mezclarlos en un solo
 * servicio esconde cuál es cuál.
 */
@Injectable({ providedIn: 'root' })
export class OrganizationService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return this.appConfig.config.url_structure;
  }

  // ─────────────────────────── armar la estructura (sólo ADMIN)

  createPosition(request: CreatePositionRequest): Observable<PositionSummary> {
    return this.http.post<PositionSummary>(`${this.base}/positions`, request);
  }

  /**
   * Las vacantes que pide la meta debajo de un puesto. Bajo un referente hay
   * que mandar la meta de cada sección; bajo un promotor no hace falta.
   */
  generateVacancies(
    positionId: number,
    sections?: { sectionGeoId: number; meta: number }[]
  ): Observable<VacancyResult[]> {
    return this.http.post<VacancyResult[]>(
      `${this.base}/positions/${positionId}/vacancies`,
      sections ? { sections } : {}
    );
  }

  /** Los demás puestos vacantes de promotor de la sección del puesto («toda la sección»). */
  getSectionVacancies(positionId: number): Observable<SectionSeat[]> {
    return this.http.get<SectionSeat[]>(`${this.base}/positions/${positionId}/section-vacancies`);
  }

  appoint(positionId: number, request: AppointRequest): Observable<Appointed> {
    return this.http.post<Appointed>(`${this.base}/positions/${positionId}/appoint`, request);
  }

  vacate(positionId: number, reason?: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/positions/${positionId}/holder`, {
      params: reason ? { reason } : {},
    });
  }

  closePosition(positionId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/positions/${positionId}`);
  }

  findPersonByCurp(curp: string): Observable<PersonLite> {
    return this.http.get<PersonLite>(`${this.base}/persons/by-curp/${encodeURIComponent(curp)}`);
  }

  searchPersons(q: string): Observable<PersonLite[]> {
    return this.http.get<PersonLite[]>(`${this.base}/persons/search`, { params: { q } });
  }

  /** Todas las campañas, la activa primero por año. */
  getCampaigns(): Observable<CampaignSummary[]> {
    return this.http.get<CampaignSummary[]>(`${this.base}/campaigns`);
  }

  /** La campaña en curso: la que se opera. */
  getActiveCampaign(): Observable<CampaignSummary> {
    return this.http.get<CampaignSummary>(`${this.base}/campaigns/active`);
  }

  /** Los puestos de referente de una estructura: la raíz del árbol. */
  getRoots(structureId: number): Observable<PositionSummary[]> {
    return this.http.get<PositionSummary[]>(`${this.base}/positions/roots`, {
      params: { structureId },
    });
  }

  getPosition(id: number): Observable<PositionSummary> {
    return this.http.get<PositionSummary>(`${this.base}/positions/${id}`);
  }

  /** Los puestos que cuelgan de uno, vigentes. */
  getChildren(id: number): Observable<PositionSummary[]> {
    return this.http.get<PositionSummary[]>(
      `${this.base}/positions/${id}/children`
    );
  }

  /**
   * Convencidos capturados por día en todo el subárbol, con los días vacíos en
   * cero. Sin rango, del primer al último día con captura.
   */
  getActivity(id: number, from?: string, to?: string): Observable<DailyActivity[]> {
    let params = new HttpParams();
    if (from) {
      params = params.set('from', from);
    }
    if (to) {
      params = params.set('to', to);
    }
    return this.http.get<DailyActivity[]>(
      `${this.base}/positions/${id}/activity`,
      { params }
    );
  }

  /** Convencidos de todo el subárbol contra la meta del puesto. */
  getProgress(id: number): Observable<PositionProgress> {
    return this.http.get<PositionProgress>(
      `${this.base}/positions/${id}/progress`
    );
  }

  /**
   * El historial de nombramientos del puesto.
   *
   * Es lo que el modelo viejo no podía responder: quién ocupaba este puesto en
   * marzo.
   */
  getAssignments(id: number): Observable<AssignmentHistory[]> {
    return this.http.get<AssignmentHistory[]>(
      `${this.base}/positions/${id}/assignments`
    );
  }

  /** El ámbito territorial de una cuenta: las secciones que cubre. */
  getScope(authId: string): Observable<TerritoryScope[]> {
    return this.http.get<TerritoryScope[]>(
      `${this.base}/positions/scope/${authId}`
    );
  }

  /** Los puestos vigentes sobre una sección del catálogo de geography. */
  getBySection(sectionGeoId: number, campaignId?: number): Observable<PositionSummary[]> {
    return this.http.get<PositionSummary[]>(
      `${this.base}/positions/by-section/${sectionGeoId}`,
      { params: campaignParams(campaignId) }
    );
  }

  /**
   * La estructura principal por municipio: gente, meta repartida y convencidos.
   * Sin campaña es la activa; con `campaignId` se lee otra (2024, para referencia).
   */
  getMunicipalitySummary(campaignId?: number): Observable<MunicipalitySummary[]> {
    return this.http.get<MunicipalitySummary[]>(
      `${this.base}/positions/municipality-summary`,
      { params: campaignParams(campaignId) }
    );
  }

  /** Los puestos de los tres niveles de un municipio, con titular y convencidos (tablero). */
  getMunicipalityBreakdown(
    municipalityGeoId: number,
    campaignId?: number
  ): Observable<MunicipalityPosition[]> {
    return this.http.get<MunicipalityPosition[]>(
      `${this.base}/positions/municipality/${municipalityGeoId}/breakdown`,
      { params: campaignParams(campaignId) }
    );
  }

  /** Todas las secciones con estructura: meta repartida, gente, convencidos y coordinadores. */
  getSectionOverview(campaignId?: number): Observable<SectionOverview[]> {
    return this.http.get<SectionOverview[]>(`${this.base}/positions/section-overview`, {
      params: campaignParams(campaignId),
    });
  }

  /** La meta repartida entre coordinadores, por sección. */
  getSectionGoals(sectionGeoIds: number[], campaignId?: number): Observable<SectionGoal[]> {
    return this.http.get<SectionGoal[]>(
      `${this.base}/positions/section-goals`,
      { params: campaignParams(campaignId).set('sectionGeoIds', sectionGeoIds.join(',')) }
    );
  }

  /**
   * La meta y el tope de hijos del puesto.
   *
   * Van los dos siempre: el servicio escribe ambos, así que el que no viaja
   * queda en null. Para dejar uno como estaba hay que mandarlo igual.
   */
  updateGoal(
    id: number,
    goal: number | null,
    childLimit: number | null
  ): Observable<PositionSummary> {
    let params = new HttpParams();
    if (goal !== null) {
      params = params.set('goal', goal);
    }
    if (childLimit !== null) {
      params = params.set('childLimit', childLimit);
    }
    return this.http.patch<PositionSummary>(
      `${this.base}/positions/${id}/goal`,
      null,
      { params }
    );
  }
}

/** Sin campaña, el servidor usa la activa. */
function campaignParams(campaignId?: number): HttpParams {
  return campaignId == null ? new HttpParams() : new HttpParams().set('campaignId', campaignId);
}
