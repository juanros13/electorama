import { CaptureService } from '@/app/domains/admin/modules/surveys/data/capture.service';
import { CatalogItem } from '@/app/domains/admin/modules/surveys/data/extended.types';
import { CurpResponse } from '@/app/domains/admin/modules/surveys/data/surveys.types';
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
  input,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
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
import { catchError, forkJoin, of } from 'rxjs';

const CURP_PATTERN =
  /^([A-Z][AEIOUX][A-Z]{2}\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\d|3[01])[HM](?:AS|B[CS]|C[CLMSH]|D[FG]|G[TR]|HG|JC|M[CNS]|N[ETL]|OC|PL|Q[TR]|S[PLR]|T[CSL]|VZ|YN|ZS)[B-DF-HJ-NP-TV-Z]{3}[A-Z\d])(\d)$/;

@Component({
  selector: 'app-survey-general-edit',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './survey-general-edit.html',
  imports: [
    ReactiveFormsModule,
    UppercaseDirective,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatStepperModule,
  ],
})
export default class SurveyGeneralEdit {
  readonly survey = input.required<any>();

  private fb = inject(FormBuilder);
  private capture = inject(CaptureService);
  private destroyRef = inject(DestroyRef);

  private surveyFDT: any = null;
  private interviewed: any = null;

