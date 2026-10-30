import { ElectionsService } from '@/app/core/elections/elections.service';
import { Municipality } from '@/app/core/data/model/GeographyTypes';
import { GeographyService } from '@/app/core/geography/geography.service';
import { PageHeader } from '@/app/shared/ui/page-header';
import { TableShell } from '@/app/shared/ui/table-shell';
import { DecimalPipe } from '@angular/common';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
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
import {
  ElectionStatus,
  PollingBoothCount,
  PollingBoothsStatusList,
} from '../data/elections.types';

const STATUS_TYPES: ElectionStatus[] = [
  { id: 0, name: 'TODOS' },
  { id: 1, name: 'Abierta' },
  { id: 2, name: 'Cerrada' },
  { id: 3, name: 'Abierta con incidencia' },
  { id: 4, name: 'Cerrada con incidencia' },
];

@Component({
  selector: 'app-pollingboot-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pollingboot-list.html',
  imports: [
    FormsModule,
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
export default class PollingbootList implements AfterViewInit {
  private elections = inject(ElectionsService);
  private geography = inject(GeographyService);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = [
    'municipality',
    'federalDistrict',
    'localDistrict',
    'section',
    'pollingBooth',
    'status',
  ];
  protected readonly statusTypes = STATUS_TYPES;

  protected loading = signal(false);
  protected data = signal<PollingBoothsStatusList[]>([]);
  protected resultsLength = signal(0);
  protected count = signal<PollingBoothCount | null>(null);

  protected municipalities = signal<Municipality[]>([
    { id: 0, entidadId: 0, nomMun: 'TODOS' },
  ]);
  protected municipalityId = signal('');
  protected statusId = signal('');

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
          this.loadCount();
          const sort = this.sort();
          return this.elections
            .getPollingBoothStatusList(
              sort.active || 'id',
              sort.direction || 'desc',
              this.paginator().pageIndex,
              this.paginator().pageSize,
              this.municipalityId(),
              this.statusId()
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
  }

  private loadCount(): void {
    this.elections
      .getPollingBoothCount(this.municipalityId(), this.statusId())
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => this.count.set(res));
  }
}
