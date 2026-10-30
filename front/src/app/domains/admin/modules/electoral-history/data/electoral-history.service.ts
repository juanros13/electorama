import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AnalysisReport,
  ElectionSummary,
  GroupBy,
  TerritoryLevel,
  TerritoryReport,
  Office,
  SectionReport,
  SectionTotals,
  TrendReport,
  SectionHistory,
} from './electoral-history.types';

/** Resultados oficiales históricos, contra msvc-electoral-history (sólo ADMIN). */
@Injectable({ providedIn: 'root' })
export class ElectoralHistoryService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get root(): string {
    return `${this.appConfig.config.url_electoral_history}/v1`;
  }

  private get base(): string {
    return `${this.root}/elections`;
  }

  /**
   * Cómo votó una sección en cada elección cargada. También lo pueden pedir
   * movilizador, promotor y referente (para la suya); el resto es sólo ADMIN.
   */
  getSectionHistory(section: number, groupBy: GroupBy = 'bloc'): Observable<SectionHistory> {
    return this.http.get<SectionHistory>(`${this.root}/sections/${section}/history`, {
      params: { groupBy },
    });
  }

  /** Un cargo en todos sus años, por municipio o distrito. */
  /** Todas las secciones del estado con sus votos por bloque (sin el voto fuera de municipio). */
  getAllSections(year: number, office: Office): Observable<SectionTotals[]> {
    return this.http.get<SectionTotals[]>(`${this.base}/${year}/${office}/sections`);
  }

  /** El análisis de un año para dirección: ayuntamientos y diputaciones locales en riesgo. */
  getAnalysis(year: number): Observable<AnalysisReport> {
    return this.http.get<AnalysisReport>(`${this.root}/analysis/${year}`);
  }

  getTrend(
    office: Office,
    groupBy: GroupBy,
    level: TerritoryLevel
  ): Observable<TrendReport> {
    return this.http.get<TrendReport>(`${this.root}/trend`, {
      params: { office, groupBy, level },
    });
  }

  getElections(): Observable<ElectionSummary[]> {
    return this.http.get<ElectionSummary[]>(this.base);
  }

  getByTerritory(
    year: number,
    office: Office,
    level: TerritoryLevel,
    groupBy: GroupBy
  ): Observable<TerritoryReport> {
    return this.http.get<TerritoryReport>(
      `${this.base}/${year}/${office}/territories`,
      { params: { level, groupBy } }
    );
  }

  getBySection(
    year: number,
    office: Office,
    level: TerritoryLevel,
    territory: number,
    groupBy: GroupBy
  ): Observable<SectionReport> {
    return this.http.get<SectionReport>(
      `${this.base}/${year}/${office}/territories/${territory}/sections`,
      { params: { level, groupBy } }
    );
  }
}
