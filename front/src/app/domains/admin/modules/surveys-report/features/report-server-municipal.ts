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
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { catchError, of } from 'rxjs';
import { SurveysReportService } from '../data/surveys-report.service';
import { GetSurveyReportMunicipality } from '../data/surveys-report.types';

@Component({
  selector: 'app-report-server-municipal',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './report-server-municipal.html',
  imports: [FormsModule, DecimalPipe, PercentPipe, PageHeader, TableShell, MatFormFieldModule, MatInputModule, MatIconModule],
})
export default class ReportServerMunicipal {
  private service = inject(SurveysReportService);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(true);
  protected rows = signal<GetSurveyReportMunicipality[]>([]);
  protected search = signal('');

  protected filtered = computed(() => {
    const q = this.search().trim().toUpperCase();
    return q ? this.rows().filter((r) => String(r.municipalityName).toUpperCase().includes(q)) : this.rows();
  });

  constructor() {
    this.service
      .getSurveyMainReport()
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((data) => {
        this.rows.set(data ?? []);
        this.loading.set(false);
      });
  }

  progress(r: GetSurveyReportMunicipality): number {
    return r.goalHolmes ? r.formCaptured / r.goalHolmes : 0;
  }
  censusProgress(r: GetSurveyReportMunicipality): number {
    return r.goalHolmes ? r.folioCaptured / r.goalHolmes : 0;
  }
}
