import { PromotersService } from '@/app/domains/admin/modules/promoters/data/promoters.service';
import { Promoter } from '@/app/domains/admin/modules/promoters/data/promoters.types';
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
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { catchError, merge, of, startWith, Subject, switchMap } from 'rxjs';

@Component({
  selector: 'app-move-advance',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './move-advance.html',
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
  ],
})
export default class MoveAdvance implements AfterViewInit {
  private service = inject(PromotersService);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = [
    'names',
    'avance',
    'meta',
    'percent',
  ];
  protected loading = signal(false);
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
          const sort = this.sort();
          return this.service
            .getPromoters(
              sort.active || 'id',
              sort.direction || 'desc',
              this.paginator().pageIndex,
              this.paginator().pageSize,
              this.query().trim()
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

  progress(row: Promoter): number {
    const goal = row.followersGoal ?? 0;
    return goal ? (row.followersCount ?? 0) / goal : 0;
  }
}
