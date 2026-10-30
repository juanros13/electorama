package com.electorama.msvc.citizenissues.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportIssueStatusDto {

  private Integer totalIssuesOnHold;
  private Integer totalIssuesInProcess;
  private Integer totalIssuesCompleted;
  private Integer totalIssuesDeleted;
  private Integer totalIssuesUrgent;
}
