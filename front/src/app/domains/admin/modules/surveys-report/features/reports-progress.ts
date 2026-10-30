import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { SurveysReportService } from '../data/surveys-report.service';
import { DashSurveyCard } from '../data/surveys-report.types';
import { ReportCard } from '../ui/report-card';
import { PageHeader } from '@/app/shared/ui/page-header';

@Component({
  selector: 'app-reports-progress',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './reports-progress.html',
  imports: [ReportCard, PageHeader],
})
export default class ReportsProgress {
  private service = inject(SurveysReportService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(true);
  protected municipalities = signal<DashSurveyCard[]>([]);

  protected totalGoal = computed(() => this.municipalities().reduce((a, m) => a + (m.goalHolmes ?? 0), 0));
  protected totalCaptured = computed(() => this.municipalities().reduce((a, m) => a + (m.formCaptured ?? 0), 0));
  protected totalToCapture = computed(() => this.municipalities().reduce((a, m) => a + (m.formToCapture ?? 0), 0));
  protected totalPercent = computed(() =>
    this.totalGoal() ? Math.round((this.totalCaptured() * 100) / this.totalGoal()) : 0
  );

  constructor() {
    this.service
      .getDashboardReport()
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((data) => {
        this.municipalities.set(data ?? []);
        this.loading.set(false);
      });
  }

  goToDetails(mun: DashSurveyCard): void {
    this.router.navigate([
      '/admin/surveys/reports/municipalitydetails',
      mun.municipalityId,
      mun.municipalityName,
    ]);
  }
}
