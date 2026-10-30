package com.electorama.msvc.citizenissues.model.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportDashboardIssuesDto {
  private ReportIssueStatusDto reportIssueStatus;
  private List<ReportIssueCategoryDto> reportIssueCategory;
  private List<ReportIssueRangeDateDto> reportIssueRangeDate;
  private ReportIssueTypeDto reportIssueType;
}
