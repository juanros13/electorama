package com.electorama.msvc.followers.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportFollowersMarkByPollingBoothDto {

  private Integer pollingBoothId;

  private ReportFollowersMobilzationDto report;
}
