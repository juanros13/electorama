import { LEVEL_NAMES } from '@/app/core/data/levels';
import { PageHeader } from '@/app/shared/ui/page-header';
import { SectionCard } from '@/app/shared/ui/section-card';
import { showError, showSuccess, showWarning } from '@/app/shared/ui/swal';
import { getBirthdateFromCurp, getSexFromCurp } from '@/app/shared/utils/curp';
import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
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
import type { Geometry } from 'geojson';
import { catchError, distinctUntilChanged, map, of, switchMap } from 'rxjs';
import {
  CaptureRequest,
  CaptureService,
  CaptureTarget,
  PeopleInvited,
  SupporterRow,
} from '../data/capture.service';
import { TerritoryService } from '../data/territory.service';
import { CatalogSection } from '../data/territory.types';
import { GeoPoint, MapSupporter, SupporterLocationMap } from './supporter-location-map';

const STATE_ID = 27;

const CURP = /^[A-Z][AEIOUX][A-Z]{2}\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}[0-9A-Z]\d$/;
const INE = /^[A-Z]{6}\d{8}[A-Z]\d{3}$/;
const PHONE = /^\d{10}$/;

interface Form {
  supportsMovement: boolean;
  names: string;
  lastName: string;
  surName: string;
  curp: string;
  ine: string;
  ineValidity: number | null;
  birthdate: string;
  sex: 'HOMBRE' | 'MUJER' | null;
  cellPhoneNumber: string;
  supportWhatsapp: boolean;
  cellPhoneNumberOther: string;
  email: string;
  sectionGeoId: number | null;
  street: string;
  externalNumber: string;
  internalNumber: string;
  neighborhood: string;
  zipCode: string;
  committeeParticipation: boolean;
  pollingRepParticipation: boolean;
  invitingOthers: boolean;
  numberOfPeopleInvited: PeopleInvited | null;
}

const EMPTY: Form = {
  supportsMovement: true,
  names: '',
  lastName: '',
  surName: '',
  curp: '',
  ine: '',
  ineValidity: null,
  birthdate: '',
  sex: null,
  cellPhoneNumber: '',
  supportWhatsapp: true,
  cellPhoneNumberOther: '',
  email: '',
  sectionGeoId: null,
  street: '',
  externalNumber: '',
  internalNumber: '',
  neighborhood: '',
  zipCode: '',
  committeeParticipation: false,
  pollingRepParticipation: false,
  invitingOthers: false,
  numberOfPeopleInvited: null,
};

/**
 * Captura de convencidos en la web, con los mismos datos que la app.
 *
 * Guarda directo en msvc-structure: el convencido queda en el puesto de quien
 * captura o, para promotores y referentes, en uno de su rama. La rama la
 * valida el servidor; aquí sólo se ofrece lo que devuelve `capture-targets`.
 */
@Component({
  selector: 'app-supporter-capture',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './supporter-capture.html',
  imports: [
    DatePipe,
    DecimalPipe,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    PageHeader,
    SectionCard,
    SupporterLocationMap,
  ],
})
export default class SupporterCapture {
  private captureApi = inject(CaptureService);
  private territory = inject(TerritoryService);
  private destroyRef = inject(DestroyRef);

  protected readonly invitedOptions: { value: PeopleInvited; label: string }[] = [
    { value: 'uno', label: '1' },
    { value: 'dos', label: '2' },
    { value: 'tres', label: '3' },
    { value: 'cuatro', label: '4' },
    { value: 'cinco', label: '5' },
    { value: 'masdecinco', label: 'Más de 5' },
  ];
  protected readonly currentYear = new Date().getFullYear();

  // ─────────────────────────── dónde queda

  protected targets = signal<CaptureTarget[]>([]);
  protected targetsLoading = signal(true);
  protected targetId = signal<number | null>(null);
  protected target = computed(
    () => this.targets().find((t) => t.positionId === this.targetId()) ?? null
  );

