import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexGrid,
  ApexLegend,
  ApexMarkers,
  ApexNonAxisChartSeries,
  ApexPlotOptions,
  ApexStroke,
  ApexTooltip,
  ApexXAxis,
  ApexYAxis,
  ChartComponent,
} from 'ng-apexcharts';

/** Lo que necesita una gráfica; lo que no venga se queda con el default de ApexCharts. */
export interface ChartOptions {
  series: ApexAxisChartSeries | ApexNonAxisChartSeries;
  chart: ApexChart;
  xaxis?: ApexXAxis;
  yaxis?: ApexYAxis | ApexYAxis[];
  colors?: string[];
  stroke?: ApexStroke;
  dataLabels?: ApexDataLabels;
  legend?: ApexLegend;
  tooltip?: ApexTooltip;
  plotOptions?: ApexPlotOptions;
  markers?: ApexMarkers;
  grid?: ApexGrid;
}

/** Texto y rejilla neutros: se leen igual en claro y en oscuro. */
export const CHART_FORE = '#8a8a8a';
export const CHART_GRID = 'rgba(138, 138, 138, 0.18)';

/** Si el tema actual es oscuro (para el tooltip de ApexCharts, que no lo detecta solo). */
export const isDarkScheme = (): boolean =>
  typeof document !== 'undefined' &&
  document.documentElement.classList.contains('scheme-dark');

/**
 * Tarjeta con una gráfica de ApexCharts: título, subtítulo opcional y la
 * gráfica, o un aviso si todavía no hay datos. Las opciones las arma la
 * pantalla; aquí sólo se pintan.
 */
@Component({
  selector: 'app-chart-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block' },
  imports: [ChartComponent],
  template: `
    <section
      class="anim-rise flex h-full flex-col rounded-xl border border-neutral-200 bg-card p-5 dark:border-neutral-800"
    >
      @if (heading()) {
        <header class="mb-3">
          <h3 class="text-sm font-semibold">{{ heading() }}</h3>
          @if (subtitle()) {
            <p class="text-xs text-neutral-500">{{ subtitle() }}</p>
          }
        </header>
      }
      @if (options(); as o) {
        <apx-chart
          class="block min-h-0 flex-auto"
          [series]="o.series"
          [chart]="o.chart"
          [xaxis]="o.xaxis!"
          [yaxis]="o.yaxis!"
          [colors]="o.colors!"
          [stroke]="o.stroke!"
          [dataLabels]="o.dataLabels!"
          [legend]="o.legend!"
          [tooltip]="o.tooltip!"
          [plotOptions]="o.plotOptions!"
          [markers]="o.markers!"
          [grid]="o.grid!"
        />
      } @else {
        <div class="flex flex-auto items-center justify-center p-8 text-sm text-neutral-400">
          {{ emptyText() }}
        </div>
      }
    </section>
  `,
})
export class ChartCard {
  heading = input('');
  subtitle = input('');
  options = input<ChartOptions | null>(null);
  emptyText = input('Sin datos para graficar.');
}
