import { AuthService } from '@/app/core/auth/auth.service';
import { Scheme, Theming } from '@/app/core/theming';
import { UserService } from '@/app/core/user/user.service';
import { Component, computed, inject } from '@angular/core';
import { MatPseudoCheckbox } from '@angular/material/core';
import { MatIcon } from '@angular/material/icon';
import { MatDivider } from '@angular/material/list';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'user',
  imports: [
    MatDivider,
    MatIcon,
    MatMenu,
    MatMenuItem,
    MatPseudoCheckbox,
    MatMenuTrigger,
    RouterLink,
  ],
  template: `
    <button
      class="flex w-full cursor-pointer items-center gap-x-3 rounded-xl p-2 text-left hover:bg-neutral-700/10 dark:hover:bg-neutral-300/10"
      [matMenuTriggerFor]="userMenu"
    >
      <div
        class="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary-600 text-sm font-semibold text-white"
      >
        {{ initials() }}
      </div>
      <div class="flex min-w-0 flex-auto flex-col select-none">
        <div class="truncate font-medium">{{ displayName() }}</div>
        <div class="text-on-surface-variant truncate text-sm">
          {{ email() }}
        </div>
      </div>
      <mat-icon
        class="size-4"
        svgIcon="ellipsis-vertical"
      />
    </button>

    <mat-menu
      class="min-w-60"
      xPosition="before"
      yPosition="above"
      #userMenu="matMenu"
    >
      <div class="flex items-center px-4 py-3">
        <div
          class="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary-600 text-sm font-semibold text-white"
        >
          {{ initials() }}
        </div>
        <div class="ml-3 flex min-w-0 flex-auto flex-col select-none">
          <div class="truncate font-medium">{{ displayName() }}</div>
          <div class="text-on-surface-variant truncate text-xs">
            {{ email() }}
          </div>
        </div>
      </div>
      <mat-divider />
      <button
        mat-menu-item
        routerLink="/admin/settings"
      >
        <mat-icon svgIcon="user-round" />
        Mi cuenta
      </button>
      <mat-divider />
      <button
        mat-menu-item
        [matMenuTriggerFor]="appearanceMenu"
      >
        <mat-icon svgIcon="sun-moon" />
        Apariencia
      </button>
      <mat-divider />
      <button
        mat-menu-item
        (click)="signOut()"
      >
        <mat-icon svgIcon="log-out" />
        Cerrar sesión
      </button>
    </mat-menu>

    <mat-menu #appearanceMenu="matMenu">
      @for (item of schemes; track item.value) {
        <button
          mat-menu-item
          (click)="updateScheme(item.value)"
        >
          <mat-pseudo-checkbox
            appearance="minimal"
            class="mr-2"
            [state]="scheme() === item.value ? 'checked' : 'unchecked'"
          />
          <span>{{ item.label }}</span>
        </button>
      }
    </mat-menu>
  `,
})
export class User {
  // Dependencies
  private theming = inject(Theming);
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private router = inject(Router);

  // State
  protected user = this.userService.user;
  protected displayName = computed(
    () =>
      this.user()?.name || this.user()?.preferred_username || 'Usuario'
  );
  protected email = computed(() => this.user()?.email ?? '');
  protected initials = computed(() => {
    const name = this.displayName().trim();
    if (!name) {
      return 'U';
    }
    return name
      .split(/\s+/)
      .map((part) => part[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  });

  protected scheme = computed(() => this.theming.scheme());
  protected schemes: { label: string; value: Scheme }[] = [
    { label: 'Claro', value: 'light' },
    { label: 'Oscuro', value: 'dark' },
    { label: 'Sistema', value: 'system' },
  ];

  updateScheme(scheme: Scheme) {
    this.theming.scheme.set(scheme);
  }

  signOut() {
    this.authService.signOut().subscribe(() => {
      this.router.navigateByUrl('/auth/sign-in');
    });
  }
}
