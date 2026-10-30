import { ElectionsService, SheetListFilters } from '@/app/core/elections/elections.service';
import { Municipality } from '@/app/core/data/model/GeographyTypes';
import { GeographyService } from '@/app/core/geography/geography.service';
import { sortData } from '@/app/shared/utils/sort';
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
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { catchError, merge, of, startWith, Subject, switchMap } from 'rxjs';
import {
  ElectionsPosition,
  PollingBoothResult,
  ValideSheet,
} from '../data/elections.types';

@Component({
  selector: 'app-sheet-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sheet-list.html',
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
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatDialogModule,
  ],
})
export default class SheetList implements AfterViewInit {
  private elections = inject(ElectionsService);
  private geography = inject(GeographyService);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = [
    'electionName',
    'electionYear',
    'electionType',
    'federalDistrict',
    'localDistrict',
    'sectionNumber',
    'pollingBoothName',
    'status',
    'action',
  ];

  protected loading = signal(false);
  protected data = signal<PollingBoothResult[]>([]);
  protected resultsLength = signal(0);
  protected advance = signal<ValideSheet | null>(null);

  protected scopes = signal<ElectionsPosition[]>([]);
  protected municipalities = signal<Municipality[]>([
    { id: 0, entidadId: 0, nomMun: 'TODOS' },
  ]);
  protected electionId = signal('');
  protected municipalityId = signal('');

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
          const sort = this.sort();
          const filters = this.filters();
          this.loadAdvance(filters);
          return this.elections
            .getSheetList(
              sort.active || 'id',
              sort.direction || 'desc',
              this.paginator().pageIndex,
              this.paginator().pageSize,
              filters
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

  openValidate(row: PollingBoothResult): void {
    import('./validate-sheet-dialog').then((m) => {
      const ref = this.dialog.open(m.default, {
        data: { pollingBooth: row, type: 2 },
        width: '900px',
        maxWidth: '95vw',
      });
      ref
        .afterClosed()
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((result) => {
          if (result?.status) {
            this.search$.next();
          }
        });
    });
  }

  private filters(): SheetListFilters {
    return {
      municipalityId: this.municipalityId(),
      federalDistrictId: '',
      localDistrictId: '',
      sectionId: '',
      typeId: '',
      electionId: this.electionId(),
    };
  }

  private loadCatalogs(): void {
    this.elections
      .getElectionPosition()
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res: any) => {
        const scopes = sortData<ElectionsPosition>(
          res?.electionsPositions ?? [],
          'sortOrder'
        );
        this.scopes.set(scopes);
        if (scopes.length) {
          this.electionId.set(String(scopes[0].id));
          this.search$.next();
        }
      });

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

  private loadAdvance(filters: SheetListFilters): void {
    this.elections
      .getSheetListAdvance(
        this.paginator().pageIndex,
        this.paginator().pageSize,
        filters
      )
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => this.advance.set(res));
  }
}
