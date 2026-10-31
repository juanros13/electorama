import { AccessService } from '@/app/core/access/access.service';
import { PageHeader } from '@/app/shared/ui/page-header';
import { SectionCard } from '@/app/shared/ui/section-card';
import { StatCard } from '@/app/shared/ui/stat-card';
import { showError } from '@/app/shared/ui/swal';
import { DatePipe, DecimalPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { catchError, distinctUntilChanged, map, of, switchMap } from 'rxjs';
import { AccountService } from '../data/account.service';
import { OrganizationService } from '../data/organization.service';
import { TerritoryService } from '../data/territory.service';
import { CatalogMunicipality } from '../data/territory.types';
import { WorkingCampaign } from '../data/working-campaign';
import { PositionActions, StructureChange } from './position-actions';
import { PositionProfile } from './position-profile';
import {
  CampaignLevel,
  CampaignSummary,
  AssignmentHistory,
  PositionLevel,
  PositionSummary,
} from '../data/organization.types';


/** Suma de una rama y si incluye todo lo que cuelga o sólo lo abierto. */
interface Rollup {
  total: number;
  complete: boolean;
}

const TERRITORY_LABEL: Record<string, string> = {
  STATE: 'Estado',
  MUNICIPALITY: 'Municipio',
  SECTION: 'Sección',
  FRAGMENT: 'Fragmento',
};

/**
 * Una fila visible del árbol.
 *
 * El árbol se guarda PLANO, no anidado: una lista de filas visibles con su
 * profundidad. Así el render es un solo `@for` —sin componentes recursivos— y
 * expandir o colapsar es insertar o quitar un tramo contiguo. Con 49 mil
 * promotores, lo que no se ve no existe para el DOM.
 */
interface TreeRow {
  position: PositionSummary;
  depth: number;
  expanded: boolean;
  loading: boolean;
  /**
   * Para cada ancestro, si ese ancestro era el último de su grupo. Es lo que
   * decide qué rieles verticales se dibujan a la izquierda: sólo los de las
   * ramas que siguen bajando.
   */
  rails: boolean[];
  /** Último de su grupo: dibuja codo `└` en vez de `├`. */
  last: boolean;
}

/**
 * El organigrama del modelo nuevo, como árbol.
 *
 * Enseña lo que el modelo viejo no podía: el PUESTO separado de quien lo ocupa.
 * Cada nodo es un puesto —con su territorio, su meta y sus convencidos— y el
 * titular es un dato suyo, así que un puesto sin nadie sale como vacante en vez
 * de desaparecer del árbol.
 *
 * Los hijos se piden al abrir cada rama: la jerarquía completa son 51
 * referentes en la principal, 6 mil coordinadores y 49 mil promotores, y
 * traerla de golpe no tendría sentido ni para la red ni para el DOM.
 */
@Component({
  selector: 'app-organization-tree',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './organization-tree.html',
  imports: [
    DatePipe,
    DecimalPipe,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    PageHeader,
    PositionActions,
    PositionProfile,
    SectionCard,
    StatCard,
  ],
})
export default class OrganizationTree {
  private service = inject(OrganizationService);
  private territory = inject(TerritoryService);
  private access = inject(AccessService);
  private accounts = inject(AccountService);
  private destroyRef = inject(DestroyRef);
  private working = inject(WorkingCampaign);

  /**
   * Las campañas: la activa (2027) se opera; una cerrada (2024) sólo se
   * consulta. Sus estructuras (principal y alterna) salen del servidor, ya no
   * van fijas como 1 y 2.
   */
  protected campaigns = signal<CampaignSummary[]>([]);
  protected campaign = signal<CampaignSummary | null>(null);
  protected structures = computed(() => this.campaign()?.structures ?? []);
  /** Los niveles de la campaña (V3), de la cúspide hacia abajo. */
  protected levels = computed(() => this.campaign()?.levels ?? []);
  /** El nivel de la cúspide: el de los puestos raíz que se crean aquí. */
  protected topLevel = computed<CampaignLevel | null>(() => this.levels()[0] ?? null);
  /** Si la raíz va sobre un municipio (hay que elegirlo) o sobre el estado de la campaña. */
  protected topOnMunicipality = computed(() => {
    const top = this.topLevel();
    return !!top && (top.territoryType === 'MUNICIPALITY' || (top.territoryType == null && top.function === 'REFERENT'));
  });
  protected structureId = signal<number | null>(null);
  protected readOnly = computed(() => this.campaign()?.status === 'CLOSED');
  /**
   * Armar estructura (nuevo referente, puestos, vacantes, metas, cerrar): sólo
   * ADMIN. El referente ve su rama y nombra titulares; msvc-structure lo valida.
   */
  protected canBuild = computed(() => this.access.isAdmin());
  /** Meta y tope: ni en campaña cerrada ni para quien no arma estructura. */
  protected goalLocked = computed(() => this.readOnly() || !this.canBuild());
  protected eyebrow = computed(() => {
    const c = this.campaign();
    return c ? `Estructura · ${c.name}${c.status === 'CLOSED' ? ' · sólo consulta' : ''}` : 'Estructura';
  });

  /** Los municipios del catálogo, para el alta de referentes (meta 2027 incluida). */
  protected municipalities = signal<CatalogMunicipality[]>([]);
  protected newReferentOpen = signal(false);
  protected newReferentMunicipality = signal<number | null>(null);
  protected newReferentGoal = signal<number | null>(null);
  protected creatingReferent = signal(false);
  /** Los municipios que ya tienen referente en la estructura que se ve. */
  protected withReferent = computed(
    () =>
      new Set(
        this.rows()
          .filter((r) => r.depth === 0 && r.position.level === 'REFERENT')
          .map((r) => r.position.municipalityGeoId)
      )
  );

  protected rows = signal<TreeRow[]>([]);
  protected loadingRoots = signal(false);

  /** Hijos ya traídos: colapsar y volver a abrir no pide otra vez. */
  private childCache = new Map<number, PositionSummary[]>();

  protected selected = signal<PositionSummary | null>(null);

  /**
   * El correo de la cuenta de Keycloak del titular elegido: con el que entra.
   * No es `person.email` (el del padrón), que puede ser otro o no estar.
   */
  protected loginEmail = toSignal(
    toObservable(this.selected).pipe(
      map((p) => p?.holderAuthId ?? null),
      distinctUntilChanged(),
      switchMap((authId) =>
        authId
          ? this.accounts.getAccount(authId).pipe(
              map((a) => a.email || a.username),
              // sin permiso (un referente en su propio puesto) o sin cuenta: no se muestra
              catchError(() => of(null))
            )
          : of(null)
      )
    ),
    { initialValue: null }
  );
  protected history = signal<AssignmentHistory[]>([]);
  protected historyLoading = signal(false);
  /** Convencidos de TODO el subárbol del puesto elegido, no sólo los suyos. */
  protected subtree = signal<number | null>(null);

  /** La meta y el tope del puesto elegido, en edición. */
  protected goalDraft = signal<number | null>(null);
  protected childLimitDraft = signal<number | null>(null);
  protected savingGoal = signal(false);

  protected goalDirty = computed(() => {
    const position = this.selected();
    return (
      !!position &&
      (this.goalDraft() !== (position.goal ?? null) ||
        this.childLimitDraft() !== (position.childLimit ?? null))
    );
  });

  /**
   * La cadena de puestos desde la raíz hasta el elegido.
   *
   * Enciende los rieles de esa rama: en un árbol de tres niveles y miles de
   * nodos, saber por dónde vas es la mitad de la lectura.
   */
  protected path = computed(() => {
    const target = this.selected();
    if (!target) {
      return new Set<number>();
    }
    const byId = new Map(this.rows().map((r) => [r.position.id, r.position]));
    const chain = new Set<number>();
    let cursor: PositionSummary | undefined = target;
    while (cursor) {
      chain.add(cursor.id);
      cursor = cursor.parentId ? byId.get(cursor.parentId) : undefined;
    }
    return chain;
  });

  protected roots = computed(() => this.rows().filter((r) => !r.depth).length);

  protected vacantVisible = computed(
    () =>
      this.rows().filter((r) => !r.position.holderPersonId && !r.position.holderAuthId).length
  );

  protected progressPct = computed(() => {
    const position = this.selected();
    const total = this.subtree();
    if (!position?.goal || total === null) {
      return null;
    }
    return Math.round((total / position.goal) * 100);
  });

  constructor() {
    this.working
      .load()
      .pipe(
        switchMap(() => this.service.getCampaigns()),
        catchError(() => {
          showError('No se pudieron leer las campañas');
          return of([] as CampaignSummary[]);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((campaigns) => {
        this.campaigns.set(campaigns);
        const chosen = campaigns.find((c) => c.id === this.working.campaign()?.id);
        const active = campaigns.find((c) => c.status === 'ACTIVE') ?? campaigns[0] ?? null;
        this.changeCampaign(chosen ?? active);
      });
  }

  /** Los municipios del estado de la campaña, para nombrar los de cada puesto. */
  private loadMunicipalities(): void {
    this.territory
      .getMunicipalities(this.working.stateId())
      .pipe(
        catchError(() => of([] as CatalogMunicipality[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((m) =>
        this.municipalities.set([...(m ?? [])].sort((a, b) => a.name.localeCompare(b.name, 'es')))
      );
  }

  /** Otra campaña: su estructura principal, desde la raíz. */
  changeCampaign(campaign: CampaignSummary | null): void {
    const otherState = campaign?.stateGeoId !== this.campaign()?.stateGeoId || !this.municipalities().length;
    this.campaign.set(campaign);
    this.working.select(campaign);
    if (otherState) {
      this.requestedMunicipalities.clear();
      this.loadMunicipalities();
    }
    this.structureId.set(null);
    const main = campaign?.structures.find((s) => s.type === 'MAIN') ?? campaign?.structures[0];
    if (main) {
      this.changeStructure(main.id);
    } else {
      this.rows.set([]);
    }
  }

  /**
   * La forma con la que se reconoce cada nivel en el árbol: la de su función
   * (cuadro, cuadro hueco, punto) o un rombo para los niveles sin función.
   */
  protected levelShape(fn: PositionLevel | null | undefined): string {
    switch (fn) {
      case 'REFERENT':
        return 'size-2.5 rounded-sm bg-primary-600';
      case 'ZONE_COORDINATOR':
        return 'size-2.5 rounded-sm border-2 border-secondary-600';
      case 'PROMOTER':
        return 'size-1.5 rounded-full bg-neutral-400';
      default:
        return 'size-2 rotate-45 bg-amber-500';
    }
  }

  protected territoryLabel(type: string): string {
    return TERRITORY_LABEL[type] ?? type;
  }

  /**
   * Cómo se lee el territorio del puesto: «Sección 233» y no la clave
   * `S-0233`, el municipio por su nombre. Lo que no se reconoce sale como
   * clave · tipo, igual que antes.
   */
  protected territoryName(position: PositionSummary): string {
    const code = position.territoryCode ?? '';
    if (position.territoryType === 'SECTION') {
      const n = /^S-0*(\d+)$/.exec(code);
      if (n) {
        return `Sección ${n[1]}`;
      }
    }
    if (position.territoryType === 'MUNICIPALITY') {
      const mun = this.municipalityName(position);
      if (mun) {
        return mun;
      }
    }
    return [code, this.territoryLabel(position.territoryType)].filter(Boolean).join(' · ');
  }

  /** El nombre de cada municipio por su id de geography, para leer los puestos. */
  private municipalityNames = computed(
    () => new Map(this.municipalities().map((m) => [m.id, m.name]))
  );

  /**
   * La meta de cada sección del catálogo (LN × %), por su id de geography.
   * Se pide por municipio la primera vez que aparece un promotor suyo en el
   * árbol: los 17 municipios juntos son 1,191 secciones que casi nunca se ven.
   */
  protected sectionMeta = signal(new Map<number, number | null>());
  private requestedMunicipalities = new Set<number>();

  private loadSectionMeta = effect(() => {
    const pending = new Set<number>();
    for (const r of this.rows()) {
      const mun = r.position.municipalityGeoId;
      if (r.position.level === 'ZONE_COORDINATOR' && mun != null && !this.requestedMunicipalities.has(mun)) {
        pending.add(mun);
      }
    }
    for (const mun of pending) {
      this.requestedMunicipalities.add(mun);
      this.territory
        .getSections(this.working.stateId(), { municipalityId: mun }, 0, 500)
        .pipe(
          // sin catálogo la fila sólo no muestra la meta; se reintenta al volver a la pantalla
          catchError(() => {
            this.requestedMunicipalities.delete(mun);
            return of(null);
          }),
          takeUntilDestroyed(this.destroyRef)
        )
        .subscribe((page) => {
          if (!page) {
            return;
          }
          this.sectionMeta.update((prev) => {
            const next = new Map(prev);
            for (const s of page.content) {
              next.set(s.id, s.meta);
            }
            return next;
          });
        });
    }
  });

  /** La meta de la sección del promotor; undefined mientras no llega el catálogo. */
  protected sectionGoal(position: PositionSummary): number | null | undefined {
    return position.sectionGeoId == null ? null : this.sectionMeta().get(position.sectionGeoId);
  }

  /** El municipio del puesto; null en los que cuelgan del estado (sin ubicar). */
  protected municipalityName(position: PositionSummary): string | null {
    return position.municipalityGeoId === null
      ? null
      : (this.municipalityNames().get(position.municipalityGeoId) ?? null);
  }

  /**
   * Convencidos de la rama ya cargada, y si esa suma está completa.
   *
   * Hace falta porque el conteo propio de un coordinador es CERO: los
   * convencidos cuelgan de los puestos de promotor, no del suyo. Enseñar
   * `membershipCount` contra su meta lo pintaba al 0% cuando su rama iba al
   * 88%.
   *
   * `complete` es lo que evita el otro engaño: si debajo queda alguna rama sin
   * abrir, la suma es parcial y se marca como tal en vez de pasar por total. El
   * total de verdad —el recursivo— lo trae `/progress` al elegir el puesto, que
   * es un clic y una consulta, no adivinanza.
   */
  protected rolled(row: TreeRow): Rollup | null {
    if (!row.position.childCount) {
      return { total: row.position.membershipCount, complete: true };
    }
    if (!row.expanded) {
      return null;
    }
    const rows = this.rows();
    const at = rows.findIndex((r) => r.position.id === row.position.id);
    let total = row.position.membershipCount;
    let complete = true;
    for (let i = at + 1; i < rows.length && rows[i].depth > row.depth; i++) {
      total += rows[i].position.membershipCount;
      // una rama cerrada por debajo significa que este total va incompleto
      if (rows[i].position.childCount && !rows[i].expanded) {
        complete = false;
      }
    }
    return { total, complete };
  }

  /** Avance contra la meta con el conteo de la rama, acotado para la barra. */
  protected ownProgress(row: TreeRow): number | null {
    const roll = this.rolled(row);
    if (!row.position.goal || !roll) {
      return null;
    }
    return Math.min(100, Math.round((roll.total / row.position.goal) * 100));
  }

  /** Si el número que se enseña incluye lo que cuelga debajo. */
  protected isRollup(row: TreeRow): boolean {
    return !!row.position.childCount && row.expanded;
  }

  protected isInPath(position: PositionSummary): boolean {
    return this.path().has(position.id);
  }

  changeStructure(id: number): void {
    if (this.structureId() === id) {
      return;
    }
    this.structureId.set(id);
    this.childCache.clear();
    this.selected.set(null);
    this.subtree.set(null);
    this.loadRoots();
  }

  loadRoots(): void {
    const structureId = this.structureId();
    if (structureId == null) {
      return;
    }
    this.loadingRoots.set(true);
    this.rows.set([]);
    this.service
      .getRoots(structureId)
      .pipe(
        catchError(() => of([] as PositionSummary[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((positions) => {
        const roots = positions ?? [];
        this.rows.set(
          roots.map((position, i) => ({
            position,
            depth: 0,
            expanded: false,
            loading: false,
            rails: [],
            last: i === roots.length - 1,
          }))
        );
        this.loadingRoots.set(false);
      });
  }

  toggle(row: TreeRow): void {
    if (!row.position.childCount) {
      return;
    }
    if (row.expanded) {
      this.collapse(row);
      return;
    }
    const cached = this.childCache.get(row.position.id);
    if (cached) {
      this.expand(row, cached);
      return;
    }
    this.patch(row, { loading: true });
    this.service
      .getChildren(row.position.id)
      .pipe(
        catchError(() => of([] as PositionSummary[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((children) => {
        this.childCache.set(row.position.id, children ?? []);
        this.patch(row, { loading: false });
        this.expand(row, children ?? []);
      });
  }

  /** Inserta los hijos justo debajo del padre, un nivel más adentro. */
  private expand(row: TreeRow, children: PositionSummary[]): void {
    const rows = [...this.rows()];
    const at = rows.findIndex((r) => r.position.id === row.position.id);
    if (at < 0) {
      return;
    }
    const inserted: TreeRow[] = children.map((position, i) => ({
      position,
      depth: row.depth + 1,
      expanded: false,
      loading: false,
      // el riel del padre sigue bajando sólo si el padre no era el último
      rails: [...row.rails, row.last],
      last: i === children.length - 1,
    }));
    rows[at] = { ...rows[at], expanded: true };
    rows.splice(at + 1, 0, ...inserted);
    this.rows.set(rows);
  }

  /** Quita el tramo contiguo de descendientes: todo lo que esté más adentro. */
  private collapse(row: TreeRow): void {
    const rows = [...this.rows()];
    const at = rows.findIndex((r) => r.position.id === row.position.id);
    if (at < 0) {
      return;
    }
    let end = at + 1;
    while (end < rows.length && rows[end].depth > row.depth) {
      end++;
    }
    rows.splice(at + 1, end - at - 1);
    rows[at] = { ...rows[at], expanded: false };
    this.rows.set(rows);
  }

  private patch(row: TreeRow, change: Partial<TreeRow>): void {
    this.rows.update((rows) =>
      rows.map((r) =>
        r.position.id === row.position.id ? { ...r, ...change } : r
      )
    );
  }

  /** Abre el detalle: historial de nombramientos y avance del subárbol. */
  select(row: TreeRow): void {
    const position = row.position;
    this.selected.set(position);
    this.goalDraft.set(position.goal ?? null);
    this.childLimitDraft.set(position.childLimit ?? null);

    this.history.set([]);
    this.historyLoading.set(true);
    this.service
      .getAssignments(position.id)
      .pipe(
        catchError(() => of([] as AssignmentHistory[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((rows) => {
        this.history.set(rows ?? []);
        this.historyLoading.set(false);
      });

    this.subtree.set(null);
    this.service
      .getProgress(position.id)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((progress) => this.subtree.set(progress?.memberships ?? null));
  }

  // ─────────────────────────── armar la estructura

  protected pickReferentMunicipality(id: number | null): void {
    this.newReferentMunicipality.set(id);
    const m = this.municipalities().find((x) => x.id === id);
    this.newReferentGoal.set(m?.meta ?? null);
  }

  /** Un referente sobre un municipio: la raíz de su rama. */
  protected createReferent(): void {
    const municipality = this.newReferentMunicipality();
    const structureId = this.structureId();
    const top = this.topLevel();
    if (structureId == null || !top || (this.topOnMunicipality() && municipality == null)) {
      return;
    }
    this.creatingReferent.set(true);
    this.service
      .createPosition({
        structureId,
        levelId: top.id,
        municipalityGeoId: this.topOnMunicipality() ? (municipality ?? undefined) : undefined,
        goal: this.newReferentGoal() ?? top.defaultGoal,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.creatingReferent.set(false);
          this.newReferentOpen.set(false);
          this.newReferentMunicipality.set(null);
          this.loadRoots();
        },
        error: (e) => {
          this.creatingReferent.set(false);
          showError(`No se pudo crear el ${this.topLevel()?.nameSingular.toLowerCase() ?? 'puesto'}`, e?.error?.message ?? 'Intenta de nuevo');
        },
      });
  }

  /**
   * Después de armar: recarga sólo lo que cambió. Un puesto con hijos nuevos se
   * vuelve a abrir con sus hijos frescos; uno que cambió de titular se relee; uno
   * que se cerró sale del árbol y refresca a su padre.
   */
  protected onChanged(change: StructureChange): void {
    if (change.kind === 'closed') {
      this.selected.set(null);
      if (change.parentId == null) {
        this.loadRoots();
      } else {
        this.refreshChildren(change.parentId);
      }
      return;
    }
    if (change.kind === 'children') {
      this.refreshChildren(change.positionId);
    }
    this.refreshPosition(change.positionId);
  }

  private refreshPosition(id: number): void {
    this.service
      .getPosition(id)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((position) => {
        if (!position) {
          return;
        }
        this.rows.update((rows) =>
          rows.map((r) => (r.position.id === id ? { ...r, position } : r))
        );
        if (this.selected()?.id === id) {
          const row = this.rows().find((r) => r.position.id === id);
          if (row) {
            this.select(row);
          }
        }
      });
  }

  private refreshChildren(parentId: number): void {
    this.childCache.delete(parentId);
    const row = this.rows().find((r) => r.position.id === parentId);
    if (!row) {
      return;
    }
    if (row.expanded) {
      this.collapse(row);
    }
    this.service
      .getChildren(parentId)
      .pipe(
        catchError(() => of([] as PositionSummary[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((children) => {
        this.childCache.set(parentId, children ?? []);
        const current = this.rows().find((r) => r.position.id === parentId);
        if (current && children?.length) {
          this.expand(current, children);
        }
      });
  }

  /** Un campo numérico vacío es "sin meta", no cero. */
  protected toCount(value: string | number | null): number | null {
    return value === '' || value === null ? null : Number(value);
  }

  /**
   * Guarda meta y tope juntos: el servicio escribe los dos, así que el que no
   * se tocó viaja con su valor de siempre.
   */
  saveGoal(): void {
    const position = this.selected();
    const goal = this.goalDraft();
    const childLimit = this.childLimitDraft();
    if (!position || (goal ?? 0) < 0 || (childLimit ?? 0) < 0) {
      return;
    }
    this.savingGoal.set(true);
    this.service
      .updateGoal(position.id, goal, childLimit)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((updated) => {
        this.savingGoal.set(false);
        if (!updated) {
          showError('No se pudo guardar la meta del puesto');
          return;
        }
        this.selected.set(updated);
        this.rows.update((rows) =>
          rows.map((r) =>
            r.position.id === updated.id ? { ...r, position: updated } : r
          )
        );
        // el hijo en caché también: colapsar y reabrir no debe traer la meta vieja
        for (const [parentId, children] of this.childCache) {
          if (children.some((c) => c.id === updated.id)) {
            this.childCache.set(
              parentId,
              children.map((c) => (c.id === updated.id ? updated : c))
            );
          }
        }
      });
  }

  /** Enter y espacio abren o cierran la rama, como en cualquier árbol. */
  onKey(event: KeyboardEvent, row: TreeRow): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.toggle(row);
    }
  }
}
