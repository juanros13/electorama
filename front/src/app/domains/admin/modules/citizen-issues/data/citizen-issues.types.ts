export interface CatIssueTypes {
    catIssueTypes: IssueTypes[];
}

export interface IssueTypes {
    id: number;
    name: string;
}

export interface CatIssueStatus {
    catIssueStatus: IssueStatus[];
}

export interface IssueStatus {
    id: number;
    name: string;
}

export interface CatIssueCategory {
    catIssueCategory: IssueCategory[];
}

export interface IssueCategory {
    id: number;
    name: string;
    issueCategorySubject?: any;
}

export interface CatIssueSolutionDocumentsCategory {
    catIssueSolutionDocumentsCategory: IssueSolutionDocumentsCategory[];
}

export interface IssueSolutionDocumentsCategory {
    id: number;
    name: string;
}

export interface IssueDTO {
    id?: number;
    invoice?: string;
    registrationDate: string;
    fullName: string;
    phone: string;
    municipalityId: number;
    localityId: number;
    localityOther: string;
    issueDetailEntities: IssueDetailDTO[];
}

export interface IssueDetailDTO {
    id?: number;
    comments: string;
    issueType: IssueTypes;
    issueCategory: IssueCategory;
    issueStatus: IssueStatus;
    issueSolution?: SolutionDAO;
    urgent: boolean;
    // Campos condicionales por categoría (programas de bienestar / con comprobante).
    name?: string | null;
    lastName?: string | null;
    surName?: string | null;
    curp?: string | null;
    issueSolutionResourceTmp?: Resource | null;
    issueSolutionDocumentsCategory?: IssueSolutionDocumentsCategory | null;
    pathFile?: string | null;
    uuid?: string;
}

export interface IssueDAO {
    id: number;
    invoice: string;
    registrationDate: string;
    fullName: string;
    phone: string;
    municipalityId: number;
    municipalityName: string;
    localityId: number;
    localityName: string;
    localityOther: string;
    userCreateId: string;
    deletedDate?: string;
    issueDetailEntities?: {
        id: number;
        comments: string;
        issueType: IssueTypes;
        issueCategory: IssueCategory;
        issueStatus: IssueStatus;
        issueSolution: SolutionDAO;
        issueSolutionDocumentsCategory: IssueSolutionDocumentsCategory;
        pathFile: string;
        uuid: string;
    }[];
}

export interface IssueDetailDAO {
    id: number;
    comments: string;
    issue: IssueDAO;
    issueCategory: IssueCategory;
    issueSolution: SolutionDAO;
    issueStatus: IssueStatus;
    issueType: IssueTypes;
    userCreateId: string;

    issueSolutionResourceTmp: Resource;
    name: string;
    lastName: string;
    surName: string;
    curp: string;
    birtDay: string;
    urgent: boolean;

    issueSolutionDocumentsCategory: IssueSolutionDocumentsCategory;
    pathFile: string;
    uuid: string;

    registrationDate: string;
    deletedDate: string;
}

export interface ResultIssueStatus {
    totalIssuesOnHold: number;
    totalIssuesInProcess: number;
    totalIssuesCompleted: number;
    totalIssuesDeleted: number;
    totalIssuesUrgent: number;
}

export interface Resource {
    id?: number;
    name: string;
    issueCategory: IssueCategory;
}

export interface Provider {
    id: number;
    name: string;
}

export interface SolutionDTO {
    followerId: number;
    issueDetailId: number;
    quantity: number;
    solutionDate: string;
    comments: string;
    issueSolutionProvider: Provider;
    issueSolutionResource: Resource;
}

export interface SolutionDAO {
    id: number;
    followerId: number;
    issueDetailId: number;
    quantity: number;
    solutionDate: string;
    comments: string;
    issueSolutionProvider: Provider;
    issueSolutionResource: Resource;
}

export interface FileDTO {
    file: any;
    issueDetailId: string;
    issueSolutionDocumentsCategory: string;
    uuid: string;
}
