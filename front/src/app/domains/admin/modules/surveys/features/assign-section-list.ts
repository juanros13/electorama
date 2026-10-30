import { closeLoading, confirmDialog, showError, showLoading, showSuccess, showWarning } from '@/app/shared/ui/swal';
import { PageHeader } from '@/app/shared/ui/page-header';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { catchError, of } from 'rxjs';
import { SurveysService } from '../data/surveys.service';
import { SurveyUser, SurveyUserSection } from '../data/surveys.types';
import { AssignFolioDialog } from '../ui/assign-folio-dialog';
import { AssignSectionDialog } from '../ui/assign-section-dialog';

@Component({
  selector: 'app-assign-section-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './assign-section-list.html',
  imports: [
    FormsModule,
    PageHeader,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
})
export default class AssignSectionList {
  private service = inject(SurveysService);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(true);
  protected users = signal<SurveyUser[]>([]);
  protected search = signal('');

  protected filtered = computed(() => {
    const q = this.search().trim().toUpperCase();
    return q
      ? this.users().filter(
          (u) => u.name?.toUpperCase().includes(q) || u.deviceId?.toUpperCase().includes(q)
        )
      : this.users();
  });

  constructor() {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.service
      .getUserMunicipality()
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((users) => {
        this.users.set(users ?? []);
        this.loading.set(false);
      });
  }

  openAssignSection(user: SurveyUser): void {
    this.dialog
      .open(AssignSectionDialog, {
        data: { surveyUser: user, userList: this.users().filter((u) => u.id !== user.id) },
        width: '480px',
      })
      .afterClosed()
      .subscribe((ok) => ok && this.load());
  }

  openAssignFolio(user: SurveyUser): void {
    this.dialog
      .open(AssignFolioDialog, {
        data: { surveyUser: user, userList: this.users().filter((u) => u.id !== user.id) },
        width: '480px',
      })
      .afterClosed()
      .subscribe((ok) => ok && this.load());
  }

  async removeSection(user: SurveyUser, sec: SurveyUserSection): Promise<void> {
    if ((user.surveyUserSection?.length ?? 0) <= 1) {
      showWarning('No permitido', 'El usuario no puede quedarse sin secciones.');
      return;
    }
    if (!(await confirmDialog('¿Eliminar ID?', `Se quitará el ID ${sec.sectionNumber} de este usuario.`))) {
      return;
    }
    showLoading('Eliminando…');
    this.service.deleteSection(sec.id).subscribe({
      next: () => {
        closeLoading();
        showSuccess('Finalizado', 'Se eliminó correctamente.');
        this.load();
      },
      error: () => {
        closeLoading();
        showError();
      },
    });
  }
}
