import { LEVEL_NAMES } from '@/app/core/data/levels';
import { confirmDialog, showError, showSuccess, showWarning } from '@/app/shared/ui/swal';
import { generatePassword } from '@/app/shared/utils/credentials';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  output,
  signal,
  untracked,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { catchError, forkJoin, map, Observable, of } from 'rxjs';
import { META_PER_MOBILIZER, META_PER_PROMOTER } from '../data/campaign-rules';
import { OrganizationService } from '../data/organization.service';
import {
  AccountMode,
  AppointRequest,
  PersonLite,
  PositionSummary,
  LinkPlan,
  SectionSeat,
} from '../data/organization.types';
import { TerritoryService } from '../data/territory.service';
import { UserService } from '@/app/core/user/user.service';

const STATE_ID = 27;

type Panel = 'none' | 'addChild' | 'vacancies' | 'appoint';

/** Qué cambió, para que el árbol recargue sólo lo necesario. */
export interface StructureChange {
  /** children: el puesto ganó hijos; self: cambió su titular; closed: se cerró. */
  kind: 'children' | 'self' | 'closed';
  positionId: number;
  parentId: number | null;
}

/** Una sección del municipio del referente, con lo que pide y lo que ya tiene. */
interface SectionPlan {
  sectionGeoId: number;
  section: string;
  meta: number;
  needed: number;
  existing: number;
}

/**
 * Las acciones para armar la estructura sobre el puesto elegido: agregar
 * puestos debajo, generar las vacantes que pide la meta, nombrar al titular
 * (con su cuenta) y dejar vacante o cerrar.
 *
 * Sólo en una campaña que no esté cerrada; la base también lo impide.
 */
@Component({
  selector: 'app-position-actions',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './position-actions.html',
  imports: [DecimalPipe, FormsModule, MatButtonModule, MatIconModule],
})
export class PositionActions {
  private organization = inject(OrganizationService);
  private territory = inject(TerritoryService);
  private users = inject(UserService);
  private destroyRef = inject(DestroyRef);

  position = input.required<PositionSummary>();
  /**
   * Quien arma la estructura (ADMIN). Sin esto, en los puestos de abajo sólo se
   * nombra titular en vacantes y se deja vacante (quitar a un promotor): el
   * referente no crea puestos ni cierra, ni toca su propio puesto.
   * msvc-structure lo vuelve a validar.
   */
  canBuild = input(true);
  changed = output<StructureChange>();

  protected readonly levels = LEVEL_NAMES;
  protected panel = signal<Panel>('none');
  protected busy = signal(false);

  /** El authId de quien está viendo: sus propios puestos no se tocan. */
  private me = computed(() => this.users.user()?.id ?? null);

  /**
   * Nombrar: ADMIN siempre; los demás, en vacantes (en su árbol sólo hay
   * vacantes debajo de ellos: sus propios puestos están ocupados por ellos).
   */
  protected canAppoint = computed(() => {
    const p = this.position();
    return this.canBuild() || (!p.holderPersonId && !p.holderAuthId);
  });
  /** Quitar al titular: ADMIN, o quien está arriba, nunca en un puesto suyo. */
  protected canVacate = computed(() => {
    const p = this.position();
    const occupied = !!(p.holderPersonId || p.holderAuthId);
    return occupied && (this.canBuild() || (!!this.me() && p.holderAuthId !== this.me()));
  });
  protected hasActions = computed(
    () => this.canBuild() || this.canAppoint() || this.canVacate()
  );

  protected childLevel = computed(() => {
    const level = this.position().level;
    return level === 'REFERENT' ? 'ZONE_COORDINATOR' : level === 'ZONE_COORDINATOR' ? 'PROMOTER' : null;
  });
  protected childLabel = computed(() => {
    const level = this.childLevel();
    return level ? LEVEL_NAMES[level].one.toLowerCase() : '';
  });
  /** Bajo un promotor: cuántos movilizadores pide su meta y cuántos faltan. */
  protected mobilizersMissing = computed(() => {
    const p = this.position();
    if (p.level !== 'ZONE_COORDINATOR') {
      return 0;
    }
    const needed = Math.ceil((p.goal ?? META_PER_PROMOTER) / META_PER_MOBILIZER);
    return Math.max(0, needed - p.childCount);
  });

  // ------------------------------------------------------------ secciones del referente

  protected sections = signal<SectionPlan[]>([]);
  protected sectionsLoading = signal(false);
  protected missingTotal = computed(() =>
    this.sections().reduce((t, s) => t + Math.max(0, s.needed - s.existing), 0)
  );

  // ------------------------------------------------------------ agregar un puesto

  protected newSection = signal<number | null>(null);
  protected newGoal = signal<number | null>(null);

  // ------------------------------------------------------------ nombrar

