import { LevelCatalog } from '@/app/core/data/level-catalog';
import { PageHeader } from '@/app/shared/ui/page-header';
import { SectionCard } from '@/app/shared/ui/section-card';
import { confirmDialog, showError, showSuccess } from '@/app/shared/ui/swal';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { map, Observable, switchMap } from 'rxjs';
import { CampaignAdminService } from '../data/campaign-admin.service';
import { DEFAULT_GOAL_PERCENTAGE } from '../data/campaign-rules';
import { TerritoryService } from '../data/territory.service';
import { CatalogSummary } from '../data/territory.types';
import { WorkingCampaign } from '../data/working-campaign';
import {
  CampaignData,
  CampaignElection,
  CampaignStatus,
  CampaignSummary,
  CampaignType,
  LevelData,
  PositionLevel,
  StructureType,
  TerritoryOption,
  TerritoryType,
} from '../data/organization.types';

type Tab = 'general' | 'levels' | 'structures' | 'elections';

/** Un nivel en edición; `positions` > 0 bloquea rango, territorio y función. */
interface LevelRow extends LevelData {
  positions: number;
}

interface StructureRow {
  id: number | null;
  name: string;
  type: StructureType;
}

export const CAMPAIGN_TYPES: { value: CampaignType; label: string }[] = [
  { value: 'STATE', label: 'Estatal' },
  { value: 'MUNICIPAL', label: 'Municipal' },
  { value: 'FEDERAL', label: 'Federal' },
  { value: 'INTERNAL', label: 'Interna' },
];

export const TERRITORY_TYPES: { value: TerritoryType | null; label: string }[] = [
  { value: null, label: 'Donde se elija' },
  { value: 'STATE', label: 'Estado' },
  { value: 'MUNICIPALITY', label: 'Municipio' },
  { value: 'SECTION', label: 'Sección' },
  { value: 'FRAGMENT', label: 'Fragmento de sección' },
];

export const FUNCTIONS: { value: PositionLevel | null; label: string; hint: string }[] = [
  { value: null, label: 'Sin función', hint: 'Sólo agrega jerarquía' },
  { value: 'REFERENT', label: 'Coordinación municipal', hint: 'Uno por municipio; cabeza de rama' },
  { value: 'ZONE_COORDINATOR', label: 'Promoción por sección', hint: 'Clave de sección (S-0233A1) y meta por sección' },
  { value: 'PROMOTER', label: 'Movilización', hint: 'Captura convencidos bajo su promotor' },
];

export const OFFICES: { value: string; label: string }[] = [
  { value: 'PRESIDENT', label: 'Presidencia' },
  { value: 'SENATOR', label: 'Senaduría' },
  { value: 'GOVERNOR', label: 'Gubernatura' },
  { value: 'FEDERAL_DEPUTY', label: 'Diputación federal' },
  { value: 'LOCAL_DEPUTY', label: 'Diputación local' },
  { value: 'MAYOR', label: 'Presidencia municipal' },
];

export const SCOPES: { value: string; label: string }[] = [
  { value: 'STATE', label: 'Estado' },
  { value: 'FEDERAL_DISTRICT', label: 'Distrito federal' },
  { value: 'LOCAL_DISTRICT', label: 'Distrito local' },
  { value: 'MUNICIPALITY', label: 'Municipio' },
];

export const STATUS_LABEL: Record<CampaignStatus, string> = {
  PLANNING: 'En planeación',
  ACTIVE: 'Activa',
  CLOSED: 'Cerrada',
};

/** Los tres niveles con los que operan 2024 y 2027: el punto de partida habitual. */
export const DEFAULT_LEVELS: LevelData[] = [
  {
    nameSingular: 'Coordinador',
    namePlural: 'Coordinadores',
    territoryType: 'MUNICIPALITY',
    function: 'REFERENT',
  },
  {
    nameSingular: 'Promotor',
    namePlural: 'Promotores',
    territoryType: 'SECTION',
    defaultGoal: 200,
    function: 'ZONE_COORDINATOR',
  },
  {
    nameSingular: 'Movilizador',
    namePlural: 'Movilizadores',
    territoryType: 'SECTION',
    defaultGoal: 20,
    function: 'PROMOTER',
  },
];

/**
 * Configurar una campaña: sus datos, los cargos en disputa, sus estructuras y,
 * sobre todo, los niveles de su organigrama. Cada campaña decide cuántos niveles
 * tiene, cómo se llaman, dónde va cada puesto, con qué meta nace y qué rol de
 * Keycloak recibe su titular (msvc-structure lo crea al guardar).
 *
 * Una campaña cerrada sólo se consulta. Un nivel con puestos ya no cambia de
 * lugar, territorio ni función; su nombre, meta y rol sí.
 */
