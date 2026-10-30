import { ExtendedService } from '@/app/domains/admin/modules/surveys/data/extended.service';
import { CatalogItem, MemberDocuments } from '@/app/domains/admin/modules/surveys/data/extended.types';
import { UppercaseDirective } from '@/app/shared/directives/uppercase';
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
  input,
  OnInit,
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
import { catchError, forkJoin, of } from 'rxjs';

const DOC_FIELDS: { key: keyof MemberDocuments; label: string; assistant: boolean }[] = [
  { key: 'identificationApplicantFile', label: 'Identificación (solicitante)', assistant: false },
  { key: 'birthCertificateApplicantFile', label: 'Acta de nacimiento (solicitante)', assistant: false },
  { key: 'curpApplicantFile', label: 'CURP (solicitante)', assistant: false },
  { key: 'addressApplicantFile', label: 'Comprobante domicilio (solicitante)', assistant: false },
  { key: 'identificationAssistantFile', label: 'Identificación (auxiliar)', assistant: true },
  { key: 'birthCertificateAssistantFile', label: 'Acta de nacimiento (auxiliar)', assistant: true },
  { key: 'curpAssistantFile', label: 'CURP (auxiliar)', assistant: true },
  { key: 'addressAssistantFile', label: 'Comprobante domicilio (auxiliar)', assistant: true },
];

interface MemberMeta {
  person: any;
  annex: any | null;
  isDisable: boolean;
  isOlder: boolean;
  fullName: string;
}

@Component({
  selector: 'app-survey-annex-edit',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './survey-annex-edit.html',
  imports: [
    ReactiveFormsModule,
    UppercaseDirective,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
  ],
})
export default class SurveyAnnexEdit implements OnInit {
  readonly survey = input.required<any>();
  readonly disableMembers = input<any[]>([]);
  readonly olderMembers = input<any[]>([]);

  private fb = inject(FormBuilder);
  private extended = inject(ExtendedService);
  private destroyRef = inject(DestroyRef);

  protected readonly docFields = DOC_FIELDS;

  protected saving = signal(false);
  protected metas = signal<MemberMeta[]>([]);
  protected memberDocs = signal<Partial<MemberDocuments>[]>([]);

  protected relationship = signal<CatalogItem[]>([]);
  protected indigenousLanguages = signal<CatalogItem[]>([]);
  protected origins = signal<CatalogItem[]>([]);
  protected information = signal<CatalogItem[]>([]);
  protected types = signal<CatalogItem[]>([]);
  protected causes = signal<CatalogItem[]>([]);

  protected form = this.fb.group({ members: this.fb.array<FormGroup>([]) });

  constructor() {
    this.loadCatalogs();
  }

  ngOnInit(): void {
    const metas: MemberMeta[] = [
      ...this.disableMembers().map((p) => this.toMeta(p, true, false)),
      ...this.olderMembers().map((p) => this.toMeta(p, false, true)),
    ];
    this.metas.set(metas);
    const array = this.membersArray;
    metas.forEach((m) => array.push(this.buildMemberForm(m)));
    this.memberDocs.set(metas.map(() => ({})));
  }

  get membersArray(): FormArray<FormGroup> {
    return this.form.get('members') as FormArray<FormGroup>;
  }
  get memberForms(): FormGroup[] {
    return this.membersArray.controls;
  }

  compareById(a: CatalogItem | null, b: CatalogItem | null): boolean {
    return a && b ? a.id === b.id : a === b;
  }

  private toMeta(person: any, isDisable: boolean, isOlder: boolean): MemberMeta {
    return {
      person,
      annex: this.selectLastAnnex(person.surveyAnnex),
      isDisable,
      isOlder,
      fullName: `${person.names} ${person.lastName} ${person.surName}`,
    };
  }

  /** El anexo vigente es el de mayor id (último actualizado). */
  private selectLastAnnex(annexes: any[]): any | null {
    if (!annexes?.length) {
      return null;
    }
    return annexes.reduce((max, a) => (a.id > (max?.id ?? -1) ? a : max), annexes[0]);
  }

