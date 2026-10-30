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
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { catchError, of } from 'rxjs';
import { OdsService } from '../data/ods.service';
import { Indicator, Instrument } from '../data/ods.types';

@Component({
  selector: 'app-ods-instruments',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ods-instruments.html',
  imports: [MatTableModule, MatPaginatorModule, MatIconModule],
})
export default class OdsInstruments {
  private service = inject(OdsService);
  private route = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);

  private paginator = viewChild.required(MatPaginator);

  protected readonly columns = ['number', 'description'];
  protected dataSource = new MatTableDataSource<Instrument>([]);
  protected indicator = signal<Indicator | null>(null);

  constructor() {
    afterNextRender(() => (this.dataSource.paginator = this.paginator()));
    const indicatorId = this.route.snapshot.paramMap.get('idIndicators') ?? '';
    this.service
      .getInstrumentsByIndicatorId(indicatorId)
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((instruments) => (this.dataSource.data = instruments ?? []));
    this.service
      .getIndicatorById(indicatorId)
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((indicator) => this.indicator.set(indicator));
  }
}
