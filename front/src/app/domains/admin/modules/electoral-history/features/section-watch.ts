import { OrganizationService } from '@/app/domains/admin/modules/structure/data/organization.service';
import { TerritoryService } from '@/app/domains/admin/modules/structure/data/territory.service';
import {
  CatalogSection,
  SectionOverview,
} from '@/app/domains/admin/modules/structure/data/territory.types';
import { TableShell } from '@/app/shared/ui/table-shell';
import { DecimalPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { catchError, forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { RiskStatus, SectionRisk } from '../data/electoral-history.types';

const STATE_ID = 27;
const PAGE_SIZE = 500;
/** Cuántas filas se pintan de golpe: la lista completa pasa de 10 mil nodos. */
const ROWS = 100;

/** Qué secciones se ven: por estado, o las que se alejaron de la marca. */
type Filter = 'ALL' | RiskStatus | 'GAP';

type SortKey =
  | 'status'
  | 'section'
  | 'marginPp'
  | 'marginVotes'
  | 'brandGapPp'
  | 'nominalList'
  | 'meta'
  | 'members'
  | 'previousMembers'
  | 'progress';

/** Una sección a vigilar con lo que la estructura tiene en ella. */
export interface WatchRow extends SectionRisk {
  /** La sección ya no está en el catálogo vigente (se dividió en 2026). */
  remapped: boolean;
  /** Meta 2027 de la sección (LN 2027 × %). */
  meta: number | null;
  nominalList2027: number | null;
  positions: number;
  promoters: number;
  members: number;
  /** Convencidos de la campaña 2024 (cerrada) en la sección: referencia. */
  previousMembers: number | null;
  coordinators: string | null;
  /** Convencidos / meta 2027, en %. */
  progress: number | null;
}

const STATUS_ORDER: Record<RiskStatus, number> = { LOST: 0, CLOSE: 1, COMPETITIVE: 2, SAFE: 3 };

const STATUS_LABEL: Record<RiskStatus, string> = {
  LOST: 'Perdida',
  CLOSE: 'Cerrada',
  COMPETITIVE: 'Competida',
  SAFE: 'Ganada',
};

const STATUS_CHIP: Record<RiskStatus, string> = {
  LOST: 'bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-300',
  CLOSE: 'bg-orange-100 text-orange-700 dark:bg-orange-500/20 dark:text-orange-300',
  COMPETITIVE: 'bg-amber-100 text-amber-700 dark:bg-amber-500/20 dark:text-amber-300',
  SAFE: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-300',
};

/** Más allá de esta brecha con la marca, la sección se vigila aunque se haya ganado. */
const GAP_PP = 20;

/**
 * Las secciones del ayuntamiento que hay que vigilar, cruzadas con la
 * estructura de campaña: la meta 2027 (geography) y los coordinadores,
 * promotores y convencidos (structure). El cruce es por número de sección:
 * los resultados son de su año y el catálogo es el vigente, así que una
 * sección que se dividió en 2026 sale sin estructura.
 */
@Component({
  selector: 'app-section-watch',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'flex flex-col gap-3' },
  templateUrl: './section-watch.html',
  imports: [DecimalPipe, MatIconModule, MatTooltipModule, TableShell],
})
export class SectionWatch implements OnInit {
  private territory = inject(TerritoryService);
  private organization = inject(OrganizationService);
  private destroyRef = inject(DestroyRef);

  /** Las secciones a vigilar, ya ordenadas de mayor a menor riesgo. */
  sections = input.required<SectionRisk[]>();
  year = input.required<number>();

  protected readonly statusLabel = STATUS_LABEL;
  protected readonly statusChip = STATUS_CHIP;
  protected readonly gapPp = GAP_PP;
  protected readonly filters: { value: Filter; label: string }[] = [
    { value: 'ALL', label: 'Todas' },
    { value: 'LOST', label: 'Perdidas' },
    { value: 'CLOSE', label: 'Cerradas (< 5 pp)' },
    { value: 'COMPETITIVE', label: 'Competidas (< 10 pp)' },
    { value: 'GAP', label: `Lejos de la marca (> ${GAP_PP} pp)` },
  ];

  protected catalog = signal<Map<number, CatalogSection> | null>(null);
  protected overview = signal<Map<number, SectionOverview> | null>(null);
  /** Convencidos 2024 por sección del catálogo (campaña cerrada). */
  protected previous = signal<Map<number, number> | null>(null);
  protected loading = signal(false);
  protected structureError = signal(false);

  protected filter = signal<Filter>('ALL');
  protected municipality = signal<number | null>(null);
  protected query = signal('');
  protected sortKey = signal<SortKey>('status');
  protected sortDesc = signal(false);

  protected municipalities = computed(() => {
    const seen = new Map<number, string>();
    for (const s of this.sections()) {
      seen.set(s.municipality, s.municipalityName);
    }
    return [...seen.entries()]
      .map(([code, name]) => ({ code, name }))
      .sort((a, b) => a.name.localeCompare(b.name, 'es'));
  });

  /** Las secciones con su estructura; sin estructura mientras carga o si falló. */
  protected rows = computed<WatchRow[]>(() => {
    const catalog = this.catalog();
    const overview = this.overview();
    const previous = this.previous();
    return this.sections().map((s) => {
      const c = catalog?.get(s.section) ?? null;
      const o = c ? (overview?.get(c.id) ?? null) : null;
      const meta = c?.meta ?? null;
      const members = o?.members ?? 0;
      return {
        ...s,
        remapped: catalog != null && c == null,
        meta,
        nominalList2027: c?.nominalList ?? null,
        positions: o?.positions ?? 0,
        promoters: o?.promoters ?? 0,
        members,
        previousMembers: c && previous ? (previous.get(c.id) ?? 0) : null,
        coordinators: o?.coordinators ?? null,
        progress: meta ? Math.round((1000 * members) / meta) / 10 : null,
      };
    });
  });

  protected visible = computed(() => {
    const f = this.filter();
    const m = this.municipality();
    const q = this.query().trim();
    const rows = this.rows().filter(
      (r) =>
        (f === 'ALL' ||
          (f === 'GAP' ? (r.brandGapPp ?? 0) > GAP_PP : r.status === f)) &&
        (m == null || r.municipality === m) &&
        (!q || String(r.section).includes(q) || String(r.section).padStart(4, '0').includes(q))
    );
    const key = this.sortKey();
    const dir = this.sortDesc() ? -1 : 1;
    const value = (r: WatchRow): number =>
      key === 'status' ? STATUS_ORDER[r.status] * 1e7 + r.marginVotes : (r[key] ?? -Infinity);
    return [...rows].sort((a, b) => (value(a) - value(b)) * dir || a.section - b.section);
  });

  protected readonly PAGE = ROWS;
  protected limit = signal(ROWS);
  /** Lo que se pinta; el CSV y los totales van sobre todo lo filtrado. */
  protected shown = computed(() => this.visible().slice(0, this.limit()));

  protected totals = computed(() => {
    const rows = this.visible();
    const sum = (f: (r: WatchRow) => number | null) => rows.reduce((t, r) => t + (f(r) ?? 0), 0);
    const meta = sum((r) => r.meta);
    const members = sum((r) => r.members);
    return {
      count: rows.length,
      short: -sum((r) => Math.min(0, r.marginVotes)),
      meta,
      members,
      progress: meta ? Math.round((1000 * members) / meta) / 10 : null,
      withoutStructure: rows.filter((r) => !r.remapped && r.positions === 0).length,
    };
  });

  ngOnInit(): void {
    this.loading.set(true);
    forkJoin({
      catalog: this.allSections(),
      overview: this.organization.getSectionOverview(),
      previous: this.organization.getCampaigns().pipe(
        switchMap((campaigns) => {
          const closed = campaigns.find((c) => c.status === 'CLOSED');
          return closed ? this.organization.getSectionOverview(closed.id) : of(null);
        }),
        catchError(() => of(null))
      ),
    })
      .pipe(
        catchError(() => {
          this.structureError.set(true);
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => {
        if (res) {
          // si una sección viene dos veces (vigente y dada de baja), gana la vigente
          const catalog = new Map<number, CatalogSection>();
          for (const c of res.catalog) {
            const n = Number(c.section);
            const prev = catalog.get(n);
            if (!prev || (c.status && !prev.status)) {
              catalog.set(n, c);
            }
          }
          this.catalog.set(catalog);
          this.overview.set(new Map(res.overview.map((o) => [o.sectionGeoId, o])));
          if (res.previous) {
            this.previous.set(new Map(res.previous.map((o) => [o.sectionGeoId, o.members])));
          }
        }
        this.loading.set(false);
      });
  }

  /** Todo el catálogo de secciones del estado (el servidor da páginas de 500). */
  private allSections(): Observable<CatalogSection[]> {
    return this.territory.getSections(STATE_ID, {}, 0, PAGE_SIZE).pipe(
      switchMap((first) => {
        const pages = Math.ceil(first.totalElements / PAGE_SIZE);
        if (pages <= 1) {
          return of(first.content);
        }
        const rest = Array.from({ length: pages - 1 }, (_, i) =>
          this.territory.getSections(STATE_ID, {}, i + 1, PAGE_SIZE)
        );
        return forkJoin(rest).pipe(map((more) => first.content.concat(...more.map((p) => p.content))));
      })
    );
  }

  protected setFilter(apply: () => void): void {
    apply();
    this.limit.set(ROWS);
  }

  protected sortBy(key: SortKey): void {
    this.limit.set(ROWS);
    if (this.sortKey() === key) {
      this.sortDesc.set(!this.sortDesc());
    } else {
      this.sortKey.set(key);
      // lo más urgente primero: en márgenes y avance, lo más bajo; en tamaños, lo más grande
      this.sortDesc.set(
        ['brandGapPp', 'nominalList', 'meta', 'members', 'previousMembers'].includes(key)
      );
    }
  }

  protected sortIcon(key: SortKey): string | null {
    if (this.sortKey() !== key) {
      return null;
    }
    return this.sortDesc() ? 'arrow-down' : 'arrow-up';
  }

  protected pad(section: number): string {
    return String(section).padStart(4, '0');
  }

  protected pp(value: number | null | undefined): string {
    if (value == null) {
      return '—';
    }
    return `${value > 0 ? '+' : value < 0 ? '−' : ''}${Math.abs(value).toFixed(1)}`;
  }

  /** La tabla tal como se ve (filtros y orden), para abrir en Excel. */
  protected exportCsv(): void {
    const head = [
      'Sección',
      'Municipio',
      'Distrito local',
      'Distrito federal',
      'Estado',
      'Mejor 4T',
      '% 4T',
      'Rival',
      '% rival',
      'Margen pp',
      'Votos de diferencia',
      `% 4T ayuntamiento ${this.year()}`,
      '% 4T marca',
      'Brecha con la marca pp',
      `LN ${this.year()}`,
      `Participación ${this.year()} %`,
      'LN 2027',
      'Meta 2027',
      'Coordinadores de zona',
      'Promotores',
      'Convencidos 2027',
      'Avance %',
      'Convencidos 2024 (referencia)',
      'Nombres de coordinadores',
      'Nota',
    ];
    const lines = this.visible().map((r) =>
      [
        r.section,
        r.municipalityName,
        r.localDistrict,
        r.federalDistrict,
        STATUS_LABEL[r.status],
        r.best4tLabel,
        r.best4tPct,
        r.rivalLabel,
        r.rivalPct,
        r.marginPp,
        r.marginVotes,
        r.bloc4tPct,
        r.brand4tPct,
        r.brandGapPp,
        r.nominalList,
        r.turnout,
        r.nominalList2027,
        r.meta,
        r.positions,
        r.promoters,
        r.members,
        r.progress,
        r.previousMembers,
        r.coordinators,
        r.remapped ? 'Sección dividida en 2026: sin estructura en el catálogo vigente' : '',
      ]
        .map(csvCell)
        .join(',')
    );
    const csv = '﻿' + [head.map(csvCell).join(','), ...lines].join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
    const a = document.createElement('a');
    a.href = url;
    a.download = `secciones_a_vigilar_${this.year()}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }
}

function csvCell(value: unknown): string {
  if (value == null) {
    return '';
  }
  const text = String(value);
  return /[",\r\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
}