  protected query = signal('');
  protected found = signal<PersonLite[]>([]);
  protected searching = signal(false);
  protected chosen = signal<PersonLite | null>(null);
  protected person = signal({ names: '', lastName: '', surName: '', curp: '', phone: '', email: '' });
  protected accountMode = signal<AccountMode>('CREATE');
  protected accountEmail = signal('');
  /**
   * Si el correo de la cuenta se escribió a mano. Mientras no, sigue al correo
   * de la persona: antes se copiaba sólo la primera tecla y la cuenta quedaba
   * con "j" en vez de "juan@…".
   */
  private accountEmailEdited = signal(false);
  protected accountPassword = signal('');
  protected showAccountPassword = signal(false);
  protected linkAuthId = signal('');
  /** La cuenta que ya existe con ese correo (respuesta 409): se ofrece ligarla. */
  protected existingAccount = signal<string | null>(null);

  constructor() {
    // al cambiar de puesto se cierran los paneles abiertos
    effect(() => {
      this.position();
      untracked(() => this.reset());
    });
  }

  private reset(): void {
    this.panel.set('none');
    this.sections.set([]);
    this.newSection.set(null);
    this.newGoal.set(null);
    this.query.set('');
    this.found.set([]);
    this.chosen.set(null);
    this.person.set({ names: '', lastName: '', surName: '', curp: '', phone: '', email: '' });
    this.accountMode.set('CREATE');
    this.accountEmail.set('');
    this.accountPassword.set('');
    this.accountEmailEdited.set(false);
    this.showAccountPassword.set(false);
    this.linkAuthId.set('');
    this.existingAccount.set(null);
    this.sectionSeats.set([]);
    this.wholeSection.set(false);
  }

  protected open(panel: Panel): void {
    this.panel.set(this.panel() === panel ? 'none' : panel);
    const p = this.position();
    if (this.panel() === 'appoint' && p.level === 'ZONE_COORDINATOR') {
      this.loadSectionSeats();
    }
    if (this.panel() === 'addChild') {
      this.newGoal.set(p.level === 'REFERENT' ? META_PER_PROMOTER : META_PER_MOBILIZER);
    }
    if ((this.panel() === 'addChild' || this.panel() === 'vacancies') && p.level === 'REFERENT') {
      this.loadSections();
    }
  }

