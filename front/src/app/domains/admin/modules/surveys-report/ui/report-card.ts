import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** Tarjeta de avance de censo (meta / capturadas / por capturar + %). */
@Component({
  selector: 'app-report-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe],
  template: `
    <div
      class="flex flex-col gap-2 rounded-xl border border-neutral-200 p-4 dark:border-neutral-800"
      [class.bg-neutral-50]="main()"
      [class.dark:bg-neutral-800]="main()"
    >
      <div class="text-sm font-bold tracking-tight">{{ title() }}</div>
      <div class="flex items-end justify-between">
        <div class="grid gap-0.5 text-xs text-neutral-500">
          <span>Meta: <b class="text-neutral-700 dark:text-neutral-200">{{ goal() | number }}</b></span>
          <span>Capturadas: <b class="text-emerald-600">{{ captured() | number }}</b></span>
          <span>Por capturar: <b class="text-amber-600">{{ toCapture() | number }}</b></span>
        </div>
        <div
          class="flex h-16 w-16 items-center justify-center rounded-full text-lg font-bold"
          [class]="
            percent() >= 80
              ? 'bg-emerald-100 text-emerald-700'
              : percent() >= 40
                ? 'bg-amber-100 text-amber-700'
                : 'bg-red-100 text-red-700'
          "
        >
          {{ percent() }}%
        </div>
      </div>
    </div>
  `,
})
export class ReportCard {
  title = input('');
  goal = input(0);
  captured = input(0);
  toCapture = input(0);
  main = input(false);

  protected percent = computed(() => (this.goal() ? Math.round((this.captured() * 100) / this.goal()) : 0));
}
