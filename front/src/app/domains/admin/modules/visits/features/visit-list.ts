import {
  closeLoading,
  confirmDialog,
  showError,
  showLoading,
  showSuccess,
} from '@/app/shared/ui/swal';
import { PageHeader } from '@/app/shared/ui/page-header';
import { TableShell } from '@/app/shared/ui/table-shell';
import { DatePipe } from '@angular/common';
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
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { catchError, merge, of, startWith, Subject, switchMap } from 'rxjs';
import { VisitService } from '../data/visit.service';

@Component({
  selector: 'app-visit-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './visit-list.html',
  imports: [
    FormsModule,
    DatePipe,
    PageHeader,
    TableShell,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatTooltipModule,
  ],
})
export default class VisitList implements AfterViewInit {
  private service = inject(VisitService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = [
    'id',
    'openTheDoor',
    'names',
    'phone',
    'sex',
    'age',
    'seccionId',
    'visitDate',
    'actions',
  ];

  protected loading = signal(false);
  protected data = signal<any[]>([]);
  protected resultsLength = signal(0);
  protected search = signal('');

  // Rango de fechas para el reporte CSV.
  protected startDate = signal('');
  protected endDate = signal('');

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
            .getVisits(
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
      .subscribe((res: any) => {
        this.loading.set(false);
        this.data.set(res?.visitedPeople?.content ?? []);
        this.resultsLength.set(res?.visitedPeople?.totalElements ?? 0);
      });
  }

  applySearch(): void {
    this.paginator().pageIndex = 0;
    this.search$.next();
  }

  add(): void {
    this.router.navigate(['/admin/visits/add']);
  }

  async addOneVisit(row: any): Promise<void> {
    if (!(await confirmDialog('¿Agregar una visita?', 'Se sumará una visita a esta persona.'))) {
      return;
    }
    showLoading('Actualizando…');
    this.service.addOneVisit(String(row.id)).subscribe({
      next: () => {
        closeLoading();
        showSuccess('Visita agregada', 'Se sumó una visita.');
        this.data.update((rows) =>
          rows.map((r) => (r.id === row.id ? { ...r, visitNumber: (r.visitNumber ?? 0) + 1 } : r))
        );
      },
      error: () => {
        closeLoading();
        showError();
      },
    });
  }

  async deleteVisit(row: any): Promise<void> {
    if (!(await confirmDialog('¿Eliminar visitado?', 'Se eliminará a esta persona visitada.'))) {
      return;
    }
    showLoading('Eliminando…');
    this.service.deletePersonVisited(String(row.id)).subscribe({
      next: () => {
        closeLoading();
        showSuccess('Eliminado', 'La persona visitada fue eliminada.');
        const today = new Date().toISOString().slice(0, 10);
        this.data.update((rows) => rows.map((r) => (r.id === row.id ? { ...r, deleted: today } : r)));
      },
      error: () => {
        closeLoading();
        showError();
      },
    });
  }

  downloadCsv(): void {
    if (!this.startDate() || !this.endDate()) {
      showError('Rango incompleto', 'Selecciona fecha inicial y final.');
      return;
    }
    const dates = { startDate: this.startDate(), endDate: this.endDate() };
    showLoading('Generando reporte…');
    this.service
      .downloadReportCsv(dates)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          closeLoading();
          const url = URL.createObjectURL(new Blob([blob], { type: 'text/csv' }));
          const a = document.createElement('a');
          a.href = url;
          a.download = `Visitas_${dates.startDate}_${dates.endDate}.csv`;
          a.click();
          URL.revokeObjectURL(url);
        },
        error: () => {
          closeLoading();
          showError();
        },
      });
  }
}
