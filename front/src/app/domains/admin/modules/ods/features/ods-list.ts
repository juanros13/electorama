import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { OdsService } from '../data/ods.service';
import { Objective } from '../data/ods.types';

@Component({
  selector: 'app-ods-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ods-list.html',
  imports: [MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule],
})
export default class OdsList {
  private service = inject(OdsService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  private paginator = viewChild.required(MatPaginator);

  protected readonly columns = ['number', 'description'];
  protected dataSource = new MatTableDataSource<Objective>([]);
  protected selected = signal<Objective | null>(null);

  constructor() {
    afterNextRender(() => (this.dataSource.paginator = this.paginator()));
    this.service
      .getOds()
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((ods) => (this.dataSource.data = ods ?? []));
  }

  select(element: Objective): void {
    this.selected.set(element);
  }

  goToGoals(id: number): void {
    this.router.navigate(['/admin/ods/goals', id]);
  }
}
