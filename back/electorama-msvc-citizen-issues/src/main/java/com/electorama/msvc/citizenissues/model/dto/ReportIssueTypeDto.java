package com.electorama.msvc.citizenissues.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportIssueTypeDto {
  private Integer totalIssuesPhysical;
  private Integer totalIssuesMedia;
  private Integer totalIssuesDigital;
}
