import { closeLoading, confirmDialog, showError, showLoading, showSuccess } from '@/app/shared/ui/swal';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { SurveysService } from '../data/surveys.service';
import { SurveyUser, SurveyUserSection } from '../data/surveys.types';

export interface AssignDialogData {
  surveyUser: SurveyUser;
  userList: SurveyUser[];
}

/** Integración de equipos: mueve una sección del encargado a otro servidor. */
@Component({
  selector: 'app-assign-section-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title>Integración de equipos</h2>
    <mat-dialog-content>
      <p class="text-neutral-500">Encargado de ruta: {{ data.surveyUser.name }}</p>

      <mat-form-field class="mt-4 w-full">
        <mat-label>Ruta (ID de usuario)</mat-label>
        <mat-icon matPrefix svgIcon="file-text" />
        <mat-select [formControl]="section">
          @for (s of data.surveyUser.surveyUserSection ?? []; track s.id) {
            <mat-option [value]="s">ID: {{ s.sectionNumber }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field class="w-full">
        <mat-label>Servidor destino</mat-label>
        <mat-icon matPrefix svgIcon="user" />
        <mat-select [formControl]="recipient">
          @for (u of data.userList; track u.id) {
            <mat-option [value]="u">{{ u.deviceId }} - {{ u.name }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button matButton mat-dialog-close [disabled]="saving()">Cancelar</button>
      <button matButton="filled" (click)="submit()" [disabled]="saving() || section.invalid || recipient.invalid">
        Integrar equipo
      </button>
    </mat-dialog-actions>
  `,
})
export class AssignSectionDialog {
  protected data = inject<AssignDialogData>(MAT_DIALOG_DATA);
  private ref = inject(MatDialogRef<AssignSectionDialog>);
  private service = inject(SurveysService);

  protected section = new FormControl<SurveyUserSection | null>(null, Validators.required);
  protected recipient = new FormControl<SurveyUser | null>(null, Validators.required);
  protected saving = signal(false);

  async submit(): Promise<void> {
    if (this.section.invalid || this.recipient.invalid) {
      return;
    }
    const recipient = this.recipient.value!;
    if (
      !(await confirmDialog(
        '¿Integrar equipo?',
        `La sección se moverá a ${recipient.name} (${recipient.deviceId}).`
      ))
    ) {
      return;
    }
    this.saving.set(true);
    showLoading('Integrando equipo…');
    this.service
      .assignSection(this.section.value!.sectionNumber, this.data.surveyUser.authId, recipient.authId)
      .subscribe({
        next: () => {
          closeLoading();
          showSuccess('Finalizado', 'La sección se asignó correctamente.');
          this.ref.close(true);
        },
        error: (e) => {
          this.saving.set(false);
          closeLoading();
          showError('Error', e?.error?.message ?? 'Valida la información e inténtalo de nuevo.');
        },
      });
  }
}
