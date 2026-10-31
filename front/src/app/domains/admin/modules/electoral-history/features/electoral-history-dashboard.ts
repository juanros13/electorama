import {
  CHART_FORE,
  CHART_GRID,
  ChartCard,
  ChartOptions,
  isDarkScheme,
} from '@/app/shared/ui/chart-card';
import { PageHeader } from '@/app/shared/ui/page-header';
import { StatCard } from '@/app/shared/ui/stat-card';
import { TableShell } from '@/app/shared/ui/table-shell';
import { showError } from '@/app/shared/ui/swal';
import { DecimalPipe, NgTemplateOutlet } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { catchError, of } from 'rxjs';
import { ElectoralAnalysis } from './electoral-analysis';
import { ElectoralHistoryService } from '../data/electoral-history.service';
import { StateSelect } from '../ui/state-select';
import {
  ElectionSummary,
  GroupBy,
  GroupVotes,
  TerritoryLevel,
  TerritoryReport,
  Office,
  SectionReport,
  TerritoryResult,
  TrendPoint,
  TrendReport,
  TrendRow,
} from '../data/electoral-history.types';

/** En el orden en que se leen en el filtro. */
const OFFICE_LABEL: Record<Office, string> = {
  MAYOR: 'Presidencia municipal',
  LOCAL_DEPUTY: 'Diputación local',
  GOVERNOR: 'Gubernatura',
  PRESIDENT: 'Presidencia de la República',
  SENATOR: 'Senaduría',
  FEDERAL_DEPUTY: 'Diputación federal',
};

/** Cómo se reparte el estado; el singular va en el encabezado de la tabla. */
const LEVEL_LABEL: Record<TerritoryLevel, { tab: string; one: string }> = {
  MUNICIPALITY: { tab: 'Municipio', one: 'Municipio' },
  LOCAL_DISTRICT: { tab: 'Distrito local', one: 'Distrito local' },
  FEDERAL_DISTRICT: { tab: 'Distrito federal', one: 'Distrito federal' },
};

const GROUP_LABEL: Record<GroupBy, string> = {
  candidacy: 'Candidatura',
  party: 'Partido',
  bloc: 'Bloque',
};

/** Cuántos grupos se pintan en la barra antes de juntar el resto en "otros". */
const BAR_GROUPS = 4;
const NEUTRAL = '#A3A3A3';

type SortKey = 'name' | 'totalVotes' | 'turnout' | 'winnerPct' | 'marginPp';
type TrendSortKey = 'name' | 'first' | 'last' | 'delta';

/** La tabla de una elección, su gráfica, la comparación entre años o el análisis para dirección. */
type View = 'election' | 'chart' | 'compare' | 'analysis';

/** El grupo que se sigue al comparar años, si existe en el cargo. */
const DEFAULT_FOCUS = 'MORENA_ALLIES';

/**
 * Cómo votó cada municipio en una elección oficial (INE / IEPCT).
 *
 * Todo sale de msvc-electoral-history, que cuadra casilla por casilla con los
 * cómputos publicados. El total estatal incluye el voto anticipado, en prisión
 * y en el extranjero, que no pertenece a ningún municipio: por eso la suma de
 * la tabla no llega exactamente al total de las tarjetas.
 */
@Component({
  selector: 'app-electoral-history-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './electoral-history-dashboard.html',
  imports: [
    ChartCard,
    DecimalPipe,
    ElectoralAnalysis,
    MatIconModule,
    MatTooltipModule,
    NgTemplateOutlet,
    PageHeader,
    StatCard,
    StateSelect,
    TableShell,
  ],
})
export default class ElectoralHistoryDashboard {
  private service = inject(ElectoralHistoryService);
  private destroyRef = inject(DestroyRef);

