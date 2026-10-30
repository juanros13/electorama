import { SessionKeys } from '@/app/core/data/enum/SessionKeys';
import { Injectable } from '@angular/core';

/**
 * Estado de navegación persistido en sessionStorage (flags de settings,
 * menú permitido, datos de coordinador completados). Portado del proyecto viejo.
 */
@Injectable({ providedIn: 'root' })
export class NavService {
    setNavSettings(value: string): void {
        sessionStorage.setItem(SessionKeys.NAVIGATE_SETTINGS, value);
    }

    getNavSettings(): boolean | null {
        const data = sessionStorage.getItem(SessionKeys.NAVIGATE_SETTINGS);
        if (data === null) {
            return null;
        }
        return data === 'true';
    }

    setAllowMenu(value: string): void {
        sessionStorage.setItem(SessionKeys.ALLOWE_MENU, value);
    }

    getAllowMenu(): boolean {
        const data = sessionStorage.getItem(SessionKeys.ALLOWE_MENU);
        if (data === null) {
            return true;
        }
        return data === 'true';
    }

    setCoordinatorDataCompleted(value: string): void {
        sessionStorage.setItem(SessionKeys.COORDINATOR_DATA_COMPLETED, value);
    }

    getCoordinatorDataCompleted(): boolean {
        const data = sessionStorage.getItem(SessionKeys.COORDINATOR_DATA_COMPLETED);
        return data === null ? false : data === 'true';
    }
}
