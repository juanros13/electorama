import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CampaignData,
  CampaignElection,
  CampaignStatus,
  CampaignSummary,
  LevelData,
  StructureType,
  TerritoryOption,
} from './organization.types';

/**
 * Configurar campañas en msvc-structure (sólo ADMIN): datos generales, estatus,
 * cargos en disputa, estructuras y niveles del organigrama. Cada escritura
 * devuelve la campaña completa ya guardada.
 */
@Injectable({ providedIn: 'root' })
export class CampaignAdminService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return `${this.appConfig.config.url_structure}/campaigns`;
  }

  list(): Observable<CampaignSummary[]> {
    return this.http.get<CampaignSummary[]>(this.base);
  }

  get(id: number): Observable<CampaignSummary> {
    return this.http.get<CampaignSummary>(`${this.base}/${id}`);
  }

  territoryOptions(): Observable<TerritoryOption[]> {
    return this.http.get<TerritoryOption[]>(`${this.base}/territory-options`);
  }

  create(data: CampaignData): Observable<CampaignSummary> {
    return this.http.post<CampaignSummary>(this.base, data);
  }

  update(id: number, data: CampaignData): Observable<CampaignSummary> {
    return this.http.put<CampaignSummary>(`${this.base}/${id}`, data);
  }

  changeStatus(id: number, status: CampaignStatus): Observable<CampaignSummary> {
    return this.http.patch<CampaignSummary>(`${this.base}/${id}/status`, { status });
  }

  /** La lista completa y en orden: el primero es la cúspide. */
  saveLevels(id: number, levels: LevelData[]): Observable<CampaignSummary> {
    return this.http.put<CampaignSummary>(`${this.base}/${id}/levels`, { levels });
  }

  saveElections(id: number, elections: Partial<CampaignElection>[]): Observable<CampaignSummary> {
    return this.http.put<CampaignSummary>(`${this.base}/${id}/elections`, { elections });
  }

  addStructure(id: number, name: string, type: StructureType): Observable<CampaignSummary> {
    return this.http.post<CampaignSummary>(`${this.base}/${id}/structures`, { name, type });
  }

  updateStructure(structureId: number, name: string, type: StructureType): Observable<CampaignSummary> {
    return this.http.put<CampaignSummary>(`${this.base}/structures/${structureId}`, { name, type });
  }

  deleteStructure(structureId: number): Observable<CampaignSummary> {
    return this.http.delete<CampaignSummary>(`${this.base}/structures/${structureId}`);
  }
}
