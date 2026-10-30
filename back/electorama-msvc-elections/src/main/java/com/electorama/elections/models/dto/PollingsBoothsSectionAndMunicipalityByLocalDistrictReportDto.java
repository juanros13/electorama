package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class PollingsBoothsSectionAndMunicipalityByLocalDistrictReportDto {
  private Long id;
  private Long localDistrictId;
  private Long municipalityId;
  private String municipalityName;
  private Long sectionNumber;
  private Long totalPollingBoothsOpened;
  private Long totalPollingBoothsClosed;
  private Long total;
}
