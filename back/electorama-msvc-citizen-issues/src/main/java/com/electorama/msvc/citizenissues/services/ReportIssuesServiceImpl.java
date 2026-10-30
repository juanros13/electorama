package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.facade.ReportIssueCategoryByDatesFacade;
import com.electorama.msvc.citizenissues.facade.ReportIssueRangeDatesFacade;
import com.electorama.msvc.citizenissues.facade.ReportIssueStatusByDatesFacade;
import com.electorama.msvc.citizenissues.facade.ReportIssueTypesByDatesFacade;
import com.electorama.msvc.citizenissues.model.dto.ReportDashboardIssuesDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportIssuesServiceImpl implements ReportIssuesService {

  @Autowired ReportIssueStatusByDatesFacade reportIssueStatusByDatesFacade;

  @Autowired ReportIssueCategoryByDatesFacade reportIssueCategoryByDatesFacade;

  @Autowired ReportIssueRangeDatesFacade reportIssueRangeDatesFacade;
  @Autowired ReportIssueTypesByDatesFacade reportIssueTypesByDatesFacade;

  @Override
  public ReportDashboardIssuesDto getReportIssuesStatusDate(
      Integer municipalityId, String startDate, String endDate) {
    ReportDashboardIssuesDto reportDashboardIssuesDto = new ReportDashboardIssuesDto();
    reportDashboardIssuesDto.setReportIssueStatus(
        reportIssueStatusByDatesFacade.getReporteIssues(municipalityId, startDate, endDate));
    reportDashboardIssuesDto.setReportIssueCategory(
        reportIssueCategoryByDatesFacade.getReporteIssues(municipalityId, startDate, endDate));
    reportDashboardIssuesDto.setReportIssueRangeDate(
        reportIssueRangeDatesFacade.getReporteIssues(municipalityId, startDate, endDate));
    reportDashboardIssuesDto.setReportIssueType(
        reportIssueTypesByDatesFacade.getReporteIssues(municipalityId, startDate, endDate));

    return reportDashboardIssuesDto;
  }
}