@Component({
  selector: 'app-campaign-config',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './campaign-config.html',
  imports: [DecimalPipe, FormsModule, MatButtonModule, MatIconModule, MatTooltipModule, PageHeader, SectionCard],
})
export default class CampaignConfig implements OnInit {
  private api = inject(CampaignAdminService);
  private territory = inject(TerritoryService);
  private working = inject(WorkingCampaign);
  private catalog = inject(LevelCatalog);
  private destroyRef = inject(DestroyRef);

  protected readonly campaignTypes = CAMPAIGN_TYPES;
  protected readonly territoryTypes = TERRITORY_TYPES;
  protected readonly functions = FUNCTIONS;
  protected readonly offices = OFFICES;
  protected readonly scopes = SCOPES;
  protected readonly statusLabel = STATUS_LABEL;

  protected campaigns = signal<CampaignSummary[]>([]);
  protected loading = signal(true);
  protected error = signal<string | null>(null);
  protected saving = signal(false);
  protected territories = signal<TerritoryOption[]>([]);

  /** La campaña elegida; null con `creating` es una nueva sin guardar. */
  protected selected = signal<CampaignSummary | null>(null);
  protected creating = signal(false);
  protected tab = signal<Tab>('general');

  protected general = signal<CampaignData>(this.emptyGeneral());
  protected levels = signal<LevelRow[]>([]);
  protected structures = signal<StructureRow[]>([]);
  protected elections = signal<Partial<CampaignElection>[]>([]);

  protected readOnly = computed(() => this.selected()?.status === 'CLOSED');
  protected hasCampaign = computed(() => this.selected() !== null);
  protected editing = computed(() => this.creating() || this.hasCampaign());
  /** "Coordinador estatal › Coordinador municipal › …": el organigrama en una línea. */
  protected levelChain = computed(() =>
    this.levels()
      .map((l) => l.nameSingular?.trim() || '(sin nombre)')
      .join(' › ')
  );
  protected usedFunctions = computed(() => new Set(this.levels().map((l) => l.function).filter(Boolean)));

  /** Los estados; los municipios se eligen aparte, sólo los del estado elegido. */
  protected stateOptions = computed(() =>
    this.territories()
      .filter((t) => t.type === 'STATE')
      .sort((a, b) => a.name.localeCompare(b.name, 'es'))
  );
  /** El estado del territorio elegido (el mismo, si es un estado; su padre, si es municipio). */
  protected territoryState = computed(() => {
    const id = this.general().territoryId;
    const t = this.territories().find((x) => x.id === id);
    return t ? (t.type === 'STATE' ? t.id : t.parentId) : null;
  });
  protected municipalityOptions = computed(() =>
    this.territories()
      .filter((t) => t.type === 'MUNICIPALITY' && t.parentId === this.territoryState())
      .sort((a, b) => a.name.localeCompare(b.name, 'es'))
  );
  /** Lo que quedó al aplicar el % de meta. */
  protected goalSummary = signal<CatalogSummary | null>(null);

