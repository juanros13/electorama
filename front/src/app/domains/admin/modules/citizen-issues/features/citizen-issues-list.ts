import { RolType } from '@/app/core/data/enum/RolType';
import { Municipality } from '@/app/core/data/model/GeographyTypes';
import { GeographyService } from '@/app/core/geography/geography.service';
import { UserService } from '@/app/core/user/user.service';
import {
  closeLoading,
  confirmDialog,
  showError,
  showLoading,
  showSuccess,
} from '@/app/shared/ui/swal';
import { PageHeader } from '@/app/shared/ui/page-header';
import { TableShell } from '@/app/shared/ui/table-shell';
import { DatePipe, DecimalPipe } from '@angular/common';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { catchError, merge, of, startWith, Subject, switchMap } from 'rxjs';
import { CitizenIssuesService } from '../data/citizen-issues.service';
import {
  IssueCategory,
  IssueDetailDAO,
  IssueStatus,
  IssueTypes,
  ResultIssueStatus,
} from '../data/citizen-issues.types';

const ALL_STATS: ResultIssueStatus = {
  totalIssuesOnHold: 0,
  totalIssuesInProcess: 0,
  totalIssuesCompleted: 0,
  totalIssuesDeleted: 0,
  totalIssuesUrgent: 0,
};

@Component({
  selector: 'app-citizen-issues-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './citizen-issues-list.html',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    DecimalPipe,
    PageHeader,
    TableShell,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
})
export default class CitizenIssuesList implements AfterViewInit {
  // Dependencies
  private router = inject(Router);
  private service = inject(CitizenIssuesService);
  private geography = inject(GeographyService);
  private userService = inject(UserService);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  // View children
  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  // Table
  protected readonly displayedColumns = [
    'invoice',
    'fullName',
    'registrationDate',
    'phone',
    'municipalityName',
    'issueStatus',
    'issueType',
    'issueCategory',
    'enabled',
    'actions',
  ];
  protected loading = signal(false);
  protected data = signal<IssueDetailDAO[]>([]);
  protected resultsLength = signal(0);
  protected stats = signal<ResultIssueStatus>(ALL_STATS);
  protected statCards = computed(() => {
    const s = this.stats();
    return [
      { label: 'En espera', value: s.totalIssuesOnHold, color: 'text-amber-600' },
      { label: 'En proceso', value: s.totalIssuesInProcess, color: 'text-secondary-600' },
      { label: 'Atendidas', value: s.totalIssuesCompleted, color: 'text-emerald-600' },
      { label: 'Urgentes', value: s.totalIssuesUrgent, color: 'text-red-600' },
      { label: 'Inhabilitadas', value: s.totalIssuesDeleted, color: 'text-neutral-500' },
    ];
  });

  // Catalogs
  protected municipalities = signal<Municipality[]>([
    { id: 0, entidadId: 0, nomMun: 'TODOS' },
  ]);
  protected types = signal<IssueTypes[]>([{ id: 0, name: 'TODOS' }]);
  protected statuses = signal<IssueStatus[]>([{ id: 0, name: 'TODOS' }]);
  protected categories = signal<IssueCategory[]>([{ id: 0, name: 'TODOS' }]);
  protected issuers = signal<{ authId: any; firstName: string }[]>([
    { authId: 0, firstName: 'TODOS' },
  ]);

  // Role-gated UI
  protected isAdmin = computed(
    () =>
      this.userService.hasRole(RolType.ADMIN) ||
      this.userService.hasRole(RolType.SOLUTION_TRACKING) ||
      this.userService.hasRole(RolType.ISSUES_SUPEVISOR)
  );
  protected canAdd = computed(() =>
    this.userService.hasRole(RolType.CITIZEN_ISSUER)
  );

  // Filters
  protected filterForm = this.fb.nonNullable.group({
    name: '',
    invoice: '',
    municipality: 0,
    userCreate: '' as string,
    category: 0,
    status: 0,
    urgent: 0,
  });

  private search$ = new Subject<void>();

  constructor() {
    this.loadCatalogs();
  }

  ngAfterViewInit(): void {
    this.sort()
      .sortChange.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => (this.paginator().pageIndex = 0));

