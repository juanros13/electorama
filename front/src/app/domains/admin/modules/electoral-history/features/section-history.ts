import { PageHeader } from '@/app/shared/ui/page-header';
import { StatCard } from '@/app/shared/ui/stat-card';
import { ChartCard, CHART_FORE, CHART_GRID, ChartOptions, isDarkScheme } from '@/app/shared/ui/chart-card';
import { DatePipe, DecimalPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { catchError, map, of, switchMap } from 'rxjs';
import { CaptureService, CaptureTarget } from '../../structure/data/capture.service';
import { TerritoryService } from '../../structure/data/territory.service';
import { ElectoralHistoryService } from '../data/electoral-history.service';
import {
  GroupBy,
  GroupVotes,
  Office,
  SectionElection,
  SectionHistory,
} from '../data/electoral-history.types';

const STATE_ID = 27;

const OFFICE_LABEL: Record<Office, string> = {
  PRESIDENT: 'Presidencia',
  SENATOR: 'Senaduría',
  GOVERNOR: 'Gubernatura',
  FEDERAL_DEPUTY: 'Diputación federal',
  LOCAL_DEPUTY: 'Diputación local',
  MAYOR: 'Presidencia municipal',
};

/** El orden en que se leen los cargos dentro de un año: de lo nacional a lo local. */
const OFFICE_ORDER: Office[] = [
  'PRESIDENT',
  'SENATOR',
  'GOVERNOR',
  'FEDERAL_DEPUTY',
  'LOCAL_DEPUTY',
  'MAYOR',
];

const OFFICE_COLOR: Record<Office, string> = {
  PRESIDENT: '#9D2449',
  SENATOR: '#C2185B',
  GOVERNOR: '#1789B0',
  FEDERAL_DEPUTY: '#5E35B1',
  LOCAL_DEPUTY: '#00897B',
  MAYOR: '#F28C28',
};

interface SectionOption {
  section: number;
  /** De dónde sale: su puesto, uno de su rama o el catálogo de su municipio. */
  hint: string;
}

/**
 * El histórico electoral de la sección de quien trabaja la estructura: cómo
 * votó en 2018, 2021 y 2024, cargo por cargo. Las secciones salen de sus
 * puestos (capture-targets); el referente, sin sección propia, elige entre las
 * de su municipio.
 */
@Component({
  selector: 'app-section-history',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './section-history.html',
  imports: [ChartCard, DatePipe, DecimalPipe, PageHeader, StatCard],
})
export default class SectionHistoryView {
  private api = inject(ElectoralHistoryService);
  private capture = inject(CaptureService);
  private territory = inject(TerritoryService);
  private destroyRef = inject(DestroyRef);

  protected readonly officeLabel = OFFICE_LABEL;

  protected options = signal<SectionOption[]>([]);
  protected optionsLoading = signal(true);
  protected section = signal<number | null>(null);
  protected groupBy = signal<GroupBy>('bloc');

  protected history = signal<SectionHistory | null>(null);
  protected loading = signal(false);
  protected error = signal('');

  /** Las elecciones por año, de la más reciente a la más antigua, y cada año de lo nacional a lo local. */
  protected years = computed(() => {
    const byYear = new Map<number, SectionElection[]>();
    for (const e of this.history()?.elections ?? []) {
      byYear.set(e.year, [...(byYear.get(e.year) ?? []), e]);
    }
    return [...byYear.entries()]
      .sort((a, b) => b[0] - a[0])
      .map(([year, elections]) => ({
        year,
        elections: [...elections].sort(
          (a, b) => OFFICE_ORDER.indexOf(a.office) - OFFICE_ORDER.indexOf(b.office)
        ),
      }));
  });

  /** La elección más reciente de mayor alcance: la de las tarjetas de arriba. */
  protected headline = computed(() => this.years()[0]?.elections[0] ?? null);

  protected trendChart = computed<ChartOptions | null>(() => {
    const h = this.history();
    if (!h || this.groupBy() !== 'bloc') {
      return null;
    }
    const years = [...new Set(h.elections.map((e) => e.year))].sort();
    const series = OFFICE_ORDER.map((office) => ({
      name: OFFICE_LABEL[office],
      data: years.map((y) => {
        const e = h.elections.find((x) => x.year === y && x.office === office);
        return e ? (e.groups.find((g) => g.code === 'MORENA_ALLIES')?.pct ?? null) : null;
      }),
    })).filter((s) => s.data.some((v) => v !== null));
    if (!series.length) {
      return null;
    }
    return {
      series,
      colors: OFFICE_ORDER.filter((o) => series.some((s) => s.name === OFFICE_LABEL[o])).map(
        (o) => OFFICE_COLOR[o]
      ),
      chart: {
        type: 'line',
        height: 260,
        toolbar: { show: false },
        zoom: { enabled: false },
        foreColor: CHART_FORE,
        fontFamily: 'inherit',
      },
      stroke: { width: 3, curve: 'straight' },
      markers: { size: 5, strokeWidth: 0 },
      dataLabels: { enabled: false },
      xaxis: { categories: years.map(String) },
      yaxis: { min: 0, max: 100, labels: { formatter: (v: number) => `${Math.round(v)}%` } },
      grid: { borderColor: CHART_GRID },
      legend: { position: 'top', horizontalAlign: 'left' },
      tooltip: {
        theme: isDarkScheme() ? 'dark' : 'light',
        y: { formatter: (v: number | null) => (v == null ? 'sin elección' : `${v}%`) },
      },
    };
  });

  constructor() {
    // las secciones: las de sus puestos; si ninguno es de sección (referente), las de su municipio
    this.capture
      .getTargets()
      .pipe(
        switchMap((targets) => {
          const own = sectionOptions(targets);
          if (own.length) {
            return of(own);
          }
          const mun = targets.find((t) => t.own)?.municipalityGeoId ?? targets[0]?.municipalityGeoId;
          if (!mun) {
            return of([] as SectionOption[]);
          }
          return this.territory.getSections(STATE_ID, { municipalityId: mun, status: true }, 0, 500).pipe(
            map((page) =>
              page.content
                .map((s) => ({ section: +s.section, hint: 'de tu municipio' }))
                .sort((a, b) => a.section - b.section)
            )
          );
        }),
        catchError(() => of([] as SectionOption[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((opts) => {
        this.options.set(opts);
        this.optionsLoading.set(false);
        this.section.set(opts[0]?.section ?? null);
      });

    toObservable(computed(() => ({ s: this.section(), g: this.groupBy() })))
      .pipe(
        switchMap(({ s, g }) => {
          if (s == null) {
            return of(null);
          }
          this.loading.set(true);
          this.error.set('');
          return this.api.getSectionHistory(s, g).pipe(
            catchError((e) => {
              this.error.set(
                e?.status === 404
                  ? `La sección ${s} no aparece en las elecciones cargadas`
                  : 'No se pudo leer el histórico de la sección'
              );
              return of(null);
            })
          );
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((h) => {
        this.history.set(h);
        this.loading.set(false);
      });
  }

  protected selectSection(value: string): void {
    this.section.set(Number(value));
  }

  /** El grupo ganador de una elección (para el chip). */
  protected winnerOf(e: SectionElection): GroupVotes | null {
    return e.groups.length ? e.groups[0] : null;
  }
}

/** "S-0233A1-11" → 233: las secciones de sus puestos, la propia primero y sin repetir. */
function sectionOptions(targets: CaptureTarget[]): SectionOption[] {
  const out: SectionOption[] = [];
  const seen = new Set<number>();
  for (const t of [...targets].sort((a, b) => Number(b.own) - Number(a.own))) {
    const m = /S-(\d{4})/.exec(t.label);
    if (!m) {
      continue;
    }
    const section = +m[1];
    if (!seen.has(section)) {
      seen.add(section);
      out.push({ section, hint: t.own ? 'tu sección' : `de ${t.label}` });
    }
  }
  return out;
}
