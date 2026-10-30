import { PageHeader } from '@/app/shared/ui/page-header';
import { StatCard } from '@/app/shared/ui/stat-card';
import { TableShell } from '@/app/shared/ui/table-shell';
import { showError } from '@/app/shared/ui/swal';
import { DecimalPipe, NgTemplateOutlet, PercentPipe } from '@angular/common';
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
import { catchError, forkJoin, of, switchMap } from 'rxjs';
import {
  coverage,
  coverageBarClass,
  coverageClass,
  requiredMobilizers,
  requiredPromoters,
} from '../data/campaign-rules';
import { OrganizationService } from '../data/organization.service';
import { MunicipalitySummary } from '../data/organization.types';
import { TerritoryService } from '../data/territory.service';
import { CatalogMunicipality } from '../data/territory.types';
import { MunicipalityBreakdown } from './municipality-breakdown';

/** El estado de campaña. */
const STATE_ID = 27;

/** Una fila del tablero: lo del Excel (requerido) junto a lo real (asignado). */
interface DashboardRow {
  id: number;
  name: string;
  sections: number;
  nominalList: number;
  meta: number;
  promotersNeeded: number;
  promoters: number;
  mobilizersNeeded: number;
  mobilizers: number;
  members: number;
  /** Convencidos contra la meta. */
  progress: number;
  /** Convencidos de la campaña anterior (2024): referencia, no cuentan al avance. */
  previousMembers: number | null;
  morenaVote2024: number | null;
}

type SortKey = keyof Omit<DashboardRow, 'id'>;

interface Column {
  key: SortKey;
  label: string;
  align: 'left' | 'right';
  hint: string;
}

/** Las columnas de la hoja del Excel, más convencidos y avance. */
const COLUMNS: readonly Column[] = [
  { key: 'name', label: 'Municipio', align: 'left', hint: '' },
  { key: 'sections', label: 'Secc.', align: 'right', hint: 'Secciones vigentes (marco INE 2026)' },
  { key: 'nominalList', label: 'LN', align: 'right', hint: 'Lista nominal oficial del INE' },
  { key: 'meta', label: 'Meta 2027', align: 'right', hint: 'Suma de los techos: LN × % de meta de cada sección' },
  { key: 'promoters', label: 'Promotores', align: 'right', hint: 'Asignados / requeridos (1 por cada 200 de meta)' },
  { key: 'mobilizers', label: 'Movilizadores', align: 'right', hint: 'Asignados / requeridos (1 por cada 20 de meta)' },
  { key: 'members', label: 'Convencidos', align: 'right', hint: 'Convencidos de la campaña en curso (estructura principal)' },
  { key: 'progress', label: 'Avance', align: 'left', hint: 'Convencidos contra la meta' },
  {
    key: 'previousMembers',
    label: 'Convencidos 2024',
    align: 'right',
    hint: 'Los de la campaña 2024, cerrada: referencia de lo que hubo, no suman al avance 2027',
  },
  {
    key: 'morenaVote2024',
    label: 'Voto MORENA 2024',
    align: 'right',
    hint: 'Total municipal del Excel de la campaña; referencia, no cómputo oficial',
  },
];

/**
 * El tablero de campaña: la hoja "TBD_RESUMEN" de "estructura estatal.xlsx"
 * —secciones, LN, meta, promotores, movilizadores y voto de MORENA 2024 por
 * municipio— con el avance real de la estructura principal al lado.
 *
 * Lo requerido sale de geography (LN oficial del INE × % de cada sección) y de
 * las reglas de la campaña; lo asignado, de msvc-structure. El voto 2024 es el
 * total municipal del Excel: referencia, no cómputo oficial (G6).
 *
 * Los requeridos se redondean hacia arriba por municipio (el Excel los deja
 * con decimales: 133.2 promotores en Balancán son 134 personas).
 */
@Component({
  selector: 'app-campaign-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './campaign-dashboard.html',
  imports: [
    DecimalPipe,
    MatIconModule,
    MatTooltipModule,
    MunicipalityBreakdown,
    NgTemplateOutlet,
    PageHeader,
    PercentPipe,
    StatCard,
    TableShell,
  ],
})
export default class CampaignDashboard {
  private territory = inject(TerritoryService);
  private organization = inject(OrganizationService);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(true);
  protected rows = signal<DashboardRow[]>([]);
  /** El municipio abierto en el desglose; null: cerrado. */
  protected selected = signal<DashboardRow | null>(null);
  protected sortKey = signal<SortKey>('name');
  protected sortDesc = signal(false);

