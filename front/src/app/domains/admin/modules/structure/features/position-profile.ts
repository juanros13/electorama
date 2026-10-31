import { LEVEL_NAMES } from '@/app/core/data/levels';
import { UserService } from '@/app/core/user/user.service';
import { ChartCard, CHART_FORE, CHART_GRID, ChartOptions, isDarkScheme } from '@/app/shared/ui/chart-card';
import { SectionCard } from '@/app/shared/ui/section-card';
import { confirmDialog, showError, showSuccess } from '@/app/shared/ui/swal';
import { generatePassword } from '@/app/shared/utils/credentials';
import { DatePipe, DecimalPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, distinctUntilChanged, map, of, switchMap } from 'rxjs';
import { AccountService } from '../data/account.service';
import { OrganizationService } from '../data/organization.service';
import { AccountSummary, DailyActivity, PositionSummary } from '../data/organization.types';

/** Cómo se lee cada rol del realm; las erratas (CORDINATOR) son las de Keycloak. */
const ROLE_LABEL: Record<string, string> = {
  CORDINATOR_MUNICIPAL: LEVEL_NAMES.REFERENT.one,
  CORDINATOR_MUNICIPAL_ALT: `${LEVEL_NAMES.REFERENT.one} (alterna)`,
  CORDINATOR_ZONE: LEVEL_NAMES.ZONE_COORDINATOR.one,
  CORDINATOR_ZONE_ALT: `${LEVEL_NAMES.ZONE_COORDINATOR.one} (alterna)`,
  PROMOTER: LEVEL_NAMES.PROMOTER.one,
  ADMIN: 'Administrador',
};

type AccountState =
  | { kind: 'none' }
  | { kind: 'loading' }
  | { kind: 'ok'; account: AccountSummary }
  | { kind: 'missing' }
  | { kind: 'forbidden' }
  | { kind: 'error' };

type Range = 'recent' | 'all';

/** ApexCharts nombra meses y días en inglés si no se le da el idioma (eje datetime y tooltip). */
const APEX_ES = {
  name: 'es',
  options: {
    months: [
      'enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
      'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre',
    ],
    shortMonths: ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'],
    days: ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado'],
    shortDays: ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb'],
  },
};

/** Los días que muestra «recientes»: los últimos con datos, no los últimos del calendario. */
const RECENT_DAYS = 30;

/**
 * El perfil de quien ocupa el puesto elegido en el organigrama: su cuenta
 * (con cambio de contraseña), su meta contra lo que lleva y la actividad por
 * día de todo lo que cuelga de él.
 *
 * Sirve para cualquier nivel: en un movilizador la actividad es la suya; en un
 * promotor o referente, la de su rama.
 */
@Component({
  selector: 'app-position-profile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './position-profile.html',
  // mismo espaciado que las tarjetas hermanas del panel de detalle
  host: { class: 'flex flex-col gap-4' },
  imports: [
    ChartCard,
    DatePipe,
    DecimalPipe,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    SectionCard,
  ],
})
export class PositionProfile {
  private accounts = inject(AccountService);
  private organization = inject(OrganizationService);
  private destroyRef = inject(DestroyRef);
  private users = inject(UserService);

  position = input.required<PositionSummary>();
  /** Campaña cerrada: se consulta, no se cambia nada (tampoco la contraseña). */
  readOnly = input(false);
  /** Convencidos de todo el subárbol; el árbol ya los pidió. */
  subtree = input<number | null>(null);
  /** Meta de la sección del promotor (catálogo de geography); undefined mientras llega. */
  sectionGoal = input<number | null | undefined>(null);

  // ─────────────────────────── cuenta

  protected account = signal<AccountState>({ kind: 'none' });
  protected password = signal('');
  protected showPassword = signal(false);
  protected savingPassword = signal(false);
  protected passwordValid = computed(() => this.password().length >= 8);

  // ─────────────────────────── actividad

  protected activity = signal<DailyActivity[]>([]);
  protected activityLoading = signal(false);
  protected range = signal<Range>('recent');

  protected visibleDays = computed(() => {
    const days = this.activity();
    return this.range() === 'recent' ? days.slice(-RECENT_DAYS) : days;
  });

  protected stats = computed(() => {
    const days = this.visibleDays();
    const active = days.filter((d) => d.captured > 0);
    const total = active.reduce((t, d) => t + d.captured, 0);
    const best = active.reduce<DailyActivity | null>(
      (b, d) => (!b || d.captured > b.captured ? d : b),
      null
    );
    return {
      total,
      activeDays: active.length,
      days: days.length,
      best,
      average: active.length ? Math.round(total / active.length) : 0,
      first: days[0]?.day ?? null,
      last: days.at(-1)?.day ?? null,
    };
  });

