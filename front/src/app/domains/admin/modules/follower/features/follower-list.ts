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
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { catchError, merge, of, startWith, Subject, switchMap } from 'rxjs';
import { FollowerService } from '../data/follower.service';
import { FollowerPromoter } from '../data/follower.types';

@Component({
  selector: 'app-follower-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './follower-list.html',
  imports: [
    FormsModule,
    DecimalPipe,
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
export default class FollowerList implements AfterViewInit {
  private service = inject(FollowerService);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = [
    'promoter',
    'names',
    'curp',
    'sex',
    'cellPhoneNumber',
    'section',
    'supportMov',
    'status',
  ];

  protected loading = signal(false);
  protected errorLoad = signal(false);
  protected data = signal<FollowerPromoter[]>([]);
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
            .getFollowers(
              sort.active || 'id',
              sort.direction || 'desc',
              this.paginator().pageIndex,
              this.paginator().pageSize,
              this.query().trim(),
              null,
              null
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
}
