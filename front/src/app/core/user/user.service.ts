import { AppConfigService } from '@/app/core/config/app-config';
import { ProfileData, UpdateDataProfile } from '@/app/core/data/model/ProfileType';
import { AuthUtils } from '@/app/core/auth/auth.utils';
import { User } from '@/app/core/user/user.types';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

/**
 * Usuario actual de la sesión, expuesto como signal. Reemplaza el ReplaySubject
 * del proyecto viejo. El usuario se llena al iniciar sesión (desde el JWT) y
 * puede refrescarse con `get()`.
 */
@Injectable({ providedIn: 'root' })
export class UserService {
    private http = inject(HttpClient);
    private appConfig = inject(AppConfigService);

    private readonly _user = signal<User | null>(null);

    /** Usuario actual como signal de solo lectura. */
    readonly user = this._user.asReadonly();

    set(user: User): void {
        this._user.set(user);
    }

    /** Valor actual del usuario (o null). */
    get current(): User | null {
        return this._user();
    }

    get accessToken(): string {
        return localStorage.getItem('accessToken') ?? '';
    }

    getDecodedAccessToken(token: string): any {
        return AuthUtils.decodeToken(token);
    }

    /** ¿El usuario actual tiene el rol indicado? */
    hasRole(role: string): boolean {
        return !!this._user()?.roles?.includes(role);
    }

    /** Refresca el usuario desde el backend. */
    get(): Observable<User> {
        return this.http
            .get<User>(`${this.appConfig.config.url_user}/profile`)
            .pipe(tap((user) => this._user.set(user)));
    }

    getProfileData(): Observable<ProfileData> {
        const url = `${this.appConfig.config.url_user}/v1.0.0/profileDB`;
        return this.http.get<ProfileData>(url);
    }

    updateProfile(user: UpdateDataProfile, authId: string): Observable<any> {
        const url = `${this.appConfig.config.url_user}/v1.0.0/update/${authId}`;
        return this.http.put(url, user);
    }

    disableUser(authId: string, statusUser: string): Observable<any> {
        const url = `${this.appConfig.config.url_user}/v1.0.0/updateStatus/${authId}/${statusUser}`;
        return this.http.put(url, null);
    }
}
