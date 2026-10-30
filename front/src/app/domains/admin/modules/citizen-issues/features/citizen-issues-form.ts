import { RolType } from '@/app/core/data/enum/RolType';
import { Municipality } from '@/app/core/data/model/GeographyTypes';
import { GeographyService } from '@/app/core/geography/geography.service';
import { UserService } from '@/app/core/user/user.service';
import { UppercaseDirective } from '@/app/shared/directives/uppercase';
import { PageHeader } from '@/app/shared/ui/page-header';
import { SectionCard } from '@/app/shared/ui/section-card';
import {
  compressImageFile,
  dataUrlToFile,
  readAsDataURL,
} from '@/app/shared/utils/image-compress';
import {
  closeLoading,
  confirmDialog,
  showError,
  showLoading,
  showSuccess,
} from '@/app/shared/ui/swal';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
  viewChild,
  ElementRef,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, forkJoin, of, switchMap } from 'rxjs';
import { CitizenIssuesService } from '../data/citizen-issues.service';
import {
  IssueCategory,
  IssueDetailDAO,
  IssueDetailDTO,
  IssueDTO,
  IssueSolutionDocumentsCategory,
  IssueStatus,
  IssueTypes,
  Resource,
} from '../data/citizen-issues.types';
import { ResourceDialog } from '../ui/resource-dialog';
import { FileKind, TypeFileDialog } from '../ui/type-file-dialog';

interface Locality {
  id: number;
  localidad: string;
}

interface FilePreview {
  type: 'img' | 'doc';
  file: string; // data URL
  uuid: string;
}

const CURP_PATTERN =
  '\\b[A-Z][AEIOU][A-Z]{2}[0-9]{2}(?:0[1-9]|1[0-2])(?:[1-2][0-9]|0[1-9]|3[0-1])[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}(?:[0-9]|[A-Z])[0-9]\\b';

// Categorías que habilitan campos de beneficiario (CURP), recurso o comprobante.
const CATEGORIES_BENEFICIARY = ['PROGRAMAS BIENESTAR', 'PROGRAMAS JCF/BECAS'];
const CATEGORIES_RESOURCE = ['INSUMOS MEDICOS', 'APOYO VARIOS (COMPRAS)', 'EDUCACIÓN'];
const CATEGORIES_FILE = [
  'INSUMOS MEDICOS',
  'GESTIONES MEDICAS',
  'APOYO JURIDICO',
  'MEDICAMENTO',
];
const MAX_FILE_BYTES = 10 * 1024 * 1024;

