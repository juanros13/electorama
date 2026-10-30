import { UserService } from '@/app/core/user/user.service';
import {
  closeLoading,
  showError,
  showLoading,
  showSuccess,
} from '@/app/shared/ui/swal';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { StructureService } from '../data/structure.service';
import { InsertCoordinatorZone, UserSection } from '../data/structure.types';

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
const EMAIL_PATTERN = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

/** Valida que password y confirmPassword coincidan. */
function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const pass = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return pass && confirm && pass !== confirm
    ? { passwordMismatch: true }
    : null;
}

@Component({
  selector: 'app-coordinator-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './coordinator-form.html',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
})
export default class CoordinatorForm {
  private fb = inject(FormBuilder);
  private service = inject(StructureService);
  private userService = inject(UserService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected sections = signal<UserSection[]>([]);
  protected saving = signal(false);

  protected form = this.fb.nonNullable.group(
    {
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.pattern(EMAIL_PATTERN)]],
      password: [
        '',
        [Validators.required, Validators.pattern(PASSWORD_PATTERN)],
      ],
      confirmPassword: ['', Validators.required],
      sections: this.fb.nonNullable.control<UserSection[]>(
        [],
        Validators.required
      ),
    },
    { validators: passwordsMatch }
  );

  constructor() {
    this.loadSections();
  }

  cancel(): void {
    this.router.navigate(['/admin/structure']);
  }

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const authId = this.userService.current?.id ?? '';

    const dto: InsertCoordinatorZone = {
      user: {
        password: v.password,
        email: v.email.trim().toLowerCase(),
        firstName: v.firstName.trim().toUpperCase(),
        lastName: v.lastName.trim().toUpperCase(),
      },
      sections: v.sections.map((s) => ({
        seccionGeoId: s.seccionGeoId,
        seccionNumber: Number(s.seccionNumber),
        municipioId: s.municipalityId,
      })),
    };

    this.saving.set(true);
    showLoading('Registrando promotor…');
    this.service.addNewCoordinatorZone(dto, authId).subscribe({
      next: () => {
        closeLoading();
        showSuccess(
          '¡Registro exitoso!',
          'El promotor se registró correctamente.'
        );
        this.router.navigate(['/admin/structure']);
      },
      error: () => {
        this.saving.set(false);
        closeLoading();
        showError(
          'No se pudo registrar',
          'Valida que el correo no haya sido registrado previamente.'
        );
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
