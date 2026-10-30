export interface User {
    id: string;
    name: string;
    email: string;
    avatar?: string;
    status?: string;
    given_name?: string;
    preferred_username?: string;
    email_verified?: boolean;
    rol?: string;
    realm_access?: RealmAccess;
    roles?: string[];
    authId?: string;
}

export interface RealmAccess {
    roles: string[];
}

export interface ResourceAccess {
    account: Account;
}

export interface Account {
    roles: string[];
}

export interface UserCoordinatorZone {
    authId: string;
    authIdReference: string;
    avatar: string;
    email: string;
    firstName: string;
    id: number;
    lastName: string;
    password: string;
    status: string;
    enabled: boolean;
    fullname: string;
}
