import { UppercaseDirective } from '@/app/shared/directives/uppercase';
import {
  closeLoading,
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
  AbstractControl,
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { CaptureService } from '../data/capture.service';
import { SurveysService } from '../data/surveys.service';
import { CatalogItem } from '../data/extended.types';
import { CurpResponse, SurveyDto } from '../data/surveys.types';

// CURP: 18 caracteres con estructura oficial.
const CURP_PATTERN =
  /^([A-Z][AEIOUX][A-Z]{2}\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\d|3[01])[HM](?:AS|B[CS]|C[CLMSH]|D[FG]|G[TR]|HG|JC|M[CNS]|N[ETL]|OC|PL|Q[TR]|S[PLR]|T[CSL]|VZ|YN|ZS)[B-DF-HJ-NP-TV-Z]{3}[A-Z\d])(\d)$/;

const MAX_MEMBERS = 20;

@Component({
  selector: 'app-surveys-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './surveys-form.html',
  imports: [
    ReactiveFormsModule,
    UppercaseDirective,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatStepperModule,
  ],
})
export default class SurveysForm {
  private fb = inject(FormBuilder);
  private surveysService = inject(SurveysService);
  private capture = inject(CaptureService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  private surveyId = this.route.snapshot.paramMap.get('id') ?? '';
  private surveyFDT: SurveyDto | null = null;

  protected today = new Date().toISOString().slice(0, 10);
  protected showStepper = signal(false);
  protected saving = signal(false);
  protected curpSuccessful = signal(false);

  // catálogos
  protected relationship = signal<CatalogItem[]>([]);
  protected professions = signal<CatalogItem[]>([]);
  protected jobs = signal<CatalogItem[]>([]);
  protected culturalActivities = signal<CatalogItem[]>([]);
  protected federalPrograms = signal<CatalogItem[]>([]);
  protected disabilities = signal<CatalogItem[]>([]);
  protected housingTypes = signal<CatalogItem[]>([]);
  protected housingConditions = signal<CatalogItem[]>([]);
  protected housingDocuments = signal<CatalogItem[]>([]);
  protected floors = signal<CatalogItem[]>([]);
  protected roofs = signal<CatalogItem[]>([]);
  protected walls = signal<CatalogItem[]>([]);
  protected localityOptions = signal<string[]>([]);

  protected readonly genders = ['Masculino', 'Femenino'];
  protected readonly idDocuments = [
    'INE',
    'PASAPORTE',
    'CARTILLA MILITAR',
    'CEDULA PROFESIONAL',
  ];
  protected readonly services: CatalogItem[] = [
    { id: 1, name: 'Luz' },
    { id: 2, name: 'Drenaje' },
    { id: 3, name: 'Fosa séptica' },
    { id: 4, name: 'Internet' },
    { id: 5, name: 'Agua' },
  ];
  private selectedServices: CatalogItem[] = [];

  // datos oficiales obtenidos de la CURP
  private birthdayByCurp: string | null = null;
  private genderByCurp: string | null = null;

  protected form = this.fb.group({
    step0: this.fb.group({
      name: ['', [Validators.required, Validators.pattern('^[a-zA-Z ñÑ]+$')]],
      lastName: ['', [Validators.required, Validators.pattern('^[a-zA-Z ñÑ]+$')]],
      surName: ['', [Validators.required, Validators.pattern('^[a-zA-Z ñÑ]+$')]],
      dateOfBirth: [{ value: '', disabled: true }, Validators.required],
      gender: [{ value: '', disabled: true }, Validators.required],
      landlinePhone: ['', Validators.maxLength(10)],
      mobilePhone: ['', [Validators.required, Validators.maxLength(10)]],
      curp: ['', [Validators.required, Validators.pattern(CURP_PATTERN)]],
      identificationDocument: ['', Validators.required],
      noIdentificationDocument: ['', [Validators.required, Validators.pattern('^[a-zA-Z0-9]*$')]],
      ethnicGroup: [false],
      profession: this.fb.control<CatalogItem | null>(null, Validators.required),
      otherProfession: [{ value: '', disabled: true }],
      job: this.fb.control<CatalogItem | null>(null, Validators.required),
      otherJob: [{ value: '', disabled: true }],
      afroMexicanGroup: [false],
      isDisabled: [false],
      isDisabledName: this.fb.control<CatalogItem | null>({ value: null, disabled: true }),
      income: ['', [Validators.required, Validators.maxLength(10)]],
      familyNumbers: ['', [Validators.required, Validators.maxLength(2)]],
      federalProgram: [false],
      surveyFederalProgram: this.fb.control<CatalogItem | null>({ value: null, disabled: true }),
      ownBusiness: [false],
      timeActivity: [{ value: '', disabled: true }, Validators.maxLength(2)],
      relationship: this.fb.control<CatalogItem | null>(null, Validators.required),
    }),
    step1: this.fb.group({
      familyMembers: this.fb.array<FormGroup>([]),
    }),
    step2: this.fb.group({
      locality: ['', Validators.maxLength(100)],
      localityOther: [{ value: '', disabled: true }, Validators.maxLength(100)],
      street: ['', Validators.required],
      exteriorNumber: ['', Validators.required],
      interiorNumber: ['', Validators.maxLength(100)],
      postalCode: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(5)]],
      reference: ['', Validators.maxLength(100)],
    }),
    step3: this.fb.group({
      housing: this.fb.control<CatalogItem | null>(null, Validators.required),
      housingOther: [''],
      housingCondition: this.fb.control<CatalogItem | null>(null, Validators.required),
      housingDocument: this.fb.control<CatalogItem | null>(null, Validators.required),
      wall: this.fb.control<CatalogItem | null>(null, Validators.required),
      roof: this.fb.control<CatalogItem | null>(null, Validators.required),
      floor: this.fb.control<CatalogItem | null>(null, Validators.required),
    }),
    step4: this.fb.group({}),
    step5: this.fb.group({
      culturalActivityParticipation: [false],
      knowledgeCulturalActivity: [false],
      surveyCulturalActivity: this.fb.control<CatalogItem | null>(null, Validators.required),
      otherTypeCulturalActivity: [{ value: '', disabled: true }, Validators.maxLength(100)],
    }),
  });

  constructor() {
    this.loadCatalogs();
    this.loadHousing();
    this.observeReactiveFields();
    this.loadSurvey();
  }

  // --- getters para el template ---
  get step0(): FormGroup {
    return this.form.get('step0') as FormGroup;
  }
  get step2(): FormGroup {
    return this.form.get('step2') as FormGroup;
  }
  get step3(): FormGroup {
    return this.form.get('step3') as FormGroup;
  }
  get step5(): FormGroup {
    return this.form.get('step5') as FormGroup;
  }
  get familyMembers(): FormArray<FormGroup> {
    return this.form.get('step1.familyMembers') as FormArray<FormGroup>;
  }
  get familyControls(): FormGroup[] {
    return this.familyMembers.controls;
  }

  compareById(a: CatalogItem | null, b: CatalogItem | null): boolean {
    return a && b ? a.id === b.id : a === b;
  }

  // --- carga inicial ---
  private loadCatalogs(): void {
    forkJoin({
      relationship: this.capture.getRelationship().pipe(catchError(() => of([]))),
      professions: this.capture.getSurveyProfession().pipe(catchError(() => of([]))),
      activities: this.capture.getActivityCatalog().pipe(catchError(() => of([]))),
      programs: this.capture.getFederalProgram().pipe(catchError(() => of([]))),
      disabilities: this.capture.getDisability().pipe(catchError(() => of([]))),
      jobs: this.capture.getSurveyJob().pipe(catchError(() => of([]))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => {
        this.relationship.set(r.relationship ?? []);
        this.professions.set(r.professions ?? []);
        this.culturalActivities.set(r.activities ?? []);
        this.federalPrograms.set(r.programs ?? []);
        this.disabilities.set(r.disabilities ?? []);
        this.jobs.set(r.jobs ?? []);
      });
  }

  private loadHousing(): void {
    forkJoin({
      type: this.capture.getHousingCatalogs('').pipe(catchError(() => of([]))),
      condition: this.capture.getHousingCatalogs('Condition').pipe(catchError(() => of([]))),
      document: this.capture.getHousingCatalogs('Document').pipe(catchError(() => of([]))),
      floor: this.capture.getHousingCatalogs('MaterialFloor').pipe(catchError(() => of([]))),
      roof: this.capture.getHousingCatalogs('MaterialRoof').pipe(catchError(() => of([]))),
      wall: this.capture.getHousingCatalogs('MaterialWall').pipe(catchError(() => of([]))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => {
        this.housingTypes.set(r.type ?? []);
        this.housingConditions.set(r.condition ?? []);
        this.housingDocuments.set(r.document ?? []);
        this.floors.set(r.floor ?? []);
        this.roofs.set(r.roof ?? []);
        this.walls.set(r.wall ?? []);
      });
  }

  private loadSurvey(): void {
    if (!this.surveyId) {
      this.router.navigate(['/admin/surveys/complete']);
      return;
    }
    this.surveysService
      .getSurveyById(this.surveyId)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((survey) => {
        if (!survey) {
          showError('No se pudo cargar', 'El censo no está disponible.');
          return;
        }
        this.surveyFDT = survey;
        this.showStepper.set(true);
        if (survey.sectionId) {
          this.loadLocalities(survey.sectionId);
        }
      });
  }

  private loadLocalities(sectionId: number): void {
    this.capture
      .getLocalitySections(sectionId)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((res) => {
        const section = res?.LocalitiesSections?.[0];
        const options = section?.nomLoc ? [section.nomLoc] : [];
        this.localityOptions.set([...options, 'Otro']);
      });
  }

  // --- campos condicionales ---
  private observeReactiveFields(): void {
    const g = this.step0;
    this.toggleOn(g.get('ownBusiness'), g.get('timeActivity'));
    this.toggleOn(g.get('isDisabled'), g.get('isDisabledName'));
    this.toggleOn(g.get('federalProgram'), g.get('surveyFederalProgram'));
    this.toggleOnOther(g.get('profession'), g.get('otherProfession'));
    this.toggleOnOther(g.get('job'), g.get('otherJob'));
    this.toggleOnValue(this.step2.get('locality'), this.step2.get('localityOther'), 'Otro');
    this.toggleOnOther(this.step3.get('housing'), this.step3.get('housingOther'), 'Otro');
    this.toggleOnOther(
      this.step5.get('surveyCulturalActivity'),
      this.step5.get('otherTypeCulturalActivity'),
      'Otra'
    );
  }

  /** Habilita `target` cuando `source` (booleano) es verdadero. */
  private toggleOn(source: AbstractControl | null, target: AbstractControl | null): void {
    source?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => (value ? this.enable(target) : this.disable(target)));
  }

  /** Habilita `target` cuando el catálogo seleccionado en `source` se llama `label`. */
  private toggleOnOther(
    source: AbstractControl | null,
    target: AbstractControl | null,
    label = 'Otro'
  ): void {
    source?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value: CatalogItem | null) =>
        value?.name === label ? this.enable(target) : this.disable(target)
      );
  }

  /** Habilita `target` cuando el valor (string) de `source` es `label`. */
  private toggleOnValue(
    source: AbstractControl | null,
    target: AbstractControl | null,
    label: string
  ): void {
    source?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => (value === label ? this.enable(target) : this.disable(target)));
  }

  private enable(control: AbstractControl | null): void {
    if (!control) {
      return;
    }
    control.enable();
    control.setValidators([Validators.required]);
    control.updateValueAndValidity();
  }

  private disable(control: AbstractControl | null): void {
    if (!control) {
      return;
    }
    control.clearValidators();
    control.setValue(control instanceof FormGroup ? {} : null);
    control.disable();
    control.updateValueAndValidity();
  }

  // --- integrantes de la familia ---
  private buildMember(): FormGroup {
    return this.fb.group({
      names: ['', [Validators.required, Validators.pattern('^[a-zA-Z ñÑ]+$')]],
      lastName: ['', [Validators.required, Validators.pattern('^[a-zA-Z ñÑ]+$')]],
      surName: ['', Validators.pattern('^[a-zA-Z ñÑ]+$')],
      dateOfBirth: ['', Validators.required],
      gender: ['', Validators.required],
      ethnicGroup: [false],
      isDisabled: [false],
      isDisabledName: this.fb.control<CatalogItem | null>({ value: null, disabled: true }),
      federalProgram: [false],
      surveyFederalProgram: this.fb.control<CatalogItem | null>({ value: null, disabled: true }),
      isHeadOfHousehold: [false],
      surveyProfession: this.fb.control<CatalogItem | null>(null, Validators.required),
      otherSurveyProfession: [{ value: '', disabled: true }],
      surveyJob: this.fb.control<CatalogItem | null>(null, Validators.required),
      otherSurveyJob: [{ value: '', disabled: true }],
      familyRelationship: this.fb.control<CatalogItem | null>(null, Validators.required),
    });
  }

  reachedMemberLimit(): boolean {
    return this.familyMembers.length >= MAX_MEMBERS;
  }

  addMember(): void {
    if (this.reachedMemberLimit()) {
      return;
    }
    this.familyMembers.push(this.buildMember());
  }

  removeMember(index: number): void {
    this.familyMembers.removeAt(index);
  }

  /** Habilita/inhabilita un control del integrante según un checkbox. */
  toggleMemberField(checked: boolean, index: number, control: string): void {
    const ctrl = this.familyControls[index].get(control);
    checked ? this.enable(ctrl) : this.disable(ctrl);
  }

  /** Habilita el campo "Otro" del integrante cuando elige la opción "Otro". */
  switchMemberOther(value: CatalogItem | null, index: number, control: string): void {
    const ctrl = this.familyControls[index].get(control);
    value?.name === 'Otro' ? this.enable(ctrl) : this.disable(ctrl);
  }

  /** Solo un integrante puede ser jefe de familia a la vez. */
  setHeadOfHousehold(index: number): void {
    this.familyControls.forEach((control, i) =>
      control.get('isHeadOfHousehold')!.setValue(i === index, { emitEvent: false })
    );
  }

  // --- servicios de la vivienda ---
  toggleService(checked: boolean, service: CatalogItem): void {
    this.selectedServices = checked
      ? [...this.selectedServices, service]
      : this.selectedServices.filter((s) => s.id !== service.id);
  }

  // --- validación de CURP ---
  validateCurp(): void {
    const { curp, name, lastName, surName } = this.step0.getRawValue();
    showLoading('Validando CURP', 'Por favor espera…');
    this.capture
      .validateCurp(curp ?? '', name ?? '', lastName ?? '', surName ?? '')
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((resp) => {
        closeLoading();
        if (!resp || resp.error !== undefined) {
          this.curpSuccessful.set(false);
          showError('¡CURP inválida!', 'Revise el formato de la CURP.');
          return;
        }
        this.applyCurpResult(resp);
      });
  }

  private applyCurpResult(curp: CurpResponse): void {
    this.birthdayByCurp = curp.Fecha ? curp.Fecha.slice(0, 10) : null;
    this.genderByCurp = curp.sexo === 1 ? 'Masculino' : 'Femenino';
    this.step0.patchValue({
      dateOfBirth: this.birthdayByCurp,
      gender: this.genderByCurp,
    });

    // Las banderas vienen en false cuando el nombre no coincide con la CURP.
    const mismatches = (['nombre', 'apellidoPaterno', 'apellidoMaterno'] as const).filter(
      (k) => curp[k] === false
    );
    const controlByFlag: Record<string, string> = {
      nombre: 'name',
      apellidoPaterno: 'lastName',
      apellidoMaterno: 'surName',
    };

    if (mismatches.length === 0) {
      ['name', 'lastName', 'surName'].forEach((c) => this.step0.get(c)!.setErrors(null));
      this.curpSuccessful.set(true);
      showSuccess('¡CURP validada!', 'Puede continuar.');
    } else {
      this.curpSuccessful.set(false);
      mismatches.forEach((flag) => {
        this.step0.get(controlByFlag[flag])!.setErrors({ manualError: true });
      });
      this.step0.markAllAsTouched();
      showWarning('¡Existen incidencias!', 'Verifica que el nombre corresponda a la CURP.');
    }
  }

  // --- envío ---
  submit(): void {
    if (this.form.invalid || !this.curpSuccessful() || this.saving()) {
      this.form.markAllAsTouched();
      showWarning('Datos incompletos', 'Registra todos los datos obligatorios y valida la CURP.');
      return;
    }
    const survey = this.surveyFDT!;
    const s0 = this.step0.getRawValue();
    const loc = this.step2.getRawValue();
    const house = this.step3.getRawValue();
    const cult = this.step5.getRawValue();
    const familyMembers = this.familyMembers.getRawValue();

    const interviewed = {
      names: s0.name,
      lastName: s0.lastName,
      surName: s0.surName,
      dateOfBirth: this.birthdayByCurp,
      gender: this.genderByCurp,
      landlinePhone: s0.landlinePhone,
      mobilePhone: s0.mobilePhone,
      curp: s0.curp,
      identificationDocument: s0.identificationDocument,
      noIdentificationDocument: s0.noIdentificationDocument,
      ethnicGroup: s0.ethnicGroup,
      afroMexicanGroup: s0.afroMexicanGroup,
      isDisabled: s0.isDisabled,
      isDisabledName: s0.isDisabledName,
      income: s0.income,
      isHeadOfHousehold: true,
      federalProgram: s0.federalProgram,
      surveyFederalProgram: s0.surveyFederalProgram,
      ownBusiness: s0.ownBusiness,
      timeActivity: s0.timeActivity,
      surveyJob: s0.job,
      otherSurveyJob: s0.otherJob,
      surveyProfession: s0.profession,
      otherSurveyProfession: s0.otherProfession,
      familyRelationship: s0.relationship,
    };

    const payload = {
      id: survey.id,
      surveyUserFolio: survey.surveyUserFolio,
      folioHouse: survey.folioHouse,
      surveyDate: survey.surveyDate,
      latitude: survey.latitude,
      longitude: survey.longitude,
      familyNumbers: s0.familyNumbers,
      deviceId: survey.deviceId,
      pathPhotoIneAdverse: survey.pathPhotoIneAdverse,
      pathPhotoIneReverse: survey.pathPhotoIneReverse,
      pathPhotoFacade: survey.pathPhotoFacade,
      pathPhotoFacadeQr: survey.pathPhotoFacadeQr,
      surveyUser: {
        id: survey.surveyUser.id,
        deviceId: survey.surveyUser.deviceId,
        name: survey.surveyUser.name,
        authId: survey.surveyUser.authId,
        userName: survey.surveyUser.userName,
        userPersonId: survey.surveyUser.id,
        municipalyId: survey.surveyUser.id,
        municipalityName: survey.surveyUser.municipalityName,
      },
      localityId: survey.localityId,
      locality: loc.locality,
      localityOther: loc.localityOther,
      street: loc.street,
      exteriorNumber: loc.exteriorNumber,
      interiorNumber: loc.interiorNumber,
      postalCode: loc.postalCode,
      reference: survey.reference,
      surveyPersonEntityList: [interviewed, ...familyMembers],
      surveyHousing: house.housing,
      housingOther: house.housingOther,
      surveyHousingCondition: house.housingCondition,
      surveyHousingDocument: house.housingDocument,
      surveyHousingMaterialWall: house.wall,
      surveyHousingMaterialRoof: house.roof,
      surveyHousingMaterialFloor: house.floor,
      surveyHousingServicesEntities: this.selectedServices,
      culturalActivityParticipation: cult.culturalActivityParticipation,
      knowledgeCulturalActivity: cult.knowledgeCulturalActivity,
      surveyCulturalActivity: cult.surveyCulturalActivity,
      otherTypeCulturalActivity: cult.otherTypeCulturalActivity,
      created: survey.created,
      updated: survey.updated,
      deleted: survey.deleted,
      statusChange: survey.statusChange,
      sectionId: 0,
      sectionNumber: 0,
    };

    this.saving.set(true);
    showLoading('Insertando información…');
    this.capture
      .updateSurvey(payload, this.surveyId)
      .pipe(catchError((e) => { throw e; }), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          closeLoading();
          showSuccess('¡Actualización exitosa!', 'La información se guardó correctamente.').then(() =>
            this.router.navigate(['/admin/surveys/extended', this.surveyId])
          );
        },
        error: () => {
          this.saving.set(false);
          closeLoading();
          showError();
        },
      });
  }
}
