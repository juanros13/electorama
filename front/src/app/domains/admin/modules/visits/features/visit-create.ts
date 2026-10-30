import { Locality, Municipality, SectionInfo } from '@/app/core/data/model/GeographyTypes';
import { UserService } from '@/app/core/user/user.service';
import { UserCoordinatorZone } from '@/app/core/user/user.types';
import { StructureService } from '@/app/domains/admin/modules/structure/data/structure.service';
import { UppercaseDirective } from '@/app/shared/directives/uppercase';
import { PageHeader } from '@/app/shared/ui/page-header';
import { SectionCard } from '@/app/shared/ui/section-card';
import {
  closeLoading,
  confirmDialog,
  showError,
  showLoading,
  showSuccess,
  showWarning,
} from '@/app/shared/ui/swal';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { VisitService } from '../data/visit.service';
import { NewVisit, SectionByUser, VisitedPersonPayload } from '../data/visit.types';
import { LatLng, VisitLocationMap } from '../ui/visit-location-map';

const NAME_PATTERN = /^[a-zA-ZñÑáéíóúÁÉÍÓÚ\s]+$/;
const PHONE_PATTERN = /^[0-9]{10}$/;
const AGE_PATTERN = /^(1[89]|[2-9][0-9]|10[0-9]|110)$/;
const ROLE_MUNICIPAL = 'CORDINATOR_MUNICIPAL';
const VISIT_NUMBER = 11;

const SEXOS = [
  { value: 'H', viewValue: 'Hombre' },
  { value: 'M', viewValue: 'Mujer' },
];
const RELATED = [
  { value: true, viewValue: 'Sí' },
  { value: false, viewValue: 'No' },
];

