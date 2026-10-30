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
import { Goal, Indicator } from '../data/ods.types';

@Component({
  selector: 'app-ods-indicators',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ods-indicators.html',
  imports: [MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule],
})
export default class OdsIndicators {
  private service = inject(OdsService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  private paginator = viewChild.required(MatPaginator);

  protected readonly columns = ['number', 'description', 'accion'];
  protected dataSource = new MatTableDataSource<Indicator>([]);
  protected goal = signal<Goal | null>(null);

  constructor() {
    afterNextRender(() => (this.dataSource.paginator = this.paginator()));
    const goalId = this.route.snapshot.paramMap.get('idgoal') ?? '';
    this.service
      .getIndicatorsByGoalId(goalId)
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((indicators) => (this.dataSource.data = indicators ?? []));
    this.service
      .getGoalById(goalId)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((goal) => this.goal.set(goal));
  }

  goToInstruments(id: number): void {
    this.router.navigate(['/admin/ods/instruments', id]);
  }
}
