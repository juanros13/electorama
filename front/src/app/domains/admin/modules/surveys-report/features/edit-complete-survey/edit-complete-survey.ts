import { SurveysService } from '@/app/domains/admin/modules/surveys/data/surveys.service';
import { SurveyDto } from '@/app/domains/admin/modules/surveys/data/surveys.types';
import { showError } from '@/app/shared/ui/swal';
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
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import SurveyAnnexEdit from './survey-annex-edit';
import SurveyFamiliesEdit from './survey-families-edit';
import SurveyGeneralEdit from './survey-general-edit';

@Component({
  selector: 'app-edit-complete-survey',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './edit-complete-survey.html',
  imports: [
    DatePipe,
    MatButtonModule,
    MatIconModule,
    SurveyGeneralEdit,
    SurveyFamiliesEdit,
    SurveyAnnexEdit,
  ],
})
export default class EditCompleteSurvey {
  private service = inject(SurveysService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  private id = this.route.snapshot.paramMap.get('id') ?? '';

  protected loading = signal(true);
  protected survey = signal<SurveyDto | null>(null);
  protected familyMembers = signal<any[]>([]);
  protected disableMembers = signal<any[]>([]);
  protected olderMembers = signal<any[]>([]);
  protected canActiveFamilies = signal(false);

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
        if (!survey || !survey.surveyPersonEntityList?.length) {
          showError(
            '¡Ups!',
            'La información de esta encuesta aún no está completa, regresa cuando hayas terminado.'
          ).then(() => this.router.navigate(['/admin/surveys']));
          return;
        }
        this.survey.set(survey);
        this.splitMembers(survey.surveyPersonEntityList);
      });
  }

  /** Solo varones de 60 a 64 años cuentan como adulto mayor para anexos. */
  private isOlder60to64Male(member: any): boolean {
    if (member.gender !== 'Masculino' || !member.dateOfBirth) {
      return false;
    }
    const birth = new Date(member.dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age >= 60 && age <= 64;
  }

  private splitMembers(persons: any[]): void {
    const family: any[] = [];
    let interviewed: any = null;
    for (const member of persons) {
      if (member.deleted !== null) {
        continue;
      }
      if (member.curp === null) {
        family.push(member);
      } else {
        interviewed = member;
      }
    }
    this.familyMembers.set(family);

    const disable: any[] = [];
    const older: any[] = [];
    for (const member of [interviewed, ...family].filter(Boolean)) {
      if (member.isDisabled === 'true') {
        disable.push(member);
      } else if (this.isOlder60to64Male(member)) {
        older.push(member);
      }
    }
    this.disableMembers.set(disable);
    this.olderMembers.set(older);
  }

  activateFamilies(): void {
    this.canActiveFamilies.set(true);
  }

  onFamiliesDeactivated(value: boolean): void {
    this.canActiveFamilies.set(value);
  }

  back(): void {
    this.router.navigate(['/admin/surveys/reports/surveysummary', this.id]);
  }
}