  protected readonly groupByOptions = Object.entries(GROUP_LABEL) as [GroupBy, string][];
  protected readonly levelOptions = Object.entries(LEVEL_LABEL).map(([value, l]) => ({
    value: value as TerritoryLevel,
    label: l.tab,
  }));
  /**
   * Por qué no se ven los nombres de las personas candidatas, si no se ven:
   * el IEPCT no publicó las listas de 2018, o el reporte no está en el territorio de la
   * contienda (un distrito junta candidaturas de varios municipios).
   */
  protected candidateHint = computed(() => {
    const report = this.report();
    if (!report || this.groupBy() !== 'candidacy') {
      return null;
    }
    if (!report.candidatesAvailable) {
      return 'El IEPCT no tiene publicadas las listas de candidaturas de esta elección: sólo se ven los nombres de las candidaturas independientes.';
    }
    const scope = report.election.scope;
    if (scope !== 'STATE' && scope !== this.level()) {
      const where =
        scope === 'MUNICIPALITY' ? 'municipio' : scope === 'LOCAL_DISTRICT' ? 'distrito local' : 'distrito federal';
      return `Los nombres de las personas candidatas se ven al ver por ${where}, que es donde se compitió.`;
    }
    return null;
  });

  /** Municipio, distrito local o distrito federal. */
  protected level = signal<TerritoryLevel>('MUNICIPALITY');
  protected levelLabel = computed(() => LEVEL_LABEL[this.level()].one);

  protected elections = signal<ElectionSummary[]>([]);
  protected office = signal<Office | null>(null);
  protected year = signal<number | null>(null);
  protected groupBy = signal<GroupBy>('candidacy');

  protected report = signal<TerritoryReport | null>(null);
  protected loading = signal(true);

  protected sortKey = signal<SortKey>('totalVotes');
  protected sortDesc = signal(true);

  // ---------------------------------------------------------------- comparar años
  /** Las vistas; la tabla de una elección es la primera y la de por defecto. */
  protected readonly tabs = [
    { view: 'election', label: 'Por elección', icon: 'vote' },
    { view: 'chart', label: 'Gráfica', icon: 'chart-bar-stacked' },
    { view: 'compare', label: 'Comparar años', icon: 'chart-line' },
    { view: 'analysis', label: 'Análisis 2024', icon: 'lightbulb' },
  ] as const;
  /** El análisis es de ayuntamientos y diputaciones locales: sólo donde se cargaron. */
  protected visibleTabs = computed(() => {
    const offices = new Set(this.elections().map((e) => e.office));
    const local = offices.has('MAYOR') && offices.has('LOCAL_DEPUTY');
    return this.tabs.filter((t) => t.view !== 'analysis' || local);
  });
  protected stateName = this.service.stateName;
  protected view = signal<View>('election');
  protected compare = computed(() => this.view() === 'compare');
  protected trend = signal<TrendReport | null>(null);
  protected trendLoading = signal(false);
  /** El grupo que se compara entre años (un bloque o un partido). */
  protected focus = signal(DEFAULT_FOCUS);
  /** El municipio que se dibuja junto al estado en la línea. */
  protected trendMunicipality = signal<number | null>(null);
  protected trendSortKey = signal<TrendSortKey>('delta');
  protected trendSortDesc = signal(true);

  protected trendYears = computed(() => this.trend()?.elections.map((e) => e.year) ?? []);

  /** Los grupos que aparecen en algún año del estado, para elegir el foco. */
  protected focusOptions = computed(() => {
    const seen = new Map<string, GroupVotes>();
    for (const p of this.trend()?.state.points ?? []) {
      for (const g of p.groups) {
        if (!seen.has(g.code)) seen.set(g.code, g);
      }
    }
    return [...seen.values()];
  });

  protected focusLabel = computed(
    () => this.focusOptions().find((g) => g.code === this.focus())?.label ?? this.focus()
  );

  protected trendRows = computed(() => {
    const rows = [...(this.trend()?.territories ?? [])];
    const key = this.trendSortKey();
    const dir = this.trendSortDesc() ? -1 : 1;
    const value = (r: TrendRow): number | string => {
      switch (key) {
        case 'name':
          return r.name;
        case 'first':
          return this.focusPct(r.points[0]);
        case 'last':
          return this.focusPct(r.points[r.points.length - 1]);
        default:
          return this.delta(r) ?? 0;
      }
    };
    return rows.sort((a, b) => {
      const x = value(a);
      const y = value(b);
      return typeof x === 'string' && typeof y === 'string'
        ? x.localeCompare(y, 'es') * dir
        : ((x as number) - (y as number)) * dir;
    });
  });