  protected sections = signal<CatalogSection[]>([]);
  /**
   * Si el puesto es de una sección, el convencido sólo puede ser de ésa: no se
   * ofrece elegir otra (msvc-structure también lo valida).
   */
  protected fixedSection = computed(() => {
    const id = this.target()?.sectionGeoId;
    return id == null ? null : (this.sections().find((s) => s.id === id) ?? null);
  });

  // ─────────────────────────── el formulario

  protected form = signal<Form>({ ...EMPTY });
  /** Dónde vive el convencido; se limpia al guardar, como el resto del formulario. */
  protected location = signal<GeoPoint | null>(null);
  /** El polígono de la sección elegida: el mapa no deja marcar fuera. */
  protected sectionGeometry = signal<Geometry | null>(null);
  protected saving = signal(false);

  // ─────────────────────────── los ya capturados

  protected supporters = signal<SupporterRow[]>([]);
  protected supportersLoading = signal(false);
  /** Los ya capturados que traen ubicación, para el mapa. */
  protected mapSupporters = computed<MapSupporter[]>(() =>
    this.supporters()
      .filter((s) => s.latitude != null && s.longitude != null)
      .map((s) => ({ lat: s.latitude!, lng: s.longitude!, name: s.personName || 'sin nombre' }))
  );
  private reload = signal(0);

