import {
  closeLoading,
  confirmDialog,
  showError,
  showLoading,
  showSuccess,
  showWarning,
} from '@/app/shared/ui/swal';
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
import { SurveysService } from '../data/surveys.service';

const ALLOWED = ['pdf', 'png', 'jpg', 'jpeg'];

@Component({
  selector: 'app-upload-survey-cedula',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './upload-survey-cedula.html',
  imports: [MatButtonModule, MatIconModule],
})
export default class UploadSurveyCedula {
  private service = inject(SurveysService);
  private sanitizer = inject(DomSanitizer);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  private id = this.route.snapshot.paramMap.get('id') ?? '';
  protected folio = signal<string>('');
  protected ready = signal(false);
  protected file = signal<File | null>(null);
  protected preview = signal<SafeUrl | null>(null);
  protected isImage = signal(false);
  protected sizeLabel = signal('');

  constructor() {
    this.service
      .getSurveyById(this.id)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((s) => {
        if (!s) {
          showError('No se pudo cargar', 'El censo no está disponible.');
          this.router.navigate(['/admin/surveys/complete']);
          return;
        }
        this.folio.set(s.surveyUserFolio?.folio ?? '');
        if (s.pathFileCedula) {
          showWarning('Cédula ya subida', 'Las cédulas solo se pueden subir una vez.').then(() =>
            this.router.navigate(['/admin/surveys/complete'])
          );
        } else {
          this.ready.set(true);
        }
      });
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0];
    if (file) {
      this.handleFile(file);
    }
  }

  onSelect(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.handleFile(file);
    }
  }

  private handleFile(file: File): void {
    const ext = file.name.split('.').pop()?.toLowerCase();
    if (!ext || !ALLOWED.includes(ext)) {
      showWarning('Formato no válido', 'Solo se permite PDF, PNG o JPG.');
      return;
    }
    this.file.set(file);
    this.isImage.set(ext !== 'pdf');
    this.preview.set(this.sanitizer.bypassSecurityTrustResourceUrl(URL.createObjectURL(file)));
    this.sizeLabel.set(this.formatSize(file.size));
  }

  clear(): void {
    this.file.set(null);
    this.preview.set(null);
    this.sizeLabel.set('');
  }

  async upload(): Promise<void> {
    const file = this.file();
    if (!file) {
      showWarning('Elige un archivo', 'Selecciona un archivo antes de continuar.');
      return;
    }
    if (!(await confirmDialog('¿Subir archivo?', '¡Una vez subida la cédula no se podrá cambiar!'))) {
      return;
    }
    showLoading('Subiendo la cédula…');
    this.service
      .uploadSurveyCedula(file, this.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          closeLoading();
          showSuccess('¡Cédula subida!', 'El archivo se guardó correctamente.').then(() =>
            this.router.navigate(['/admin/surveys/complete'])
          );
        },
        error: () => {
          closeLoading();
          showError();
        },
      });
  }

  back(): void {
    this.router.navigate(['/admin/surveys/complete']);
  }

  private formatSize(bytes: number): string {
    if (bytes >= 1_000_000) {
      return `${(bytes / 1_000_000).toFixed(2)} MB`;
    }
    if (bytes >= 1000) {
      return `${(bytes / 1000).toFixed(2)} KB`;
    }
    return `${bytes} bytes`;
  }
}
