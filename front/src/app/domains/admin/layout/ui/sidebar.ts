import { Component } from '@angular/core';
import { Navigation } from '@/app/domains/admin/layout/ui/navigation';
import { User } from '@/app/domains/admin/layout/ui/user';

@Component({
  selector: 'admin-sidebar',
  imports: [Navigation, User],
  host: {
    class: 'flex w-full flex-auto flex-col',
  },
  template: `
    <!-- Header -->
    <div class="relative flex items-center gap-x-2.5 pt-5 pr-4 pb-0 pl-6">
      <!-- Logo -->
      <img
        src="/images/logo/logo.svg"
        class="size-8"
        alt="Electorama"
      />

      <div class="flex flex-col">
        <div
          class="font-display text-xl leading-none font-bold tracking-wide text-primary-600"
        >
          ELECTORAMA
        </div>
        <div class="font-mono text-2xs leading-3 font-medium tracking-tighter text-neutral-500">
          Gestión electoral
        </div>
      </div>
    </div>

    <!-- Navigation -->
    <navigation class="mt-8 mb-4 flex-auto" />

    <!-- Spacer -->
    <div class="flex-auto"></div>

    <!-- Footer -->
    <div class="p-2">
      <user />
    </div>
  `,
})
export class AdminSidebar {}
