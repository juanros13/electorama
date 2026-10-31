import { ChangeDetectionStrategy, Component, inject, output } from '@angular/core';
import { ElectoralHistoryService } from '../data/electoral-history.service';
import { STATES } from '../data/states';

/**
 * El estado del histórico. Cambiarlo cambia el de todo el módulo; quien lo usa
 * recarga con `(changed)`.
 */
@Component({
  selector: 'app-state-select',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <label class="flex items-center gap-2 text-sm">
      <span class="text-neutral-500">Estado</span>
      <select
        class="min-w-48 rounded-lg border border-neutral-200 bg-card px-3 py-2 text-sm dark:border-neutral-800"
        aria-label="Estado"
        (change)="select(+$any($event.target).value)"
      >
        @for (s of states; track s.id) {
          <option [value]="s.id" [selected]="s.id === service.stateId()">{{ s.name }}</option>
        }
      </select>
    </label>
  `,
})
export class StateSelect {
  protected service = inject(ElectoralHistoryService);
  protected readonly states = STATES;

  changed = output<number>();

  protected select(id: number): void {
    this.service.setState(id);
    this.changed.emit(id);
  }
}