  protected activityChart = computed<ChartOptions | null>(() => {
    const days = this.visibleDays();
    if (!days.some((d) => d.captured > 0)) {
      return null;
    }
    return {
      series: [
        {
          name: 'Convencidos capturados',
          data: days.map((d) => ({ x: d.day, y: d.captured })),
        },
      ],
      colors: ['#9D2449'],
      chart: {
        type: 'bar',
        height: 220,
        toolbar: { show: false },
        zoom: { enabled: false },
        locales: [APEX_ES],
        defaultLocale: 'es',
        foreColor: CHART_FORE,
        fontFamily: 'inherit',
      },
      plotOptions: { bar: { columnWidth: '80%', borderRadius: 2 } },
      dataLabels: { enabled: false },
      // los días llegan como "2024-03-26" (medianoche UTC): en UTC la etiqueta es el día correcto
      xaxis: { type: 'datetime', labels: { datetimeUTC: true, format: 'dd MMM' } },
      yaxis: { labels: { formatter: (v: number) => Math.round(v).toLocaleString('es-MX') } },
      grid: { borderColor: CHART_GRID },
      tooltip: {
        theme: isDarkScheme() ? 'dark' : 'light',
        x: { format: 'dddd dd MMM yyyy' },
        y: { formatter: (v: number) => `${v.toLocaleString('es-MX')} convencidos` },
      },
    };
  });

  // ─────────────────────────── meta

  protected progressPct = computed(() => {
    const goal = this.position().goal;
    const done = this.subtree();
    return goal && done !== null ? Math.round((done / goal) * 100) : null;
  });

  constructor() {
    // la cuenta cambia con el titular, no con cada recarga del puesto
    toObservable(this.position)
      .pipe(
        map((p) => p.holderAuthId),
        distinctUntilChanged(),
        switchMap((authId) => {
          this.password.set('');
          this.showPassword.set(false);
          if (!authId) {
            return of<AccountState>({ kind: 'none' });
          }
          this.account.set({ kind: 'loading' });
          return this.accounts.getAccount(authId).pipe(
            map((account): AccountState => ({ kind: 'ok', account })),
            catchError((e: HttpErrorResponse) =>
              of<AccountState>(
                e.status === 404
                  ? { kind: 'missing' }
                  : e.status === 403
                    ? { kind: 'forbidden' }
                    : { kind: 'error' }
              )
            )
          );
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((state) => this.account.set(state));

    toObservable(this.position)
      .pipe(
        map((p) => p.id),
        distinctUntilChanged(),
        switchMap((id) => {
          this.activityLoading.set(true);
          return this.organization.getActivity(id).pipe(catchError(() => of(null)));
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((days) => {
        this.activityLoading.set(false);
        if (days === null) {
          showError('No se pudo leer la actividad del puesto');
        }
        this.activity.set(days ?? []);
      });
  }

  /** Si quien está viendo es el titular de este puesto. */
  protected isMine = computed(() => {
    const me = this.users.user()?.id;
    return !!me && this.position().holderAuthId === me;
  });

  protected roleLabel(role: string): string {
    return ROLE_LABEL[role] ?? role;
  }

  protected levelLabel(): string {
    return this.position().levelName;
  }

  protected generate(): void {
    this.password.set(generatePassword());
    this.showPassword.set(true);
  }

  protected async changePassword(): Promise<void> {
    const state = this.account();
    if (state.kind !== 'ok' || !this.passwordValid() || this.readOnly()) {
      return;
    }
    const who = this.position().holderName || state.account.username;
    const ok = await confirmDialog(
      '¿Cambiar la contraseña?',
      `${who} (${state.account.username}) tendrá que entrar con la nueva. La anterior deja de servir.`
    );
    if (!ok) {
      return;
    }
    this.savingPassword.set(true);
    this.accounts
      .changePassword(state.account.authId, this.password())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.savingPassword.set(false);
          showSuccess(
            'Contraseña cambiada',
            `Compártela con ${who}: usuario ${state.account.username}.`
          );
          this.password.set('');
          this.showPassword.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.savingPassword.set(false);
          showError('No se pudo cambiar la contraseña', e?.error?.message ?? 'Intenta de nuevo');
        },
      });
  }
}
