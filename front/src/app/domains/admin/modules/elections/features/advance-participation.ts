import { ElectionsService } from '@/app/core/elections/elections.service';
import { sortData } from '@/app/shared/utils/sort';
import { DecimalPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { catchError, of } from 'rxjs';
import {
  ElectionResult,
  ElectionResultBySection,
  ElectionsPosition,
  ElectionsType,
  PoliticalPartyResult,
} from '../data/elections.types';

@Component({
  selector: 'app-advance-participation',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './advance-participation.html',
  imports: [
    FormsModule,
    DecimalPipe,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatIconModule,
  ],
})
export default class AdvanceParticipation {
  private elections = inject(ElectionsService);
  private destroyRef = inject(DestroyRef);

  protected loading = signal(false);
  protected electionsTypes = signal<ElectionsType[]>([]);
  protected electionsScopes = signal<ElectionsPosition[]>([]);
  protected electionId = signal('1');
  protected typeId = signal('');
  protected query = signal('');
  protected dateUpdated = signal('');

  private sections = signal<ElectionResultBySection[]>([]);
  protected filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    const list = this.sections();
    return q
      ? list.filter((s) => s.electionPositionName.toLowerCase().includes(q))
      : list;
  });

  constructor() {
    this.loadCatalogs();
    const intervalId = setInterval(() => this.loadResults(), 360_000);
    this.destroyRef.onDestroy(() => clearInterval(intervalId));
  }

  onFilterChange(): void {
    this.loadResults();
  }

  sortedCandidates(section: ElectionResultBySection): ElectionResult[] {
    return [...section.dataElectionResult].sort(
      (a, b) => b.totalVotes - a.totalVotes
    );
  }

  private loadCatalogs(): void {
    this.elections
      .getElectionType()
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res: any) => this.electionsTypes.set(res?.electionsTypes ?? []));

    this.elections
      .getElectionPosition()
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((res: any) => {
        const scopes = sortData<ElectionsPosition>(
          res?.electionsPositions ?? [],
          'sortOrder'
        );
        this.electionsScopes.set(scopes);
        if (scopes.length) {
          this.electionId.set(String(scopes[0].id));
        }
        this.loadResults();
      });
  }

  private loadResults(): void {
    this.loading.set(true);
    this.elections
      .getPoliticalPartyResult(this.electionId(), this.typeId())
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((data: PoliticalPartyResult | null) => {
        this.loading.set(false);
        this.stampTime();
        this.sections.set(data ? this.group(data) : []);
      });
  }

  private group(data: PoliticalPartyResult): ElectionResultBySection[] {
    const map = new Map<string, ElectionResultBySection>();
    for (const value of data.electionResult ?? []) {
      const existing = map.get(value.electionName);
      if (existing) {
        existing.totalVotes += value.totalVotes;
        existing.dataElectionResult.push(value);
      } else {
        const voterList = (data.voters_list ?? []).find(
          (l) => l.electionId === value.electionId
        );
        map.set(value.electionName, {
          length: value.resultList.length,
          totalVotes: value.totalVotes,
          electionPositionName: value.electionName,
          dataElectionResult: [value],
          voterList: voterList?.voterList ?? 0,
        });
      }
    }
    return Array.from(map.values());
  }

  private stampTime(): void {
    const now = new Date();
    const pad = (n: number) => n.toString().padStart(2, '0');
    this.dateUpdated.set(`${pad(now.getHours())}:${pad(now.getMinutes())}`);
  }
}
