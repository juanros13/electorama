import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Observable } from 'rxjs';
import { showError } from '@/app/shared/ui/swal';
import { CitizenIssuesService } from '../data/citizen-issues.service';
import { IssueCategory, Provider, Resource } from '../data/citizen-issues.types';

export interface ResourceDialogData {
  type: 'RECURSO' | 'PROVEEDOR';
  categories: IssueCategory[];
}

/** Alta rápida de recurso (con categoría) o proveedor desde el formulario. */
@Component({
  selector: 'app-resource-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  template: `
    <h2 mat-dialog-title>
      {{ data.type === 'RECURSO' ? 'Nuevo recurso' : 'Nuevo proveedor' }}
    </h2>
    <mat-dialog-content>
      <mat-form-field class="w-full">
        <mat-label>Nombre</mat-label>
        <input matInput [formControl]="name" />
        @if (name.hasError('required')) {
          <mat-error>Este campo es requerido.</mat-error>
        }
      </mat-form-field>

      @if (data.type === 'RECURSO') {
        <mat-form-field class="w-full">
          <mat-label>Categoría</mat-label>
          <mat-select [formControl]="category" [compareWith]="compareById">
            @for (c of data.categories; track c.id) {
              <mat-option [value]="c">{{ c.name }}</mat-option>
            }
          </mat-select>
          @if (category.hasError('required')) {
            <mat-error>Selecciona una categoría.</mat-error>
          }
        </mat-form-field>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button matButton mat-dialog-close [disabled]="saving()">Cancelar</button>
      <button matButton="filled" (click)="save()" [disabled]="saving()">
        Guardar
      </button>
    </mat-dialog-actions>
  `,
})
export class ResourceDialog {
  protected data = inject<ResourceDialogData>(MAT_DIALOG_DATA);
  private ref = inject(MatDialogRef<ResourceDialog>);
  private service = inject(CitizenIssuesService);

  protected name = new FormControl('', { nonNullable: true, validators: [Validators.required] });
  protected category = new FormControl<IssueCategory | null>(null, Validators.required);
  protected saving = signal(false);

  compareById(a: { id: number } | null, b: { id: number } | null): boolean {
    return a?.id === b?.id;
  }

  save(): void {
    if (this.name.invalid || (this.data.type === 'RECURSO' && this.category.invalid)) {
      this.name.markAsTouched();
      this.category.markAsTouched();
      return;
    }
    this.saving.set(true);

    const request$: Observable<Resource | Provider> =
      this.data.type === 'RECURSO'
        ? this.service.saveResource({
            name: this.name.value,
            issueCategory: this.category.value!,
          })
        : this.service.saveProvider(this.name.value);

    request$.subscribe({
      next: (created) => this.ref.close(created),
      error: () => {
        this.saving.set(false);
        showError();
      },
    });
  }
}
