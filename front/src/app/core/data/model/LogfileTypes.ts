export interface Log{
    action: string;
    quantity: number;
    message: string;
    data: string | null;
    accionId: number | null;
    module: string;
    ip: string;
}
