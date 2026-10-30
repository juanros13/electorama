import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

/**
 * Obtiene la IP pública del cliente vía ipify (JSONP).
 *
 * Requiere `withJsonpSupport()` en `provideHttpClient`. Se usa para el logging
 * de auditoría. TODO: revisar necesidad/privacidad de la llamada externa.
 */
@Injectable({ providedIn: 'root' })
export class IpService {
    private http = inject(HttpClient);

    getIPAddress(): Observable<any> {
        return this.http.jsonp('https://api64.ipify.org?format=jsonp', 'callback');
    }
}
