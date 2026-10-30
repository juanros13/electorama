import { UserService } from '@/app/core/user/user.service';
import { PageHeader } from '@/app/shared/ui/page-header';
import { StatCard } from '@/app/shared/ui/stat-card';
import { DecimalPipe, PercentPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { DashboardService } from '../data/dashboard.service';
import { ReportCanvasResumeByAdmin } from '../data/dashboard.types';

@Component({
  selector: 'app-dashboard-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard-home.html',
  imports: [DecimalPipe, PercentPipe, PageHeader, StatCard],
})
export default class DashboardHome {
  private service = inject(DashboardService);
  private userService = inject(UserService);
  private destroyRef = inject(DestroyRef);

  protected greeting = computed(
    () => this.userService.user()?.name || this.userService.user()?.preferred_username || ''
  );

  protected totalVisits = signal<number | null>(null);
  protected meta = signal<number | null>(null);
  protected canvasResume = signal<ReportCanvasResumeByAdmin[]>([]);

  constructor() {
    this.service
      .getReportAllVisitByAdmin()
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => this.totalVisits.set(res?.totalVisits ?? 0));

    this.service
      .getReportMetaByAdmin()
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => this.meta.set(res?.meta ?? 0));

    this.service
      .getReportAllCanvasResumeByAdmin()
      .pipe(
        catchError(() => of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res) => this.canvasResume.set(res ?? []));
  }
}