  ngOnInit(): void {
    this.reload();
    this.api
      .territoryOptions()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (t) => this.territories.set(t), error: () => this.territories.set([]) });
  }

  // ------------------------------------------------------------ cargar y elegir

  protected reload(selectId?: number): void {
    this.loading.set(true);
    this.api
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.loading.set(false);
          this.error.set(null);
          this.campaigns.set(list);
          const id = selectId ?? this.selected()?.id ?? this.working.campaign()?.id;
          const pick = list.find((c) => c.id === id) ?? list.find((c) => c.status === 'ACTIVE') ?? list[0];
          if (pick && !this.creating()) {
            this.select(pick);
          }
        },
        error: () => {
          this.loading.set(false);
          this.error.set('No se pudieron leer las campañas de msvc-structure.');
        },
      });
  }

  /** La campaña elegida aquí pasa a ser la de trabajo: catálogo, tablero y captura usan su estado. */
  protected select(c: CampaignSummary): void {
    this.creating.set(false);
    this.load(c);
    this.working.select(c);
  }

  /** Copia la campaña a los borradores de cada pestaña. */
  private load(c: CampaignSummary): void {
    if (this.selected()?.id !== c.id) {
      this.goalSummary.set(null);
    }
    this.selected.set(c);
    this.general.set({
      name: c.name,
      type: c.type,
      year: c.year,
      electionDay: c.electionDay,
      territoryId: c.territoryId,
      startDate: c.startDate,
      endDate: c.endDate,
      description: c.description,
      goalPercentage: c.goalPercentage,
    });
    this.levels.set(
      c.levels.map((l) => ({
        id: l.id,
        code: l.code,
        nameSingular: l.nameSingular,
        namePlural: l.namePlural,
        territoryType: l.territoryType,
        defaultGoal: l.defaultGoal,
        defaultChildLimit: l.defaultChildLimit,
        function: l.function,
        roleName: l.roleName,
        alternateRoleName: l.alternateRoleName,
        positions: l.positions,
      }))
    );
    this.structures.set(c.structures.map((s) => ({ id: s.id, name: s.name, type: s.type })));
    this.elections.set(c.elections.map((e) => ({ ...e })));
    if (c.status === 'ACTIVE') {
      this.catalog.set(c.levels);
    }
  }

  protected newCampaign(): void {
    this.creating.set(true);
    this.selected.set(null);
    this.tab.set('general');
    this.general.set(this.emptyGeneral());
    this.levels.set([]);
    this.structures.set([]);
    this.elections.set([]);
  }

  private emptyGeneral(): CampaignData {
    return {
      name: '',
      type: 'STATE',
      territoryId: null,
      electionDay: null,
      startDate: null,
      endDate: null,
      goalPercentage: DEFAULT_GOAL_PERCENTAGE,
    };
  }

  protected patchGeneral(patch: Partial<CampaignData>): void {
    this.general.update((g) => ({ ...g, ...patch }));
  }

  // ------------------------------------------------------------ datos generales

  protected saveGeneral(): void {
    const g = this.general();
    if (!g.name?.trim()) {
      showError('La campaña necesita un nombre');
      return;
    }
    const current = this.selected();
    const request = current ? this.api.update(current.id, g) : this.api.create(g);
    this.save(request, current ? 'Datos guardados' : 'Campaña creada', (c) => {
      if (!current) {
        this.creating.set(false);
        this.tab.set('levels');
      }
    });
  }

  /**
   * Guarda el % y lo copia a las secciones vigentes del territorio (todo el
   * estado, o el municipio si la campaña es municipal).
   */
  protected async applyGoal(): Promise<void> {
    const c = this.selected();
    const pct = this.general().goalPercentage;
    if (!c?.stateGeoId || pct == null || pct <= 0 || pct > 100) {
      showError('El % de meta va de más de 0 a 100');
      return;
    }
    const where = c.municipalityGeoId ? 'el municipio' : 'el estado';
    if (
      !(await confirmDialog(
        `¿Aplicar ${pct}% a las secciones?`,
        `Cambia el % y la meta de todas las secciones vigentes de ${where}, incluidas las que se ajustaron a mano.`
      ))
    ) {
      return;
    }
    this.saving.set(true);
    this.api
      .update(c.id, this.general())
      .pipe(
        switchMap((saved) =>
          this.territory
            .applyGoal(saved.stateGeoId!, pct, saved.municipalityGeoId)
            .pipe(map((summary) => ({ saved, summary })))
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: ({ saved, summary }) => {
          this.saving.set(false);
          this.load(saved);
          this.campaigns.update((list) => list.map((x) => (x.id === saved.id ? saved : x)));
          this.working.refresh(saved);
          this.goalSummary.set(summary);
          showSuccess('Meta aplicada');
        },
        error: (e: HttpErrorResponse) => {
          this.saving.set(false);
          showError('No se pudo aplicar la meta', e.error?.message ?? e.error?.detail ?? 'Intenta de nuevo');
        },
      });
  }

  protected async changeStatus(status: CampaignStatus): Promise<void> {
    const c = this.selected();
    if (!c) {
      return;
    }
    const text =
      status === 'ACTIVE'
        ? 'Pasa a ser la campaña que se opera en su territorio.'
        : 'Ya no se podrá editar ni armar su estructura: sólo se consulta.';
    if (!(await confirmDialog(status === 'ACTIVE' ? `¿Activar ${c.name}?` : `¿Cerrar ${c.name}?`, text))) {
      return;
    }
    this.save(this.api.changeStatus(c.id, status), status === 'ACTIVE' ? 'Campaña activa' : 'Campaña cerrada');
  }

  // ------------------------------------------------------------ niveles

  protected useDefaultLevels(): void {
    this.levels.set(DEFAULT_LEVELS.map((l) => ({ ...l, positions: 0 })));
  }

  protected addLevel(): void {
    this.levels.update((ls) => [
      ...ls,
      { nameSingular: '', namePlural: '', territoryType: null, function: null, positions: 0 },
    ]);
  }

  protected patchLevel(index: number, patch: Partial<LevelRow>): void {
    this.levels.update((ls) => ls.map((l, i) => (i === index ? { ...l, ...patch } : l)));
  }

  /** Sube o baja un nivel; los que tienen puestos no se mueven (ni se brinca sobre ellos). */
  protected moveLevel(index: number, delta: -1 | 1): void {
    const target = index + delta;
    const ls = this.levels();
    if (target < 0 || target >= ls.length || ls[index].positions || ls[target].positions) {
      return;
    }
    const next = [...ls];
    [next[index], next[target]] = [next[target], next[index]];
    this.levels.set(next);
  }

  protected canMove(index: number, delta: -1 | 1): boolean {
    const ls = this.levels();
    const target = index + delta;
    return target >= 0 && target < ls.length && !ls[index].positions && !ls[target].positions;
  }

  protected removeLevel(index: number): void {
    if (this.levels()[index].positions) {
      return;
    }
    this.levels.update((ls) => ls.filter((_, i) => i !== index));
  }

  protected saveLevels(): void {
    const c = this.selected();
    if (!c) {
      return;
    }
    const payload: LevelData[] = this.levels().map(({ positions, ...l }) => ({
      ...l,
      roleName: l.roleName?.trim() || null,
      alternateRoleName: l.alternateRoleName?.trim() || null,
      code: l.code?.trim() || null,
    }));
    this.save(this.api.saveLevels(c.id, payload), 'Niveles guardados', (saved) => {
      if (saved.status === 'ACTIVE') {
        this.catalog.set(saved.levels);
      }
    });
  }

  // ------------------------------------------------------------ estructuras

  protected addStructure(): void {
    const used = new Set(this.structures().map((s) => s.type));
    const type: StructureType = used.has('MAIN') ? (used.has('ALTERNATE') ? 'ALLIED' : 'ALTERNATE') : 'MAIN';
    const name = type === 'MAIN' ? 'Principal' : type === 'ALTERNATE' ? 'Alterna' : 'Aliada';
    this.structures.update((ss) => [...ss, { id: null, name, type }]);
  }

  protected patchStructure(index: number, patch: Partial<StructureRow>): void {
    this.structures.update((ss) => ss.map((s, i) => (i === index ? { ...s, ...patch } : s)));
  }

  protected saveStructure(index: number): void {
    const c = this.selected();
    const s = this.structures()[index];
    if (!c || !s.name.trim()) {
      showError('La estructura necesita un nombre');
      return;
    }
    const request =
      s.id == null
        ? this.api.addStructure(c.id, s.name.trim(), s.type)
        : this.api.updateStructure(s.id, s.name.trim(), s.type);
    this.save(request, 'Estructura guardada');
  }

  protected async removeStructure(index: number): Promise<void> {
    const s = this.structures()[index];
    if (s.id == null) {
      this.structures.update((ss) => ss.filter((_, i) => i !== index));
      return;
    }
    if (!(await confirmDialog(`¿Quitar la estructura ${s.name}?`, 'Sólo se puede si todavía no tiene puestos.'))) {
      return;
    }
    this.save(this.api.deleteStructure(s.id), 'Estructura quitada');
  }

  // ------------------------------------------------------------ cargos

  protected addElection(): void {
    const day = this.general().electionDay ?? undefined;
    this.elections.update((es) => [...es, { office: '', scope: '', seats: 1, electionDay: day }]);
  }

  protected patchElection(index: number, patch: Partial<CampaignElection>): void {
    this.elections.update((es) => es.map((e, i) => (i === index ? { ...e, ...patch } : e)));
  }

  protected removeElection(index: number): void {
    this.elections.update((es) => es.filter((_, i) => i !== index));
  }

  protected saveElections(): void {
    const c = this.selected();
    if (!c) {
      return;
    }
    this.save(this.api.saveElections(c.id, this.elections()), 'Cargos guardados');
  }

  // ------------------------------------------------------------ utilidades

  protected territoryName(id: number | null | undefined): string {
    return this.territories().find((t) => t.id === id)?.name ?? '';
  }

  protected functionHint(fn: PositionLevel | null | undefined): string {
    return FUNCTIONS.find((f) => f.value === (fn ?? null))?.hint ?? '';
  }

  /** Guarda, refresca la lista con lo que devolvió el servidor y avisa. */
  private save(request: Observable<CampaignSummary>, message: string, then?: (c: CampaignSummary) => void): void {
    this.saving.set(true);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.load(saved);
        this.campaigns.update((list) =>
          list.some((c) => c.id === saved.id) ? list.map((c) => (c.id === saved.id ? saved : c)) : [saved, ...list]
        );
        this.working.refresh(saved);
        then?.(saved);
        showSuccess(message);
      },
      error: (e: HttpErrorResponse) => {
        this.saving.set(false);
        showError('No se pudo guardar', e.error?.message ?? 'Intenta de nuevo');
      },
    });
  }
}
