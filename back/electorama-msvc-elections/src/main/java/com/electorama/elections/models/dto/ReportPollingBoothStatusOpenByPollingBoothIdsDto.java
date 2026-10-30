package com.electorama.elections.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportPollingBoothStatusOpenByPollingBoothIdsDto {

  private Integer pollingBoothId;

  private String openDate;

  private String closeDate;

  private PollingBoothStatusDto status;
}
