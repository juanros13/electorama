import { StatusType } from '@/app/core/data/enum/StatusType';
import { UserCoordinatorZone } from '@/app/core/user/user.types';
import { UserService } from '@/app/core/user/user.service';
import {
  closeLoading,
  confirmDialog,
  showError,
  showLoading,
  showSuccess,
} from '@/app/shared/ui/swal';
import { PageHeader } from '@/app/shared/ui/page-header';
import { TableShell } from '@/app/shared/ui/table-shell';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { StructureService } from '../data/structure.service';

@Component({
  selector: 'app-coordinators-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './coordinators-list.html',
  imports: [
    FormsModule,
    PageHeader,
    TableShell,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
})
export default class CoordinatorsList implements AfterViewInit {
  private structure = inject(StructureService);
  private userService = inject(UserService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = ['name', 'email', 'status', 'action'];
  protected readonly dataSource = new MatTableDataSource<UserCoordinatorZone>(
    []
  );
  protected loading = signal(true);
  protected query = signal('');

  constructor() {
    this.load();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator();
    this.dataSource.sort = this.sort();
  }

  applyFilter(value: string): void {
    this.query.set(value);
    this.dataSource.filter = value.trim().toLowerCase();
  }

  goToAdd(): void {
    this.router.navigate(['/admin/structure/addcoordinator']);
  }

  async setStatus(
    coordinator: UserCoordinatorZone,
    enable: boolean
  ): Promise<void> {
    const ok = await confirmDialog(
      '¿Estás seguro?',
      enable
        ? '¿Habilitar a este promotor?'
        : '¿Inhabilitar a este promotor?'
    );
    if (!ok) {
      return;
    }
    showLoading('Actualizando información…');
    this.userService
      .disableUser(
        coordinator.authId,
        enable ? StatusType.ENABLE : StatusType.DISABLE
      )
      .subscribe({
        next: () => {
          closeLoading();
          this.dataSource.data = this.dataSource.data.map((item) =>
            item.authId === coordinator.authId
              ? { ...item, enabled: enable }
              : item
          );
          showSuccess('¡Proceso exitoso!');
        },
        error: () => {
          closeLoading();
          showError();
        },
      });
  }

  private load(): void {
    this.loading.set(true);
    this.structure
      .getCoordinatorsZoneByAuthId()
      .pipe(
        catchError(() => of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => {
        this.dataSource.data = res ?? [];
        this.loading.set(false);
      });
  }
}
