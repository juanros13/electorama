import { CaptureService } from '@/app/domains/admin/modules/surveys/data/capture.service';
import { CatalogItem } from '@/app/domains/admin/modules/surveys/data/extended.types';
import {
  closeLoading,
  confirmDialog,
  showError,
  showLoading,
  showSuccess,
  showWarning,
} from '@/app/shared/ui/swal';
import { UppercaseDirective } from '@/app/shared/directives/uppercase';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  input,
  OnInit,
  output,
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
import { catchError, forkJoin, of } from 'rxjs';

const MAX_MEMBERS = 20;

@Component({
  selector: 'app-survey-families-edit',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './survey-families-edit.html',
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
export default class SurveyFamiliesEdit implements OnInit {
  readonly survey = input.required<any>();
  readonly members = input<any[]>([]);
  readonly activeFamilies = input(false);
  readonly deactivateFamilies = output<boolean>();

  private fb = inject(FormBuilder);
  private capture = inject(CaptureService);
  private destroyRef = inject(DestroyRef);

  protected today = new Date().toISOString().slice(0, 10);
  protected showStepper = signal(false);
  protected saving = signal(false);

  protected relationship = signal<CatalogItem[]>([]);
  protected professions = signal<CatalogItem[]>([]);
  protected jobs = signal<CatalogItem[]>([]);
  protected disabilities = signal<CatalogItem[]>([]);
  protected federalPrograms = signal<CatalogItem[]>([]);

  protected readonly genders = ['Masculino', 'Femenino'];

  protected familyMembers = this.fb.array<FormGroup>([]);

  constructor() {
    this.loadCatalogs();
  }

  ngOnInit(): void {
    for (const person of this.members()) {
      this.familyMembers.push(this.buildMember(person));
    }
    if (this.activeFamilies()) {
      this.addMember();
    }
    this.showStepper.set(true);
  }

  get familyControls(): FormGroup[] {
    return this.familyMembers.controls;
  }

  compareById(a: CatalogItem | null, b: CatalogItem | null): boolean {
    return a && b ? a.id === b.id : a === b;
  }

  private loadCatalogs(): void {
    forkJoin({
      relationship: this.capture.getRelationship().pipe(catchError(() => of([]))),
      professions: this.capture.getSurveyProfession().pipe(catchError(() => of([]))),
      jobs: this.capture.getSurveyJob().pipe(catchError(() => of([]))),
      disabilities: this.capture.getDisability().pipe(catchError(() => of([]))),
      programs: this.capture.getFederalProgram().pipe(catchError(() => of([]))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => {
        this.relationship.set(r.relationship ?? []);
        this.professions.set(r.professions ?? []);
        this.jobs.set(r.jobs ?? []);
        this.disabilities.set(r.disabilities ?? []);
        this.federalPrograms.set(r.programs ?? []);
      });
  }

  private buildMember(person?: any): FormGroup {
    const isDisabled = person?.isDisabled === 'true';
    const hasFederalProgram = person?.surveyFederalProgram != null;
    const professionIsOther = person?.surveyProfession?.name === 'Otro';
    const jobIsOther = person?.surveyJob?.name === 'Otro';

    return this.fb.group({
      id: [person?.id ?? null],
      names: [person?.names ?? '', [Validators.required, Validators.pattern('^[a-zA-Z ñÑ]+$')]],
      lastName: [person?.lastName ?? '', [Validators.required, Validators.pattern('^[a-zA-Z ñÑ]+$')]],
      surName: [person?.surName ?? '', Validators.pattern('^[a-zA-Z ñÑ]+$')],
      dateOfBirth: [person?.dateOfBirth ? String(person.dateOfBirth).slice(0, 10) : '', Validators.required],
      gender: [person?.gender ?? '', Validators.required],
      surveyProfession: this.fb.control<CatalogItem | null>(person?.surveyProfession ?? null, Validators.required),
      otherSurveyProfession: [{ value: person?.otherSurveyProfession ?? '', disabled: !professionIsOther }],
      surveyJob: this.fb.control<CatalogItem | null>(person?.surveyJob ?? null, Validators.required),
      otherSurveyJob: [{ value: person?.otherSurveyJob ?? '', disabled: !jobIsOther }],
      ethnicGroup: [person?.ethnicGroup === 'true'],
      familyRelationship: this.fb.control<CatalogItem | null>(person?.familyRelationship ?? null),
      isDisabled: [isDisabled],
      federalProgram: [hasFederalProgram],
      isDisabledName: this.fb.control<CatalogItem | null>({ value: person?.isDisabledName ?? null, disabled: !isDisabled }),
      surveyFederalProgram: this.fb.control<CatalogItem | null>({ value: person?.surveyFederalProgram ?? null, disabled: !hasFederalProgram }),
      isHeadOfHousehold: [person?.isHeadOfHousehold ?? false],
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

  async removeMember(index: number): Promise<void> {
    const group = this.familyControls[index];
    const id = group.get('id')!.value;
    if (id === null || id === undefined) {
      this.familyMembers.removeAt(index);
      if (this.activeFamilies() && this.familyMembers.length === 0) {
        this.deactivateFamilies.emit(false);
      }
      return;
    }
    if (
      !(await confirmDialog(
        '¿Borrar a este integrante?',
        'La información de esta persona será eliminada definitivamente.'
      ))
    ) {
      return;
    }
    const member = { ...group.getRawValue(), deleted: new Date().toISOString() };
    showLoading('Actualizando…');
    this.capture
      .deletePersonEntity({ ...this.survey(), surveyPersonEntityList: [member] }, this.survey().id)
      .pipe(catchError((e) => { throw e; }), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          closeLoading();
          showSuccess('¡Familiar borrado!', 'La información se actualizó correctamente.').then(() =>
            location.reload()
          );
        },
        error: () => {
          closeLoading();
          showError();
        },
      });
  }

  toggleMemberField(checked: boolean, index: number, control: string): void {
    const ctrl = this.familyControls[index].get(control);
    checked ? this.enable(ctrl) : this.disable(ctrl);
  }

  switchMemberOther(value: CatalogItem | null, index: number, control: string): void {
    const ctrl = this.familyControls[index].get(control);
    value?.name === 'Otro' ? this.enable(ctrl) : this.disable(ctrl);
  }

  setHeadOfHousehold(index: number): void {
    this.familyControls.forEach((control, i) =>
      control.get('isHeadOfHousehold')!.setValue(i === index, { emitEvent: false })
    );
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

  save(): void {
    if (this.familyMembers.invalid || this.saving()) {
      this.familyMembers.markAllAsTouched();
      showWarning('Datos incompletos', 'Registra todos los datos obligatorios antes de continuar.');
      return;
    }
    const members = this.familyMembers.getRawValue();
    this.saving.set(true);
    showLoading('Insertando información…');
    this.capture
      .updatePersonEntity({ ...this.survey(), surveyPersonEntityList: members }, this.survey().id)
      .pipe(catchError((e) => { throw e; }), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          closeLoading();
          showSuccess('¡Actualización exitosa!', 'Los datos de la familia se guardaron correctamente.').then(
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