  protected readonly columns = COLUMNS;
  protected readonly coverageClass = coverageClass;
  protected readonly coverageBarClass = coverageBarClass;

  protected sorted = computed(() => {
    const key = this.sortKey();
    const dir = this.sortDesc() ? -1 : 1;
    return [...this.rows()].sort((a, b) => {
      const x = a[key];
      const y = b[key];
      if (typeof x === 'string' && typeof y === 'string') {
        return x.localeCompare(y, 'es') * dir;
      }
      // sin dato va al final, en cualquier sentido
      if (x === null) return 1;
      if (y === null) return -1;
      return ((x as number) - (y as number)) * dir;
    });
  });

  /** La fila de total: sumas, y el avance recalculado sobre las sumas. */
  protected total = computed<DashboardRow>(() => {
    const rows = this.rows();
    const sum = (key: SortKey) =>
      rows.reduce((acc, r) => acc + ((r[key] as number | null) ?? 0), 0);
    const meta = sum('meta');
    const members = sum('members');
    return {
      id: 0,
      name: 'Total',
      sections: sum('sections'),
      nominalList: sum('nominalList'),
      meta,
      promotersNeeded: sum('promotersNeeded'),
      promoters: sum('promoters'),
      mobilizersNeeded: sum('mobilizersNeeded'),
      mobilizers: sum('mobilizers'),
      members,
      progress: coverage(members, meta),
      previousMembers: rows.some((r) => r.previousMembers !== null)
        ? sum('previousMembers')
        : null,
      morenaVote2024: rows.some((r) => r.morenaVote2024 !== null)
        ? sum('morenaVote2024')
        : null,
    };
  });

  constructor() {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    forkJoin({
      municipalities: this.territory
        .getMunicipalities(STATE_ID)
        .pipe(catchError(() => of(null))),
      summary: this.organization
        .getMunicipalitySummary()
        .pipe(catchError(() => of(null))),
      // la campaña cerrada más reciente, sólo como referencia
      previous: this.organization.getCampaigns().pipe(
        switchMap((campaigns) => {
          const closed = campaigns.find((c) => c.status === 'CLOSED');
          return closed ? this.organization.getMunicipalitySummary(closed.id) : of(null);
        }),
        catchError(() => of(null))
      ),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ municipalities, summary, previous }) => {
        this.loading.set(false);
        if (!municipalities) {
          showError('No se pudo leer el catálogo de municipios');
          return;
        }
        if (!summary) {
          // lo del Excel se puede enseñar igual; lo real queda en cero
          showError('No se pudo leer la estructura; el avance sale en cero');
        }
        this.rows.set(toRows(municipalities, summary ?? [], previous));
      });
  }

  protected sortBy(key: SortKey): void {
    if (this.sortKey() === key) {
      this.sortDesc.update((d) => !d);
    } else {
      this.sortKey.set(key);
      // los números se leen de mayor a menor; los nombres, de la A a la Z
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
}

function toRows(
  municipalities: CatalogMunicipality[],
  summary: MunicipalitySummary[],
  previous: MunicipalitySummary[] | null
): DashboardRow[] {
  const byMunicipality = new Map(summary.map((s) => [s.municipalityGeoId, s]));
  const before = new Map((previous ?? []).map((s) => [s.municipalityGeoId, s.members]));
  return municipalities
    .filter((m) => m.sections > 0)
    .map((m) => {
      const real = byMunicipality.get(m.id);
      const members = real?.members ?? 0;
      return {
        id: m.id,
        name: m.name,
        sections: m.sections,
        nominalList: m.nominalList,
        meta: m.meta,
        promotersNeeded: requiredPromoters(m.meta),
        promoters: real?.promoters ?? 0,
        mobilizersNeeded: requiredMobilizers(m.meta),
        mobilizers: real?.mobilizers ?? 0,
        members,
        progress: coverage(members, m.meta),
        previousMembers: previous ? (before.get(m.id) ?? 0) : null,
        morenaVote2024: m.morenaVote2024,
      };
    });
}
