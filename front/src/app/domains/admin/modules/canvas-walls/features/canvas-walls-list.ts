import { RolType } from '@/app/core/data/enum/RolType';
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
  computed,
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
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { catchError, merge, of, startWith, Subject, switchMap } from 'rxjs';
import { CanvasWallsService } from '../data/canvas-walls.service';
import { CanvasWall } from '../data/canvas-wall.types';

@Component({
  selector: 'app-canvas-walls-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './canvas-walls-list.html',
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
export default class CanvasWallsList implements AfterViewInit {
  private service = inject(CanvasWallsService);
  private userService = inject(UserService);
  private destroyRef = inject(DestroyRef);

  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = [
    'coordinateZone',
    'reference',
    'project',
    'category',
    'municipality',
    'locality',
    'section',
    'photo',
    'status',
    'actions',
  ];

  protected loading = signal(false);
  protected data = signal<CanvasWall[]>([]);
  protected resultsLength = signal(0);
  protected query = signal('');
  protected isAdmin = computed(() => this.userService.hasRole(RolType.ADMIN));

  private search$ = new Subject<void>();

  ngAfterViewInit(): void {
    this.sort()
      .sortChange.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => (this.paginator().pageIndex = 0));

    merge(this.sort().sortChange, this.paginator().page, this.search$)
      .pipe(
        startWith(null),
        switchMap(() => {
          this.loading.set(true);
          const sort = this.sort();
          return this.service
            .getCanvasWalls(
              sort.active || 'id',
              sort.direction || 'desc',
              this.paginator().pageIndex,
              this.paginator().pageSize,
              this.query().trim()
            )
            .pipe(catchError(() => of(null)));
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((page: any) => {
        this.loading.set(false);
        this.data.set(page?.content ?? []);
        this.resultsLength.set(page?.totalElements ?? 0);
      });
  }

  search(): void {
    this.paginator().pageIndex = 0;
    this.search$.next();
  }

  openPhoto(path: string): void {
    if (!path) {
      return;
    }
    this.service
      .getPhoto(path)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((blob) => {
        if (blob) {
          window.open(URL.createObjectURL(blob), '_blank');
        }
      });
  }

  async remove(row: CanvasWall): Promise<void> {
    const ok = await confirmDialog(
      '¿Estás seguro?',
      `¿Eliminar la lona/barda "${row.reference}"?`
    );
    if (!ok) {
      return;
    }
    showLoading('Eliminando…');
    this.service.deleteCanvasWalls(row.id).subscribe({
      next: () => {
        closeLoading();
        showSuccess('¡Proceso exitoso!', 'Se eliminó el registro.');
        this.data.update((list) => list.filter((item) => item.id !== row.id));
      },
      error: () => {
        closeLoading();
        showError();
      },
    });
  }
}
