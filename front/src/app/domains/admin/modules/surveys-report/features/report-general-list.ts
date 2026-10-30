import { PageHeader } from '@/app/shared/ui/page-header';
import { TableShell } from '@/app/shared/ui/table-shell';
import { DecimalPipe, PercentPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { SurveysReportService } from '../data/surveys-report.service';
import { ReportByMunList } from '../data/surveys-report.types';

@Component({
  selector: 'app-report-general-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './report-general-list.html',
  imports: [
    FormsModule,
    DecimalPipe,
    PercentPipe,
    PageHeader,
    TableShell,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
  ],
})
export default class ReportGeneralList {
  private service = inject(SurveysReportService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(true);
  protected rows = signal<ReportByMunList[]>([]);
  protected search = signal('');

  protected filtered = computed(() => {
    const q = this.search().trim().toUpperCase();
    if (!q) {
      return this.rows();
    }
    return this.rows().filter(
      (r) =>
        String(r.municipalityName).toUpperCase().includes(q) ||
        String(r.name).toUpperCase().includes(q) ||
        String(r.sectionNumber).includes(q)
    );
  });

  constructor() {
    this.service
      .getReportByMunList()
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((data) => {
        this.rows.set(data ?? []);
        this.loading.set(false);
      });
  }

  progress(r: ReportByMunList): number {
    return r.goalHolmes ? r.formCaptured / r.goalHolmes : 0;
  }

  goToServer(r: ReportByMunList): void {
    this.router.navigate(['/admin/surveys/reports/serverreport', r.authId]);
  }

  downloadCsv(): void {
    const header = [
      'ID',
      'NOMBRE',
      'MUNICIPIO',
      'SECCION',
      'META_VIVIENDAS',
      'VIVIENDAS_CENSADAS',
      'CEDULAS_CAPTURADAS',
      'FALTANTE',
      'AVANCE',
    ];
    const lines = this.rows().map((r) =>
      [
        r.id,
        `"${r.name ?? ''}"`,
        `"${r.municipalityName ?? ''}"`,
        r.sectionNumber,
        r.goalHolmes,
        r.folioCaptured,
        r.formCaptured,
        r.goalHolmes ? r.goalHolmes - r.formCaptured : 0,
        r.goalHolmes ? (r.formCaptured / r.goalHolmes).toFixed(4) : 0,
      ].join(',')
    );
    const csv = [header.join(','), ...lines].join('\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
    const a = document.createElement('a');
    a.href = url;
    a.download = 'reporte_municipal.csv';
    a.click();
    URL.revokeObjectURL(url);
  }
}
