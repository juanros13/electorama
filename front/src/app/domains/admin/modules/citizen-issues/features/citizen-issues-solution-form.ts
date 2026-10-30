import { RolType } from '@/app/core/data/enum/RolType';
import { UserService } from '@/app/core/user/user.service';
import { FollowerService } from '@/app/domains/admin/modules/follower/data/follower.service';
import { UppercaseDirective } from '@/app/shared/directives/uppercase';
import { PageHeader } from '@/app/shared/ui/page-header';
import {
  closeLoading,
  confirmDialog,
  showError,
  showLoading,
  showSuccess,
} from '@/app/shared/ui/swal';
import { getBirthdateFromCurp, getSexFromCurp } from '@/app/shared/utils/curp';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';
import { CitizenIssuesService } from '../data/citizen-issues.service';
import {
  IssueDetailDAO,
  Provider,
  Resource,
  SolutionDTO,
} from '../data/citizen-issues.types';
import { ResourceDialog } from '../ui/resource-dialog';

const CURP_PATTERN =
  '\\b[A-Z][AEIOU][A-Z]{2}[0-9]{2}(?:0[1-9]|1[0-2])(?:[1-2][0-9]|0[1-9]|3[0-1])[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}(?:[0-9]|[A-Z])[0-9]\\b';

