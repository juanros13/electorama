package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ResponseMunicipalityReportDto {
  private Long municipalityId;
  private String municipalityName;
  private Integer totalPollingBoothsOpened;
  private Integer totalPollingBoothsClosed;
  private Integer total;
}
