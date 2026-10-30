import { levelName } from '@/app/core/data/levels';
import { PageHeader } from '@/app/shared/ui/page-header';
import { SectionCard } from '@/app/shared/ui/section-card';
import { StatCard } from '@/app/shared/ui/stat-card';
import { confirmDialog, showError } from '@/app/shared/ui/swal';
import { DecimalPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { catchError, forkJoin, of } from 'rxjs';
import {
  coverageClass,
  DEFAULT_GOAL_PERCENTAGE,
  requiredMobilizers,
  requiredPromoters,
} from '../data/campaign-rules';
import { OrganizationService } from '../data/organization.service';
import { PositionSummary } from '../data/organization.types';
import { TerritoryService } from '../data/territory.service';
import {
  CatalogDistrict,
  CatalogMunicipality,
  CatalogSection,
  CatalogSummary,
  SectionChange,
  SectionFilter,
  SectionGoal,
  SectionLocality,
} from '../data/territory.types';

/** El estado de campaña. La API acepta cualquiera; la operación es Tabasco. */
const STATE_ID = 27;
const PAGE_SIZE = 50;

type Tab = 'sections' | 'municipalities' | 'federal' | 'local';

/** Cómo cuadra la meta repartida entre coordinadores contra el techo de la sección. */
type Balance = 'none' | 'ok' | 'under' | 'over';

const SECTION_TYPE: Record<number, string> = {
  1: 'Urbana',
  2: 'Mixta',
  3: 'Rural',
};

/**
 * El catálogo territorial: secciones, municipios y distritos del marco INE.
 *
 * La meta tiene dos pisos y esta pantalla enseña los dos juntos:
 *
 *  - el TECHO de la sección (`geo_secciones.meta`): el % de su lista nominal
 *    2027 (61 por default). Aquí se editan la LN y el %, no el techo;
 *  - la meta de cada COORDINADOR, que vive en su puesto de `msvc-structure`
 *    y también se puede ajustar desde el detalle de la sección.
 *
 * Ninguna se calcula de la otra. Cuando la suma de los coordinadores no cuadra
 * con el techo se avisa, sin bloquear: el front viejo lo impedía y eso obligaba
 * a repartir la meta completa antes de poder guardar nada.
 */
@Component({
  selector: 'app-territory-catalog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './territory-catalog.html',
  imports: [
    DecimalPipe,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    PageHeader,
    SectionCard,
    StatCard,
  ],
})
export default class TerritoryCatalog {
  private territory = inject(TerritoryService);
  private organization = inject(OrganizationService);
  private destroyRef = inject(DestroyRef);

  protected readonly pageSize = PAGE_SIZE;
  protected readonly levelName = levelName;
  protected tab = signal<Tab>('sections');

  // ---------------------------------------------------------------- catálogos
  protected municipalities = signal<CatalogMunicipality[]>([]);
  protected federalDistricts = signal<CatalogDistrict[]>([]);
  protected localDistricts = signal<CatalogDistrict[]>([]);
  protected catalogsLoading = signal(false);

  protected activeFederal = computed(() =>
    this.federalDistricts().filter((d) => d.status)
  );

  // ---------------------------------------------------------------- secciones
  protected filter = signal<SectionFilter>({ status: true });
  /** Las cifras de arriba: siguen al filtro de secciones. */
  protected summary = signal<CatalogSummary | null>(null);
  protected summaryLabel = computed(() => {
    const status = this.filter().status;
    return status === true
      ? 'Secciones vigentes'
      : status === false
        ? 'Secciones de baja'
        : 'Secciones';
  });
  protected page = signal(0);
  protected sections = signal<CatalogSection[]>([]);
  protected total = signal(0);
  protected sectionsLoading = signal(false);
  /** Meta repartida por sección de la página visible. */
  protected goals = signal<Map<number, SectionGoal>>(new Map());

  protected pageEnd = computed(() =>
    Math.min((this.page() + 1) * PAGE_SIZE, this.total())
  );
  protected lastPage = computed(
    () => (this.page() + 1) * PAGE_SIZE >= this.total()
  );

  // ---------------------------------------------------------------- detalle
  protected selected = signal<CatalogSection | null>(null);
  /** Sólo se usa en secciones sin LN; con LN el techo sale del porcentaje. */
  protected draftMeta = signal<number | null>(null);
  protected draftNominalList = signal<number | null>(null);
  protected draftPercentage = signal<number | null>(null);
  protected draftStatus = signal(true);

  /** El techo que quedaría al guardar. */
  protected previewMeta = computed(() => {
    const ln = this.draftNominalList();
    const pct = this.draftPercentage();
    if (ln === null) {
      return this.draftMeta();
    }
    return pct === null ? null : Math.round((ln * pct) / 100);
  });

  protected invalidDraft = computed(() => {
    const ln = this.draftNominalList();
    const pct = this.draftPercentage();
    const meta = this.draftMeta();
    // una LN capturada no se borra: el back sólo la cambia
    const clearedNominalList =
      ln === null && this.selected()?.nominalList != null;
    return (
      clearedNominalList ||
      (ln !== null && ln < 0) ||
      pct === null ||
      pct < 0 ||
      pct > 100 ||
      (ln === null && (meta === null || meta < 0))
    );
  });
  protected savingSection = signal(false);

  protected positions = signal<PositionSummary[]>([]);
  protected positionsLoading = signal(false);
  /** Metas de coordinador en edición, por id de puesto. */
  protected goalDrafts = signal<Record<number, number | null>>({});
  protected savingGoal = signal<number | null>(null);

  protected localities = signal<SectionLocality[]>([]);
  protected localitiesLoading = signal(false);
  protected newLocality = signal('');
  protected editingLocality = signal<number | null>(null);
  protected localityDraft = signal('');

  /** Suma de las metas de los coordinadores de la sección elegida. */
  protected assigned = computed(() =>
    this.positions()
      .filter((p) => p.level === 'ZONE_COORDINATOR')
      .reduce((sum, p) => sum + (p.goal ?? 0), 0)
  );

  protected dirtySection = computed(() => {
    const s = this.selected();
    return (
      !!s &&
      (this.draftNominalList() !== (s.nominalList ?? null) ||
        this.draftPercentage() !== (s.goalPercentage ?? null) ||
        (s.nominalList === null && this.draftMeta() !== (s.meta ?? null)) ||
        this.draftStatus() !== (s.status ?? false))
    );
  });

  // ---------------------------------------------------------------- municipios
  protected renaming = signal<number | null>(null);
  protected renameDraft = signal('');

  constructor() {
    this.loadCatalogs();
    this.loadSections();
    this.loadSummary();
  }

  // ======================================================================
  // carga
  // ======================================================================

  private loadCatalogs(): void {
    this.catalogsLoading.set(true);
    forkJoin({
      municipalities: this.territory
        .getMunicipalities(STATE_ID)
        .pipe(catchError(() => of([] as CatalogMunicipality[]))),
      federal: this.territory
        .getFederalDistricts(STATE_ID)
        .pipe(catchError(() => of([] as CatalogDistrict[]))),
      local: this.territory
        .getLocalDistricts(STATE_ID)
        .pipe(catchError(() => of([] as CatalogDistrict[]))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ municipalities, federal, local }) => {
        this.municipalities.set(municipalities);
        this.federalDistricts.set(federal);
        this.localDistricts.set(local);
        this.catalogsLoading.set(false);
      });
  }

  protected loadSections(): void {
    this.sectionsLoading.set(true);
    this.territory
      .getSections(STATE_ID, this.filter(), this.page(), PAGE_SIZE)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((result) => {
        this.sectionsLoading.set(false);
        if (!result) {
          this.sections.set([]);
          this.total.set(0);
          showError('No se pudo leer el catálogo de secciones');
          return;
        }
        this.sections.set(result.content);
        this.total.set(result.totalElements);
        this.loadGoals(result.content.map((s) => s.id));
      });
  }

  /**
   * Los totales del filtro actual. Si falla se quedan los anteriores: las
   * tarjetas no son lo importante de la pantalla, la lista ya avisa sus errores.
   */
  private loadSummary(): void {
    this.territory
      .getSectionsSummary(STATE_ID, this.filter())
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((summary) => summary && this.summary.set(summary));
  }

  /** La meta repartida de la página visible, en una sola consulta. */
  private loadGoals(ids: number[]): void {
    this.goals.set(new Map());
    if (!ids.length) {
      return;
    }
    this.organization
      .getSectionGoals(ids)
      .pipe(
        catchError(() => of([] as SectionGoal[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((rows) =>
        this.goals.set(new Map(rows.map((g) => [g.sectionGeoId, g])))
      );
  }

  // ======================================================================
  // filtros y paginación
  // ======================================================================

  protected setFilter(change: Partial<SectionFilter>): void {
    this.filter.update((f) => ({ ...f, ...change }));
    this.page.set(0);
    this.loadSections();
    this.loadSummary();
  }

  protected clearFilters(): void {
    this.filter.set({ status: true });
    this.page.set(0);
    this.loadSections();
    this.loadSummary();
  }

  protected hasFilters = computed(() => {
    const f = this.filter();
    return !!(
      f.municipalityId ||
      f.federalDistrict ||
      f.localDistrict ||
      f.q ||
      f.status !== true
    );
  });

  protected goToPage(delta: number): void {
    this.page.update((p) => Math.max(0, p + delta));
    this.loadSections();
  }

  /** Desde las otras pestañas: ver las secciones de un municipio o distrito. */
  protected showSectionsOf(change: Partial<SectionFilter>): void {
    this.filter.set({ status: true, ...change });
    this.page.set(0);
    this.tab.set('sections');
    this.loadSections();
    this.loadSummary();
  }

  /** Los selects nativos entregan texto; vacío es "todos". */
  protected toNumber(value: string | number | null): number | null {
    return value === '' || value === null ? null : Number(value);
  }

  /** Vigencia → valor del select: `null` es "todas". */
  protected statusValue(status: boolean | null | undefined): string {
    return status === null || status === undefined ? 'all' : String(status);
  }

  protected toStatus(value: string): boolean | null {
    return value === 'all' ? null : value === 'true';
  }

  // ======================================================================
  // lectura de filas
  // ======================================================================

  protected sectionType(type: number | null): string {
    return type ? (SECTION_TYPE[type] ?? '—') : '—';
  }

  protected goalOf(section: CatalogSection): SectionGoal | undefined {
    return this.goals().get(section.id);
  }

  // las reglas de la campaña, para la plantilla
  protected readonly requiredPromoters = requiredPromoters;
  protected readonly requiredMobilizers = requiredMobilizers;
  protected readonly coverageClass = coverageClass;

  protected toNullableNumber(value: string | number | null): number | null {
    return value === '' || value === null ? null : Number(value);
  }

  protected balance(meta: number | null, assigned: number | null): Balance {
    if (!assigned) {
      return 'none';
    }
    const ceiling = meta ?? 0;
    if (assigned === ceiling) {
      return 'ok';
    }
    return assigned > ceiling ? 'over' : 'under';
  }

  protected balanceHint(meta: number | null, assigned: number): string {
    const ceiling = meta ?? 0;
    switch (this.balance(meta, assigned)) {
      case 'over':
        return `Los promotores suman ${assigned - ceiling} más que el techo`;
      case 'under':
        return `Faltan ${ceiling - assigned} por repartir entre promotores`;
      case 'ok':
        return 'La meta está repartida completa';
      default:
        return 'Ningún promotor tiene meta en esta sección';
    }
  }

  // ======================================================================
  // detalle de la sección
  // ======================================================================

  protected select(section: CatalogSection): void {
    this.selected.set(section);
    this.draftMeta.set(section.meta ?? null);
    this.draftNominalList.set(section.nominalList ?? null);
    this.draftPercentage.set(
      section.goalPercentage ?? DEFAULT_GOAL_PERCENTAGE
    );
    this.draftStatus.set(section.status ?? false);
    this.newLocality.set('');
    this.editingLocality.set(null);
    this.loadPositions(section.id);
    this.loadLocalities(section.id);
  }

  protected close(): void {
    this.selected.set(null);
  }

  private loadPositions(sectionId: number): void {
    this.positionsLoading.set(true);
    this.positions.set([]);
    this.organization
      .getBySection(sectionId)
      .pipe(
        catchError(() => of([] as PositionSummary[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((rows) => {
        this.positions.set(rows);
        this.goalDrafts.set(
          Object.fromEntries(rows.map((p) => [p.id, p.goal ?? null]))
        );
        this.positionsLoading.set(false);
      });
  }

  private loadLocalities(sectionId: number): void {
    this.localitiesLoading.set(true);
    this.localities.set([]);
    this.territory
      .getLocalities(sectionId)
      .pipe(
        catchError(() => of([] as SectionLocality[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((rows) => {
        this.localities.set(rows);
        this.localitiesLoading.set(false);
      });
  }

  protected async saveSection(): Promise<void> {
    const section = this.selected();
    if (!section || this.invalidDraft()) {
      return;
    }
    const nominalList = this.draftNominalList();
    const status = this.draftStatus();
    // con LN la meta no viaja: el back la calcula con el porcentaje
    const change: SectionChange = {
      goalPercentage: this.draftPercentage(),
      status,
      ...(nominalList !== section.nominalList ? { nominalList } : {}),
      ...(nominalList === null ? { meta: this.draftMeta() } : {}),
    };
    if (
      section.status &&
      !status &&
      !(await confirmDialog(
        `¿Dar de baja la sección ${section.section}?`,
        'Deja de salir en los catálogos vigentes. Sus registros no se borran.'
      ))
    ) {
      return;
    }
    this.savingSection.set(true);
    this.territory
      .updateSection(section.id, change)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((updated) => {
        this.savingSection.set(false);
        if (!updated) {
          showError('No se pudo guardar la sección');
          return;
        }
        this.replaceSection(updated);
        this.select(updated);
        // los agregados de municipios y distritos cambian con la meta
        this.loadCatalogs();
        this.loadSummary();
      });
  }

  private replaceSection(updated: CatalogSection): void {
    this.sections.update((rows) =>
      rows.map((s) => (s.id === updated.id ? updated : s))
    );
  }

  protected setGoalDraft(positionId: number, value: string | number): void {
    this.goalDrafts.update((d) => ({
      ...d,
      [positionId]: value === '' ? null : Number(value),
    }));
  }

  protected goalChanged(position: PositionSummary): boolean {
    return (this.goalDrafts()[position.id] ?? null) !== (position.goal ?? null);
  }

  /** Guarda la meta del puesto sin tocar su tope de hijos. */
  protected saveGoal(position: PositionSummary): void {
    const goal = this.goalDrafts()[position.id] ?? null;
    if (goal !== null && goal < 0) {
      return;
    }
    this.savingGoal.set(position.id);
    this.organization
      .updateGoal(position.id, goal, position.childLimit)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((updated) => {
        this.savingGoal.set(null);
        if (!updated) {
          showError('No se pudo guardar la meta del promotor');
          return;
        }
        this.positions.update((rows) =>
          rows.map((p) => (p.id === updated.id ? updated : p))
        );
        this.loadGoals(this.sections().map((s) => s.id));
      });
  }

  // ---------------------------------------------------------------- localidades

  protected addLocality(): void {
    const section = this.selected();
    const name = this.newLocality().trim().toUpperCase();
    if (!section || !name) {
      return;
    }
    this.territory
      .createLocality(section, name)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((created) => {
        if (!created) {
          showError('No se pudo agregar la localidad');
          return;
        }
        this.newLocality.set('');
        this.afterLocalityChange(section);
      });
  }

  protected startRenameLocality(locality: SectionLocality): void {
    this.editingLocality.set(locality.id);
    this.localityDraft.set(locality.localidad ?? '');
  }

  protected saveLocality(locality: SectionLocality): void {
    const section = this.selected();
    const name = this.localityDraft().trim().toUpperCase();
    if (!section || !name) {
      return;
    }
    this.territory
      .renameLocality(locality, name)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((saved) => {
        if (!saved) {
          showError('No se pudo renombrar la localidad');
          return;
        }
        this.editingLocality.set(null);
        this.afterLocalityChange(section);
      });
  }

  protected async removeLocality(locality: SectionLocality): Promise<void> {
    const section = this.selected();
    if (
      !section ||
      !(await confirmDialog(`¿Quitar la localidad ${locality.localidad}?`))
    ) {
      return;
    }
    this.territory
      .deleteLocality(locality.id)
      .pipe(
        catchError(() => of(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((result) => {
        if (result === false) {
          showError('No se pudo quitar la localidad');
          return;
        }
        this.afterLocalityChange(section);
      });
  }

  /** Recarga la lista y el conteo de la fila. */
  private afterLocalityChange(section: CatalogSection): void {
    this.loadLocalities(section.id);
    this.territory
      .getSections(STATE_ID, { q: section.section, status: null }, 0, PAGE_SIZE)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((page) => {
        const fresh = page?.content.find((s) => s.id === section.id);
        if (fresh) {
          this.replaceSection(fresh);
          this.selected.set(fresh);
        }
      });
  }

  // ======================================================================
  // municipios
  // ======================================================================

  protected startRename(m: CatalogMunicipality): void {
    this.renaming.set(m.id);
    this.renameDraft.set(m.name);
  }

  protected saveRename(m: CatalogMunicipality): void {
    const name = this.renameDraft().trim().toUpperCase();
    if (!name || name === m.name) {
      this.renaming.set(null);
      return;
    }
    this.territory
      .renameMunicipality(m.id, name)
      .pipe(
        catchError(() => of(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((result) => {
        if (result === false) {
          showError('No se pudo renombrar el municipio');
          return;
        }
        this.renaming.set(null);
        this.municipalities.update((rows) =>
          rows.map((row) => (row.id === m.id ? { ...row, name } : row))
        );
        this.loadSections();
      });
  }
}
