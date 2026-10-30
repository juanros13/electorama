package com.electorama.elections.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportPollingBoothResultsByPollingBoothIdsDto {

  private Integer pollingBoothId;

  private ReportProgressDto report;
}
