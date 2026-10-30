import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let loading: LoadingService;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    loading = TestBed.inject(LoadingService);
  });

  afterEach(() => vi.useRealTimers());

  it('no se enseña si el trabajo termina antes de 150 ms', () => {
    // una petición rápida no debe hacer parpadear la barra
    loading.start();
    vi.advanceTimersByTime(100);
    loading.stop();
    vi.advanceTimersByTime(200);
    expect(loading.active()).toBe(false);
  });

  it('se enseña cuando el trabajo pasa de 150 ms', () => {
    loading.start();
    vi.advanceTimersByTime(160);
    expect(loading.active()).toBe(true);
    loading.stop();
    expect(loading.active()).toBe(false);
  });

  it('sigue visible hasta que termina el último trabajo', () => {
    loading.start();
    loading.start();
    vi.advanceTimersByTime(160);
    loading.stop();
    expect(loading.active()).toBe(true);
    loading.stop();
    expect(loading.active()).toBe(false);
  });

  it('un stop de más no deja el contador en negativo', () => {
    // si no, el siguiente start quedaría "cerrado" y la barra no saldría
    loading.stop();
    loading.start();
    vi.advanceTimersByTime(160);
    expect(loading.active()).toBe(true);
  });

  it('track cierra el trabajo aunque el observable falle', () => {
    const source = new Subject<number>();
    loading.track(source).subscribe({ error: () => undefined });
    vi.advanceTimersByTime(160);
    expect(loading.active()).toBe(true);
    source.error(new Error('500'));
    expect(loading.active()).toBe(false);
  });

  it('track cierra el trabajo si se cancela la suscripción', () => {
    const subscription = loading.track(new Subject<number>()).subscribe();
    vi.advanceTimersByTime(160);
    subscription.unsubscribe();
    expect(loading.active()).toBe(false);
  });
});
