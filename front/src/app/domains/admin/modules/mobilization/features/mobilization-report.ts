import { PageHeader } from '@/app/shared/ui/page-header';
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
import { catchError, forkJoin, of } from 'rxjs';
import { MobilizationService } from '../data/mobilization.service';
import {
  GetMobilizationAlt,
  MobilizationByFederalDistrict,
  MobilizationByLocalDistrict,
  MobilizationByMunicipality,
} from '../data/mobilization.types';

const FEDERAL_DISTRICT_NAMES = [
  'MACUSPANA',
  'CÁRDENAS',
  'COMALCALCO',
  'CENTRO, VILLAHERMOSA',
  'PARAISO',
  'CENTRO, RANCHERIA IXTACOMITAN',
];

const sum = (items: { goal?: number; progress?: number }[], key: 'goal' | 'progress') =>
  items.reduce((acc, item) => acc + (item[key] ?? 0), 0);

@Component({
  selector: 'app-mobilization-report',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './mobilization-report.html',
  imports: [DecimalPipe, PercentPipe, PageHeader],
})
export default class MobilizationReport {
  private service = inject(MobilizationService);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(false);
  protected byMunicipality = signal<MobilizationByMunicipality[]>([]);
  protected byFederal = signal<MobilizationByFederalDistrict[]>([]);
  protected byLocal = signal<MobilizationByLocalDistrict[]>([]);
  protected byAlt = signal<GetMobilizationAlt[]>([]);

  protected totalGoal = computed(
    () => sum(this.byMunicipality(), 'goal') + sum(this.byAlt(), 'goal')
  );
  protected totalProgress = computed(
    () => sum(this.byMunicipality(), 'progress') + sum(this.byAlt(), 'progress')
  );
  protected totalPercent = computed(() => {
    const goal = this.totalGoal();
    return goal ? this.totalProgress() / goal : 0;
  });

  constructor() {
    this.load();
    // Refresco automático (jornada electoral en vivo).
    const intervalId = setInterval(() => this.load(), 60_000);
    this.destroyRef.onDestroy(() => clearInterval(intervalId));
  }

  ratio(row: { goal?: number; progress?: number }): number {
    return row.goal ? (row.progress ?? 0) / row.goal : 0;
  }

  private load(): void {
    this.loading.set(true);
    forkJoin([
      this.service.getMobilizationByMunicipality(),
      this.service.getMobilizationByFederalDistrict(),
      this.service.getMobilizationByLocalDistrict(),
      this.service.getMobilizationReferentReportAlt(),
    ])
      .pipe(
        catchError(() =>
          of([[], [], [], []] as [
            MobilizationByMunicipality[],
            MobilizationByFederalDistrict[],
            MobilizationByLocalDistrict[],
            GetMobilizationAlt[],
          ])
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(([municipality, federal, local, alt]) => {
        this.byMunicipality.set(municipality ?? []);
        (federal ?? []).forEach((fd, index) => {
          fd.federalDistrictName = FEDERAL_DISTRICT_NAMES[index] ?? fd.federalDistrict;
        });
        this.byFederal.set(federal ?? []);
        this.byLocal.set(local ?? []);
        this.byAlt.set(alt ?? []);
        this.loading.set(false);
      });
  }
}