  /** Las secciones del municipio con su meta 2027 y cuántos promotores ya tienen. */
  private loadSections(): void {
    const p = this.position();
    if (p.municipalityGeoId == null) {
      return;
    }
    this.sectionsLoading.set(true);
    forkJoin({
      catalog: this.territory.getSections(STATE_ID, { municipalityId: p.municipalityGeoId, status: true }, 0, 500),
      children: this.organization.getChildren(p.id),
    })
      .pipe(
        catchError(() => {
          showError('No se pudieron leer las secciones del municipio');
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => {
        this.sectionsLoading.set(false);
        if (!res) {
          return;
        }
        const existing = new Map<number, number>();
        for (const c of res.children) {
          if (c.sectionGeoId != null) {
            existing.set(c.sectionGeoId, (existing.get(c.sectionGeoId) ?? 0) + 1);
          }
        }
        this.sections.set(
          res.catalog.content
            .map((s) => ({
              sectionGeoId: s.id,
              section: s.section,
              meta: s.meta ?? 0,
              needed: Math.ceil((s.meta ?? 0) / META_PER_PROMOTER),
              existing: existing.get(s.id) ?? 0,
            }))
            .sort((a, b) => a.section.localeCompare(b.section))
        );
      });
  }

  // ------------------------------------------------------------ acciones

  protected addChild(): void {
    const p = this.position();
    const level = this.childLevel();
    if (!level) {
      return;
    }
    if (level === 'ZONE_COORDINATOR' && this.newSection() == null) {
      showError('Elige la sección del promotor');
      return;
    }
    this.run(
      this.organization.createPosition({
        level,
        parentPositionId: p.id,
        sectionGeoId: level === 'ZONE_COORDINATOR' ? (this.newSection() ?? undefined) : undefined,
        goal: this.newGoal(),
      }),
      () => {
        this.panel.set('none');
        this.changed.emit({ kind: 'children', positionId: p.id, parentId: p.parentId });
      }
    );
  }

  protected async generateForMunicipality(): Promise<void> {
    const p = this.position();
    const missing = this.missingTotal();
    if (!missing) {
      return;
    }
    if (!(await confirmDialog(`¿Crear ${missing} promotores vacantes?`, 'Uno por cada 200 de meta en cada sección; sólo los que faltan.'))) {
      return;
    }
    const sections = this.sections()
      .filter((s) => s.needed > s.existing)
      .map((s) => ({ sectionGeoId: s.sectionGeoId, meta: s.meta }));
    this.run(this.organization.generateVacancies(p.id, sections), (result) => {
      const created = result.reduce((t, r) => t + r.created, 0);
      showSuccess(`${created} promotores vacantes creados`);
      this.panel.set('none');
      this.changed.emit({ kind: 'children', positionId: p.id, parentId: p.parentId });
    });
  }

  protected async completeMobilizers(): Promise<void> {
    const p = this.position();
    const missing = this.mobilizersMissing();
    if (!missing || !(await confirmDialog(`¿Crear ${missing} movilizadores vacantes?`, `Uno por cada ${META_PER_MOBILIZER} de la meta del promotor.`))) {
      return;
    }
    this.run(this.organization.generateVacancies(p.id), (result) => {
      showSuccess(`${result.reduce((t, r) => t + r.created, 0)} movilizadores vacantes creados`);
      this.changed.emit({ kind: 'children', positionId: p.id, parentId: p.parentId });
    });
  }

  protected search(): void {
    const q = this.query().trim();
    if (q.length < 3) {
      return;
    }
    this.searching.set(true);
    const isCurp = /^[A-Z]{4}\d{6}[HM][A-Z]{5}[A-Z0-9]\d$/i.test(q);
    const request: Observable<PersonLite[]> = isCurp
      ? this.organization.findPersonByCurp(q.toUpperCase()).pipe(map((p) => [p]))
      : this.organization.searchPersons(q);
    request
      .pipe(
        catchError(() => of([] as PersonLite[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => {
        this.searching.set(false);
        this.found.set(res.slice(0, 10));
      });
  }

  protected choose(person: PersonLite | null): void {
    this.chosen.set(person);
    this.existingAccount.set(null);
    if (person?.email && !this.accountEmailEdited()) {
      this.accountEmail.set(person.email);
    }
    if (person?.authId) {
      this.accountMode.set('LINK');
      this.linkAuthId.set(person.authId);
    }
  }

  protected setPerson(field: keyof ReturnType<typeof this.person>, value: string): void {
    this.person.update((p) => ({ ...p, [field]: value }));
    if (field === 'email' && !this.accountEmailEdited()) {
      this.accountEmail.set(value);
    }
  }

  protected editAccountEmail(value: string): void {
    this.accountEmail.set(value);
    this.accountEmailEdited.set(value.trim() !== '');
  }

  /** Contraseña de 8 caracteres, visible para dársela a quien se nombra. */
  protected generateAccountPassword(): void {
    this.accountPassword.set(generatePassword());
    this.showAccountPassword.set(true);
  }

  // ------------------------------------------------------------ toda la sección

  /** Los demás puestos vacantes de promotor de la sección: los toma «toda la sección». */
  protected sectionSeats = signal<SectionSeat[]>([]);
  protected wholeSection = signal(false);
  protected sectionSeatsGoal = computed(() =>
    this.sectionSeats().reduce((t, s) => t + (s.goal ?? 0), 0)
  );
  protected sectionSeatsLabel = computed(() =>
    [...this.sectionSeats()]
      .sort((a, b) =>
        (a.sectionKey ?? '').localeCompare(b.sectionKey ?? '', 'es', { numeric: true })
      )
      .map((s) => s.sectionKey?.replace(/^S-\d+/, '') || `#${s.positionId}`)
      .join(', ')
  );

  private loadSectionSeats(): void {
    this.organization
      .getSectionVacancies(this.position().id)
      .pipe(
        catchError(() => of([] as SectionSeat[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((seats) => this.sectionSeats.set(seats));
  }

  protected appoint(replaceInBranch = false): void {
    const p = this.position();
    const chosen = this.chosen();
    const data = this.person();
    if (!chosen && (!data.names.trim() || !data.lastName.trim())) {
      showWarning('Falta el nombre y el primer apellido');
      return;
    }
    const mode = this.accountMode();
    if (mode === 'CREATE' && !this.accountEmail().trim()) {
      showWarning('Falta el correo de la cuenta', 'Es el usuario con el que va a entrar a la app.');
      return;
    }
    if (mode === 'CREATE' && this.accountPassword().length < 8) {
      showWarning(
        'Falta la contraseña de la cuenta',
        'Lleva al menos 8 caracteres; con «Generar» se crea una y se muestra para dársela.'
      );
      return;
    }
    if (mode === 'LINK' && !this.linkAuthId().trim()) {
      showWarning('Falta el id de la cuenta que se va a ligar');
      return;
    }
    const request: AppointRequest = {
      person: chosen
        ? { personId: chosen.id }
        : {
            names: data.names,
            lastName: data.lastName,
            surName: data.surName || undefined,
            curp: data.curp || undefined,
            phone: data.phone || undefined,
            email: data.email || undefined,
          },
      account:
        mode === 'CREATE'
          ? { mode, email: this.accountEmail().trim(), password: this.accountPassword() }
          : mode === 'LINK'
            ? { mode, authId: this.linkAuthId().trim() }
            : { mode },
      replaceInBranch: replaceInBranch || undefined,
      wholeSection: (this.wholeSection() && this.sectionSeats().length > 0) || undefined,
    };
    this.busy.set(true);
    this.organization
      .appoint(p.id, request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (done) => {
          this.busy.set(false);
          const also = done.alsoAppointed?.length ?? 0;
          const notes = [
            also ? `También quedó en ${also} puesto(s) más de la sección: lleva toda su meta.` : '',
            done.vacatedPositions?.length
              ? `Quedaron vacantes ${done.vacatedPositions.length} puesto(s) de la rama` +
                (done.revokedRoles?.length ? ` y se le quitó: ${done.revokedRoles.join(', ')}.` : '.')
              : '',
          ].filter(Boolean);
          showSuccess(
            done.accountCreated
              ? `Nombrado, con su cuenta nueva (${done.role})`
              : done.authId
                ? 'Nombrado con la cuenta ligada'
                : 'Nombrado sin cuenta',
            notes.length ? notes.join(' ') : undefined
          );
          this.reset();
          this.changed.emit({ kind: 'self', positionId: p.id, parentId: p.parentId });
          // los puestos que dejó vacantes, y los demás de la sección, también cambiaron
          for (const id of [...(done.vacatedPositions ?? []), ...(done.alsoAppointed ?? [])]) {
            this.changed.emit({ kind: 'self', positionId: id, parentId: null });
          }
        },
        error: (e: HttpErrorResponse) => {
          this.busy.set(false);
          const plan: LinkPlan | undefined = e.error?.detail?.plan;
          if (e.status === 409 && e.error?.detail?.code === 'BRANCH_CONFLICT' && plan) {
            this.confirmReplace(plan);
            return;
          }
          const authId = e.error?.detail?.authId;
          if (e.status === 409 && authId) {
            this.existingAccount.set(authId);
            return;
          }
          showError('No se pudo nombrar', e.error?.message ?? 'Intenta de nuevo');
        },
      });
  }

  /**
   * La cuenta ya ocupa otros puestos en esta misma rama: se avisa qué queda
   * vacante y qué roles pierde, y sólo con confirmación se nombra.
   */
  private async confirmReplace(plan: LinkPlan): Promise<void> {
    const who = this.chosen()
      ? [this.chosen()!.names, this.chosen()!.lastName].filter(Boolean).join(' ')
      : 'Esta cuenta';
    const seats = plan.vacates
      .map((v) => `${LEVEL_NAMES[v.level]?.one ?? v.level} ${v.label}`)
      .join(', ');
    const roles = plan.rolesToRevoke.length
      ? ` y se le quitan los roles ${plan.rolesToRevoke.join(', ')}`
      : '';
    const ok = await confirmDialog(
      'Ya ocupa otro puesto en esta rama',
      `${who} es titular de: ${seats}. Si continúas, ese puesto queda vacante${roles}, ` +
        `y recibe el rol ${plan.roleToGrant}.`
    );
    if (ok) {
      this.appoint(true);
    }
  }

  /** La cuenta ya existía: se nombra ligándola en vez de crear otra. */
  protected linkExisting(): void {
    const authId = this.existingAccount();
    if (!authId) {
      return;
    }
    this.accountMode.set('LINK');
    this.linkAuthId.set(authId);
    this.existingAccount.set(null);
    this.appoint();
  }

  protected async vacate(): Promise<void> {
    const p = this.position();
    if (!(await confirmDialog('¿Dejar vacante el puesto?', `${p.holderName ?? ''} deja de ser titular; el puesto y sus convencidos se quedan.`))) {
      return;
    }
    this.run(this.organization.vacate(p.id, 'baja del titular'), () =>
      this.changed.emit({ kind: 'self', positionId: p.id, parentId: p.parentId })
    );
  }

  protected async close(): Promise<void> {
    const p = this.position();
    if (!(await confirmDialog('¿Cerrar el puesto?', 'Deja de existir desde hoy; su historia se conserva. Sólo si no tiene puestos debajo.'))) {
      return;
    }
    this.run(this.organization.closePosition(p.id), () =>
      this.changed.emit({ kind: 'closed', positionId: p.id, parentId: p.parentId })
    );
  }

  private run<T>(request: Observable<T>, done: (result: T) => void): void {
    this.busy.set(true);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (result) => {
        this.busy.set(false);
        done(result);
      },
      error: (e: HttpErrorResponse) => {
        this.busy.set(false);
        showError('No se pudo completar', e.error?.message ?? 'Intenta de nuevo');
      },
    });
  }
}
