import { DatePipe, DecimalPipe, PercentPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { catchError, of, switchMap, tap } from 'rxjs';
import { coverage, coverageBarClass, coverageClass } from '../data/campaign-rules';
import { OrganizationService } from '../data/organization.service';
import { MunicipalityPosition } from '../data/organization.types';
import { WorkingCampaign } from '../data/working-campaign';

/** Lo que el tablero ya sabe del municipio: lo requerido sale del Excel. */
export interface BreakdownMunicipality {
  id: number;
  name: string;
  sections: number;
  meta: number;
  promotersNeeded: number;
  mobilizersNeeded: number;
}

/** Un promotor (coordinador de zona) con sus movilizadores y lo que lleva su rama. */
interface PromoterNode {
  position: MunicipalityPosition;
  mobilizers: MunicipalityPosition[];
  members: number;
  last7: number;
}

/** Una sección del municipio: sus promotores y la suma de lo que llevan. */
interface SectionNode {
  sectionGeoId: number;
  /** El número de la sección (233), de la clave del puesto "S-0233A1". */
  section: number;
  promoters: PromoterNode[];
  goal: number;
  members: number;
  last7: number;
  promotersHeld: number;
  mobilizers: number;
  mobilizersHeld: number;
  /** Nombres de los titulares, para buscar. */
  names: string;
}

type SectionFilter = 'all' | 'staffed' | 'vacant';
type SectionSort = 'section' | 'members' | 'progress';

/** Cuántas secciones se pintan de inicio; Centro tiene cientos. */
const PAGE = 30;

/**
 * El desglose de un municipio en el tablero de campaña: cómo va cada nivel de
 * la estructura principal —referente, promotores y movilizadores— y, sección
 * por sección, quién ocupa cada puesto y cuántos convencidos lleva.
 */
@Component({
  selector: 'app-municipality-breakdown',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './municipality-breakdown.html',
  imports: [DatePipe, DecimalPipe, MatIconModule, PercentPipe],
  host: { '(document:keydown.escape)': 'closed.emit()' },
})
export class MunicipalityBreakdown {
  private organization = inject(OrganizationService);
  private working = inject(WorkingCampaign);

  municipality = input.required<BreakdownMunicipality>();
  closed = output<void>();

  protected readonly coverageClass = coverageClass;
  protected readonly coverageBarClass = coverageBarClass;

  protected loading = signal(true);
  protected error = signal(false);
  protected positions = signal<MunicipalityPosition[]>([]);

  protected query = signal('');
  protected filter = signal<SectionFilter>('all');
  protected sort = signal<SectionSort>('section');
  protected limit = signal(PAGE);
  protected expanded = signal<ReadonlySet<number>>(new Set());

  constructor() {
    toObservable(computed(() => this.municipality().id))
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set(false);
          this.positions.set([]);
          this.expanded.set(new Set());
          this.limit.set(PAGE);
        }),
        switchMap((id) =>
          this.organization.getMunicipalityBreakdown(id, this.working.campaign()?.id).pipe(
            catchError(() => {
              this.error.set(true);
              return of([] as MunicipalityPosition[]);
            })
          )
        ),
        takeUntilDestroyed(inject(DestroyRef))
      )
      .subscribe((rows) => {
        this.positions.set(rows);
        this.loading.set(false);
      });
  }

  protected referents = computed(() =>
    this.positions().filter((p) => p.level === 'REFERENT')
  );

  protected sections = computed<SectionNode[]>(() => {
    const all = this.positions();
    const byParent = new Map<number, MunicipalityPosition[]>();
    for (const p of all) {
      if (p.level === 'PROMOTER' && p.parentId !== null) {
        byParent.set(p.parentId, [...(byParent.get(p.parentId) ?? []), p]);
      }
    }
    const bySection = new Map<number, SectionNode>();
    for (const p of all) {
      if (p.level !== 'ZONE_COORDINATOR' || p.sectionGeoId === null) {
        continue;
      }
      const mobilizers = byParent.get(p.id) ?? [];
      const node: PromoterNode = {
        position: p,
        mobilizers,
        members: p.members + mobilizers.reduce((a, m) => a + m.members, 0),
        last7: p.membersLast7 + mobilizers.reduce((a, m) => a + m.membersLast7, 0),
      };
      const s =
        bySection.get(p.sectionGeoId) ??
        ({
          sectionGeoId: p.sectionGeoId,
          section: sectionNumber(p.sectionKey) ?? p.sectionGeoId,
          promoters: [],
          goal: 0,
          members: 0,
          last7: 0,
          promotersHeld: 0,
          mobilizers: 0,
          mobilizersHeld: 0,
          names: '',
        } as SectionNode);
      s.promoters.push(node);
      s.goal += p.goal ?? 0;
      s.members += node.members;
      s.last7 += node.last7;
      s.promotersHeld += p.holderPersonId !== null ? 1 : 0;
      s.mobilizers += mobilizers.length;
      s.mobilizersHeld += mobilizers.filter((m) => m.holderName).length;
      s.names += ` ${p.holderName ?? ''} ${mobilizers.map((m) => m.holderName ?? '').join(' ')}`;
      bySection.set(p.sectionGeoId, s);
    }
    return [...bySection.values()];
  });

  /** Los tres niveles de un vistazo. */
  protected levels = computed(() => {
    const all = this.positions();
    const count = (level: MunicipalityPosition['level']) => {
      const of = all.filter((p) => p.level === level);
      const held = of.filter((p) => p.holderPersonId !== null);
      return {
        positions: of.length,
        held: held.length,
        people: new Set(held.map((p) => p.holderPersonId)).size,
      };
    };
    const members = all.reduce((a, p) => a + p.members, 0);
    const last7 = all.reduce((a, p) => a + p.membersLast7, 0);
    const lastCapture = all.reduce<string | null>(
      (a, p) => (p.lastCapture && (!a || p.lastCapture > a) ? p.lastCapture : a),
      null
    );
    return {
      promoters: count('ZONE_COORDINATOR'),
      mobilizers: count('PROMOTER'),
      members,
      last7,
      lastCapture,
      sectionsStaffed: this.sections().filter((s) => s.promotersHeld > 0).length,
    };
  });

  protected visible = computed(() => {
    const q = normalize(this.query().trim());
    const filter = this.filter();
    const sort = this.sort();
    const rows = this.sections().filter((s) => {
      if (filter === 'staffed' && s.promotersHeld === 0) return false;
      if (filter === 'vacant' && s.promotersHeld === s.promoters.length) return false;
      if (!q) return true;
      return String(s.section).includes(q) || normalize(s.names).includes(q);
    });
    return rows.sort((a, b) => {
      if (sort === 'members') return b.members - a.members || a.section - b.section;
      if (sort === 'progress')
        return coverage(b.members, b.goal) - coverage(a.members, a.goal) || a.section - b.section;
      return a.section - b.section;
    });
  });

  protected shown = computed(() => this.visible().slice(0, this.limit()));

  protected coverage = coverage;

  protected toggle(section: number): void {
    this.expanded.update((set) => {
      const next = new Set(set);
      if (!next.delete(section)) {
        next.add(section);
      }
      return next;
    });
  }

  protected setQuery(value: string): void {
    this.query.set(value);
    this.limit.set(PAGE);
  }

  protected setFilter(value: SectionFilter): void {
    this.filter.set(value);
    this.limit.set(PAGE);
  }

  protected more(): void {
    this.limit.update((n) => n + PAGE);
  }
}

/** "S-0233A1" → 233. */
function sectionNumber(key: string | null): number | null {
  const m = key ? /^S-(\d+)/.exec(key) : null;
  return m ? +m[1] : null;
}

/** Minúsculas y sin acentos, para buscar nombres. */
function normalize(s: string): string {
  return s
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase();
}