    merge(this.sort().sortChange, this.paginator().page, this.search$)
      .pipe(
        startWith(null),
        switchMap(() => {
          this.loading.set(true);
          const f = this.filterForm.getRawValue();
          const sort = this.sort();
          return this.service
            .getIssuesDetails(
              sort.active || 'registrationDate',
              sort.direction || 'desc',
              this.paginator().pageIndex,
              this.paginator().pageSize,
              f.name,
              f.invoice,
              f.municipality,
              f.userCreate,
              f.category,
              f.status,
              f.urgent
            )
            .pipe(catchError(() => of(null)));
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res: any) => {
        this.loading.set(false);
        this.data.set(res?.content ?? []);
        this.resultsLength.set(res?.totalElements ?? 0);
        this.loadStats();
      });
  }

  search(): void {
    this.paginator().pageIndex = 0;
    this.search$.next();
  }

  goToAdd(): void {
    this.router.navigate(['/admin/citizen-issues/add']);
  }

  goToEdit(issue: IssueDetailDAO): void {
    this.router.navigate(['/admin/citizen-issues/update', issue.id]);
  }

  goToSolution(issue: IssueDetailDAO): void {
    this.router.navigate(['/admin/citizen-issues/solution', issue.id]);
  }

  async disable(issue: IssueDetailDAO): Promise<void> {
    const ok = await confirmDialog(
      '¿Estás seguro?',
      `¿Inhabilitar la incidencia de ${issue.issue.fullName}?`
    );
    if (!ok) {
      return;
    }
    showLoading('Actualizando información…');
    this.service.deleteIssueDetail(issue.id).subscribe({
      next: (res) => {
        closeLoading();
        showSuccess(
          '¡Proceso exitoso!',
          `Se inhabilitó la incidencia de ${issue.issue.fullName}.`
        );
        this.data.update((list) =>
          list.map((item) => (item.id === res.id ? res : item))
        );
        this.loadStats();
      },
      error: () => {
        closeLoading();
        showError();
      },
    });
  }

  async remove(issue: IssueDetailDAO): Promise<void> {
    const ok = await confirmDialog(
      '¿Estás seguro?',
      `¿Eliminar de forma permanente la incidencia de ${issue.issue.fullName}?`
    );
    if (!ok) {
      return;
    }
    showLoading('Eliminando…');
    this.service.deleteIssueDetail4Ever(issue.id).subscribe({
      next: () => {
        closeLoading();
        showSuccess(
          '¡Proceso exitoso!',
          `Se eliminó la incidencia de ${issue.issue.fullName}.`
        );
        this.data.update((list) => list.filter((item) => item.id !== issue.id));
        this.loadStats();
      },
      error: () => {
        closeLoading();
        showError();
      },
    });
  }

  private loadCatalogs(): void {
    this.geography
      .getMunicipalitiesByStateId('27')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (municipios) =>
          this.municipalities.set([
            { id: 0, entidadId: 0, nomMun: 'TODOS' },
            ...[...municipios].sort((a, b) => a.id - b.id),
          ]),
        error: () => showError(),
      });

    this.service
      .getCatIssuesTypes()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res: any) =>
        this.types.set([{ id: 0, name: 'TODOS' }, ...res.catIssueTypes])
      );

    this.service
      .getCatIssuesStatus()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res) =>
        this.statuses.set([{ id: 0, name: 'TODOS' }, ...res.catIssueStatus])
      );

    this.service
      .getCatIssuesCategory()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res) =>
        this.categories.set([{ id: 0, name: 'TODOS' }, ...res.catIssueCategory])
      );

    this.service
      .getCitizenIssuer()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res: any) =>
        this.issuers.set([{ authId: 0, firstName: 'TODOS' }, ...res])
      );
  }

  private loadStats(): void {
    const f = this.filterForm.getRawValue();
    this.service
      .getResultsIssuesStatus(
        f.municipality,
        f.userCreate,
        f.category,
        f.status,
        f.urgent,
        f.name,
        f.invoice
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (stats) => this.stats.set(stats),
        error: () => {
          /* stats best-effort */
        },
      });
  }
}