@Component({
  selector: 'app-visit-create',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './visit-create.html',
  imports: [
    ReactiveFormsModule,
    UppercaseDirective,
    PageHeader,
    SectionCard,
    VisitLocationMap,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
  ],
})
export default class VisitCreate {
  private fb = inject(FormBuilder);
  private service = inject(VisitService);
  private structure = inject(StructureService);
  private userService = inject(UserService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected readonly sexos = SEXOS;
  protected readonly related = RELATED;

  protected today = new Date().toISOString().slice(0, 10);
  protected minDate = new Date(Date.now() - 7 * 86_400_000).toISOString().slice(0, 10);

  protected user = this.userService.user();
  protected isMunicipal = this.user?.rol === ROLE_MUNICIPAL;

  protected municipalities = signal<Municipality[]>([]);
  protected coordinators = signal<UserCoordinatorZone[]>([]);
  protected sections = signal<SectionByUser[]>([]);
  protected localities = signal<Locality[]>([]);
  protected municipalitiesGeoJson = signal<any | null>(null);
  protected sectionGeoJson = signal<any | null>(null);
  protected saving = signal(false);

  protected form = this.fb.group({
    step1: this.fb.group({
      coordinator: this.fb.control<UserCoordinatorZone | null>(
        { value: null, disabled: !this.isMunicipal },
        Validators.required
      ),
      section: this.fb.control<SectionByUser | null>(
        { value: null, disabled: this.isMunicipal },
        Validators.required
      ),
      municipality: this.fb.control<Municipality | null>({ value: null, disabled: true }, Validators.required),
      locality: this.fb.control<Locality | null>({ value: null, disabled: true }, Validators.required),
      user: this.fb.control<string>({ value: this.user?.name ?? '', disabled: true }),
      visitDate: ['', Validators.required],
    }),
    step2: this.fb.array<FormGroup>([]),
  });

  constructor() {
    this.init();
  }

  get step1(): FormGroup {
    return this.form.get('step1') as FormGroup;
  }
  get step2(): FormArray<FormGroup> {
    return this.form.get('step2') as FormArray<FormGroup>;
  }
  get people(): FormGroup[] {
    return this.step2.controls;
  }

  compareById(a: { id?: number } | null, b: { id?: number } | null): boolean {
    return a && b ? a.id === b.id : a === b;
  }
  compareSection(a: SectionByUser | null, b: SectionByUser | null): boolean {
    return a && b ? a.seccionGeoId === b.seccionGeoId : a === b;
  }
  compareAuthId(a: UserCoordinatorZone | null, b: UserCoordinatorZone | null): boolean {
    return a && b ? a.authId === b.authId : a === b;
  }

  private init(): void {
    this.service
      .getMunicipalitiesByStateId('27')
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((r: any) => this.municipalities.set((r?.municipios ?? []).sort((a: Municipality, b: Municipality) => a.id - b.id)));

    this.service
      .getMunicipalitiesGeoJson()
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((geo) => this.municipalitiesGeoJson.set(geo));

    if (this.isMunicipal) {
      this.service
        .getUsersByMunicipalityCoordinator()
        .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
        .subscribe((users) => this.coordinators.set(users ?? []));
    } else if (this.user) {
      // El propio usuario es el coordinador.
      const self: UserCoordinatorZone = {
        authId: this.user.id,
        authIdReference: this.user.id,
        avatar: '',
        email: this.user.email,
        firstName: this.user.name,
        id: 0,
        lastName: this.user.name,
        password: '',
        status: '',
        enabled: true,
        fullname: this.user.name,
      };
      this.coordinators.set([self]);
      this.step1.get('coordinator')!.setValue(self);
      this.loadSections(this.user.id);
    }

    // Primera fila de captura.
    this.addPerson();
  }

  // --- step1 cascada ---
  onCoordinatorChange(coordinator: UserCoordinatorZone): void {
    this.step1.patchValue({ municipality: null, locality: null, user: coordinator.firstName });
    this.step1.get('section')!.enable();
    this.step1.get('section')!.reset();
    this.sections.set([]);
    this.localities.set([]);
    this.loadSections(coordinator.authId);
  }

  onSectionChange(section: SectionByUser): void {
    this.localities.set([]);
    this.step1.get('locality')!.enable();
    this.step1.get('locality')!.reset();
    this.loadLocalities(String(section.seccionGeoId));
    this.loadSectionInfo(String(section.seccionGeoId));
    this.service
      .getSectionGeoJson(section.seccionGeoId)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((geo) => this.sectionGeoJson.set(geo));
  }

  private loadSections(authId: string): void {
    this.service
      .getSectionsByUser(authId)
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((sections) => this.sections.set((sections ?? []).filter((s) => s.deleted == null)));
  }

  private loadLocalities(sectionId: string): void {
    this.service
      .getLocalitiesBySectionId(sectionId)
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((localities) => this.localities.set(localities ?? []));
  }

  private loadSectionInfo(sectionId: string): void {
    this.service
      .getSectionInfoById(sectionId)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((info: SectionInfo | null) => {
        if (!info) {
          return;
        }
        const municipality = this.municipalities().find((m) => m.id === Number(info.municipioId));
        this.step1.patchValue({ municipality: municipality ?? null });
      });
  }

  // --- step2 FormArray ---
  private buildPerson(): FormGroup {
    return this.fb.group({
      letOpenDoor: [false],
      calle: ['', [Validators.required, Validators.pattern(NAME_PATTERN)]],
      numero: ['', Validators.required],
      latitude: this.fb.control<string>({ value: '', disabled: true }, Validators.required),
      longitude: this.fb.control<string>({ value: '', disabled: true }, Validators.required),
      // visibles solo si abrieron la puerta
      acceptAditionalInformation: [false],
      sex: ['M', Validators.required],
      isRelated: [true, Validators.required],
      age: [''],
      // visibles solo si aceptan más información
      name: [''],
      fatherlastname: [''],
      motherlastname: [''],
      phone: [''],
    });
  }

  addPerson(count = 1): void {
    for (let i = 0; i < count; i++) {
      this.step2.push(this.buildPerson());
    }
  }

  async removePerson(index: number): Promise<void> {
    if (this.step2.length <= 1) {
      return;
    }
    if (await confirmDialog('¿Eliminar visitado?', 'Se quitará esta captura.')) {
      this.step2.removeAt(index);
    }
  }

  onLocation(index: number, location: LatLng): void {
    this.people[index].patchValue({
      latitude: String(location.lat),
      longitude: String(location.lng),
    });
  }

  /** Abrió la puerta: exige edad; si no, la limpia. */
  onOpenDoorToggle(index: number, checked: boolean): void {
    const g = this.people[index];
    this.setValidators(g.get('age')!, checked ? [Validators.required, Validators.pattern(AGE_PATTERN)] : []);
    if (!checked) {
      g.patchValue({ acceptAditionalInformation: false });
      this.onAcceptInfoToggle(index, false);
    }
  }

  /** Acepta más info: exige nombre/apellidos/teléfono. */
  onAcceptInfoToggle(index: number, checked: boolean): void {
    const g = this.people[index];
    this.setValidators(g.get('name')!, checked ? [Validators.required, Validators.pattern(NAME_PATTERN)] : []);
    this.setValidators(g.get('fatherlastname')!, checked ? [Validators.required, Validators.pattern(NAME_PATTERN)] : []);
    this.setValidators(g.get('motherlastname')!, checked ? [Validators.required, Validators.pattern(NAME_PATTERN)] : []);
    this.setValidators(g.get('phone')!, checked ? [Validators.required, Validators.pattern(PHONE_PATTERN)] : []);
  }

  private setValidators(control: FormControl | any, validators: any[]): void {
    control.setValidators(validators);
    control.updateValueAndValidity();
  }

  // --- guardar ---
  private buildVisitedPeople(): VisitedPersonPayload[] {
    return this.people.map((g) => {
      const v = g.getRawValue();
      const base = {
        openTheDoor: !!v.letOpenDoor,
        street: (v.calle as string).toUpperCase(),
        externalNumber: v.numero as string,
        longitude: v.longitude as string,
        latitude: v.latitude as string,
        visitNumber: VISIT_NUMBER,
      };
      if (!v.letOpenDoor) {
        return base;
      }
      return {
        ...base,
        names: v.name ? (v.name as string).toUpperCase() : null,
        lastName: v.fatherlastname ? (v.fatherlastname as string).toUpperCase() : null,
        surName: v.motherlastname ? (v.motherlastname as string).toUpperCase() : null,
        phone: v.phone || null,
        ageOfThePersonWhoOpensTheDoor: v.age,
        sexOfTheOneWhoOpensTheDoor: v.sex as string,
        isRelated: v.isRelated as boolean,
      };
    });
  }

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.people.length === 0) {
      showWarning('Sin visitados', 'Agrega al menos una persona visitada.');
      return;
    }

