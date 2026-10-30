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
import { Router } from '@angular/router';
import { catchError, merge, of, startWith, Subject, switchMap } from 'rxjs';
import { SurveysReportService } from '../data/surveys-report.service';
import { ReportByMunList } from '../data/surveys-report.types';

@Component({
  selector: 'app-report-general-progress',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './report-general-progress.html',
  imports: [
    FormsModule,
    DecimalPipe,
    PercentPipe,
    PageHeader,
    TableShell,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
  ],
})
export default class ReportGeneralProgress implements AfterViewInit {
  private service = inject(SurveysReportService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected loading = signal(false);
  protected rows = signal<ReportByMunList[]>([]);
  protected resultsLength = signal(0);
  protected search = signal('');

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
            .getGeneralReport(
              sort.active || 'id',
              sort.direction || 'desc',
              this.paginator().pageIndex,
              this.paginator().pageSize,
              this.search().trim()
            )
            .pipe(catchError(() => of(null)));
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((page: any) => {
        this.loading.set(false);
        this.rows.set(page?.content ?? []);
        this.resultsLength.set(page?.totalElements ?? 0);
      });
  }

  applySearch(): void {
    this.paginator().pageIndex = 0;
    this.search$.next();
  }

  progress(r: ReportByMunList): number {
    return r.goalHolmes ? r.formCaptured / r.goalHolmes : 0;
  }

  goToServer(r: ReportByMunList): void {
    this.router.navigate(['/admin/surveys/reports/serverreport', r.authId]);
  }

  downloadCsv(): void {
    const header = ['ID', 'NOMBRE', 'MUNICIPIO', 'SECCION', 'META', 'VIVIENDAS', 'CEDULAS', 'AVANCE'];
    const lines = this.rows().map((r) =>
      [
        r.id,
        `"${r.name ?? ''}"`,
        `"${r.municipalityName ?? ''}"`,
        r.sectionNumber,
        r.goalHolmes,
        r.folioCaptured,
        r.formCaptured,
        r.goalHolmes ? (r.formCaptured / r.goalHolmes).toFixed(4) : 0,
      ].join(',')
    );
    const csv = [header.join(','), ...lines].join('\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
    const a = document.createElement('a');
    a.href = url;
    a.download = 'progreso_general.csv';
    a.click();
    URL.revokeObjectURL(url);
  }
}
