import { AppConfigService } from '@/app/core/config/app-config';
import { Log } from '@/app/core/data/model/LogfileTypes';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

/**
 * Auditoría: registra acciones de usuario (`insertNewLog`) en el microservicio
 * de logfile. Servicio transversal usado por auth y por los módulos de negocio.
 */
@Injectable({ providedIn: 'root' })
export class LogfileService {
    private http = inject(HttpClient);
    private appConfig = inject(AppConfigService);

    /** Info de una sección por id (vive aquí por paridad con el código viejo). */
    getSectionInfoById(sectionId: string): Observable<any> {
        const url = `${this.appConfig.config.url_geography}/seccion/${sectionId}`;
        return this.http.get<any>(url);
    }

    insertNewLog(log: Log): Observable<any> {
        return this.http.post(this.appConfig.config.url_log, log);
    }
}