  /** La línea del grupo en foco: el estado y, si se eligió, un municipio. */
  protected trendChart = computed<ChartOptions | null>(() => {
    const trend = this.trend();
    if (!trend) {
      return null;
    }
    const color = this.focusOptions().find((g) => g.code === this.focus())?.color ?? NEUTRAL;
    const series = [
      { name: this.stateName(), data: trend.state.points.map((p) => this.focusPct(p)) },
    ];
    const colors = [color];
    const chosen = trend.territories.find((m) => m.code === this.trendMunicipality());
    if (chosen) {
      series.push({ name: chosen.name, data: chosen.points.map((p) => this.focusPct(p)) });
      colors.push('#1E3A8A');
    }
    return {
      series,
      colors,
      chart: {
        type: 'line',
        height: 260,
        toolbar: { show: false },
        zoom: { enabled: false },
        foreColor: CHART_FORE,
        fontFamily: 'inherit',
      },
      stroke: { width: 3, curve: 'straight', dashArray: chosen ? [0, 6] : [0] },
      markers: { size: 5 },
      dataLabels: {
        enabled: true,
        formatter: (v: number) => `${v.toFixed(1)}%`,
        background: { enabled: false },
        offsetY: -8,
      },
      xaxis: { categories: trend.elections.map((e) => String(e.year)) },
      yaxis: { min: 0, max: 100, labels: { formatter: (v: number) => `${v}%` } },
      grid: { borderColor: CHART_GRID },
      legend: { position: 'top', horizontalAlign: 'left' },
      tooltip: {
        theme: isDarkScheme() ? 'dark' : 'light',
        y: { formatter: (v: number) => `${v.toFixed(1)} %` },
      },
    };
  });

  /** Una elección: cada municipio como barra al 100 %, repartida por grupo. */
  protected resultChart = computed<ChartOptions | null>(() => {
    const report = this.report();
    if (!report?.territories.length) {
      return null;
    }
    const rows = [...report.territories].sort(
      (a, b) => (b.groups[0]?.pct ?? 0) - (a.groups[0]?.pct ?? 0)
    );
    const groups = report.state.groups.slice(0, 6);
    const pctOf = (t: TerritoryResult, code: string) =>
      t.groups.find((g) => g.code === code)?.pct ?? 0;
    const series = groups.map((g) => ({ name: g.label, data: rows.map((m) => pctOf(m, g.code)) }));
    series.push({
      name: 'Otros, nulos y no registrados',
      data: rows.map((m) =>
        Math.max(0, 100 - groups.reduce((sum, g) => sum + pctOf(m, g.code), 0))
      ),
    });
    return {
      series,
      colors: [...groups.map((g) => g.color ?? NEUTRAL), '#D4D4D4'],
      chart: {
        type: 'bar',
        stacked: true,
        stackType: '100%',
        height: 40 + rows.length * 26,
        toolbar: { show: false },
        foreColor: CHART_FORE,
        fontFamily: 'inherit',
      },
      plotOptions: { bar: { horizontal: true, barHeight: '70%' } },
      dataLabels: {
        enabled: true,
        formatter: (v: number) => (v >= 12 ? `${v.toFixed(0)}%` : ''),
        style: { fontSize: '10px' },
      },
      xaxis: { categories: rows.map((m) => m.name) },
      grid: { borderColor: CHART_GRID },
      legend: { position: 'top', horizontalAlign: 'left' },
      tooltip: {
        theme: isDarkScheme() ? 'dark' : 'light',
        y: { formatter: (v: number) => `${v.toFixed(1)} %` },
      },
    };
  });

  /** El municipio abierto y sus secciones. */
  protected openMunicipality = signal<number | null>(null);
  protected sections = signal<SectionReport | null>(null);
  protected sectionsLoading = signal(false);

  /** Los cargos que tienen al menos una elección cargada, en el orden de OFFICE_LABEL. */
  protected offices = computed(() => {
    const loaded = new Set(this.elections().map((e) => e.office));
    return (Object.keys(OFFICE_LABEL) as Office[])
      .filter((o) => loaded.has(o))
      .map((o) => ({ value: o, label: OFFICE_LABEL[o] }));
  });

  protected years = computed(() =>
    this.elections()
      .filter((e) => e.office === this.office())
      .map((e) => e.year)
      .sort((a, b) => b - a)
  );

  protected state = computed(() => this.report()?.state ?? null);

  /** La leyenda: los grupos del estado, en el orden en que quedaron. */
  protected legend = computed(() => this.state()?.groups ?? []);

