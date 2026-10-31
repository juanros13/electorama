import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
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
import { DEFAULT_STATE_ID, stateName } from './states';

const STATE_KEY = 'electoral-history.stateId';

/**
 * Resultados oficiales históricos, contra msvc-electoral-history (sólo ADMIN).
 *
 * Todo es de un estado: el que se elige en el selector del módulo (se recuerda en
 * este navegador) o el que pide quien llama, p.ej. el de la campaña.
 */
@Injectable({ providedIn: 'root' })
export class ElectoralHistoryService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  /** El estado elegido (clave INE). */
  readonly stateId = signal<number>(storedState());
  readonly stateName = computed(() => stateName(this.stateId()));

  setState(id: number): void {
    this.stateId.set(id);
    try {
      localStorage.setItem(STATE_KEY, String(id));
    } catch {
      // sin almacenamiento: sólo dura la sesión
    }
  }

  private get root(): string {
    return `${this.appConfig.config.url_electoral_history}/v1`;
  }

  private get base(): string {
    return `${this.root}/elections`;
  }

  private state(stateId?: number): string {
    return String(stateId ?? this.stateId());
  }

  /**
   * Cómo votó una sección en cada elección cargada. También lo pueden pedir
   * movilizador, promotor y referente (para la suya); el resto es sólo ADMIN.
   */
  getSectionHistory(
    section: number,
    groupBy: GroupBy = 'bloc',
    stateId?: number
  ): Observable<SectionHistory> {
    return this.http.get<SectionHistory>(`${this.root}/sections/${section}/history`, {
      params: { groupBy, stateId: this.state(stateId) },
    });
  }

  /** Todas las secciones del estado con sus votos por bloque (sin el voto fuera de municipio). */
  getAllSections(year: number, office: Office, stateId?: number): Observable<SectionTotals[]> {
    return this.http.get<SectionTotals[]>(`${this.base}/${year}/${office}/sections`, {
      params: { stateId: this.state(stateId) },
    });
  }

  /** El análisis de un año para dirección: ayuntamientos y diputaciones locales en riesgo. */
  getAnalysis(year: number): Observable<AnalysisReport> {
    return this.http.get<AnalysisReport>(`${this.root}/analysis/${year}`, {
      params: { stateId: this.state() },
    });
  }

  /** Un cargo en todos sus años, por municipio o distrito. */
  getTrend(
    office: Office,
    groupBy: GroupBy,
    level: TerritoryLevel
  ): Observable<TrendReport> {
    return this.http.get<TrendReport>(`${this.root}/trend`, {
      params: { office, groupBy, level, stateId: this.state() },
    });
  }

  /** Las elecciones con datos en el estado: las federales y las locales que se cargaron. */
  getElections(stateId?: number): Observable<ElectionSummary[]> {
    return this.http.get<ElectionSummary[]>(this.base, {
      params: { stateId: this.state(stateId) },
    });
  }

  getByTerritory(
    year: number,
    office: Office,
    level: TerritoryLevel,
    groupBy: GroupBy
  ): Observable<TerritoryReport> {
    return this.http.get<TerritoryReport>(
      `${this.base}/${year}/${office}/territories`,
      { params: { level, groupBy, stateId: this.state() } }
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
      { params: { level, groupBy, stateId: this.state() } }
    );
  }
}

function storedState(): number {
  try {
    const value = Number(localStorage.getItem(STATE_KEY));
    return value >= 1 && value <= 32 ? value : DEFAULT_STATE_ID;
  } catch {
    return DEFAULT_STATE_ID;
  }
}