  protected today = new Date().toISOString().slice(0, 10);
  protected showStepper = signal(false);
  protected saving = signal(false);
  protected curpSuccessful = signal(false);

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
  protected readonly idDocuments = ['INE', 'PASAPORTE', 'CARTILLA MILITAR', 'CEDULA PROFESIONAL'];
  protected readonly services: CatalogItem[] = [
    { id: 1, name: 'Luz' },
    { id: 2, name: 'Drenaje' },
    { id: 3, name: 'Fosa séptica' },
    { id: 4, name: 'Internet' },
    { id: 5, name: 'Agua' },
  ];
  private selectedServices: CatalogItem[] = [];

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
      housingOther: [{ value: '', disabled: true }],
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
    this.observeReactiveFields();
    this.loadEverything();
  }

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

  compareById(a: CatalogItem | null, b: CatalogItem | null): boolean {
    return a && b ? a.id === b.id : a === b;
  }

  isServiceChecked(service: CatalogItem): boolean {
    return this.selectedServices.some((s) => s.name === service.name);
  }

  private loadEverything(): void {
    forkJoin({
      relationship: this.capture.getRelationship().pipe(catchError(() => of([]))),
      professions: this.capture.getSurveyProfession().pipe(catchError(() => of([]))),
      activities: this.capture.getActivityCatalog().pipe(catchError(() => of([]))),
      programs: this.capture.getFederalProgram().pipe(catchError(() => of([]))),
      disabilities: this.capture.getDisability().pipe(catchError(() => of([]))),
      jobs: this.capture.getSurveyJob().pipe(catchError(() => of([]))),
      type: this.capture.getHousingCatalogs('').pipe(catchError(() => of([]))),
      condition: this.capture.getHousingCatalogs('Condition').pipe(catchError(() => of([]))),
      document: this.capture.getHousingCatalogs('Document').pipe(catchError(() => of([]))),
      floor: this.capture.getHousingCatalogs('MaterialFloor').pipe(catchError(() => of([]))),
      roof: this.capture.getHousingCatalogs('MaterialRoof').pipe(catchError(() => of([]))),
      wall: this.capture.getHousingCatalogs('MaterialWall').pipe(catchError(() => of([]))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => {
        this.relationship.set(r.relationship ?? []);
        this.professions.set(r.professions ?? []);
        this.culturalActivities.set(r.activities ?? []);
        this.federalPrograms.set(r.programs ?? []);
        this.disabilities.set(r.disabilities ?? []);
        this.jobs.set(r.jobs ?? []);
        this.housingTypes.set(r.type ?? []);
        this.housingConditions.set(r.condition ?? []);
        this.housingDocuments.set(r.document ?? []);
        this.floors.set(r.floor ?? []);
        this.roofs.set(r.roof ?? []);
        this.walls.set(r.wall ?? []);
        this.patchValues();
        this.loadLocalities();
      });
  }

  private loadLocalities(): void {
    const sectionId = this.surveyFDT?.sectionId;
    if (!sectionId) {
      return;
    }
    this.capture
      .getLocalitySections(sectionId)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((res) => {
        const nomLoc = res?.LocalitiesSections?.[0]?.nomLoc;
        const options = nomLoc ? [nomLoc] : [];
        if (this.surveyFDT?.locality && !options.includes(this.surveyFDT.locality)) {
          options.unshift(this.surveyFDT.locality);
        }
        this.localityOptions.set([...options, 'Otro']);
      });
  }

  private patchValues(): void {
    const survey = this.survey();
    this.surveyFDT = survey;
    this.interviewed =
      survey.surveyPersonEntityList.find((m: any) => m.curp !== null && m.deleted === null) ?? {};
    const i = this.interviewed;

    this.selectedServices = [...(survey.surveyHousingServicesEntities ?? [])];
    this.birthdayByCurp = i.dateOfBirth ? String(i.dateOfBirth).slice(0, 10) : null;
    this.genderByCurp = i.gender ?? null;

    const isDisabled = i.isDisabled === 'true';
    const hasFederalProgram = i.surveyFederalProgram != null;
    const professionIsOther = i.surveyProfession?.name === 'Otro';
    const jobIsOther = i.surveyJob?.name === 'Otro';
    const localityIsOther = survey.locality === 'Otro';
    const housingIsOther = survey.surveyHousing?.name === 'Otro';
    const culturalIsOther = survey.surveyCulturalActivity?.name === 'Otra';

    // Habilita los condicionales que traen datos (sin disparar valueChanges).
    if (i.ownBusiness === 'true') this.enable(this.step0.get('timeActivity'));
    if (isDisabled) this.enable(this.step0.get('isDisabledName'));
    if (hasFederalProgram) this.enable(this.step0.get('surveyFederalProgram'));
    if (professionIsOther) this.enable(this.step0.get('otherProfession'));
    if (jobIsOther) this.enable(this.step0.get('otherJob'));
    if (localityIsOther) this.enable(this.step2.get('localityOther'));
    if (housingIsOther) this.enable(this.step3.get('housingOther'));
    if (culturalIsOther) this.enable(this.step5.get('otherTypeCulturalActivity'));

    this.step0.patchValue(
      {
        name: i.names,
        lastName: i.lastName,
        surName: i.surName,
        dateOfBirth: this.birthdayByCurp,
        gender: this.genderByCurp,
        landlinePhone: i.landlinePhone,
        mobilePhone: i.mobilePhone,
        curp: i.curp,
        identificationDocument: i.identificationDocument,
        noIdentificationDocument: i.noIdentificationDocument,
        ethnicGroup: i.ethnicGroup === 'true',
        profession: i.surveyProfession ?? null,
        otherProfession: i.otherSurveyProfession ?? '',
        job: i.surveyJob ?? null,
        otherJob: i.otherSurveyJob ?? '',
        afroMexicanGroup: i.afroMexicanGroup === 'true',
        isDisabled,
        isDisabledName: i.isDisabledName ?? null,
        income: i.income,
        familyNumbers: survey.familyNumbers,
        federalProgram: hasFederalProgram,
        surveyFederalProgram: i.surveyFederalProgram ?? null,
        ownBusiness: i.ownBusiness === 'true',
        timeActivity: i.timeActivity ?? '',
        relationship: i.familyRelationship ?? null,
      },
      { emitEvent: false }
    );

    this.step2.patchValue(
      {
        locality: survey.locality,
        localityOther: survey.localityOther ?? '',
        street: survey.street,
        exteriorNumber: survey.exteriorNumber,
        interiorNumber: survey.interiorNumber,
        postalCode: survey.postalCode,
        reference: survey.reference,
      },
      { emitEvent: false }
    );

    this.step3.patchValue(
      {
        housing: survey.surveyHousing ?? null,
        housingOther: survey.housingOther ?? '',
        housingCondition: survey.surveyHousingCondition ?? null,
        housingDocument: survey.surveyHousingDocument ?? null,
        wall: survey.surveyHousingMaterialWall ?? null,
        roof: survey.surveyHousingMaterialRoof ?? null,
        floor: survey.surveyHousingMaterialFloor ?? null,
      },
      { emitEvent: false }
    );

    this.step5.patchValue(
      {
        culturalActivityParticipation: survey.culturalActivityParticipation === 'true',
        knowledgeCulturalActivity: survey.knowledgeCulturalActivity === 'true',
        surveyCulturalActivity: survey.surveyCulturalActivity ?? null,
        otherTypeCulturalActivity: survey.otherTypeCulturalActivity ?? '',
      },
      { emitEvent: false }
    );

    this.showStepper.set(true);
  }

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

  private toggleOn(source: AbstractControl | null, target: AbstractControl | null): void {
    source?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => (value ? this.enable(target) : this.disable(target)));
  }
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
    control.enable({ emitEvent: false });
    control.setValidators([Validators.required]);
    control.updateValueAndValidity({ emitEvent: false });
  }
  private disable(control: AbstractControl | null): void {
    if (!control) {
      return;
    }
    control.clearValidators();
    control.setValue(null, { emitEvent: false });
    control.disable({ emitEvent: false });
    control.updateValueAndValidity({ emitEvent: false });
  }

  toggleService(checked: boolean, service: CatalogItem): void {
    this.selectedServices = checked
      ? [...this.selectedServices, service]
      : this.selectedServices.filter((s) => s.name !== service.name);
  }

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
    this.step0.patchValue({ dateOfBirth: this.birthdayByCurp, gender: this.genderByCurp });

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
      mismatches.forEach((flag) => this.step0.get(controlByFlag[flag])!.setErrors({ manualError: true }));
      this.step0.markAllAsTouched();
      showWarning('¡Existen incidencias!', 'Verifica que el nombre corresponda a la CURP.');
    }
  }

  save(): void {
    if (this.form.invalid || !this.curpSuccessful() || this.saving()) {
      this.form.markAllAsTouched();
      showWarning('Datos incompletos', 'Registra todos los datos obligatorios y valida la CURP.');
      return;
    }
    const survey = this.surveyFDT;
    const s0 = this.step0.getRawValue();
    const loc = this.step2.getRawValue();
    const house = this.step3.getRawValue();
    const cult = this.step5.getRawValue();
    const services = this.selectedServices.map(({ name }) => ({ name }));

    const payload = {
      ...survey,
      familyNumbers: s0.familyNumbers,
      locality: loc.locality,
      localityOther: loc.localityOther,
      street: loc.street,
      exteriorNumber: loc.exteriorNumber,
      interiorNumber: loc.interiorNumber,
      postalCode: loc.postalCode,
      reference: loc.reference,
      surveyHousing: house.housing,
      housingOther: house.housingOther,
      surveyHousingCondition: house.housingCondition,
      surveyHousingDocument: house.housingDocument,
      surveyHousingMaterialWall: house.wall,
      surveyHousingMaterialRoof: house.roof,
      surveyHousingMaterialFloor: house.floor,
      surveyHousingServicesEntities: services,
      culturalActivityParticipation: cult.culturalActivityParticipation,
      knowledgeCulturalActivity: cult.knowledgeCulturalActivity,
      surveyCulturalActivity: cult.surveyCulturalActivity,
      otherTypeCulturalActivity: cult.otherTypeCulturalActivity,
      surveyPersonEntityList: [
        {
          id: this.interviewed.id,
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
          isHeadOfHousehold: this.interviewed.isHeadOfHousehold,
          federalProgram: s0.federalProgram,
          surveyFederalProgram: s0.surveyFederalProgram,
          ownBusiness: s0.ownBusiness,
          timeActivity: s0.timeActivity,
          surveyJob: s0.job,
          otherSurveyJob: s0.otherJob,
          surveyProfession: s0.profession,
          otherSurveyProfession: s0.otherProfession,
          familyRelationship: s0.relationship,
        },
      ],
    };

    this.saving.set(true);
    showLoading('Insertando información…');
    this.capture
      .updateSurvey(payload, survey.id)
      .pipe(catchError((e) => { throw e; }), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          closeLoading();
          showSuccess('¡Actualización exitosa!', 'Los datos generales se guardaron correctamente.').then(
            () => location.reload()
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
