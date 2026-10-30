import { closeLoading, showError, showLoading } from '@/app/shared/ui/swal';
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
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { catchError, merge, of, startWith, Subject, switchMap } from 'rxjs';
import Swal from 'sweetalert2';
import { SurveysService } from '../data/surveys.service';
import { SurveyDto } from '../data/surveys.types';

@Component({
  selector: 'app-surveys-list-complete',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './surveys-list-complete.html',
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
    MatTooltipModule,
  ],
})
export default class SurveysListComplete implements AfterViewInit {
  private service = inject(SurveysService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = [
    'dispositivo',
    'folio',
    'fecha',
    'capturista',
    'fotoIne',
    'actions',
  ];

  protected loading = signal(false);
  protected data = signal<SurveyDto[]>([]);
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
            .getSurveysFoliosComplete(
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
      .subscribe((page) => {
        this.loading.set(false);
        this.data.set(page?.content ?? []);
        this.resultsLength.set(page?.totalElements ?? 0);
      });
  }

  applySearch(): void {
    this.paginator().pageIndex = 0;
    this.search$.next();
  }

  viewSummary(survey: SurveyDto): void {
    this.router.navigate(['/admin/surveys/reports/surveysummary', survey.id]);
  }

  uploadCedula(survey: SurveyDto): void {
    this.router.navigate(['/admin/surveys/uploadcedula', survey.id]);
  }

  previewIne(path: string): void {
    if (!path) {
      showError('Sin foto', 'Este registro no tiene foto de INE.');
      return;
    }
    // PDF: abrir en pestaña; imagen: modal.
    showLoading('Cargando…');
    this.service
      .getSurveySummary(path)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          closeLoading();
          if (!blob || blob.size === 0) {
            showError('Sin imagen', 'No hay archivo disponible.');
            return;
          }
          const isPdf = /\.pdf$/i.test(path);
          const url = URL.createObjectURL(
            new Blob([blob], { type: isPdf ? 'application/pdf' : blob.type })
          );
          if (isPdf) {
            window.open(url, '_blank');
            return;
          }
          Swal.fire({
            title: 'Foto INE',
            imageUrl: url,
            imageAlt: 'INE',
            confirmButtonText: 'Cerrar',
            confirmButtonColor: '#1565C0',
          }).then(() => URL.revokeObjectURL(url));
        },
        error: () => {
          closeLoading();
          showError('Sin archivo', 'No se pudo cargar el archivo.');
        },
      });
  }
}
