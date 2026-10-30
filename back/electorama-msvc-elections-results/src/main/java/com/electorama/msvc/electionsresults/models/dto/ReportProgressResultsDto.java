package com.electorama.msvc.electionsresults.models.dto;

import lombok.Data;

@Data
public class ReportProgressResultsDto {

  private Integer pollingBoothId;

  private ReportProgressDto reportProgress;
}