  constructor() {
    this.captureApi
      .getTargets()
      .pipe(
        catchError(() => {
          showError('No se pudo saber dónde puedes capturar');
          return of([] as CaptureTarget[]);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((targets) => {
        this.targets.set(targets);
        this.targetsLoading.set(false);
        this.pickTarget(targets.find((t) => t.own && t.level === 'PROMOTER') ?? targets[0] ?? null);
      });

    // las secciones del municipio del puesto elegido
    toObservable(this.target)
      .pipe(
        map((t) => t?.municipalityGeoId ?? null),
        distinctUntilChanged(),
        switchMap((mun) =>
          this.territory
            .getSections(STATE_ID, { municipalityId: mun, status: true }, 0, mun ? 500 : 1500)
            .pipe(
              map((page) =>
                [...page.content].sort((a, b) => a.section.localeCompare(b.section))
              ),
              catchError(() => of([] as CatalogSection[]))
            )
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((sections) => this.sections.set(sections));

    // el polígono de la sección: al cambiar, el punto marcado ya no vale
    toObservable(computed(() => this.form().sectionGeoId))
      .pipe(
        distinctUntilChanged(),
        switchMap((id) => {
          this.location.set(null);
          return id
            ? this.territory.getSectionGeometry(id).pipe(catchError(() => of(null)))
            : of(null);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((geometry) => this.sectionGeometry.set(geometry));

    // los convencidos del puesto elegido
    toObservable(computed(() => ({ id: this.targetId(), n: this.reload() })))
      .pipe(
        switchMap(({ id }) => {
          if (!id) {
            return of([] as SupporterRow[]);
          }
          this.supportersLoading.set(true);
          return this.captureApi.getByPosition(id).pipe(catchError(() => of([] as SupporterRow[])));
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((rows) => {
        this.supporters.set(rows);
        this.supportersLoading.set(false);
      });
  }

  protected levelLabel(t: CaptureTarget): string {
    return LEVEL_NAMES[t.level]?.one ?? t.level;
  }

  protected pickTarget(t: CaptureTarget | null): void {
    this.targetId.set(t?.positionId ?? null);
    // la sección del puesto: fija si el puesto es de sección; si es de
    // municipio (referente), se elige entre las suyas
    this.set('sectionGeoId', t?.sectionGeoId ?? null);
  }

  protected selectTarget(id: number | string): void {
    this.pickTarget(this.targets().find((t) => t.positionId === Number(id)) ?? null);
  }

  protected set<K extends keyof Form>(key: K, value: Form[K]): void {
    this.form.update((f) => ({ ...f, [key]: value }));
  }

  /** La CURP trae sexo y fecha de nacimiento. */
  protected setCurp(value: string): void {
    const curp = (value ?? '').toUpperCase().trim();
    this.form.update((f) => {
      const next = { ...f, curp };
      if (curp.length === 18) {
        next.sex = getSexFromCurp(curp) ?? f.sex;
        next.birthdate = getBirthdateFromCurp(curp) ?? f.birthdate;
      }
      return next;
    });
  }

  protected save(): void {
    const f = this.form();
    const t = this.target();
    const problem = this.validate(f, t);
    if (problem) {
      showWarning(problem);
      return;
    }
    const supports = f.supportsMovement;
    const request: CaptureRequest = {
      positionId: t!.positionId,
      supportsMovement: supports,
      sex: f.sex,
      sectionGeoId: f.sectionGeoId,
      street: f.street,
      externalNumber: f.externalNumber,
      internalNumber: f.internalNumber,
      neighborhood: f.neighborhood,
      zipCode: f.zipCode,
      latitude: this.location()?.lat ?? null,
      longitude: this.location()?.lng ?? null,
      ...(supports
        ? {
            names: f.names,
            lastName: f.lastName,
            surName: f.surName,
            curp: f.curp,
            ine: f.ine || undefined,
            ineValidity: f.ineValidity,
            birthdate: f.birthdate || null,
            cellPhoneNumber: f.cellPhoneNumber,
            supportWhatsapp: f.supportWhatsapp,
            cellPhoneNumberOther: f.cellPhoneNumberOther,
            email: f.email,
            committeeParticipation: f.committeeParticipation,
            pollingRepParticipation: f.pollingRepParticipation,
            invitingOthers: f.invitingOthers,
            numberOfPeopleInvited: f.invitingOthers ? f.numberOfPeopleInvited : null,
          }
        : {}),
    };
    this.saving.set(true);
    this.captureApi
      .capture(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (done) => {
          this.saving.set(false);
          showSuccess(
            'Convencido guardado',
            `Quedó en ${done.positionLabel}` +
              (done.newPerson ? '.' : '; la persona ya estaba en el padrón y se completaron sus datos.')
          );
          // se queda la sección y el puesto: se suele capturar a varios de la misma zona
          this.form.set({ ...EMPTY, sectionGeoId: f.sectionGeoId });
          this.location.set(null);
          this.reload.update((n) => n + 1);
        },
        error: (e: HttpErrorResponse) => {
          this.saving.set(false);
          if (e.status === 409 && e.error?.detail?.code === 'ALREADY_SUPPORTER') {
            showWarning('Ya es convencido', `Está registrado en ${e.error.detail.where}.`);
            return;
          }
          if (e.status === 400 && e.error?.detail?.code === 'OUTSIDE_SECTION') {
            showWarning('La ubicación está fuera de la sección', 'Marca el domicilio dentro del área sombreada del mapa.');
            return;
          }
          if (e.status === 400 || e.status === 403) {
            showWarning('No se pudo guardar', e.error?.message);
            return;
          }
          showError('No se pudo guardar', e.error?.message ?? 'Intenta de nuevo');
        },
      });
  }

  private validate(f: Form, t: CaptureTarget | null): string | null {
    if (!t) {
      return 'Elige en qué puesto queda el convencido';
    }
    if (!f.sectionGeoId) {
      return 'Falta la sección donde vive';
    }
    if (!f.sex) {
      return 'Falta el sexo';
    }
    if (!f.supportsMovement) {
      return null;
    }
    if (f.names.trim().length < 2 || f.lastName.trim().length < 2) {
      return 'Falta el nombre y el primer apellido';
    }
    if (!CURP.test(f.curp)) {
      return 'La CURP no es válida (18 caracteres)';
    }
    if (f.ine && !INE.test(f.ine.toUpperCase())) {
      return 'La clave de elector no es válida (18 caracteres)';
    }
    if (f.ineValidity != null && (f.ineValidity < 2000 || f.ineValidity > this.currentYear + 10)) {
      return 'La vigencia de la INE no es válida';
    }
    if (f.cellPhoneNumber && !PHONE.test(f.cellPhoneNumber)) {
      return 'El teléfono lleva 10 dígitos';
    }
    if (f.invitingOthers && !f.numberOfPeopleInvited) {
      return 'Falta a cuántas personas invita';
    }
    return null;
  }
}
