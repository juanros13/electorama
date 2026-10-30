import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { SurveysService } from '../data/surveys.service';
import { SurveyDto } from '../data/surveys.types';
import SurveysForm from './surveys-form';

@Component({
  selector: 'app-full-data-survey',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './full-data-survey.html',
  imports: [DatePipe, MatButtonModule, MatIconModule, SurveysForm],
})
export default class FullDataSurvey {
  private service = inject(SurveysService);
  private sanitizer = inject(DomSanitizer);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(true);
  protected survey = signal<SurveyDto | null>(null);
  protected images = signal<SafeUrl[]>([]);

  private id = this.route.snapshot.paramMap.get('id');

  constructor() {
    if (!this.id) {
      this.router.navigate(['/admin/surveys']);
      return;
    }
    this.service
      .getSurveyById(this.id)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((survey) => {
        this.loading.set(false);
        this.survey.set(survey);
        if (survey) {
          this.loadImages(survey);
        }
      });
  }

  private loadImages(survey: SurveyDto): void {
    const paths = [
      survey.pathPhotoIneAdverse,
      survey.pathPhotoIneReverse,
      survey.pathPhotoFacade,
      survey.pathPhotoFacadeQr,
    ].filter(Boolean);
    for (const path of paths) {
      this.service
        .getSurveySummary(path)
        .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
        .subscribe((blob) => {
          if (!blob) {
            return;
          }
          const url = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(blob));
          this.images.update((imgs) => [...imgs, url]);
        });
    }
  }

  back(): void {
    this.router.navigate(['/admin/surveys']);
  }
}