  private loadCatalogs(): void {
    forkJoin({
      rel: this.extended.getRelationship().pipe(catchError(() => of([]))),
      lang: this.extended.getIndigenousLanguages().pipe(catchError(() => of([]))),
      origins: this.extended.getOriginsOfDisability().pipe(catchError(() => of([]))),
      info: this.extended.getDisabilityInformation().pipe(catchError(() => of([]))),
      types: this.extended.getTypesOfDisability().pipe(catchError(() => of([]))),
      causes: this.extended.getCausesOfDisability().pipe(catchError(() => of([]))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => {
        this.relationship.set(r.rel ?? []);
        this.indigenousLanguages.set([...(r.lang ?? []), { id: 99, name: 'Ninguna' }]);
        this.origins.set(r.origins ?? []);
        this.information.set(r.info ?? []);
        this.types.set(r.types ?? []);
        this.causes.set(r.causes ?? []);
      });
  }

  private buildMemberForm(meta: MemberMeta): FormGroup {
    const a = meta.annex;
    const ethnic = a?.ethnicOrAfromexicanGroup === 'SI';
    const speaks = a?.speaksIndigenousLanguage === 'SI';
    const healthCert = a?.healthCertificate === 'SI';
    const reqDisable = meta.isDisable ? Validators.required : null;

    return this.fb.group({
      id: [a?.id ?? null],
      surveyPersonId: [meta.person.id],
      ethnic: [ethnic],
      whichGroup: [{ value: a?.whichGroup ?? '', disabled: !ethnic }],
      indigenous: [speaks],
      indigenousLanguage: this.fb.control<CatalogItem | null>({ value: a?.surveyIndigenousLanguage ?? null, disabled: !speaks }),
      otherSpeak: [a?.otherSpeak ?? ''],
      // discapacidad (solo si isDisable)
      origin: this.fb.control<CatalogItem | null>({ value: a?.surveyOriginOfDisability ?? null, disabled: !meta.isDisable }, reqDisable),
      information: this.fb.control<CatalogItem | null>({ value: a?.surveyDisabilityInformation ?? null, disabled: !meta.isDisable }, reqDisable),
      type: this.fb.control<CatalogItem | null>({ value: a?.surveyTypeOfDisability ?? null, disabled: !meta.isDisable }, reqDisable),
      cause: this.fb.control<CatalogItem | null>({ value: a?.surveyCausesOfDisability ?? null, disabled: !meta.isDisable }, reqDisable),
      anotherCause: [a?.anotherCause ?? ''],
      healthCertificate: [{ value: healthCert, disabled: !meta.isDisable }],
      whichCertificate: [{ value: a?.whichCertificate ?? '', disabled: !healthCert }],
      requiresHelp: [{ value: a?.requiriesHelp === 'SI', disabled: !meta.isDisable }],
      // auxiliar / tutor
      nameAux: [a?.namesAux ?? ''],
      lastNameAux: [a?.lastNameAux ?? ''],
      surNameAux: [a?.surNameAux ?? ''],
      relationship: this.fb.control<CatalogItem | null>(a?.familyRelationship ?? null),
      otherRelationship: [a?.otherRelationShip ?? ''],
      phoneNumber: [a?.phoneNumber ?? ''],
    });
  }

  toggleField(checked: boolean, index: number, control: string): void {
    const ctrl = this.memberForms[index].get(control);
    checked ? this.enable(ctrl) : this.disable(ctrl);
  }

  private enable(control: AbstractControl | null): void {
    if (!control) {
      return;
    }
    control.enable({ emitEvent: false });
    control.updateValueAndValidity({ emitEvent: false });
  }
  private disable(control: AbstractControl | null): void {
    if (!control) {
      return;
    }
    control.setValue(null, { emitEvent: false });
    control.disable({ emitEvent: false });
    control.updateValueAndValidity({ emitEvent: false });
  }

  onFile(event: Event, index: number, field: keyof MemberDocuments): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) {
      return;
    }
    this.memberDocs.update((docs) => {
      const copy = docs.map((d) => ({ ...d }));
      copy[index][field] = file;
      return copy;
    });
  }

