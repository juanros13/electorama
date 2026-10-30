import { UserService } from '@/app/core/user/user.service';
import { PageHeader } from '@/app/shared/ui/page-header';
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
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { SurveysReportService } from '../data/surveys-report.service';
import { CompleteSurvey } from '../data/surveys-report.types';

@Component({
  selector: 'app-survey-summary',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './survey-summary.html',
  imports: [DatePipe, PageHeader, MatButtonModule, MatIconModule],
})
export default class SurveySummary {
  private service = inject(SurveysReportService);
  private userService = inject(UserService);
  private sanitizer = inject(DomSanitizer);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(true);
  protected survey = signal<CompleteSurvey | null>(null);
  protected images = signal<SafeUrl[]>([]);
  protected canEdit = this.userService.hasRole('SURVEY_MUNICIPALITY');

  private id = this.route.snapshot.paramMap.get('id');

  constructor() {
    if (this.id) {
      this.service
        .getCompleteSurvey(this.id)
        .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
        .subscribe((s) => {
          this.survey.set(s);
          this.loading.set(false);
          if (s) {
            this.loadImages(s);
          }
        });
    }
  }

  private loadImages(s: CompleteSurvey): void {
    const paths = [s.pathPhotoFacadeQr, s.pathPhotoIneAdverse, s.pathPhotoIneReverse].filter(Boolean);
    for (const path of paths) {
      this.service
        .getSurveySummary(path)
        .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
        .subscribe((blob) => {
          if (blob && blob.size > 0) {
            const url = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(blob));
            this.images.update((imgs) => [...imgs, url]);
          }
        });
    }
  }

  namesOf(entities: { name: string }[] | null | undefined): string {
    return (entities ?? []).map((e) => e.name).join(', ');
  }

  back(): void {
    this.router.navigate(['/admin/surveys/complete']);
  }

  edit(): void {
    this.router.navigate(['/admin/surveys/reports/editsurvey', this.id]);
  }
}
