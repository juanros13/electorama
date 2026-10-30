import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  signal,
  untracked,
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export type StatAccent =
  | 'primary'
  | 'secondary'
  | 'emerald'
  | 'amber'
  | 'red'
  | 'neutral';

const CHIP: Record<StatAccent, string> = {
  primary: 'bg-primary-100 text-primary-700 dark:bg-primary-500/20 dark:text-primary-300',
  secondary: 'bg-secondary-100 text-secondary-700 dark:bg-secondary-500/20 dark:text-secondary-300',
  emerald: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-300',
  amber: 'bg-amber-100 text-amber-700 dark:bg-amber-500/20 dark:text-amber-300',
  red: 'bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-300',
  neutral: 'bg-neutral-200 text-neutral-700 dark:bg-neutral-700 dark:text-neutral-200',
};

/** Lo que tarda el conteo de un valor al siguiente. */
const COUNT_MS = 900;

/** Arranca rápido y frena al llegar: el número "aterriza" en el valor. */
const easeOutCubic = (t: number): number => 1 - Math.pow(1 - t, 3);

/**
 * Tarjeta de métrica (valor + label + icono opcional con acento de color).
 *
 * Cuando el valor es numérico y cambia, cuenta desde el que se ve hasta el
 * nuevo en vez de saltar. Los dígitos son de ancho fijo (`tabular-nums`) para
 * que el número no baile mientras cuenta. Con "reducir movimiento" salta directo.
 */
@Component({
  selector: 'app-stat-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block' },
  imports: [MatIconModule],
  template: `
    <div
      class="anim-rise flex h-full items-center gap-4 rounded-xl border border-neutral-200 bg-card p-5 transition-shadow hover:shadow-md dark:border-neutral-800"
    >
      @if (icon()) {
        <div
          class="flex size-12 shrink-0 items-center justify-center rounded-lg"
          [class]="chip()"
        >
          <mat-icon [svgIcon]="icon()" />
        </div>
      }
      <div class="min-w-0 flex-1">
        @if (isText()) {
          <!-- un nombre (p. ej. una candidatura con su candidata) no cabe a
               tamaño de número: más chico y completo, en las líneas que ocupe -->
          <div class="font-display text-lg leading-snug font-bold break-words">
            {{ display() }}
          </div>
        } @else {
          <div
            class="font-display text-3xl leading-none font-bold tabular-nums transition-transform duration-300"
            [class.scale-105]="counting()"
          >
            {{ display() }}
          </div>
        }
        <div class="mt-1 truncate text-sm text-neutral-500" [attr.title]="label()">
          {{ label() }}
        </div>
      </div>
    </div>
  `,
})
export class StatCard {
  label = input('');
  value = input<string | number | null>(null);
  icon = input('');
  accent = input<StatAccent>('primary');

  protected chip = computed(() => CHIP[this.accent()]);

  /**
   * Un nombre, no una cifra: se pinta más chico. "57.8 %" o "12.3 pp" llegan
   * como texto pero son cifras y se quedan grandes.
   */
  protected isText = computed(() => {
    const v = this.value();
    return typeof v === 'string' && !/^[\d.,\s%+\-−]*(pp)?$/i.test(v.trim());
  });

  /** El número que se ve ahora; va alcanzando a `value()` cuadro a cuadro. */
  private shown = signal(0);
  protected counting = signal(false);
  private frame: number | null = null;

  protected display = computed(() => {
    const v = this.value();
    if (v === null || v === undefined) {
      return '—';
    }
    return typeof v === 'number'
      ? Math.round(this.shown()).toLocaleString('es-MX')
      : v;
  });

  constructor() {
    effect(() => {
      const target = this.value();
      if (typeof target === 'number') {
        untracked(() => this.countTo(target));
      }
    });
    inject(DestroyRef).onDestroy(() => this.stop());
  }

  private countTo(target: number): void {
    this.stop();
    const from = this.shown();
    const reduced =
      typeof matchMedia === 'function' &&
      matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (from === target || reduced || typeof requestAnimationFrame !== 'function') {
      this.shown.set(target);
      return;
    }

    this.counting.set(true);
    const start = performance.now();
    const step = (now: number) => {
      const t = Math.min((now - start) / COUNT_MS, 1);
      this.shown.set(from + (target - from) * easeOutCubic(t));
      if (t < 1) {
        this.frame = requestAnimationFrame(step);
      } else {
        this.frame = null;
        this.counting.set(false);
      }
    };
    this.frame = requestAnimationFrame(step);
  }

  private stop(): void {
    if (this.frame !== null) {
      cancelAnimationFrame(this.frame);
      this.frame = null;
    }
    this.counting.set(false);
  }
}
