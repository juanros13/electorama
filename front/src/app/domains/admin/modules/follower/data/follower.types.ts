export interface Sexo {
    value: string;
    viewValue: string;
}

export interface RolFamilia {
    value: string;
    viewValue: string;
}

export interface EstadoCivil {
    value: string;
    viewValue: string;
}

/** Fila de la lista de simpatizantes (forma backend-driven, campos usados en UI). */
export interface FollowerPromoter {
    id: number;
    promoterAuthId?: string;
    promoterName?: string;
    names?: string;
    lastName?: string;
    surName?: string;
    curp?: string;
    catSex?: { id?: number; nombre: string } | null;
    cellPhoneNumber?: string;
    sectionNumber?: string | number;
    supportsMovement?: string;
    deleted?: string | null;
}

/** Página devuelta por el backend para listados. */
export interface Page<T> {
    content: T[];
    totalElements: number;
}
