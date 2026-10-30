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
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { catchError, merge, of, startWith, Subject, switchMap } from 'rxjs';
import { PromotersService } from '../data/promoters.service';
import { Promoter } from '../data/promoters.types';

@Component({
  selector: 'app-promoters-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './promoters-list.html',
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
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
})
export default class PromotersList implements AfterViewInit {
  private service = inject(PromotersService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = [
    'names',
    'curp',
    'cellPhoneNumber',
    'neighborhood',
    'status',
    'followersCount',
    'followersGoal',
    'followersGoalPercent',
  ];

  protected loading = signal(false);
  protected errorLoad = signal(false);
  protected data = signal<Promoter[]>([]);
  protected resultsLength = signal(0);
  protected query = signal('');

  private search$ = new Subject<void>();

  ngAfterViewInit(): void {
    this.sort()
      .sortChange.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => (this.paginator().pageIndex = 0));

    merge(this.sort().sortChange, this.paginator().page, this.search$)
      .pipe(
        startWith(null),
        switchMap(() => {
          this.loading.set(true);
          this.errorLoad.set(false);
          const sort = this.sort();
          return this.service
            .getPromoters(
              sort.active || 'id',
              sort.direction || 'desc',
              this.paginator().pageIndex,
              this.paginator().pageSize,
              this.query().trim()
            )
            .pipe(
              catchError(() => {
                this.errorLoad.set(true);
                return of(null);
              })
            );
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

  /** % de avance sobre meta (0..1), con guarda de división por cero. */
  progress(row: Promoter): number {
    const goal = row.followersGoal ?? 0;
    if (!goal) {
      return 0;
    }
    return (row.followersCount ?? 0) / goal;
  }

  goToFollowers(row: Promoter): void {
    this.router.navigate(['/admin/promoters/followers', row.id]);
  }
}
