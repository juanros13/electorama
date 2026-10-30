import { Tree, TreeItem, TreeItemGroup } from '@angular/aria/tree';
import { CdkMonitorFocus } from '@angular/cdk/a11y';
import { NgTemplateOutlet } from '@angular/common';
import { Component, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIcon } from '@angular/material/icon';
import {
  isActive,
  IsActiveMatchOptions,
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
} from '@angular/router';
import { filter, take } from 'rxjs';
import { AccessService } from '@/app/core/access/access.service';
import { NavigationItem } from '@/app/domains/admin/layout/data/navigation';

@Component({
  selector: 'navigation',
  imports: [
    MatIcon,
    NgTemplateOutlet,
    RouterLinkActive,
    Tree,
    TreeItem,
    TreeItemGroup,
    RouterLink,
    CdkMonitorFocus,
  ],
  template: `
    <div class="flex flex-col gap-y-4">
      @for (section of navigation(); track section.id) {
        <div class="flex flex-col px-4">
          @if (section.route) {
            <!-- Top-level link (item sin hijos con ruta, p.ej. Dashboard) -->
            <a
              class="navigation-item flex cursor-pointer items-center gap-x-2 rounded-lg px-2.5 py-2 select-none hover:bg-neutral-700/10 dark:hover:bg-neutral-300/10"
              routerLinkActive="bg-primary-600/10 font-semibold text-primary-700 dark:bg-primary-500/15 dark:text-primary-300"
              [routerLink]="section.route"
              [routerLinkActiveOptions]="section.activeOptions ?? { exact: true }"
            >
              @if (section.icon) {
                <mat-icon
                  class="pointer-events-none size-4"
                  [svgIcon]="section.icon"
                />
              }
              <div class="flex flex-auto flex-col font-medium">
                {{ section.label }}
                @if (section.description) {
                  <div class="text-xs text-neutral-400">
                    {{ section.description }}
                  </div>
                }
              </div>
            </a>
          } @else {
            <!-- Section title -->
            <div class="px-2.5 py-1.5 text-sm font-semibold text-secondary-400">
              {{ section.label }}

              <!-- Section description -->
              @if (section.description) {
                <div class="text-xs font-medium text-neutral-400">
                  {{ section.description }}
                </div>
              }
            </div>

            <!-- Section content -->
            @if (section.children && section.children.length > 0) {
              <ul
                ngTree
                class="mt-1 flex flex-col gap-y-1"
                [nav]="true"
                #tree="ngTree"
              >
                <ng-template
                  [ngTemplateOutlet]="treeNodes"
                  [ngTemplateOutletContext]="{
                    nodes: section.children,
                    parent: tree,
                  }"
                />
              </ul>
            }
          }

          <!-- Menu item -->
          <ng-template
            let-nodes="nodes"
            let-parent="parent"
            #treeNodes
          >
            @for (node of nodes; track node.id) {
              <a
                cdkMonitorElementFocus
                ngTreeItem
                routerLinkActive="bg-primary-600/10 font-semibold text-primary-700 dark:bg-primary-500/15 dark:text-primary-300"
                class="navigation-item flex cursor-pointer items-center gap-x-2 rounded-lg px-2.5 py-2 select-none hover:bg-neutral-700/10 dark:hover:bg-neutral-300/10"
                [parent]="parent"
                [value]="node.id"
                [label]="node.label"
                [disabled]="node.disabled"
                [selectable]="!node.children"
                [(expanded)]="node.expanded"
                [routerLink]="node.route"
                [routerLinkActiveOptions]="
                  node.activeOptions ?? { exact: true }
                "
                (click)="$event.preventDefault()"
                #rla="routerLinkActive"
                #treeItem="ngTreeItem"
              >
                <!-- Icon -->
                @if (node.icon) {
                  <mat-icon
                    class="pointer-events-none size-4"
                    [svgIcon]="node.icon"
                  />
                }

                <!-- Label -->
                <div class="flex flex-auto flex-col font-medium">
                  {{ node.label }}

                  <!-- Description -->
                  @if (node.description) {
                    <div class="text-xs">
                      {{ node.description }}
                    </div>
                  }
                </div>

                <!-- Badge -->
                @if (node.badge) {
                  <div
                    class="rounded bg-pink-400 px-1.5 py-0.5 text-xs font-semibold dark:bg-pink-700"
                  >
                    {{ node.badge }}
                  </div>
                }

                <!-- Expand icon -->
                @if (node.children && node.children.length > 0) {
                  <mat-icon
                    svgIcon="chevron-right"
                    class="pointer-events-none size-4 transition-[rotate]"
                    [class.rotate-90]="node.expanded"
                  />
                }
              </a>

              <!-- Children -->
              @if (node.children && node.children.length > 0) {
                <ul
                  class="flex flex-col gap-y-1 [&_ul>.navigation-item]:pl-14.5 [&>.navigation-item]:pl-8.5"
                  [class.hidden]="!node.expanded"
                  [class.mt-1]="node.expanded"
                  role="group"
                >
                  <ng-template
                    ngTreeItemGroup
                    [ownedBy]="treeItem"
                    #group="ngTreeItemGroup"
                  >
                    <ng-template
                      [ngTemplateOutlet]="treeNodes"
                      [ngTemplateOutletContext]="{
                        nodes: node.children,
                        parent: group,
                      }"
                    />
                  </ng-template>
                </ul>
              }
            }
          </ng-template>
        </div>
      }
    </div>
  `,
})
export class Navigation {
  // Dependencies
  private router = inject(Router);
  private access = inject(AccessService);

  // State
  protected navigation = signal<NavigationItem[]>([]);
  protected navigationEnd = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      take(1)
    )
  );

  constructor() {
    // El menú sale del rol del token, no del backend: mientras se rehace el
    // front, abrir una vista a un rol tiene que costar una línea en
    // `core/access/access.ts`. `NavigationMenuService` sigue en el código para
    // cuando el menú vuelva a mandarlo `msvc-users`.
    effect(() => {
      const items = this.access.navigation();
      this.navigationEnd();
      this.navigation.set(this.expandActiveRoute(structuredClone(items)));
    });
  }

  /**
   * Expand all parent routes of the active route.
   * @param items
   */
  expandActiveRoute(items: NavigationItem[]): NavigationItem[] {
    for (const item of items) {
      if (item.children?.length) {
        item.children = this.expandActiveRoute(item.children);

        if (item.children.some((child) => child.expanded)) {
          item.expanded = true;
        }
      }

      if (
        item.route &&
        isActive(
          item.route,
          this.router,
          this.isActiveOption(item.activeOptions ?? { exact: true })
        )()
      ) {
        item.expanded = true;
      }
    }
    return items;
  }

  /**
   * Convert simple exact option to full IsActiveMatchOptions.
   * @param options
   */
  isActiveOption(
    options: { exact: boolean } | IsActiveMatchOptions
  ): IsActiveMatchOptions {
    if ('exact' in options) {
      return options.exact
        ? {
            paths: 'exact',
            queryParams: 'exact',
            fragment: 'ignored',
            matrixParams: 'ignored',
          }
        : {
            paths: 'subset',
            queryParams: 'subset',
            fragment: 'ignored',
            matrixParams: 'ignored',
          };
    }

    return options;
  }
}
