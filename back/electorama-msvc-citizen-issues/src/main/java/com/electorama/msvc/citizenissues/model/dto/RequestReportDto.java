package com.electorama.msvc.citizenissues.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestReportDto {
  private Integer municipalityId;
  private String startDate;
  private String endDate;
}