@Component({
  selector: 'app-citizen-issues-solution-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './citizen-issues-solution-form.html',
  imports: [
    ReactiveFormsModule,
    UppercaseDirective,
    PageHeader,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatStepperModule,
  ],
})
export default class CitizenIssuesSolutionForm {
  private fb = inject(FormBuilder);
  private service = inject(CitizenIssuesService);
  private followers = inject(FollowerService);
  private userService = inject(UserService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  protected today = new Date().toISOString().slice(0, 10);
  protected saving = signal(false);
  protected dao = signal<IssueDetailDAO | null>(null);
  /** Si ya tiene solución: la vista es de solo lectura. */
  protected readonly = signal(false);

  protected sexOptions = signal<any[]>([]);
  protected providers = signal<Provider[]>([]);
  protected filteredProviders = signal<Provider[]>([]);
  protected resources = signal<Resource[]>([]);
  protected filteredResources = signal<Resource[]>([]);

  protected enableAddItems =
    this.userService.hasRole(RolType.ADMIN) ||
    this.userService.hasRole(RolType.ISSUES_SUPEVISOR);

  protected form = this.fb.group({
    step1: this.fb.group({
      names: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      surName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      cellPhoneNumber: ['', Validators.required],
      curp: ['', Validators.pattern(CURP_PATTERN)],
      sex: this.fb.control<any>(null, Validators.required),
      birthdate: this.fb.control<string | null>(null),
    }),
    step2: this.fb.group({
      issueResource: this.fb.control<Resource | null>(null, Validators.required),
      issueResourceQty: [1, [Validators.required, Validators.min(0)]],
      dateSolution: [this.today, Validators.required],
      provider: this.fb.control<Provider | null>(null, Validators.required),
      comments: ['', Validators.required],
      searchResource: [''],
      searchProvider: [''],
    }),
  });

  constructor() {
    this.loadCatalogs();
    this.watchCurp();
  }

  get step1(): FormGroup {
    return this.form.get('step1') as FormGroup;
  }
  get step2(): FormGroup {
    return this.form.get('step2') as FormGroup;
  }

  compareById(a: { id?: number } | null, b: { id?: number } | null): boolean {
    return a && b ? a.id === b.id : a === b;
  }

  // --- CURP → sexo + fecha de nacimiento ---
  private watchCurp(): void {
    this.step1
      .get('curp')!
      .valueChanges.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((curp: string | null) => {
        if (this.step1.get('curp')!.invalid || !curp) {
          this.step1.patchValue({ sex: null, birthdate: null }, { emitEvent: false });
          return;
        }
        const sexName = getSexFromCurp(curp);
        const match = this.sexOptions().find((s) => s.name === sexName);
        this.step1.patchValue(
          { sex: match ?? null, birthdate: getBirthdateFromCurp(curp) },
          { emitEvent: false }
        );
      });
  }

  // --- filtros de recurso / proveedor ---
  filterResources(): void {
    const text = (this.step2.get('searchResource')!.value ?? '').toLowerCase();
    const filtered = this.resources().filter((r) => r.name.toLowerCase().includes(text));
    this.filteredResources.set(filtered.length ? filtered : [{ id: 0, name: 'Sin Resultados' } as Resource]);
  }

  filterProviders(): void {
    const text = (this.step2.get('searchProvider')!.value ?? '').toLowerCase();
    const filtered = this.providers().filter((p) => p.name.toLowerCase().includes(text));
    this.filteredProviders.set(filtered.length ? filtered : [{ id: 0, name: 'Sin Resultados' } as Provider]);
  }

  openResourceDialog(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.dialog
      .open(ResourceDialog, {
        data: { type: 'RECURSO', categories: this.dao()?.issueCategory ? [this.dao()!.issueCategory] : [] },
        width: '480px',
      })
      .afterClosed()
      .subscribe((created: Resource | undefined) => {
        if (!created) {
          return;
        }
        this.resources.update((r) => [created, ...r]);
        this.filteredResources.set(this.resources());
      });
  }

  openProviderDialog(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.dialog
      .open(ResourceDialog, { data: { type: 'PROVEEDOR', categories: [] }, width: '480px' })
      .afterClosed()
      .subscribe((created: Provider | undefined) => {
        if (!created) {
          return;
        }
        this.providers.update((p) => [created, ...p]);
        this.filteredProviders.set(this.providers());
      });
  }

  downloadFile(path: string): void {
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

  async cancel(): Promise<void> {
    if (this.form.dirty && !(await confirmDialog('¿Estás seguro?', 'Si regresas perderás los cambios.'))) {
      return;
    }
    this.router.navigate(['/admin/citizen-issues']);
  }

  /** Marca la incidencia EN PROCESO (id 2) sin registrar aún la solución. */
  markInProgress(): void {
    const dao = this.dao();
    if (!dao || this.saving()) {
      return;
    }
    this.saving.set(true);
    showLoading('Actualizando estatus…');
    this.service
      .updateIssueDetail({ ...dao, issueStatus: { id: 2, name: 'EN PROCESO' } }, String(dao.id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          closeLoading();
          showSuccess('¡Actualización exitosa!', 'La incidencia quedó EN PROCESO.');
          this.router.navigate(['/admin/citizen-issues']);
        },
        error: () => {
          this.saving.set(false);
          closeLoading();
          showError();
        },
      });
  }

  /** Registra el beneficiario + la solución y cierra la incidencia (ATENDIDO). */
  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    const dao = this.dao()!;
    if (dao.issueSolution) {
      this.router.navigate(['/admin/citizen-issues']);
      return;
    }

    const s1 = this.step1.getRawValue();
    const follower = {
      names: (s1.names as string).trim(),
      lastName: (s1.lastName as string).trim(),
      surName: (s1.surName as string).trim(),
      cellPhoneNumber: (s1.cellPhoneNumber as string).trim(),
      curp: s1.curp,
      catSex: s1.sex,
      birthdate: s1.birthdate ?? '2000-01-01',
      birthEntityId: 1977,
      sectionNumber: '0000',
    };

    this.saving.set(true);
    showLoading('Registrando solución…');
    this.followers
      .createCitizenFollower(follower)
      .pipe(
        switchMap((createdFollower) => {
          const s2 = this.step2.getRawValue();
          const solution: SolutionDTO = {
            followerId: createdFollower.id,
            issueDetailId: dao.id,
            quantity: s2.issueResourceQty as number,
            solutionDate: s2.dateSolution as string,
            comments: (s2.comments as string).trim(),
            issueSolutionProvider: s2.provider as Provider,
            issueSolutionResource: s2.issueResource as Resource,
          };
          return this.service.saveSolution(solution);
        }),
        switchMap((solution) =>
          this.service.updateIssueDetail(
            { ...dao, issueStatus: { id: 3, name: 'ATENDIDO' }, issueSolution: solution },
            String(dao.id)
          )
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          closeLoading();
          showSuccess('¡Registro exitoso!', `Folio ${dao.issue.invoice} atendido.`);
          this.router.navigate(['/admin/citizen-issues']);
        },
        error: () => {
          this.saving.set(false);
          closeLoading();
          showError();
        },
      });
  }

  // --- carga inicial ---
  private loadCatalogs(): void {
    this.followers
      .getCatalogSex()
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((r: any) => this.sexOptions.set(r?.catSex ?? []));

    this.service
      .getAllProviders()
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((p) => {
        this.providers.set(p ?? []);
        this.filteredProviders.set(this.providers());
      });

    this.loadDetail();
  }

  private loadDetail(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/admin/citizen-issues']);
      return;
    }

    this.service
      .getIssueDetailById(id)
      .pipe(
        switchMap((dao) => {
          this.dao.set(dao);
          this.loadResources(String(dao.issueCategory.id));

          // Ya resuelta: traer follower y mostrar en modo lectura.
          if (dao.issueSolution?.followerId) {
            return this.followers
              .getFollowerById(String(dao.issueSolution.followerId))
              .pipe(catchError(() => of(null)));
          }

          // Sin resolver: precargar beneficiario desde el detalle.
          this.step1.patchValue({
            names: dao.name,
            lastName: dao.lastName,
            surName: dao.surName,
            curp: dao.curp,
          });
          this.step2.patchValue({ issueResource: dao.issueSolutionResourceTmp });
          if (dao.issueSolutionResourceTmp) {
            this.step2.get('issueResource')!.disable();
          }
          return of(null);
        }),
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((follower) => {
        const dao = this.dao();
        if (dao?.issueSolution && follower) {
          this.readonly.set(true);
          this.step1.patchValue({
            names: follower.names,
            lastName: follower.lastName,
            surName: follower.surName,
            curp: follower.curp,
            cellPhoneNumber: follower.cellPhoneNumber,
            sex: follower.catSex,
            birthdate: follower.birthdate,
          });
          this.step2.patchValue({
            issueResource: dao.issueSolution.issueSolutionResource,
            issueResourceQty: dao.issueSolution.quantity,
            dateSolution: dao.issueSolution.solutionDate,
            provider: dao.issueSolution.issueSolutionProvider,
            comments: dao.issueSolution.comments,
          });
          this.form.disable();
        }
      });
  }

  private loadResources(categoryId: string): void {
    this.service
      .getResoursesByCategoryId(categoryId)
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((r) => {
        this.resources.set(r ?? []);
        this.filteredResources.set(this.resources());
      });
  }
}
