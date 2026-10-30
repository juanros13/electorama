import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export type FileKind = 'IMG' | 'DOC';

/** Selector de tipo de comprobante: imagen (con compresión) o PDF. */
@Component({
  selector: 'app-type-file-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>Tipo de comprobante</h2>
    <mat-dialog-content>
      <p class="text-neutral-500">Elige cómo quieres cargar el comprobante.</p>
      <div class="mt-4 grid grid-cols-2 gap-4">
        <button
          type="button"
          class="flex flex-col items-center gap-2 rounded-xl border border-neutral-200 p-6 hover:bg-neutral-50 dark:border-neutral-700 dark:hover:bg-neutral-800"
          (click)="choose('IMG')"
        >
          <mat-icon class="text-emerald-500" svgIcon="image" />
          <span class="font-medium">Imagen</span>
          <span class="text-xs text-neutral-500">.jpg / .png</span>
        </button>
        <button
          type="button"
          class="flex flex-col items-center gap-2 rounded-xl border border-neutral-200 p-6 hover:bg-neutral-50 dark:border-neutral-700 dark:hover:bg-neutral-800"
          (click)="choose('DOC')"
        >
          <mat-icon class="text-red-500" svgIcon="file-text" />
          <span class="font-medium">Documento</span>
          <span class="text-xs text-neutral-500">.pdf</span>
        </button>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button matButton mat-dialog-close>Cancelar</button>
    </mat-dialog-actions>
  `,
})
export class TypeFileDialog {
  private ref = inject(MatDialogRef<TypeFileDialog>);

  choose(typeFile: FileKind): void {
    this.ref.close({ typeFile });
  }
}
