export interface ReportAllVisitsByAdmin {
    totalVisits: number;
    totalVisitsSecond: number;
    totalVisitsDeleted: number;
}

export interface ReportMetaByAdmin {
    meta: number;
}

export interface DatesRequest {
    startDate: string;
    endDate: string;
}

export interface ReportCanvasResumeByAdmin {
    id: number;
    category: string;
    total: number;
    goal: number;
    percent: number;
}