  protected sorted = computed(() => {
    const rows = [...(this.report()?.territories ?? [])];
    const key = this.sortKey();
    const dir = this.sortDesc() ? -1 : 1;
    const value = (r: TerritoryResult): number | string | null => {
      switch (key) {
        case 'name':
          return r.name;
        case 'winnerPct':
          return r.groups[0]?.pct ?? null;
        default:
          return r[key];
      }
    };
    return rows.sort((a, b) => {
      const x = value(a);
      const y = value(b);
      if (typeof x === 'string' && typeof y === 'string') {
        return x.localeCompare(y, 'es') * dir;
      }
      if (x === null) return 1;
      if (y === null) return -1;
      return ((x as number) - (y as number)) * dir;
    });
  });

  constructor() {
    this.loadElections();
  }

  /** Otro estado: otras elecciones (las locales cambian) y todo se vuelve a pedir. */
  protected changeState(): void {
    this.report.set(null);
    this.trend.set(null);
    this.trendMunicipality.set(null);
    if (this.view() === 'analysis') {
      this.view.set('election');
    }
    this.loadElections();
  }

  private loadElections(): void {
    this.loading.set(true);
    this.service
      .getElections()
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((elections) => {
        this.elections.set(elections ?? []);
        if (!elections?.length) {
          this.loading.set(false);
          if (!elections) {
            showError('No se pudo leer el histórico electoral');
          }
          return;
        }
        // se queda con el cargo y el año elegidos si el estado los tiene; si no,
        // la elección más reciente del primer cargo con datos
        const office = this.offices().find((o) => o.value === this.office())?.value;
        this.office.set(office ?? this.offices()[0].value);
        if (!this.years().includes(this.year() ?? -1)) {
          this.year.set(this.years()[0]);
        }
        this.load();
        if (this.compare()) {
          this.loadTrend();
        }
      });
  }

  // ======================================================================
  // filtros
  // ======================================================================

  protected setOffice(office: Office): void {
    this.office.set(office);
    // si el año elegido no existe para este cargo, el más reciente
    if (!this.years().includes(this.year() ?? -1)) {
      this.year.set(this.years()[0]);
    }
    this.load();
    if (this.compare()) {
      this.loadTrend();
    }
  }

  protected setYear(year: number): void {
    this.year.set(year);
    this.load();
  }

  protected setGroupBy(groupBy: GroupBy): void {
    this.groupBy.set(groupBy);
    this.load();
    if (this.compare()) {
      this.loadTrend();
    }
  }

  /**
   * Cambia de pestaña. Al comparar años no se puede agrupar por candidatura:
   * las coaliciones cambian de un año a otro, así que se pasa a bloque.
   */
  protected setView(view: View): void {
    if (this.view() === view) {
      return;
    }
    this.view.set(view);
    if (view === 'compare') {
      if (this.groupBy() === 'candidacy') {
        this.groupBy.set('bloc');
        this.load();
      }
      this.loadTrend();
    }
  }

  /** Cambia el reparto; el territorio abierto ya no existe en el nuevo nivel. */
  protected setLevel(level: TerritoryLevel): void {
    if (this.level() === level) {
      return;
    }
    this.level.set(level);
    this.openMunicipality.set(null);
    this.sections.set(null);
    this.trendMunicipality.set(null);
    this.load();
    if (this.compare()) {
      this.loadTrend();
    }
  }

  protected setFocus(code: string): void {
    this.focus.set(code);
  }

  protected showInTrend(code: number): void {
    this.trendMunicipality.set(this.trendMunicipality() === code ? null : code);
  }

  private loadTrend(): void {
    const office = this.office();
    if (!office) {
      return;
    }
    this.trendLoading.set(true);
    this.service
      .getTrend(office, this.groupBy(), this.level())
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((trend) => {
        this.trendLoading.set(false);
        if (!trend) {
          showError('No se pudo leer la comparación entre años');
          return;
        }
        this.trend.set(trend);
        // si el foco no existe en este cargo/agrupación, el bloque 4T o el primero
        const codes = trend.state.points.flatMap((p) => p.groups.map((g) => g.code));
        if (!codes.includes(this.focus())) {
          this.focus.set(codes.includes(DEFAULT_FOCUS) ? DEFAULT_FOCUS : (codes[0] ?? DEFAULT_FOCUS));
        }
      });
  }

  protected focusPct(point: TrendPoint | undefined): number {
    return point?.groups.find((g) => g.code === this.focus())?.pct ?? 0;
  }

