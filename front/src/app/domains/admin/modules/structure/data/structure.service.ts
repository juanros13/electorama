import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

/**
 * Datos del usuario en msvc-users que usa el alta de visitas (authIdReference).
 * El organigrama y las secciones viven en msvc-structure (organization.service).
 */
@Injectable({ providedIn: 'root' })
export class StructureService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get userBase(): string {
    return this.appConfig.config.url_user;
  }

  getInfoUserByAuthId(authId: string): Observable<any> {
    return this.http.get<any>(`${this.userBase}/v1.0.0/readUser/${authId}`);
  }

}
