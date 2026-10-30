import { ElectionsService } from '@/app/core/elections/elections.service';
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
import { FollowerMarkVoter } from '../data/elections.types';

const sumBy = (rows: FollowerMarkVoter[], key: 'numVoters' | 'numVotersPending') =>
  rows.reduce((acc, r) => acc + (r[key] ?? 0), 0);

@Component({
  selector: 'app-voting-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './voting-dashboard.html',
  imports: [DecimalPipe, PercentPipe, PageHeader],
})
export default class VotingDashboard {
  private elections = inject(ElectionsService);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(false);
  protected byMunicipality = signal<FollowerMarkVoter[]>([]);
  protected byFederal = signal<FollowerMarkVoter[]>([]);
  protected byLocal = signal<FollowerMarkVoter[]>([]);

  protected marked = computed(() => sumBy(this.byMunicipality(), 'numVoters'));
  protected pending = computed(() =>
    sumBy(this.byMunicipality(), 'numVotersPending')
  );
  protected total = computed(() => this.marked() + this.pending());
  protected percent = computed(() =>
    this.total() ? this.marked() / this.total() : 0
  );

  constructor() {
    this.load();
    const intervalId = setInterval(() => this.load(), 60_000);
    this.destroyRef.onDestroy(() => clearInterval(intervalId));
  }

  ratio(row: FollowerMarkVoter): number {
    const total = (row.numVoters ?? 0) + (row.numVotersPending ?? 0);
    return total ? (row.numVoters ?? 0) / total : 0;
  }

  private load(): void {
    this.loading.set(true);
    forkJoin([
      this.elections.getDataMarkVoting(1),
      this.elections.getDataMarkVoting(2),
      this.elections.getDataMarkVoting(3),
    ])
      .pipe(
        catchError(() => of([[], [], []] as FollowerMarkVoter[][])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(([mun, fed, loc]) => {
        this.loading.set(false);
        this.byMunicipality.set(mun ?? []);
        this.byFederal.set(fed ?? []);
        this.byLocal.set(loc ?? []);
      });
  }
}