@Component({
  selector: 'app-citizen-issues-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './citizen-issues-form.html',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    UppercaseDirective,
    PageHeader,
    SectionCard,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
})
export default class CitizenIssuesForm {
  private fb = inject(FormBuilder);
  private service = inject(CitizenIssuesService);
  private geography = inject(GeographyService);
  private userService = inject(UserService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  private pdfInput = viewChild.required<ElementRef<HTMLInputElement>>('pdfInput');
  private imgInput = viewChild.required<ElementRef<HTMLInputElement>>('imgInput');

  protected municipalities = signal<Municipality[]>([]);
  protected filteredLocalities = signal<Locality[]>([]);
  protected types = signal<IssueTypes[]>([]);
  protected statuses = signal<IssueStatus[]>([]);
  protected categories = signal<IssueCategory[]>([]);
  protected documentCategories = signal<IssueSolutionDocumentsCategory[]>([]);

  /** Recursos disponibles y filtrados, alineados por índice del FormArray. */
  protected filteredResources = signal<Resource[][]>([[]]);
  private allResources: Resource[][] = [[]];

  /** Previsualizaciones de archivo por índice del FormArray. */
  protected filesPreview = signal<(FilePreview | null)[]>([null]);

  protected today = new Date().toISOString().slice(0, 10);
  protected saving = signal(false);
  protected editId = signal<string | null>(null);
  protected editDao = signal<IssueDetailDAO | null>(null);
  /** En edición no se pueden agregar/eliminar incidencias. */
  protected disabledFormArray = signal(false);

  private localities: Locality[] = [];

  protected enableAddItems =
    this.userService.hasRole(RolType.ADMIN) ||
    this.userService.hasRole(RolType.ISSUES_SUPEVISOR);

  protected form = this.fb.group({
    step1: this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
      cellPhoneNumber: ['', Validators.required],
      municipality: this.fb.control<Municipality | null>(null, Validators.required),
      locality: this.fb.control<Locality | null>(null),
      localityOther: this.fb.control<string | null>({ value: null, disabled: true }, Validators.required),
      searchText: [''],
    }),
    step2: this.fb.array([this.buildDetail()]),
  });

  constructor() {
    this.loadCatalogs();
  }

  // --- getters de conveniencia para el template ---
  get step1(): FormGroup {
    return this.form.get('step1') as FormGroup;
  }
  get step2(): FormArray {
    return this.form.get('step2') as FormArray;
  }
  get details(): FormGroup[] {
    return this.step2.controls as FormGroup[];
  }

  compareById(a: { id: number } | null, b: { id: number } | null): boolean {
    return a && b ? a.id === b.id : a === b;
  }

  private buildDetail(): FormGroup {
    return this.fb.group({
      issueType: this.fb.control<IssueTypes | null>(null, Validators.required),
      issueStatus: this.fb.control<IssueStatus | null>(null, Validators.required),
      issueCategory: this.fb.control<IssueCategory | null>(null, Validators.required),
      comments: ['', Validators.required],
      urgent: [false],
      name: this.fb.control<string | null>({ value: null, disabled: true }, [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
      ]),
      lastName: this.fb.control<string | null>({ value: null, disabled: true }, [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
      ]),
      surName: this.fb.control<string | null>({ value: null, disabled: true }, [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
      ]),
      curp: this.fb.control<string | null>({ value: null, disabled: true }, Validators.pattern(CURP_PATTERN)),
      issueSolutionResourceTmp: this.fb.control<Resource | null>(
        { value: null, disabled: true },
        Validators.required
      ),
      searchResource: [''],
      file: this.fb.control<string | null>({ value: null, disabled: true }),
      issueSolutionDocumentsCategory: this.fb.control<IssueSolutionDocumentsCategory | null>(null),
      uuid: [crypto.randomUUID()],
    });
  }

  // --- localidad ---
  onMunicipalityChange(municipality: Municipality): void {
    this.step1.patchValue({ locality: null, localityOther: '', searchText: '' });
    this.step1.get('localityOther')!.disable();
    this.localities = [];
    this.filteredLocalities.set([]);
    this.geography
      .getLocalitiesByMunicipalityName(municipality.nomMun)
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((resp: any[]) => {
        this.localities = [{ id: 0, localidad: 'OTRA' }, ...this.dedupe(resp ?? [])];
        this.filteredLocalities.set(this.localities);
      });
  }

  filterLocalities(): void {
    const text = (this.step1.get('searchText')!.value ?? '').toLowerCase();
    const filtered = this.localities.filter((l) => l.localidad.toLowerCase().includes(text));
    this.filteredLocalities.set(filtered.length ? filtered : [{ id: 0, localidad: 'Sin Resultados' }]);
  }

  onLocalityChange(locality: Locality): void {
    const other = this.step1.get('localityOther')!;
    other.patchValue('');
    if (locality?.localidad === 'OTRA') {
      other.enable();
    } else {
      other.disable();
    }
  }

  clearLocality(): void {
    this.step1.patchValue({ locality: null, localityOther: '', searchText: '' });
    this.step1.get('localityOther')!.disable();
    this.filteredLocalities.set(this.localities);
  }

  // --- FormArray ---
  addDetail(): void {
    this.step2.push(this.buildDetail());
    this.allResources.push([]);
    this.filteredResources.update((r) => [...r, []]);
    this.filesPreview.update((f) => [...f, null]);
  }

  removeDetail(index: number): void {
    if (index < 1 || index >= this.step2.length) {
      return;
    }
    this.step2.removeAt(index);
    this.allResources.splice(index, 1);
    this.filteredResources.update((r) => r.filter((_, i) => i !== index));
    this.filesPreview.update((f) => f.filter((_, i) => i !== index));
  }

  // --- categoría: habilita/inhabilita campos condicionales ---
  onCategoryChange(category: IssueCategory, index: number): void {
    const g = this.details[index];
    const name = category?.name;

    this.toggle(g, ['name', 'lastName', 'surName', 'curp'], CATEGORIES_BENEFICIARY.includes(name));

    const resourceEnabled = CATEGORIES_RESOURCE.includes(name);
    this.toggle(g, ['issueSolutionResourceTmp'], resourceEnabled);
    if (resourceEnabled) {
      this.service
        .getResoursesByCategoryId(String(category.id))
        .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
        .subscribe((resources) => {
          this.allResources[index] = resources ?? [];
          this.filteredResources.update((r) => {
            const copy = [...r];
            copy[index] = this.allResources[index];
            return copy;
          });
        });
    }

    const fileEnabled = CATEGORIES_FILE.includes(name);
    const fileCtrl = g.get('file')!;
    if (fileEnabled) {
      fileCtrl.enable();
    } else {
      fileCtrl.disable();
      fileCtrl.setValue(null);
      g.get('issueSolutionDocumentsCategory')!.setValue(null);
      this.setPreview(index, null);
    }
  }

  private toggle(group: FormGroup, controls: string[], enable: boolean): void {
    for (const c of controls) {
      const ctrl = group.get(c)!;
      if (enable) {
        ctrl.enable();
      } else {
        ctrl.disable();
        ctrl.setValue(null);
      }
    }
  }

  // --- recursos ---
  filterResources(index: number): void {
    const text = (this.details[index].get('searchResource')!.value ?? '').toLowerCase();
    const filtered = this.allResources[index].filter((r) => r.name.toLowerCase().includes(text));
    this.filteredResources.update((r) => {
      const copy = [...r];
      copy[index] = filtered.length ? filtered : [{ id: 0, name: 'Sin Resultados' } as Resource];
      return copy;
    });
  }

  openResourceDialog(event: Event, index: number): void {
    event.preventDefault();
    event.stopPropagation();
    const rowCategory = this.details[index].get('issueCategory')!.value as IssueCategory;
    this.dialog
      .open(ResourceDialog, {
        data: { type: 'RECURSO', categories: this.categories() },
        width: '480px',
      })
      .afterClosed()
      .subscribe((created: Resource | undefined) => {
        if (!created) {
          return;
        }
        if (created.issueCategory?.id === rowCategory?.id) {
          this.allResources[index] = [created, ...this.allResources[index]];
          this.filteredResources.update((r) => {
            const copy = [...r];
            copy[index] = this.allResources[index];
            return copy;
          });
        }
      });
  }

  // --- archivos ---
  private activeRow = 0;

  openFileDialog(event: Event, index: number): void {
    event.preventDefault();
    event.stopPropagation();
    this.activeRow = index;
    this.dialog
      .open(TypeFileDialog, { width: '420px' })
      .afterClosed()
      .subscribe((result: { typeFile: FileKind } | undefined) => {
        if (!result) {
          return;
        }
        if (result.typeFile === 'IMG') {
          this.details[index].get('issueSolutionDocumentsCategory')!.setValue(this.documentCategories()[0] ?? null);
          this.imgInput().nativeElement.click();
        } else {
          this.details[index].get('issueSolutionDocumentsCategory')!.setValue(this.documentCategories()[1] ?? null);
          this.pdfInput().nativeElement.click();
        }
      });
  }

  async onImageSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) {
      return;
    }
    const compressed = await compressImageFile(file);
    if (dataUrlToFile(compressed, file.name).size > MAX_FILE_BYTES) {
      showError('Archivo muy grande', 'La imagen excede el tamaño máximo de 10MB.');
      return;
    }
    const i = this.activeRow;
    this.details[i].get('file')!.patchValue(file.name);
    this.setPreview(i, { type: 'img', file: compressed, uuid: this.details[i].get('uuid')!.value });
  }

  async onPdfSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) {
      return;
    }
    if (file.type !== 'application/pdf') {
      showError('Archivo inválido', 'Selecciona un archivo PDF.');
      return;
    }
    if (file.size > MAX_FILE_BYTES) {
      showError('Archivo muy grande', 'El PDF excede el tamaño máximo de 10MB.');
      return;
    }
    const i = this.activeRow;
    const dataUrl = await readAsDataURL(file);
    this.details[i].get('file')!.patchValue(file.name);
    this.setPreview(i, { type: 'doc', file: dataUrl, uuid: this.details[i].get('uuid')!.value });
  }

  removeFile(index: number): void {
    this.details[index].get('file')!.setValue(null);
    this.details[index].get('issueSolutionDocumentsCategory')!.setValue(null);
    this.setPreview(index, null);
  }

  downloadExistingFile(path: string): void {
    this.service
      .downloadFile(path)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = path.replace(/\//g, '');
        link.click();
        URL.revokeObjectURL(url);
      });
  }

  private setPreview(index: number, preview: FilePreview | null): void {
    this.filesPreview.update((f) => {
      const copy = [...f];
      copy[index] = preview;
      return copy;
    });
  }

  // --- navegación / guardado ---
  async cancel(): Promise<void> {
    if (this.form.dirty && !(await confirmDialog('¿Estás seguro?', 'Si regresas perderás los cambios.'))) {
      return;
    }
    this.router.navigate(['/admin/citizen-issues']);
  }

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.editId()) {
      this.updateIssue();
    } else {
      this.createIssue();
    }
  }

  private createIssue(): void {
    const s1 = this.step1.getRawValue();
    const details = this.details.map((g) => g.getRawValue() as IssueDetailDTO);
    const issue: IssueDTO = {
      registrationDate: this.today,
      fullName: (s1.fullName as string).trim(),
      phone: (s1.cellPhoneNumber as string).trim(),
      municipalityId: (s1.municipality as Municipality).id,
      localityId: (s1.locality as Locality | null)?.id ?? 0,
      localityOther: (s1.localityOther as string | null)?.trim() ?? '',
      issueDetailEntities: details,
    };

    this.saving.set(true);
    showLoading('Registrando incidencia…');
    this.service
      .createIssue(issue)
      .pipe(
        switchMap((created) => {
          const uploads = this.buildUploads(created.issueDetailEntities ?? []);
          return uploads.length ? forkJoin(uploads) : of([]);
        }),
        catchError((err) => {
          throw err;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          closeLoading();
          showSuccess('¡Registro exitoso!', 'La incidencia se registró correctamente.');
          this.router.navigate(['/admin/citizen-issues']);
        },
        error: () => {
          this.saving.set(false);
          closeLoading();
          showError();
        },
      });
  }

  /** Construye los uploads de archivos casando preview.uuid con el detalle creado. */
  private buildUploads(created: { id: number; uuid: string; issueSolutionDocumentsCategory?: IssueSolutionDocumentsCategory }[]) {
    const previews = this.filesPreview();
    const uploads = [];
    for (const detail of created) {
      const preview = previews.find((p) => p?.uuid === detail.uuid);
      if (!preview) {
        continue;
      }
      const fileName = preview.type === 'img' ? 'comprobante.jpeg' : 'comprobante.pdf';
      const formData = new FormData();
      formData.append('file', dataUrlToFile(preview.file, fileName), fileName);
      formData.append('IssueDetailId', String(detail.id));
      formData.append('IssueSolutionDocumentsCategory', String(detail.issueSolutionDocumentsCategory?.id ?? ''));
      formData.append('uuid', detail.uuid);
      uploads.push(this.service.saveIssueFile(formData));
    }
    return uploads;
  }

  private updateIssue(): void {
    const dao = this.editDao()!;
    const g = this.details[0];
    const raw = g.getRawValue();

    // Status ATENDIDO o sin cambios: solo confirmar y volver.
    if ((raw.issueStatus as IssueStatus)?.name === 'ATENDIDO' || !this.form.dirty) {
      showSuccess('¡Actualización exitosa!', 'La incidencia se actualizó correctamente.');
      this.router.navigate(['/admin/citizen-issues']);
      return;
    }

    const detail: IssueDetailDAO = {
      ...dao,
      comments: (raw.comments as string).trim(),
      issueType: raw.issueType as IssueTypes,
      issueCategory: raw.issueCategory as IssueCategory,
      issueStatus: raw.issueStatus as IssueStatus,
      urgent: !!raw.urgent,
      issueSolutionDocumentsCategory:
        (raw.issueSolutionDocumentsCategory as IssueSolutionDocumentsCategory) ?? dao.issueSolutionDocumentsCategory,
      pathFile: (raw.file as string) ?? dao.pathFile,
    };

    const fileChanged = dao.pathFile !== raw.file && !!this.filesPreview()[0];

    this.saving.set(true);
    showLoading('Actualizando incidencia…');
    this.service
      .updateIssueDetail(detail, this.editId()!)
      .pipe(
        switchMap(() => {
          if (!fileChanged) {
            return of(null);
          }
          const preview = this.filesPreview()[0]!;
          const fileName = preview.type === 'img' ? 'comprobante.jpeg' : 'comprobante.pdf';
          const formData = new FormData();
          formData.append('file', dataUrlToFile(preview.file, fileName), fileName);
          formData.append('IssueDetailId', String(detail.id));
          formData.append('IssueSolutionDocumentsCategory', String(detail.issueSolutionDocumentsCategory?.id ?? ''));
          formData.append('uuid', detail.uuid);
          return this.service.saveIssueFile(formData);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          closeLoading();
          showSuccess('¡Actualización exitosa!', `Folio ${dao.issue.invoice} actualizado.`);
          this.router.navigate(['/admin/citizen-issues']);
        },
        error: () => {
          this.saving.set(false);
          closeLoading();
          showError();
        },
      });
  }

  // --- carga de catálogos / edición ---
  private loadCatalogs(): void {
    forkJoin({
      municipalities: this.geography.getMunicipalitiesByStateId('27').pipe(catchError(() => of([]))),
      types: this.service.getCatIssuesTypes().pipe(catchError(() => of(null))),
      statuses: this.service.getCatIssuesStatus().pipe(catchError(() => of(null))),
      categories: this.service.getCatIssuesCategory().pipe(catchError(() => of(null))),
      docs: this.service.getCatIssueSolutionDocumentsCategory().pipe(catchError(() => of(null))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => {
        this.municipalities.set([...(r.municipalities as Municipality[])].sort((a, b) => a.id - b.id));
        this.types.set((r.types as any)?.catIssueTypes ?? []);
        this.statuses.set((r.statuses as any)?.catIssueStatus ?? []);
        this.categories.set((r.categories as any)?.catIssueCategory ?? []);
        this.documentCategories.set((r.docs as any)?.catIssueSolutionDocumentsCategory ?? []);
        this.loadForEdit();
      });
  }

  private loadForEdit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.editId.set(id);
    this.disabledFormArray.set(true);

    this.service
      .getIssueDetailById(id)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((dao) => {
        if (!dao) {
          showError('No se pudo cargar', 'La incidencia no está disponible.');
          this.router.navigate(['/admin/citizen-issues']);
          return;
        }
        this.editDao.set(dao);
        this.patchForEdit(dao);
      });
  }

  private patchForEdit(dao: IssueDetailDAO): void {
    this.step1.patchValue({
      fullName: dao.issue.fullName,
      cellPhoneNumber: dao.issue.phone,
    });
    this.step1.get('municipality')!.disable();
    this.step1.get('locality')!.disable();

    const g = this.details[0];
    g.patchValue({
      urgent: dao.urgent,
      issueType: dao.issueType,
      issueStatus: dao.issueStatus,
      issueCategory: dao.issueCategory,
      comments: dao.comments,
    });

    if (CATEGORIES_FILE.includes(dao.issueCategory?.name)) {
      g.get('file')!.enable();
      g.get('file')!.patchValue(dao.pathFile);
      if (dao.issueSolutionDocumentsCategory && dao.pathFile) {
        this.service
          .loadFile(dao.pathFile)
          .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
          .subscribe(async (blob) => {
            if (!blob) {
              return;
            }
            const dataUrl = await readAsDataURL(blob);
            const type = dao.issueSolutionDocumentsCategory.id === 1 ? 'img' : 'doc';
            this.setPreview(0, { type, file: dataUrl, uuid: g.get('uuid')!.value });
          });
      }
    }

    // No editable cuando está ATENDIDO (status id 3).
    if (dao.issueStatus?.id === 3) {
      this.step1.disable();
      this.step2.disable();
    }
  }

  private dedupe(localities: Locality[]): Locality[] {
    const seen = new Set<string>();
    return localities.filter((l) => (seen.has(l.localidad) ? false : (seen.add(l.localidad), true)));
  }
}
