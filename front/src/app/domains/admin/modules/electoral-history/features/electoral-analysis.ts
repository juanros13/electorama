import {
  CHART_FORE,
  CHART_GRID,
  ChartCard,
  ChartOptions,
  isDarkScheme,
} from '@/app/shared/ui/chart-card';
import { StatCard } from '@/app/shared/ui/stat-card';
import { TableShell } from '@/app/shared/ui/table-shell';
import { showError } from '@/app/shared/ui/swal';
import { DecimalPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { catchError, of } from 'rxjs';
import { ElectoralHistoryService } from '../data/electoral-history.service';
import { AnalysisReport, RiskStatus } from '../data/electoral-history.types';
import { SectionWatch } from './section-watch';

/** El año que se analiza: el último con ayuntamientos y diputaciones locales. */
const YEAR = 2024;

const STATUS: Record<RiskStatus, { label: string; chip: string; bar: string }> = {
  LOST: {
    label: 'Perdido',
    chip: 'bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-300',
    bar: 'bg-red-500',
  },
  CLOSE: {
    label: 'Cerrado',
    chip: 'bg-orange-100 text-orange-700 dark:bg-orange-500/20 dark:text-orange-300',
    bar: 'bg-orange-500',
  },
  COMPETITIVE: {
    label: 'Competido',
    chip: 'bg-amber-100 text-amber-700 dark:bg-amber-500/20 dark:text-amber-300',
    bar: 'bg-amber-400',
  },
  SAFE: {
    label: 'Seguro',
    chip: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-300',
    bar: 'bg-emerald-500',
  },
};

/** "EMILIANO ZAPATA" → "Emiliano Zapata"; los distritos se dejan como vienen. */
export const titleCase = (name: string): string =>
  name.startsWith('DISTRITO')
    ? name
    : name
        .toLowerCase()
        .split(' ')
        .map((w) => (w.length <= 2 ? w : w[0].toUpperCase() + w.slice(1)))
        .join(' ');

/**
 * Qué hay que vigilar de la última elección rumbo a la siguiente, para
 * dirección. Se enfoca en lo que se vuelve a elegir (ayuntamientos y
 * diputaciones locales) y usa la gubernatura (o la presidencia) como la
 * «marca»: cuánto se quedó cada candidato local debajo de ella.
 *
 * Todo sale ya calculado de msvc-electoral-history (/v1/analysis/{año}),
 * incluidos los hallazgos: aquí sólo se pintan.
 */
@Component({
  selector: 'app-electoral-analysis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'flex flex-col gap-6' },
  templateUrl: './electoral-analysis.html',
  imports: [
    ChartCard,
    DecimalPipe,
    MatIconModule,
    MatTooltipModule,
    SectionWatch,
    StatCard,
    TableShell,
  ],
})
export class ElectoralAnalysis implements OnInit {
  private service = inject(ElectoralHistoryService);
  private destroyRef = inject(DestroyRef);

  protected readonly year = YEAR;
  protected readonly status = STATUS;
  protected readonly statuses = Object.keys(STATUS) as RiskStatus[];
  protected readonly title = titleCase;

  protected analysis = signal<AnalysisReport | null>(null);
  protected loading = signal(false);

  protected kpis = computed(() => this.analysis()?.kpis ?? null);
  protected brandName = computed(() =>
    this.analysis()?.brand.office === 'PRESIDENT' ? 'Presidencia' : 'Gubernatura'
  );

  /** Barras de la marca contra el candidato a ayuntamiento, de mayor a menor brecha. */
  protected gapChart = computed<ChartOptions | null>(() => {
    const a = this.analysis();
    if (!a) {
      return null;
    }
    const rows = [...a.municipalities]
      .filter((m) => m.brandGapPp != null)
      .sort((x, y) => (y.brandGapPp ?? 0) - (x.brandGapPp ?? 0));
    return {
      series: [
        { name: `${this.brandName()} (marca)`, data: rows.map((m) => m.brand4tPct ?? 0) },
        { name: 'Ayuntamiento', data: rows.map((m) => m.bloc4tPct ?? 0) },
      ],
      colors: ['#9D2449', '#D9A7B6'],
      chart: {
        type: 'bar',
        height: 320,
        toolbar: { show: false },
        foreColor: CHART_FORE,
        fontFamily: 'inherit',
      },
      plotOptions: { bar: { columnWidth: '70%', borderRadius: 2 } },
      dataLabels: { enabled: false },
      xaxis: {
        categories: rows.map((m) => titleCase(m.name)),
        labels: { rotate: -45, trim: false },
      },
      yaxis: { min: 0, max: 100, labels: { formatter: (v: number) => `${v}%` } },
      grid: { borderColor: CHART_GRID },
      legend: { position: 'top', horizontalAlign: 'left' },
      tooltip: {
        theme: isDarkScheme() ? 'dark' : 'light',
        shared: true,
        intersect: false,
        y: { formatter: (v: number) => `${v.toFixed(1)} %` },
      },
    };
  });

  /** Votos de cada rival en ayuntamientos, elección por elección. */
  protected rivalsChart = computed<ChartOptions | null>(() => {
    const a = this.analysis();
    if (!a?.rivals.length) {
      return null;
    }
    return {
      series: a.rivals.map((r) => ({ name: r.label, data: r.points.map((p) => p.votes) })),
      colors: a.rivals.map((r) => r.color ?? '#A3A3A3'),
      chart: {
        type: 'line',
        height: 300,
        toolbar: { show: false },
        zoom: { enabled: false },
        foreColor: CHART_FORE,
        fontFamily: 'inherit',
      },
      stroke: { width: 3, curve: 'straight' },
      markers: { size: 5 },
      dataLabels: { enabled: false },
      xaxis: { categories: a.rivals[0].points.map((p) => String(p.year)) },
      yaxis: { labels: { formatter: (v: number) => `${Math.round(v / 1000)} mil` } },
      grid: { borderColor: CHART_GRID },
      legend: { position: 'top', horizontalAlign: 'left' },
      tooltip: {
        theme: isDarkScheme() ? 'dark' : 'light',
        y: { formatter: (v: number) => `${v.toLocaleString('es-MX')} votos` },
      },
    };
  });

  ngOnInit(): void {
    this.loading.set(true);
    this.service
      .getAnalysis(YEAR)
      .pipe(
        catchError(() => {
          showError('No se pudo cargar el análisis');
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((a) => {
        this.analysis.set(a);
        this.loading.set(false);
      });
  }

  protected pp(value: number | null | undefined): string {
    if (value == null) {
      return '—';
    }
    return `${value > 0 ? '+' : value < 0 ? '−' : ''}${Math.abs(value).toFixed(1)} pp`;
  }

  /** La cabecera de un distrito: "DISTRITO LOCAL 04 · CENTRO" → "Centro". */
  protected seat(name: string): string {
    const at = name.indexOf(' · ');
    return at < 0 ? '' : titleCase(name.slice(at + 3));
  }
}
