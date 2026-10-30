import { ElectionsService } from '@/app/core/elections/elections.service';
import { sortData } from '@/app/shared/utils/sort';
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
import {
  GetPollingsBoothsByFederalDistrict,
  GetPollingsBoothsByLocalDistrict,
  PollingBooth,
  PollingMunicipalitiesReport,
} from '../data/defense-vote.types';

@Component({
  selector: 'app-polling-booth-report',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './polling-booth-report.html',
  imports: [DecimalPipe, PercentPipe, PageHeader],
})
export default class PollingBoothReport {
  private elections = inject(ElectionsService);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(false);
  protected booths = signal<PollingBooth[]>([]);
  protected municipalities = signal<PollingMunicipalitiesReport[]>([]);
  protected federal = signal<GetPollingsBoothsByFederalDistrict[]>([]);
  protected local = signal<GetPollingsBoothsByLocalDistrict[]>([]);

  protected open = computed(
    () =>
      this.booths().filter((b) => [1, 3].includes(b.pollingBoothStatus?.id))
        .length
  );
  protected close = computed(
    () =>
      this.booths().filter((b) => [2, 4].includes(b.pollingBoothStatus?.id))
        .length
  );
  protected percent = computed(() => {
    const total = this.booths().length;
    return total ? this.open() / total : 0;
  });

  constructor() {
    this.load();
    const intervalId = setInterval(() => this.load(), 60_000);
    this.destroyRef.onDestroy(() => clearInterval(intervalId));
  }

  private load(): void {
    this.loading.set(true);
    forkJoin([
      this.elections.getMunicipalitiesReport(),
      this.elections.getAllPollingBooth(),
      this.elections.getAllPollingBoothFederal(),
      this.elections.getAllPollingBoothLocal(),
    ])
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res: any) => {
        this.loading.set(false);
        if (!res) {
          return;
        }
        const [municipalities, boothsResp, federal, local] = res;
        this.municipalities.set(
          sortData(municipalities ?? [], 'municipalityName')
        );
        this.booths.set(boothsResp?.findAllPollingBooth ?? []);
        this.federal.set(federal?.getPollingsBoothsByFederalDistrict ?? []);
        this.local.set(local?.getPollingsBoothsByLocalDistrict ?? []);
      });
  }
}
