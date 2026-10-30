import { closeLoading, confirmDialog, showError, showLoading, showSuccess, showWarning } from '@/app/shared/ui/swal';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { catchError, of } from 'rxjs';
import { SurveysService } from '../data/surveys.service';
import { SurveyUser } from '../data/surveys.types';
import { AssignDialogData } from './assign-section-dialog';

/** Mueve un rango de folios del encargado a otro servidor (validado contra folios permitidos). */
@Component({
  selector: 'app-assign-folio-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title>Asignar folios</h2>
    <mat-dialog-content>
      <p class="text-neutral-500">Encargado: {{ data.surveyUser.name }}</p>
      @if (minLimit() !== null) {
        <p class="text-xs text-neutral-500">Folios permitidos: {{ minLimit() }} – {{ maxLimit() }}</p>
      }

      <form class="mt-4 grid gap-2" [formGroup]="form">
        <mat-form-field class="w-full">
          <mat-label>Servidor destino</mat-label>
          <mat-icon matPrefix svgIcon="user" />
          <mat-select formControlName="recipient">
            @for (u of data.userList; track u.id) {
              <mat-option [value]="u">{{ u.deviceId }} - {{ u.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <div class="grid grid-cols-2 gap-2">
          <mat-form-field>
            <mat-label>Folio inicial</mat-label>
            <input matInput type="number" formControlName="min" />
          </mat-form-field>
          <mat-form-field>
            <mat-label>Folio final</mat-label>
            <input matInput type="number" formControlName="max" />
            @if (form.controls.max.hasError('menorQue')) {
              <mat-error>Debe ser ≥ folio inicial.</mat-error>
            }
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button matButton mat-dialog-close [disabled]="saving()">Cancelar</button>
      <button matButton="filled" (click)="submit()" [disabled]="saving()">Mover folios</button>
    </mat-dialog-actions>
  `,
})
export class AssignFolioDialog {
  protected data = inject<AssignDialogData>(MAT_DIALOG_DATA);
  private ref = inject(MatDialogRef<AssignFolioDialog>);
  private service = inject(SurveysService);

  protected minLimit = signal<number | null>(null);
  protected maxLimit = signal<number | null>(null);
  protected saving = signal(false);
  private allowed = new Set<number>();

  protected form = new FormGroup(
    {
      recipient: new FormControl<SurveyUser | null>(null, Validators.required),
      min: new FormControl<number | null>(null, Validators.required),
      max: new FormControl<number | null>(null, Validators.required),
    },
    { validators: minNotGreater }
  );

  constructor() {
    this.service
      .getFoliosCompareByUser(this.data.surveyUser.id)
      .pipe(catchError(() => of([])), takeUntilDestroyed())
      .subscribe((folios) => {
        const nums = (folios ?? []).map((f) => Number(f.folio)).filter((n) => !Number.isNaN(n));
        this.allowed = new Set(nums);
        if (nums.length) {
          this.minLimit.set(Math.min(...nums));
          this.maxLimit.set(Math.max(...nums));
        }
      });
  }

  /** Cada folio del rango debe estar en el conjunto permitido del encargado. */
  private rangeAllowed(min: number, max: number): boolean {
    for (let i = min; i <= max; i++) {
      if (!this.allowed.has(i)) {
        return false;
      }
    }
    return true;
  }

  async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { recipient, min, max } = this.form.getRawValue();
    if (!this.rangeAllowed(Number(min), Number(max))) {
      showWarning('Rango inválido', 'Algunos folios no están disponibles para mover.');
      return;
    }
    if (
      !(await confirmDialog('¿Mover folios?', `Se moverán a ${recipient!.name} (${recipient!.deviceId}).`))
    ) {
      return;
    }
    this.saving.set(true);
    showLoading('Moviendo folios…');
    this.service.assignFolios(Number(min), Number(max), recipient!.id).subscribe({
      next: () => {
        closeLoading();
        showSuccess('Finalizado', 'Los folios se movieron correctamente.');
        this.ref.close(true);
      },
      error: (e) => {
        this.saving.set(false);
        closeLoading();
        showError('Error', e?.error?.message ?? 'Valida la información y el rango ingresado.');
      },
    });
  }
}

/** Valida min <= max. */
function minNotGreater(group: any) {
  const min = group.get('min')?.value;
  const max = group.get('max')?.value;
  if (min != null && max != null && Number(max) < Number(min)) {
    group.get('max')?.setErrors({ menorQue: true });
  } else if (group.get('max')?.hasError('menorQue')) {
    group.get('max')?.setErrors(null);
  }
  return null;
}
