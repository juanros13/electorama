import { AccessService } from '@/app/core/access/access.service';
import { AppConfigService } from '@/app/core/config/app-config';
import { AuthUtils } from '@/app/core/auth/auth.utils';
import { ActionType, ModuleType } from '@/app/core/data/enum/ActionType';
import { Log } from '@/app/core/data/model/LogfileTypes';
import { IpService } from '@/app/core/ip/ip.service';
import { LogfileService } from '@/app/core/logfile/logfile.service';
import { User } from '@/app/core/user/user.types';
import { UserService } from '@/app/core/user/user.service';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, of, switchMap, tap, throwError } from 'rxjs';

/**
 * Autenticación contra Keycloak.
 *
 * TODO(PKCE): hoy usa el flujo ROPC (`grant_type=password` + `client_secret`)
 * portado del proyecto viejo. Cuando Ops habilite el cliente público + PKCE,
 * reemplazar `signIn`/`signInUsingToken` por Authorization Code + PKCE y quitar
 * `keycloak_secret`. Ver `docs/migration/00-inventario-secrets-urls.md`.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
    private http = inject(HttpClient);
    private userService = inject(UserService);
    private ipService = inject(IpService);
    private logService = inject(LogfileService);
    private appConfig = inject(AppConfigService);
    private access = inject(AccessService);

    private _authenticated = false;

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    set accessToken(token: string) {
        localStorage.setItem('accessToken', token);
    }

    get accessToken(): string {
        return localStorage.getItem('accessToken') ?? '';
    }

    set refreshToken(token: string) {
        localStorage.setItem('refreshToken', token);
    }

    get refreshToken(): string {
        return localStorage.getItem('refreshToken') ?? '';
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    forgotPassword(email: string): Observable<any> {
        return this.http.post(`${this.appConfig.config.url_user}/recoverypassword`, {
            email,
        });
    }

    resetPassword(password: string): Observable<any> {
        return this.http.post('api/auth/reset-password', password);
    }

    /** Inicia sesión (flujo ROPC contra Keycloak). */
    signIn(credentials: { email: string; password: string }): Observable<any> {
        if (this._authenticated) {
            return throwError(() => new Error('User is already logged in.'));
        }

        return this.http
            .post(this.appConfig.tokenUrl, this.passwordGrantBody(credentials), {
                headers: this.formHeaders(),
            })
            .pipe(
                switchMap((response: any) => {
                    this.accessToken = response.access_token;
                    this.refreshToken = response.refresh_token;
                    this._authenticated = true;
                    // el menú y los guards salen de los roles del token nuevo
                    this.access.refresh();

                    const user = this.buildUserFromToken(this.accessToken);
                    if (user) {
                        this.userService.set(user);
                    }

                    this.logLogin(credentials.email);
                    return of(response);
                })
            );
    }

    /** Valida credenciales sin afectar la sesión actual. */
    checkPassword(credentials: { email: string; password: string }): Observable<any> {
        return this.http.post(this.appConfig.tokenUrl, this.passwordGrantBody(credentials), {
            headers: this.formHeaders(),
        });
    }

    /** Renueva la sesión usando el refresh token. */
    signInUsingToken(): Observable<boolean> {
        const body = this.withClientSecret(
            new HttpParams()
                .set('grant_type', 'refresh_token')
                .set('client_id', this.appConfig.config.keycloak_client)
                .set('refresh_token', this.refreshToken)
        );

        return this.http
            .post(this.appConfig.tokenUrl, body, { headers: this.formHeaders() })
            .pipe(
                catchError(() => of(false)),
                switchMap((response: any) => {
                    if (!response || response === 'false') {
                        return of(false);
                    }

                    this.accessToken = response.access_token;
                    this.refreshToken = response.refresh_token;
                    this._authenticated = true;
                    this.access.refresh();

                    const user = this.buildUserFromToken(this.accessToken);
                    if (!user) {
                        return of(false);
                    }
                    this.userService.set(user);
                    return of(true);
                })
            );
    }

    signOut(): Observable<any> {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        this._authenticated = false;
        // sin token no hay roles: el menú se vacía en el acto
        this.access.refresh();
        return of(true);
    }

    signUp(user: { name: string; email: string; password: string }): Observable<any> {
        return this.http.post(`${this.appConfig.config.url_user}/register`, user);
    }

    sendEmailVerify(email: { email: string }): Observable<any> {
        return this.http.post(`${this.appConfig.config.url_user}/sendmailverify`, email);
    }

    unlockSession(credentials: { email: string; password: string }): Observable<any> {
        return this.http.post('api/auth/unlock-session', credentials);
    }

    /** Comprueba el estado de autenticación. */
    check(): Observable<boolean> {
        if (this._authenticated) {
            return of(true);
        }

        if (this.accessToken === 'undefined' || !this.accessToken) {
            localStorage.removeItem('accessToken');
            return of(false);
        }

        if (AuthUtils.isTokenExpired(this.accessToken)) {
            return this.signInUsingToken();
        }

        return this.signInUsingToken();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Private methods
    // -----------------------------------------------------------------------------------------------------

    private formHeaders(): HttpHeaders {
        return new HttpHeaders({
            'Content-Type': 'application/x-www-form-urlencoded',
        });
    }

    private passwordGrantBody(credentials: {
        email: string;
        password: string;
    }): HttpParams {
        return this.withClientSecret(
            new HttpParams()
                .set('username', credentials.email)
                .set('password', credentials.password)
                .set('grant_type', 'password')
                .set('client_id', this.appConfig.config.keycloak_client)
        );
    }

    /** El cliente del front es público: el secreto sólo se manda si la config lo trae. */
    private withClientSecret(params: HttpParams): HttpParams {
        const secret = this.appConfig.config.keycloak_secret;
        return secret ? params.set('client_secret', secret) : params;
    }

    private buildUserFromToken(token: string): User | null {
        const decoded = this.userService.getDecodedAccessToken(token);
        if (!decoded) {
            return null;
        }

        // NOTE: el rol "principal" se toma por índice fijo igual que el viejo
        // (realm_access.roles[3]). La fuente de verdad es `roles`; el mapeo
        // robusto por RolType se resuelve en la navegación por rol.
        return {
            id: decoded.sub,
            name: decoded.name,
            email: decoded.email,
            given_name: decoded.given_name,
            preferred_username: decoded.preferred_username,
            email_verified: decoded.email_verified,
            rol: decoded.realm_access?.roles?.[3],
            roles: decoded.realm_access?.roles,
        } as User;
    }

    private logLogin(email: string): void {
        const now = new Date();
        this.ipService.getIPAddress().subscribe({
            next: (res: any) => {
                const log: Log = {
                    action: ActionType.LOG_IN,
                    quantity: 1,
                    message: `El usuario: ${email} inicio sesion`,
                    data: `email: ${email}, hora: ${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`,
                    accionId: null,
                    module: ModuleType.LOGIN,
                    ip: res?.ip,
                };
                this.logService.insertNewLog(log).subscribe();
            },
            error: () => {
                /* logging de IP best-effort: no bloquear el login */
            },
        });
    }
}
