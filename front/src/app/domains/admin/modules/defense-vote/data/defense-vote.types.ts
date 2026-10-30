export interface PollingBoothStatus {
  id: any;
  name: any;
}

export interface PollingBooth {
  id: number;
  seccionNumber: number;
  municipality: string;
  municipalityId: number;
  federalDistrict: number;
  federalDistrictName: string;
  localDistrict: number;
  pollingBoothNumber: string;
  pollingBoothStatus: PollingBoothStatus;
}

export interface PollingMunicipalitiesReport {
  municipalityId: number;
  municipalityName: string;
  totalPollingBoothsOpened: number;
  totalPollingBoothsClosed: number;
  total: number;
}

export interface GetPollingsBoothsByLocalDistrict {
  id: number;
  localDistrictId: number;
  totalPollingBoothsOpened: number;
  totalPollingBoothsClosed: number;
  total: number;
}

export interface GetPollingsBoothsByFederalDistrict {
  id: number;
  federalDistrictId: number;
  federalDistrictName: string;
  totalPollingBoothsOpened: number;
  totalPollingBoothsClosed: number;
  total: number;
}
