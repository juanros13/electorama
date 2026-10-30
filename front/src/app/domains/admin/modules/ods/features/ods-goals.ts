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
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { OdsService } from '../data/ods.service';
import { Goal, Objective } from '../data/ods.types';

@Component({
  selector: 'app-ods-goals',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ods-goals.html',
  imports: [MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule],
})
export default class OdsGoals {
  private service = inject(OdsService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  private paginator = viewChild.required(MatPaginator);

  protected readonly columns = ['number', 'description', 'accion'];
  protected dataSource = new MatTableDataSource<Goal>([]);
  protected ods = signal<Objective | null>(null);

  constructor() {
    afterNextRender(() => (this.dataSource.paginator = this.paginator()));
    const odsId = this.route.snapshot.paramMap.get('idOds') ?? '';
    this.service
      .getGoalsByOdsId(odsId)
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((goals) => (this.dataSource.data = goals ?? []));
    this.service
      .getOdsById(odsId)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((ods) => this.ods.set(ods));
  }

  goToIndicators(id: number): void {
    this.router.navigate(['/admin/ods/indicators', id]);
  }
}
