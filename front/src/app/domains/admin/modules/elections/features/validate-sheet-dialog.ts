import {
  ElectionResultUpdateRequest,
  ElectionsService,
} from '@/app/core/elections/elections.service';
import {
  closeLoading,
  confirmDialog,
  showError,
  showLoading,
  showSuccess,
} from '@/app/shared/ui/swal';
import { sortData } from '@/app/shared/utils/sort';
import { DecimalPipe } from '@angular/common';
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
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { catchError, of } from 'rxjs';
import {
  ElectionResultDtoList,
  PollingBoothResult,
} from '../data/elections.types';

export interface ValidateSheetDialogData {
  pollingBooth: PollingBoothResult;
  type: number; // 1 = física, 2 = digital
}

@Component({
  selector: 'app-validate-sheet-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './validate-sheet-dialog.html',
  imports: [
    ReactiveFormsModule,
    DecimalPipe,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
  ],
})
export default class ValidateSheetDialog {
  protected data = inject<ValidateSheetDialogData>(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<ValidateSheetDialog>);
  private fb = inject(FormBuilder);
  private elections = inject(ElectionsService);
  private sanitizer = inject(DomSanitizer);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(true);
  protected parties = signal<ElectionResultDtoList[]>([]);
  protected form = signal<FormGroup>(this.fb.group({}));
  protected imagePath = signal<SafeResourceUrl | null>(null);
  protected zoom = signal(1);
  protected totalVotes = signal(0);

  private validationId = 3;
  private associationId = '';

  constructor() {
    const pb = this.data.pollingBooth;
    this.associationId = String(pb.pollingBoothsElectionAssociationId);
    this.validationId = this.statusToValidationId(pb.status);
    this.loadData();
  }

  controlName(party: ElectionResultDtoList): string {
    return `_${party.softorder}_${party.id}`;
  }

  zoomIn(): void {
    this.zoom.update((z) => Math.min(3, z + 0.25));
  }

  zoomOut(): void {
    this.zoom.update((z) => Math.max(0.5, z - 0.25));
  }

  async validate(): Promise<void> {
    if (this.form().invalid) {
      this.form().markAllAsTouched();
      return;
    }
    const ok = await confirmDialog(
      'Actualizar información',
      'Esta información se actualizará. ¿Deseas continuar?'
    );
    if (!ok) {
      return;
    }
    showLoading('Guardando…');

    let validationId = this.data.type === 1 ? 6 : 3;
    const parties = this.parties();
    const form = this.form();
    const requests: ElectionResultUpdateRequest[] = [];

    Object.keys(form.controls).forEach((key, index) => {
      const previous = parties[index].result ?? 0;
      const value = Number(form.get(key)!.value);
      if (value !== previous && validationId !== 6) {
        validationId = 2;
      }
      requests.push({
        politicalPartyId: parties[index].politicalPartyId,
        electionAssociationId: Number(this.associationId),
        result: value,
      });
    });

    this.elections.updateResultSheet(requests).subscribe({
      next: () =>
        this.elections
          .updateValidate(this.associationId, String(validationId))
          .subscribe({
            next: () => {
              closeLoading();
              showSuccess(
                'Actualizar información',
                'Se actualizó la información correctamente.'
              );
              this.dialogRef.close({ status: validationId });
            },
            error: () => {
              closeLoading();
              showError();
            },
          }),
      error: () => {
        closeLoading();
        showError();
      },
    });
  }

  private statusToValidationId(status: string): number {
    switch (status) {
      case 'Corregida':
        return 2;
      case 'Sin Capturar':
        return 1;
      case 'Con resultados':
        return 4;
      case 'Validada':
        return 3;
      case 'Con resultados y acta':
        return 5;
      case 'Valida fisica':
        return 6;
      default:
        return 3;
    }
  }

  private loadData(): void {
    const useA = [2, 3, 6].includes(this.validationId);
    const request = useA
      ? this.elections.getElectionResultByIdA(this.associationId)
      : this.elections.getElectionResultById(this.associationId);

    request
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res: any[] | null) => {
        this.loading.set(false);
        if (!res?.length) {
          return;
        }
        const entry = res[0];
        const list = sortData<ElectionResultDtoList>(
          entry.electionResultDtoList ?? [],
          'softorder'
        );
        this.parties.set(list);
        this.buildForm(list);
        if (entry.pathSheet) {
          this.loadImage(entry.pathSheet);
        }
      });
  }

  private buildForm(list: ElectionResultDtoList[]): void {
    const controls: Record<string, any> = {};
    for (const party of list) {
      controls[this.controlName(party)] = this.fb.control(party.result ?? 0, {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.max(this.data.pollingBooth.votersList),
        ],
      });
    }
    const group = this.fb.group(controls);
    this.form.set(group);
    this.recomputeTotal(group);
    group.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.recomputeTotal(group));
  }

  private recomputeTotal(group: FormGroup): void {
    const total = Object.keys(group.controls).reduce(
      (acc, key) => acc + Number(group.get(key)!.value || 0),
      0
    );
    this.totalVotes.set(total);
  }

  private loadImage(path: string): void {
    this.elections
      .getPhotoBase64(path)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((blob) => {
        if (!blob) {
          return;
        }
        const reader = new FileReader();
        reader.onload = () =>
          this.imagePath.set(
            this.sanitizer.bypassSecurityTrustResourceUrl(
              reader.result as string
            )
          );
        reader.readAsDataURL(blob);
      });
  }
}
