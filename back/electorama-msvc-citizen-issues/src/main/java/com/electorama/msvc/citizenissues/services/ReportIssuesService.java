package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.dto.ReportDashboardIssuesDto;

public interface ReportIssuesService {

  ReportDashboardIssuesDto getReportIssuesStatusDate(
      Integer municipalityId, String startDate, String endDate);
}
