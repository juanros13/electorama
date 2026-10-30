import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, shareReplay, throwError } from 'rxjs';
import { AccountSummary } from './organization.types';

/**
 * Las cuentas de Keycloak de quien ocupa un puesto, vía msvc-users
 * (`/v2/accounts`: ADMIN, y el referente sobre los titulares de su rama). Las
 * crea msvc-structure al nombrar; aquí se leen y se les cambia la contraseña
 * desde el organigrama.
 */
@Injectable({ providedIn: 'root' })
export class AccountService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return `${this.appConfig.config.url_user}/v2/accounts`;
  }

  /**
   * La tarjeta del puesto y el perfil piden la misma cuenta al elegir un
   * puesto: se comparte la respuesta. Un error no se guarda, para reintentar.
   */
  private cache = new Map<string, Observable<AccountSummary>>();

  getAccount(authId: string): Observable<AccountSummary> {
    let request = this.cache.get(authId);
    if (!request) {
      request = this.http.get<AccountSummary>(`${this.base}/${authId}`).pipe(
        catchError((e) => {
          this.cache.delete(authId);
          return throwError(() => e);
        }),
        shareReplay({ bufferSize: 1, refCount: false })
      );
      this.cache.set(authId, request);
    }
    return request;
  }

  /** Contraseña definitiva (no se le pide cambiarla al entrar); 204 sin cuerpo. */
  changePassword(authId: string, password: string): Observable<void> {
    return this.http.put<void>(`${this.base}/${authId}/password`, { password });
  }
}
