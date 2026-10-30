import { LEVEL_NAMES } from '@/app/core/data/levels';
import { PageHeader } from '@/app/shared/ui/page-header';
import { StatCard } from '@/app/shared/ui/stat-card';
import { showError } from '@/app/shared/ui/swal';
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
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { catchError, debounceTime, of, switchMap } from 'rxjs';
import {
  CaptureService,
  CaptureTarget,
  SupporterFilter,
  SupporterListRow,
  SupporterPage,
  SupporterSummary,
} from '../data/capture.service';

const PAGE_SIZE = 25;

type YesNoAll = 'all' | 'yes' | 'no';

/**
 * Los convencidos del movilizador (y de quien tiene gente a cargo, de toda su
 * rama): tarjetas con la meta arriba, buscar por nombre, CURP o teléfono, y
 * filtrar por simpatía, encuesta, ubicación y fecha de captura.
 */
@Component({
  selector: 'app-supporter-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './supporter-list.html',
  imports: [DatePipe, DecimalPipe, FormsModule, MatButtonModule, MatIconModule, PageHeader, RouterLink, StatCard],
})
export default class SupporterList {
  private api = inject(CaptureService);
  private destroyRef = inject(DestroyRef);

  // ─────────────────────────── de qué puesto

  protected targets = signal<CaptureTarget[]>([]);
  protected targetsLoading = signal(true);
  protected targetId = signal<number | null>(null);
  protected target = computed(
    () => this.targets().find((t) => t.positionId === this.targetId()) ?? null
  );

  // ─────────────────────────── filtros

  protected q = signal('');
  protected supports = signal<YesNoAll>('all');
  protected committee = signal<YesNoAll>('all');
  protected pollingRep = signal<YesNoAll>('all');
  protected withLocation = signal<YesNoAll>('all');
  protected from = signal('');
  protected to = signal('');
  protected page = signal(0);

  protected filter = computed<SupporterFilter>(() => ({
    q: this.q().trim() || undefined,
    supports: this.supports() === 'all' ? null : this.supports() === 'yes' ? 'SI' : 'NO',
    committee: toBool(this.committee()),
    pollingRep: toBool(this.pollingRep()),
    withLocation: toBool(this.withLocation()),
    from: this.from() || null,
    to: this.to() || null,
  }));
  protected filtering = computed(() => {
    const f = this.filter();
    return !!(f.q || f.supports || f.committee != null || f.pollingRep != null || f.withLocation != null || f.from || f.to);
  });

  // ─────────────────────────── datos

  protected summary = signal<SupporterSummary | null>(null);
  protected result = signal<SupporterPage>({ content: [], total: 0 });
  protected loading = signal(false);

  protected pages = computed(() => Math.max(1, Math.ceil(this.result().total / PAGE_SIZE)));
  protected progress = computed(() => {
    const s = this.summary();
    return s && s.goal ? Math.round((s.total / s.goal) * 100) : null;
  });
  protected missing = computed(() => {
    const s = this.summary();
    return s && s.goal ? Math.max(0, s.goal - s.total) : null;
  });

  constructor() {
    this.api
      .getTargets()
      .pipe(
        catchError(() => {
          showError('No se pudo saber de qué puestos puedes ver convencidos');
          return of([] as CaptureTarget[]);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((targets) => {
        this.targets.set(targets);
        this.targetsLoading.set(false);
        // por omisión, su puesto de más arriba: ve toda su rama
        this.targetId.set(targets.find((t) => t.own)?.positionId ?? targets[0]?.positionId ?? null);
      });

    // las tarjetas: cambian con el puesto
    toObservable(this.targetId)
      .pipe(
        switchMap((id) => (id ? this.api.summary(id).pipe(catchError(() => of(null))) : of(null))),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((s) => this.summary.set(s));

    // el listado: cambia con el puesto, los filtros y la página (la búsqueda espera a que dejes de escribir)
    toObservable(computed(() => ({ id: this.targetId(), f: this.filter(), page: this.page() })))
      .pipe(
        debounceTime(250),
        switchMap(({ id, f, page }) => {
          if (!id) {
            return of({ content: [], total: 0 } as SupporterPage);
          }
          this.loading.set(true);
          return this.api
            .search(id, f, page, PAGE_SIZE)
            .pipe(catchError(() => of({ content: [], total: 0 } as SupporterPage)));
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((r) => {
        this.result.set(r);
        this.loading.set(false);
      });
  }

  protected levelLabel(t: CaptureTarget): string {
    return LEVEL_NAMES[t.level]?.one ?? t.level;
  }

  /** Cualquier filtro nuevo regresa a la primera página. */
  protected setFilter<T>(sig: { set(v: T): void }, value: T): void {
    sig.set(value);
    this.page.set(0);
  }

  protected clearFilters(): void {
    this.q.set('');
    this.supports.set('all');
    this.committee.set('all');
    this.pollingRep.set('all');
    this.withLocation.set('all');
    this.from.set('');
    this.to.set('');
    this.page.set(0);
  }

  protected selectTarget(id: number | string): void {
    this.targetId.set(Number(id));
    this.page.set(0);
  }

  protected sectionNumber(row: SupporterListRow): string {
    return row.section ? String(+row.section.replace(/^S-/, '')) : '—';
  }

  protected mapsLink(row: SupporterListRow): string | null {
    return row.latitude != null && row.longitude != null
      ? `https://www.google.com/maps?q=${row.latitude},${row.longitude}`
      : null;
  }
}

function toBool(v: YesNoAll): boolean | null {
  return v === 'all' ? null : v === 'yes';
}
