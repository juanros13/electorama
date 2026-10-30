export interface FollowerMobilization {
    followerIds: number[];
    voteFlag: boolean;
}

export interface MobilizationByMunicipality {
    id: number;
    municipapalityId: number;
    municipality: string;
    goal: number;
    progress: number;
}

export interface MobilizationByLocalDistrict {
    id: number;
    localDistrict: string;
    goal: number;
    progress: number;
}

export interface MobilizationByFederalDistrict {
    id: number;
    federalDistrict: string;
    federalDistrictName?: string;
    goal: number;
    progress: number;
}

export interface GetReferentAltResponse {
    success: string;
    getReferentAlt: GetReferentAlt[];
}

export interface GetReferentAlt {
    id: number;
    municipalityId: number;
    seccionId: number;
    seccionNumber: string;
    municipality: string;
    goal: number;
    progress: number;
    referentId: string;
    coordinatorId: string;
    coordinatorName: string;
    referentName?: string;
}

export interface GetMobilizationAltReport {
    getMobilizationAlt: GetMobilizationAlt[];
    success: string;
}

export interface GetMobilizationAlt {
    referentAuthId: string;
    goal: number;
    progress: number;
    referentName: string;
}
