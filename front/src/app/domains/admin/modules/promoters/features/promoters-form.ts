import { UserService } from '@/app/core/user/user.service';
import { UppercaseDirective } from '@/app/shared/directives/uppercase';
import { PageHeader } from '@/app/shared/ui/page-header';
import { SectionCard } from '@/app/shared/ui/section-card';
import {
  closeLoading,
  showError,
  showLoading,
  showSuccess,
} from '@/app/shared/ui/swal';
import {
  generateEmail,
  generatePassword,
  generatePhrase,
} from '@/app/shared/utils/credentials';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { PromotersService } from '../data/promoters.service';
import {
  PromoterDTO,
  SectionByUserPromoter,
} from '../data/promoters.types';

const CURP_PATTERN =
  '\\b[A-Z][AEIOUX][A-Z]{2}[0-9]{2}(?:0[1-9]|1[0-2])(?:[1-2][0-9]|0[1-9]|3[0-1])[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}(?:[0-9]|[A-Z])[0-9]\\b';
const INE_PATTERN =
  '\\b(?:[A-Z][A-Z]){3}[0-9]{2}(?:0[1-9]|1[0-2])(?:[1-2][0-9]|0[1-9]|3[0-1])(?:0[1-9]|[1-2][0-9]|3[0-2]|88|87)[HM][0-9]{3}\\b';

@Component({
  selector: 'app-promoters-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './promoters-form.html',
  imports: [
    ReactiveFormsModule,
    UppercaseDirective,
    PageHeader,
    SectionCard,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
})
export default class PromotersForm {
  private fb = inject(FormBuilder);
  private service = inject(PromotersService);
  private userService = inject(UserService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected readonly maxYear = new Date().getFullYear() + 10;
  protected sections = signal<SectionByUserPromoter[]>([]);
  protected saving = signal(false);

  protected form = this.fb.nonNullable.group({
    section: this.fb.control<SectionByUserPromoter | null>(null, Validators.required),
    names: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    surName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    curp: ['', [Validators.required, Validators.pattern(CURP_PATTERN)]],
    ine: ['', [Validators.required, Validators.pattern(INE_PATTERN)]],
    expiration: ['', [Validators.required, Validators.min(2018), Validators.max(this.maxYear)]],
    cellPhoneNumber: ['', Validators.required],
    street: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    outdoorNumber: ['', Validators.required],
    interiorNumber: [''],
    neighborhoodId: ['', Validators.required],
    postalCode: ['', [Validators.required, Validators.pattern(/^\d{5}$/)]],
    notes: [''],
  });

  constructor() {
    this.loadSections();
  }

  compareSection(a: SectionByUserPromoter | null, b: SectionByUserPromoter | null): boolean {
    return a?.seccionGeoId === b?.seccionGeoId;
  }

  cancel(): void {
    this.router.navigate(['/admin/promoters']);
  }

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const section = v.section!;
    const email = generateEmail();

    const dto: PromoterDTO = {
      authId: null,
      promoterAuthId: null,
      userCreateId: this.userService.current?.id ?? '',
      municipalityId: section.municipalityId,
      seccionId: section.seccionGeoId,
      followerSirenaId: null,
      names: v.names.trim(),
      lastName: v.lastName.trim(),
      surName: v.surName.trim(),
      curp: v.curp.trim(),
      ine: v.ine.trim(),
      vencimiento: parseInt(v.expiration, 10),
      cellPhoneNumber: v.cellPhoneNumber.trim(),
      street: v.street.trim(),
      outdoorNumber: v.outdoorNumber.trim(),
      interiorNumber: v.interiorNumber?.trim() ?? '',
      neighborhoodId: v.neighborhoodId.trim(),
      postalCode: v.postalCode.trim(),
      notes: v.notes?.trim() ?? '',
      email,
      user: {
        firstName: v.names.trim(),
        lastName: `${v.lastName.trim()} ${v.surName.trim()}`,
        email,
        password: generatePassword(),
        identification: generatePhrase(),
      },
    };

    this.saving.set(true);
    showLoading('Registrando promotor…');
    this.service.createPromoter(dto).subscribe({
      next: () => {
        closeLoading();
        showSuccess('¡Registro exitoso!', 'El movilizador se registró correctamente.');
        this.router.navigate(['/admin/promoters']);
      },
      error: () => {
        this.saving.set(false);
        closeLoading();
        showError();
      },
    });
  }

  private loadSections(): void {
    const id = this.userService.current?.id;
    if (!id) {
      return;
    }
    this.service
      .getSectionsByUser(id)
      .pipe(
        catchError(() => of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((sections) =>
        this.sections.set((sections ?? []).filter((s) => s.deleted == null))
      );
  }
}