  /** Puntos que ganó o perdió el foco entre la primera y la última elección. */
  protected delta(row: TrendRow): number | null {
    if (row.points.length < 2) {
      return null;
    }
    return this.focusPct(row.points[row.points.length - 1]) - this.focusPct(row.points[0]);
  }

  protected deltaClass(delta: number | null): string {
    if (delta === null || Math.abs(delta) < 0.05) {
      return 'text-neutral-500';
    }
    return delta > 0
      ? 'text-emerald-600 dark:text-emerald-400'
      : 'text-red-600 dark:text-red-400';
  }

  protected sortTrendBy(key: TrendSortKey): void {
    if (this.trendSortKey() === key) {
      this.trendSortDesc.update((d) => !d);
    } else {
      this.trendSortKey.set(key);
      this.trendSortDesc.set(key !== 'name');
    }
  }

  protected trendSortIcon(key: TrendSortKey): string {
    if (this.trendSortKey() !== key) {
      return '';
    }
    return this.trendSortDesc() ? 'arrow-down' : 'arrow-up';
  }

  private load(): void {
    const office = this.office();
    const year = this.year();
    if (!office || !year) {
      return;
    }
    this.loading.set(true);
    this.service
      .getByTerritory(year, office, this.level(), this.groupBy())
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((report) => {
        this.loading.set(false);
        if (!report) {
          showError('No se pudo leer la elección');
          return;
        }
        this.report.set(report);
        // el municipio abierto se vuelve a pedir con la nueva elección
        const open = this.openMunicipality();
        if (open !== null) {
          this.loadSections(open);
        }
      });
  }

  // ======================================================================
  // secciones
  // ======================================================================

  protected toggle(municipality: TerritoryResult): void {
    if (this.openMunicipality() === municipality.code) {
      this.openMunicipality.set(null);
      this.sections.set(null);
      return;
    }
    this.openMunicipality.set(municipality.code);
    this.loadSections(municipality.code);
  }

  private loadSections(code: number): void {
    const office = this.office();
    const year = this.year();
    if (!office || !year) {
      return;
    }
    this.sectionsLoading.set(true);
    this.sections.set(null);
    this.service
      .getBySection(year, office, this.level(), code, this.groupBy())
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((report) => {
        this.sectionsLoading.set(false);
        if (!report) {
          showError('No se pudieron leer las secciones');
          return;
        }
        this.sections.set(report);
      });
  }

  // ======================================================================
  // orden
  // ======================================================================

  protected sortBy(key: SortKey): void {
    if (this.sortKey() === key) {
      this.sortDesc.update((d) => !d);
    } else {
      this.sortKey.set(key);
      this.sortDesc.set(key !== 'name');
    }
  }

  protected sortIcon(key: SortKey): string {
    if (this.sortKey() !== key) {
      return '';
    }
    return this.sortDesc() ? 'arrow-down' : 'arrow-up';
  }

  protected ariaSort(key: SortKey): string {
    if (this.sortKey() !== key) {
      return 'none';
    }
    return this.sortDesc() ? 'descending' : 'ascending';
  }

  // ======================================================================
  // lectura
  // ======================================================================

  protected officeLabel(office: Office | null): string {
    return office ? OFFICE_LABEL[office] : '';
  }

  /** Los primeros grupos y el resto junto, para la barra apilada. */
  protected bar(groups: GroupVotes[]): { label: string; pct: number; color: string }[] {
    const top = groups.slice(0, BAR_GROUPS).map((g) => ({
      label: g.label,
      pct: g.pct,
      color: g.color ?? NEUTRAL,
    }));
    const rest = groups.slice(BAR_GROUPS).reduce((sum, g) => sum + g.pct, 0);
    return rest > 0 ? [...top, { label: 'Otros', pct: rest, color: NEUTRAL }] : top;
  }

  protected barTooltip(groups: GroupVotes[]): string {
    return groups.map((g) => `${g.label}: ${g.pct.toFixed(1)} %`).join(' · ');
  }

  protected group(result: TerritoryResult, code: string | null): GroupVotes | null {
    return result.groups.find((g) => g.code === code) ?? null;
  }

  protected color(group: GroupVotes | null): string {
    return group?.color ?? NEUTRAL;
  }

  protected percent(value: number | null | undefined): string {
    return value === null || value === undefined ? '—' : `${value.toFixed(1)} %`;
  }
}
