import { LoadingBar } from '@/app/core/loading/loading-bar';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [LoadingBar, RouterOutlet],
  host: {
    // Ensure root component fills the entire viewport
    class: 'flex min-h-full w-full flex-auto flex-col',
  },
  // el cargador global va aquí para que salga en cualquier layout
  template: `
    <app-loading-bar />
    <router-outlet />
  `,
})
export class App {}