  docName(index: number, field: keyof MemberDocuments): string {
    return this.memberDocs()[index]?.[field]?.name ?? 'Sin archivo';
  }

  private buildAnnex(group: FormGroup, meta: MemberMeta): any {
    const v = group.getRawValue();
    const base = meta.annex ? { ...meta.annex } : {};
    return {
      ...base,
      id: v.id,
      surveyPersonId: v.surveyPersonId,
      ethnicOrAfromexicanGroup: v.ethnic ? 'SI' : 'NO',
      whichGroup: v.ethnic ? v.whichGroup : null,
      speaksIndigenousLanguage: v.indigenous ? 'SI' : 'NO',
      surveyIndigenousLanguage: v.indigenous ? v.indigenousLanguage : null,
      otherSpeak: v.otherSpeak || '',
      hasADisability: meta.isDisable ? 'SI' : 'NO',
      surveyOriginOfDisability: meta.isDisable ? v.origin : null,
      surveyDisabilityInformation: meta.isDisable ? v.information : null,
      surveyTypeOfDisability: meta.isDisable ? v.type : null,
      surveyCausesOfDisability: meta.isDisable ? v.cause : null,
      anotherCause: meta.isDisable ? v.anotherCause : null,
      healthCertificate: meta.isDisable ? (v.healthCertificate ? 'SI' : 'NO') : null,
      whichCertificate: meta.isDisable ? v.whichCertificate : null,
      requiriesHelp: meta.isDisable ? (v.requiresHelp ? 'SI' : 'NO') : null,
      namesAux: v.nameAux,
      lastNameAux: v.lastNameAux,
      surNameAux: v.surNameAux,
      familyRelationship: v.relationship || null,
      otherRelationShip: v.otherRelationship || '',
      phoneNumber: v.phoneNumber,
      surveyTutorOrAssistant: v.relationship || null,
      surveyRequestingOrAuxiliaryDocument: null,
    };
  }

  async save(): Promise<void> {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      showWarning('Faltan datos', 'Completa los campos obligatorios.');
      return;
    }
    if (!(await confirmDialog('¿Insertar anexos?', 'Se guardarán los datos de los anexos.'))) {
      return;
    }

    const annexes = this.memberForms.map((g, i) => this.buildAnnex(g, this.metas()[i]));
    const toUpdate = annexes.filter((a) => a.id != null);
    const toCreate = annexes
      .filter((a) => a.id == null)
      .map((a) => {
        const { id, ...rest } = a;
        return rest;
      });

    const requests = [
      ...toUpdate.map((a) => this.extended.updateSurveyAnnex(a, a.id)),
      ...(toCreate.length ? [this.extended.saveSurveyAnnex(toCreate)] : []),
    ];

    if (requests.length === 0) {
      this.uploadDocuments();
      return;
    }

    this.saving.set(true);
    showLoading('Insertando información…');
    forkJoin(requests)
      .pipe(catchError((e) => { throw e; }), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.uploadDocuments(),
        error: () => {
          this.saving.set(false);
          closeLoading();
          showError();
        },
      });
  }

  private uploadDocuments(): void {
    showLoading('Subiendo archivos…');
    const uploads = this.metas()
      .map((m, i) => ({ docs: this.memberDocs()[i], personId: m.person.id }))
      .filter((x) => this.extended.hasAnyDocument(x.docs))
      .map((x) =>
        this.extended
          .uploadMemberDocuments(x.docs, String(x.personId), String(this.survey().id))
          .pipe(catchError(() => of(null)))
      );

    if (uploads.length === 0) {
      this.finish();
      return;
    }
    forkJoin(uploads)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.finish());
  }

  private finish(): void {
    closeLoading();
    showSuccess('Finalizado', 'Los anexos se guardaron correctamente.').then(() => location.reload());
  }
}
