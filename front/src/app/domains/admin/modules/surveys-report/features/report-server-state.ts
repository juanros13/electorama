import { TableShell } from '@/app/shared/ui/table-shell';
import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { SurveysReportService } from '../data/surveys-report.service';
import { ReportSurveysByServer, ServerSurvey } from '../data/surveys-report.types';

@Component({
  selector: 'app-report-server-state',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './report-server-state.html',
  imports: [DatePipe, TableShell, MatButtonModule, MatIconModule, MatTooltipModule],
})
export default class ReportServerState {
  private service = inject(SurveysReportService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(true);
  protected report = signal<ReportSurveysByServer | null>(null);

  constructor() {
    const authId = this.route.snapshot.paramMap.get('id');
    if (authId) {
      this.service
        .getSurveysByServer(authId)
        .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
        .subscribe((r) => {
          this.report.set(r);
          this.loading.set(false);
        });
    }
  }

  back(): void {
    this.router.navigate(['/admin/surveys/reports/municipalitylist']);
  }

  viewSummary(survey: ServerSurvey): void {
    this.router.navigate(['/admin/surveys/reports/surveysummary', survey.id]);
  }
}
