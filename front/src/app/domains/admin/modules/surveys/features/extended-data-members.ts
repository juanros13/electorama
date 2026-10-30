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
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { ExtendedService } from '../data/extended.service';
import { CatalogItem, MemberDocuments } from '../data/extended.types';
import { SurveysService } from '../data/surveys.service';
import { SurveyPerson } from '../data/surveys.types';

const DOC_FIELDS: (keyof MemberDocuments)[] = [
  'identificationApplicantFile',
  'birthCertificateApplicantFile',
  'curpApplicantFile',
  'addressApplicantFile',
  'identificationAssistantFile',
  'birthCertificateAssistantFile',
  'curpAssistantFile',
  'addressAssistantFile',
];

const DOC_LABELS: Record<string, string> = {
  identificationApplicantFile: 'Identificación (solicitante)',
  birthCertificateApplicantFile: 'Acta de nacimiento (solicitante)',
  curpApplicantFile: 'CURP (solicitante)',
  addressApplicantFile: 'Comprobante domicilio (solicitante)',
  identificationAssistantFile: 'Identificación (auxiliar)',
  birthCertificateAssistantFile: 'Acta de nacimiento (auxiliar)',
  curpAssistantFile: 'CURP (auxiliar)',
  addressAssistantFile: 'Comprobante domicilio (auxiliar)',
};

interface MemberMeta {
  person: SurveyPerson;
  isDisable: boolean;
  isOlder: boolean;
  fullName: string;
}

@Component({
  selector: 'app-extended-data-members',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './extended-data-members.html',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
  ],
})
export default class ExtendedDataMembers {
  private fb = inject(FormBuilder);
  private surveysService = inject(SurveysService);
  private extended = inject(ExtendedService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected readonly docFields = DOC_FIELDS;
  protected readonly docLabels = DOC_LABELS;

  private surveyId = this.route.snapshot.paramMap.get('id') ?? '';

  protected loading = signal(true);
  protected saving = signal(false);
  protected members = signal<MemberMeta[]>([]);
  protected memberDocs = signal<Partial<MemberDocuments>[]>([]);

  // catálogos
  protected indigenousLanguages = signal<CatalogItem[]>([]);
  protected origins = signal<CatalogItem[]>([]);
  protected information = signal<CatalogItem[]>([]);
  protected types = signal<CatalogItem[]>([]);
  protected causes = signal<CatalogItem[]>([]);
  protected relationships = signal<CatalogItem[]>([]);

  protected form = this.fb.group({ members: this.fb.array<FormGroup>([]) });

  constructor() {
    this.loadCatalogs();
    this.loadSurvey();
  }

  get memberForms(): FormGroup[] {
    return (this.form.get('members') as FormArray<FormGroup>).controls;
  }

  compareById(a: CatalogItem | null, b: CatalogItem | null): boolean {
    return a && b ? a.id === b.id : a === b;
  }

  private isOlder60to64Male(person: SurveyPerson): boolean {
    if (person.gender !== 'Masculino' || !person.dateOfBirth) {
      return false;
    }
    const birth = new Date(person.dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age >= 60 && age <= 64;
  }

  private loadCatalogs(): void {
    forkJoin({
      lang: this.extended.getIndigenousLanguages().pipe(catchError(() => of([]))),
      origins: this.extended.getOriginsOfDisability().pipe(catchError(() => of([]))),
      info: this.extended.getDisabilityInformation().pipe(catchError(() => of([]))),
      types: this.extended.getTypesOfDisability().pipe(catchError(() => of([]))),
      causes: this.extended.getCausesOfDisability().pipe(catchError(() => of([]))),
      rel: this.extended.getRelationship().pipe(catchError(() => of([]))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => {
        this.indigenousLanguages.set([...(r.lang ?? []), { id: 99, name: 'Ninguna' }]);
        this.origins.set(r.origins ?? []);
        this.information.set(r.info ?? []);
        this.types.set(r.types ?? []);
        this.causes.set(r.causes ?? []);
        this.relationships.set(r.rel ?? []);
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
        this.loading.set(false);
        const people = survey?.surveyPersonEntityList ?? [];
        const metas: MemberMeta[] = [];
        for (const p of people) {
          const isDisable = p.isDisabled === 'true';
          const isOlder = !isDisable && this.isOlder60to64Male(p);
          if (isDisable || isOlder) {
            metas.push({
              person: p,
              isDisable,
              isOlder,
              fullName: `${p.names} ${p.lastName} ${p.surName}`,
            });
          }
        }
        this.members.set(metas);
        const array = this.form.get('members') as FormArray<FormGroup>;
        metas.forEach((m) => array.push(this.buildMemberForm(m)));
        this.memberDocs.set(metas.map(() => ({})));
      });
  }

  private buildMemberForm(meta: MemberMeta): FormGroup {
    return this.fb.group({
      ethnic: [false],
      whichGroup: [''],
      indigenous: [false],
      indigenousLanguage: this.fb.control<CatalogItem | null>(null),
      otherSpeak: [''],
      // discapacidad (solo si isDisable)
      origin: this.fb.control<CatalogItem | null>(null, meta.isDisable ? Validators.required : null),
      information: this.fb.control<CatalogItem | null>(null, meta.isDisable ? Validators.required : null),
      type: this.fb.control<CatalogItem | null>(null, meta.isDisable ? Validators.required : null),
      cause: this.fb.control<CatalogItem | null>(null, meta.isDisable ? Validators.required : null),
      anotherCause: [''],
      healthCertificate: [false],
      whichCertificate: [''],
      requiresHelp: [false],
      // auxiliar / tutor
      nameAux: [''],
      lastNameAux: [''],
      surNameAux: [''],
      relationship: this.fb.control<CatalogItem | null>(null),
      otherRelationship: [''],
      phoneNumber: [''],
    });
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

  back(): void {
    this.router.navigate(['/admin/surveys/complete']);
  }

  async save(): Promise<void> {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      showWarning('Faltan datos', 'Completa los campos obligatorios.');
      return;
    }
    if (!(await confirmDialog('¿Insertar anexos?', 'Se guardarán los datos extendidos.'))) {
      return;
    }

    const annexes = this.memberForms.map((g, i) => {
      const v = g.getRawValue();
      const meta = this.members()[i];
      return {
        surveyPersonId: meta.person.id,
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
        familyRelationship: v.relationship,
        otherRelationShip: v.otherRelationship || '',
        phoneNumber: v.phoneNumber,
        surveyTutorOrAssistant: v.relationship,
        surveyRequestingOrAuxiliaryDocument: null,
      };
    });

    this.saving.set(true);
    showLoading('Insertando información…');
    this.extended
      .saveSurveyAnnex(annexes)
      .pipe(
        catchError((e) => {
          throw e;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
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
    const uploads = this.members()
      .map((m, i) => ({ docs: this.memberDocs()[i], personId: m.person.id }))
      .filter((x) => this.extended.hasAnyDocument(x.docs))
      .map((x) =>
        this.extended.uploadMemberDocuments(x.docs, String(x.personId), this.surveyId).pipe(catchError(() => of(null)))
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
    showSuccess('Finalizado', 'Los datos extendidos se guardaron correctamente.').then(() =>
      this.router.navigate(['/admin/surveys/complete'])
    );
  }
}