    const s1 = this.step1.getRawValue();
    const coordinator = s1.coordinator as UserCoordinatorZone;

    this.saving.set(true);
    showLoading('Registrando visitas…');

    // Obtiene el authIdReference del coordinador para la visita.
    this.structure
      .getInfoUserByAuthId(coordinator.authId)
      .pipe(catchError(() => of({ authIdReference: '' })), takeUntilDestroyed(this.destroyRef))
      .subscribe((info: any) => {
        const visit: NewVisit = {
          municipalityId: (s1.municipality as Municipality).id,
          seccionId: (s1.section as SectionByUser).seccionGeoId,
          coordinateZoneId: coordinator.authId,
          localityId: (s1.locality as Locality).id,
          visitDate: s1.visitDate as string,
          numberOfVisited: 0,
          userCreatedId: this.user?.id ?? '1',
          visitedPeople: this.buildVisitedPeople(),
          referenceAuthId: info?.authIdReference ?? '',
        };

        this.service.addNewVisit(visit).subscribe({
          next: () => {
            closeLoading();
            showSuccess('¡Registro exitoso!', `Se registraron ${visit.visitedPeople.length} visita(s).`);
            this.router.navigate(['/admin/visits']);
          },
          error: () => {
            this.saving.set(false);
            closeLoading();
            showError();
          },
        });
      });
  }

  async cancel(): Promise<void> {
    if (this.form.dirty && !(await confirmDialog('¿Estás seguro?', 'Si regresas perderás los cambios.'))) {
      return;
    }
    this.router.navigate(['/admin/visits']);
  }
}
