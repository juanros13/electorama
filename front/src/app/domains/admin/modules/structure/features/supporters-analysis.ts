import { ElectoralHistoryService } from '@/app/domains/admin/modules/electoral-history/data/electoral-history.service';
import { SectionTotals } from '@/app/domains/admin/modules/electoral-history/data/electoral-history.types';
import {
  CHART_FORE,
  CHART_GRID,
  ChartCard,
  ChartOptions,
  isDarkScheme,
} from '@/app/shared/ui/chart-card';
import { downloadCsv } from '@/app/shared/ui/csv';
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
import { catchError, forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { OrganizationService } from '../data/organization.service';
import { CampaignSummary } from '../data/organization.types';
import {
  SupporterCoordinator,
  SupporterProfileRow,
  SupportersService,
  SupporterSection,
} from '../data/supporters.service';
import { TerritoryService } from '../data/territory.service';
import { CatalogSection } from '../data/territory.types';

const STATE_ID = 27;
const PAGE_SIZE = 500;
const ROWS = 100;
const BLOC_4T = 'MORENA_ALLIES';

type Tab = 'votes' | 'mobilization' | 'structure' | 'profile';

/** Una sección con sus convencidos 2024 y su resultado oficial. */
export interface SupporterRow {
  sectionGeoId: number;
  section: number;
  municipalityId: number | null;
  municipality: string;
  main: number;
  alternate: number;
  /** Principal + alterna. */
  supporters: number;
  voted: number;
  /** De la elección de referencia (gubernatura 2024). */
  nominalList: number | null;
  totalVotes: number | null;
  turnout: number | null;
  votes4t: number | null;
  /** Votos 4T / convencidos, en %. Más de 100: la estructura no llegó a todo el voto. */
  conversion: number | null;
  /** Convencidos / LN, en %. */
  coverage: number | null;
  /** Marcados «votó» / votos emitidos oficiales, en %. Más de 100 es imposible. */
  votedIndex: number | null;
  /** Bloque 4T en ayuntamiento, en %. */
  mayor4tPct: number | null;
  /** El bloque 4T no fue el primer bloque en el ayuntamiento de esa sección. */
  mayorLost: boolean;
  /** Hubo más convencidos que votos 4T: estructura inflada. */
  inflated: boolean;
}

interface MunicipalityRow {
  id: number | null;
  name: string;
  supporters: number;
  voted: number;
  nominalList: number;
  totalVotes: number;
  votes4t: number;
  sections: number;
  inflated: number;
  conversion: number | null;
  coverage: number | null;
  votedPct: number | null;
  votedIndex: number | null;
}

export interface CoordinatorRow extends SupporterCoordinator {
  section: number | null;
  municipality: string;
  /** Convencidos / meta, en %. */
  goalPct: number | null;
  /** «Votó» / convencidos, en %. */
  votedPct: number | null;
  /** Conversión de la sección del coordinador (votos 4T / convencidos). */
  sectionConversion: number | null;
}

type SectionFilter = 'ALL' | 'INFLATED' | 'LOST_WITH_STRUCTURE' | 'IMPOSSIBLE_VOTED';
type SectionSort = 'section' | 'supporters' | 'votes4t' | 'conversion' | 'coverage' | 'votedIndex';
type CoordinatorSort = 'members' | 'goalPct' | 'votedPct' | 'sectionConversion' | 'promoters';

/** Qué se pregunta en la encuesta de captura, en el orden en que se muestra. */
const DIMENSIONS: { key: string; label: string; order?: string[] }[] = [
  { key: 'supportsMovement', label: '¿Apoya al movimiento?' },
  { key: 'committeeParticipation', label: '¿Participaría en un comité?' },
  { key: 'pollingRepParticipation', label: '¿Sería representante de casilla?' },
  { key: 'invitingOthers', label: '¿Invitaría a otras personas?' },
  {
    key: 'peopleInvited',
    label: '¿A cuántas personas invitaría?',
    order: ['uno', 'dos', 'tres', 'cuatro', 'cinco', 'masdecinco'],
  },
  { key: 'supportWhatsapp', label: '¿Apoyaría por WhatsApp?' },
  { key: 'sex', label: 'Sexo', order: ['MUJER', 'HOMBRE'] },
  { key: 'ageGroup', label: 'Edad a la jornada', order: ['18-29', '30-44', '45-59', '60+'] },
];

const VALUE_LABEL: Record<string, string> = {
  SI: 'Sí',
  NO: 'No',
  uno: 'Una',
  dos: 'Dos',
  tres: 'Tres',
  cuatro: 'Cuatro',
  cinco: 'Cinco',
  masdecinco: 'Más de cinco',
  MUJER: 'Mujer',
  HOMBRE: 'Hombre',
  '': 'Sin respuesta',
  '--': 'Sin respuesta',
};

const pct = (part: number | null, whole: number | null): number | null =>
  part == null || !whole ? null : Math.round((1000 * part) / whole) / 10;

/**
 * Los simpatizantes de la campaña 2024, ya cerrada, como análisis: cuántos
 * convencidos había contra los votos que de verdad llegaron, qué tan confiable
 * fue la marca «votó», cómo rindió cada coordinador y quiénes eran según la
 * encuesta de captura.
 *
 * Junta tres servicios por número de sección: msvc-structure (convencidos),
 * msvc-geography (catálogo: id → número y municipio) y
 * msvc-electoral-history (resultado oficial de gubernatura y ayuntamiento).
 */
@Component({
  selector: 'app-supporters-analysis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './supporters-analysis.html',
  imports: [
    ChartCard,
    DecimalPipe,
    MatIconModule,
    MatTooltipModule,
    NgTemplateOutlet,
    PageHeader,
    StatCard,
    TableShell,
  ],
})
export default class SupportersAnalysis {
  private organization = inject(OrganizationService);
  private supportersApi = inject(SupportersService);
  private territory = inject(TerritoryService);
  private electoral = inject(ElectoralHistoryService);
  private destroyRef = inject(DestroyRef);

  protected readonly tabs: { value: Tab; label: string; icon: string }[] = [
    { value: 'votes', label: 'Convencidos contra votos', icon: 'vote' },
    { value: 'mobilization', label: 'Movilización «votó»', icon: 'users-round' },
    { value: 'structure', label: 'Estructura 2024', icon: 'network' },
    { value: 'profile', label: 'Perfil', icon: 'chart-pie' },
  ];
  protected tab = signal<Tab>('votes');

  protected campaign = signal<CampaignSummary | null>(null);
  protected loading = signal(true);
  protected sections = signal<SupporterRow[]>([]);
  protected coordinators = signal<CoordinatorRow[]>([]);
  protected profile = signal<SupporterProfileRow[]>([]);

  protected readonly PAGE = ROWS;

  // ------------------------------------------------------------ totales

  protected totals = computed(() => {
    const rows = this.sections();
    const sum = (f: (r: SupporterRow) => number | null) => rows.reduce((t, r) => t + (f(r) ?? 0), 0);
    const supporters = sum((r) => r.supporters);
    const voted = sum((r) => r.voted);
    const votes4t = sum((r) => r.votes4t);
    const totalVotes = sum((r) => r.totalVotes);
    return {
      supporters,
      main: sum((r) => r.main),
      alternate: sum((r) => r.alternate),
      voted,
      votes4t,
      totalVotes,
      nominalList: sum((r) => r.nominalList),
      conversion: pct(votes4t, supporters),
      votedPct: pct(voted, supporters),
      votedIndex: pct(voted, totalVotes),
      inflated: rows.filter((r) => r.inflated).length,
      lostWithStructure: rows.filter((r) => r.mayorLost && (r.coverage ?? 0) >= 30).length,
      impossibleVoted: rows.filter((r) => (r.votedIndex ?? 0) > 100).length,
      sections: rows.length,
    };
  });

  protected byMunicipality = computed<MunicipalityRow[]>(() => {
    const groups = new Map<string, SupporterRow[]>();
    for (const r of this.sections()) {
      const list = groups.get(r.municipality) ?? [];
      list.push(r);
      groups.set(r.municipality, list);
    }
    return [...groups.entries()]
      .map(([name, rows]) => {
        const sum = (f: (r: SupporterRow) => number | null) =>
          rows.reduce((t, r) => t + (f(r) ?? 0), 0);
        const supporters = sum((r) => r.supporters);
        const voted = sum((r) => r.voted);
        const votes4t = sum((r) => r.votes4t);
        const totalVotes = sum((r) => r.totalVotes);
        const nominalList = sum((r) => r.nominalList);
        return {
          id: rows[0].municipalityId,
          name,
          supporters,
          voted,
          nominalList,
          totalVotes,
          votes4t,
          sections: rows.length,
          inflated: rows.filter((r) => r.inflated).length,
          conversion: pct(votes4t, supporters),
          coverage: pct(supporters, nominalList),
          votedPct: pct(voted, supporters),
          votedIndex: pct(voted, totalVotes),
        };
      })
      .sort((a, b) => b.supporters - a.supporters);
  });

  // ------------------------------------------------------------ secciones

  protected sectionFilter = signal<SectionFilter>('ALL');
  protected sectionMunicipality = signal<string | null>(null);
  protected sectionSort = signal<SectionSort>('conversion');
  protected sectionDesc = signal(false);
  protected sectionLimit = signal(ROWS);

  protected municipalities = computed(() => this.byMunicipality().map((m) => m.name).sort((a, b) => a.localeCompare(b, 'es')));

  protected filteredSections = computed(() => {
    const f = this.sectionFilter();
    const m = this.sectionMunicipality();
    const key = this.sectionSort();
    const dir = this.sectionDesc() ? -1 : 1;
    return this.sections()
      .filter(
        (r) =>
          (m == null || r.municipality === m) &&
          (f === 'ALL' ||
            (f === 'INFLATED' && r.inflated) ||
            (f === 'LOST_WITH_STRUCTURE' && r.mayorLost && (r.coverage ?? 0) >= 30) ||
            (f === 'IMPOSSIBLE_VOTED' && (r.votedIndex ?? 0) > 100))
      )
      .sort((a, b) => ((a[key] ?? -Infinity) - (b[key] ?? -Infinity)) * dir || a.section - b.section);
  });
  protected shownSections = computed(() => this.filteredSections().slice(0, this.sectionLimit()));

  // ------------------------------------------------------------ coordinadores

  protected coordinatorMunicipality = signal<string | null>(null);
  protected coordinatorSort = signal<CoordinatorSort>('members');
  protected coordinatorDesc = signal(true);
  protected coordinatorLimit = signal(ROWS);

  protected coordinatorStats = computed(() => {
    const rows = this.coordinators();
    const withGoal = rows.filter((r) => r.goal);
    const members = rows.map((r) => r.members).sort((a, b) => a - b);
    return {
      count: rows.length,
      promoters: rows.reduce((t, r) => t + r.promoters, 0),
      median: members.length ? members[Math.floor(members.length / 2)] : null,
      metGoal: withGoal.filter((r) => r.members >= (r.goal ?? 0)).length,
      withGoal: withGoal.length,
      empty: rows.filter((r) => !r.members).length,
    };
  });

  protected filteredCoordinators = computed(() => {
    const m = this.coordinatorMunicipality();
    const key = this.coordinatorSort();
    const dir = this.coordinatorDesc() ? -1 : 1;
    return this.coordinators()
      .filter((r) => m == null || r.municipality === m)
      .sort((a, b) => ((a[key] ?? -Infinity) - (b[key] ?? -Infinity)) * dir || a.positionId - b.positionId);
  });
  protected shownCoordinators = computed(() =>
    this.filteredCoordinators().slice(0, this.coordinatorLimit())
  );

  // ------------------------------------------------------------ perfil

  protected profileCharts = computed(() => {
    const rows = this.profile();
    return DIMENSIONS.map((d) => {
      const counts = new Map<string, number>();
      for (const r of rows.filter((x) => x.dimension === d.key)) {
        const label = VALUE_LABEL[r.value] ?? r.value;
        counts.set(label, (counts.get(label) ?? 0) + r.count);
      }
      const order = (d.order ?? []).map((v) => VALUE_LABEL[v] ?? v);
      const entries = [...counts.entries()].sort((a, b) => {
        const ia = order.indexOf(a[0]);
        const ib = order.indexOf(b[0]);
        if (a[0] === 'Sin respuesta') return 1;
        if (b[0] === 'Sin respuesta') return -1;
        if (ia >= 0 || ib >= 0) return (ia < 0 ? 99 : ia) - (ib < 0 ? 99 : ib);
        return b[1] - a[1];
      });
      const total = entries.reduce((t, e) => t + e[1], 0);
      return { key: d.key, label: d.label, options: barChart(entries, total) };
    });
  });

  protected captureChart = computed<ChartOptions | null>(() => {
    const months = this.profile()
      .filter((r) => r.dimension === 'capturedMonth' && r.value)
      .sort((a, b) => a.value.localeCompare(b.value));
    if (!months.length) {
      return null;
    }
    return {
      series: [{ name: 'Convencidos capturados', data: months.map((m) => m.count) }],
      colors: ['#9D2449'],
      chart: { type: 'bar', height: 260, toolbar: { show: false }, foreColor: CHART_FORE, fontFamily: 'inherit' },
      plotOptions: { bar: { columnWidth: '60%', borderRadius: 2 } },
      dataLabels: { enabled: false },
      xaxis: { categories: months.map((m) => m.value) },
      yaxis: { labels: { formatter: (v: number) => `${Math.round(v / 1000)} mil` } },
      grid: { borderColor: CHART_GRID },
      tooltip: {
        theme: isDarkScheme() ? 'dark' : 'light',
        y: { formatter: (v: number) => `${v.toLocaleString('es-MX')} convencidos` },
      },
    };
  });

  protected source = computed(() =>
    this.profile()
      .filter((r) => r.dimension === 'source')
      .map((r) => `${r.value || 'sin dato'}: ${r.count.toLocaleString('es-MX')}`)
      .join(' · ')
  );

  constructor() {
    this.load();
  }

  private load(): void {
    this.organization
      .getCampaigns()
      .pipe(
        switchMap((campaigns) => {
          const closed = campaigns.find((c) => c.status === 'CLOSED' && c.year === 2024)
            ?? campaigns.find((c) => c.status === 'CLOSED');
          if (!closed) {
            throw new Error('no hay campaña cerrada');
          }
          this.campaign.set(closed);
          return forkJoin({
            sections: this.supportersApi.getSections(closed.id),
            coordinators: this.supportersApi.getCoordinators(closed.id),
            profile: this.supportersApi.getProfile(closed.id),
            catalog: this.allSections(),
            governor: this.electoral.getAllSections(closed.year ?? 0, 'GOVERNOR'),
            mayor: this.electoral.getAllSections(closed.year ?? 0, 'MAYOR'),
          });
        }),
        catchError(() => {
          showError('No se pudo armar el análisis de simpatizantes');
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => {
        this.loading.set(false);
        if (!res) {
          return;
        }
        const catalog = new Map(res.catalog.map((c) => [c.id, c]));
        const governor = new Map(res.governor.map((s) => [s.section, s]));
        const mayor = new Map(res.mayor.map((s) => [s.section, s]));
        const sections = res.sections.map((s) => toRow(s, catalog.get(s.sectionGeoId), governor, mayor));
        this.sections.set(sections);

        const bySectionGeo = new Map(sections.map((s) => [s.sectionGeoId, s]));
        const municipalityName = new Map<number, string>();
        for (const c of res.catalog) {
          if (c.municipalityId != null && c.municipalityName) {
            municipalityName.set(c.municipalityId, c.municipalityName);
          }
        }
        this.coordinators.set(
          res.coordinators.map((c) => {
            const sec = c.sectionGeoId != null ? bySectionGeo.get(c.sectionGeoId) : undefined;
            const cat = c.sectionGeoId != null ? catalog.get(c.sectionGeoId) : undefined;
            return {
              ...c,
              section: cat ? Number(cat.section) : null,
              municipality:
                (c.municipalityGeoId != null ? municipalityName.get(c.municipalityGeoId) : undefined) ??
                cat?.municipalityName ??
                '—',
              goalPct: pct(c.members, c.goal),
              votedPct: pct(c.voted, c.members),
              sectionConversion: sec?.conversion ?? null,
            };
          })
        );
        this.profile.set(res.profile);
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

  // ------------------------------------------------------------ acciones

  protected setSectionFilter(apply: () => void): void {
    apply();
    this.sectionLimit.set(ROWS);
  }

  protected sortSections(key: SectionSort): void {
    if (this.sectionSort() === key) {
      this.sectionDesc.set(!this.sectionDesc());
    } else {
      this.sectionSort.set(key);
      this.sectionDesc.set(key !== 'conversion' && key !== 'section');
    }
    this.sectionLimit.set(ROWS);
  }

  protected sortCoordinators(key: CoordinatorSort): void {
    if (this.coordinatorSort() === key) {
      this.coordinatorDesc.set(!this.coordinatorDesc());
    } else {
      this.coordinatorSort.set(key);
      this.coordinatorDesc.set(true);
    }
    this.coordinatorLimit.set(ROWS);
  }

  protected icon(active: boolean, desc: boolean): string | null {
    return active ? (desc ? 'arrow-down' : 'arrow-up') : null;
  }

  protected pad(section: number | null): string {
    return section == null ? '—' : String(section).padStart(4, '0');
  }

  protected exportSections(): void {
    downloadCsv(
      `simpatizantes_2024_secciones.csv`,
      [
        'Sección', 'Municipio', 'Convencidos principal', 'Convencidos alterna', 'Convencidos',
        'Marcados votó', 'LN 2024', 'Votos emitidos (gubernatura)', 'Votos 4T (gubernatura)',
        'Conversión %', 'Cobertura %', 'Índice votó %', '4T ayuntamiento %',
        'Ayuntamiento perdido', 'Inflada',
      ],
      this.filteredSections().map((r) => [
        r.section, r.municipality, r.main, r.alternate, r.supporters, r.voted, r.nominalList,
        r.totalVotes, r.votes4t, r.conversion, r.coverage, r.votedIndex, r.mayor4tPct,
        r.mayorLost ? 'sí' : 'no', r.inflated ? 'sí' : 'no',
      ])
    );
  }

  protected exportCoordinators(): void {
    downloadCsv(
      `simpatizantes_2024_coordinadores.csv`,
      [
        'Coordinador de zona', 'Municipio', 'Sección', 'Tipo', 'Meta', 'Promotores',
        'Convencidos', '% de la meta', 'Marcados votó', '% votó', 'Conversión de la sección %',
      ],
      this.filteredCoordinators().map((r) => [
        r.holderName, r.municipality, r.section, r.territoryType === 'SECTION' ? 'Sección' : 'Municipal',
        r.goal, r.promoters, r.members, r.goalPct, r.voted, r.votedPct, r.sectionConversion,
      ])
    );
  }
}

function toRow(
  s: SupporterSection,
  c: CatalogSection | undefined,
  governor: Map<number, SectionTotals>,
  mayor: Map<number, SectionTotals>
): SupporterRow {
  const number = c ? Number(c.section) : 0;
  const g = governor.get(number);
  const m = mayor.get(number);
  const supporters = s.main + s.alternate;
  const votes4t = g ? (g.blocVotes[BLOC_4T] ?? 0) : null;
  let mayorLost = false;
  let mayor4tPct: number | null = null;
  if (m && m.totalVotes) {
    const own = m.blocVotes[BLOC_4T] ?? 0;
    const best = Math.max(0, ...Object.entries(m.blocVotes).filter(([k]) => k !== BLOC_4T).map(([, v]) => v));
    mayorLost = own < best;
    mayor4tPct = pct(own, m.totalVotes);
  }
  return {
    sectionGeoId: s.sectionGeoId,
    section: number,
    municipalityId: c?.municipalityId ?? null,
    municipality: c?.municipalityName ?? '—',
    main: s.main,
    alternate: s.alternate,
    supporters,
    voted: s.voted,
    nominalList: g?.nominalList ?? null,
    totalVotes: g?.totalVotes ?? null,
    turnout: g?.turnout ?? null,
    votes4t,
    conversion: pct(votes4t, supporters),
    coverage: pct(supporters, g?.nominalList ?? null),
    votedIndex: pct(s.voted, g?.totalVotes ?? null),
    mayor4tPct,
    mayorLost,
    inflated: votes4t != null && supporters > votes4t,
  };
}

function barChart(entries: [string, number][], total: number): ChartOptions {
  return {
    series: [{ name: 'Convencidos', data: entries.map((e) => e[1]) }],
    colors: ['#9D2449'],
    chart: { type: 'bar', height: 70 + entries.length * 30, toolbar: { show: false }, foreColor: CHART_FORE, fontFamily: 'inherit' },
    plotOptions: { bar: { horizontal: true, barHeight: '65%', borderRadius: 2 } },
    dataLabels: {
      enabled: true,
      formatter: (v: number) => `${total ? Math.round((1000 * v) / total) / 10 : 0} %`,
      style: { fontSize: '11px' },
    },
    xaxis: {
      categories: entries.map((e) => e[0]),
      labels: { formatter: (v: string) => `${Math.round(Number(v) / 1000)} mil` },
    },
    grid: { borderColor: CHART_GRID },
    tooltip: {
      theme: isDarkScheme() ? 'dark' : 'light',
      y: { formatter: (v: number) => `${v.toLocaleString('es-MX')} convencidos` },
    },
  };
}
