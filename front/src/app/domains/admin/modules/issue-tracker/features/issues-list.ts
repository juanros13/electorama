import { Municipality } from '@/app/core/data/model/GeographyTypes';
import { GeographyService } from '@/app/core/geography/geography.service';
import { PageHeader } from '@/app/shared/ui/page-header';
import { TableShell } from '@/app/shared/ui/table-shell';
import { DatePipe, DecimalPipe } from '@angular/common';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { catchError, merge, of, startWith, Subject, switchMap } from 'rxjs';
import { IssuesTrackerService } from '../data/issues-tracker.service';
import {
  ContentIssuesTrack,
  ListIncidentsContent,
  SubCategory,
} from '../data/issues-track.types';

@Component({
  selector: 'app-issues-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './issues-list.html',
  imports: [
    FormsModule,
    DatePipe,
    DecimalPipe,
    PageHeader,
    TableShell,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
  ],
})
export default class IssuesList implements AfterViewInit {
  private service = inject(IssuesTrackerService);
  private geography = inject(GeographyService);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = [
    'municipality',
    'federalDistrict',
    'localDistrict',
    'sectionNumber',
    'pollingBoothsId',
    'nameSub',
    'created',
    'description',
  ];

  protected loading = signal(false);
  protected data = signal<ContentIssuesTrack[]>([]);
  protected resultsLength = signal(0);
  protected stats = signal<ListIncidentsContent[]>([]);
  protected totalIncidents = computed(() =>
    this.stats().reduce((acc, s) => acc + (s.total ?? 0), 0)
  );

  protected municipalities = signal<Municipality[]>([
    { id: 0, entidadId: 0, nomMun: 'TODOS' },
  ]);
  protected subcategories = signal<SubCategory[]>([{ id: 0, name: 'TODAS' }]);
  protected municipalityId = signal('');
  protected categoryId = signal('');

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
          this.loadStats();
          const sort = this.sort();
          return this.service
            .getIssuesTrack(
              sort.active || 'created',
              sort.direction || 'desc',
              this.paginator().pageIndex,
              this.paginator().pageSize,
              this.municipalityId(),
              this.categoryId()
            )
            .pipe(catchError(() => of(null)));
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((page: any) => {
        this.loading.set(false);
        this.data.set(page?.content ?? []);
        this.resultsLength.set(page?.totalElements ?? 0);
      });
  }

  search(): void {
    this.paginator().pageIndex = 0;
    this.search$.next();
  }

  private loadCatalogs(): void {
    this.geography
      .getMunicipalitiesByStateId('27')
      .pipe(
        catchError(() => of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((municipios) =>
        this.municipalities.set([
          { id: 0, entidadId: 0, nomMun: 'TODOS' },
          ...[...municipios].sort((a, b) => a.id - b.id),
        ])
      );

    this.service
      .getCategoriesIssues()
      .pipe(
        catchError(() => of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((subs) =>
        this.subcategories.set([{ id: 0, name: 'TODAS' }, ...subs])
      );
  }

  private loadStats(): void {
    this.service
      .getListTotalIncidentsStats(this.municipalityId(), this.categoryId())
      .pipe(
        catchError(() => of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => this.stats.set(res));
  }
}
