import { ElectionsService } from '@/app/core/elections/elections.service';
import { Municipality } from '@/app/core/data/model/GeographyTypes';
import { GeographyService } from '@/app/core/geography/geography.service';
import { PageHeader } from '@/app/shared/ui/page-header';
import { TableShell } from '@/app/shared/ui/table-shell';
import { DecimalPipe, PercentPipe } from '@angular/common';
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
import { ProgressVote, VotingFlow } from '../data/elections.types';

@Component({
  selector: 'app-voting-flow-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './voting-flow-list.html',
  imports: [
    FormsModule,
    DecimalPipe,
    PercentPipe,
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
export default class VotingFlowList implements AfterViewInit {
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
    'pollingbooth',
    'category',
    'votingflow',
    'nominallist',
    'faltante',
    'percent',
  ];

  protected loading = signal(false);
  protected data = signal<VotingFlow[]>([]);
  protected resultsLength = signal(0);
  protected progress = signal<ProgressVote | null>(null);

  protected municipalities = signal<Municipality[]>([
    { id: 0, entidadId: 0, nomMun: 'TODOS' },
  ]);
  protected municipalityId = signal('');

  private search$ = new Subject<void>();

  constructor() {
    this.loadCatalogs();
    this.loadProgress();
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
          return this.elections
            .getVotingFlow(
              sort.active || 'id',
              sort.direction || 'desc',
              this.paginator().pageIndex,
              this.paginator().pageSize,
              this.municipalityId()
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
    this.loadProgress();
  }

  faltante(row: VotingFlow): number {
    return Math.max(0, row.votersList - (row.progressVote ?? 0));
  }

  ratio(row: VotingFlow): number {
    return row.votersList ? (row.progressVote ?? 0) / row.votersList : 0;
  }

  totalPercent(): number {
    const p = this.progress();
    return p?.voterList ? p.progressVote / p.voterList : 0;
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

  private loadProgress(): void {
    this.elections
      .getVotingFlowProgress()
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => this.progress.set(res));
  }
}
