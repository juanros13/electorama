package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ResponseSectionReportByDistrictFederalDto {
  private Long id;
  private Long federalDistrict;
  private String federalDistrictName;
  private Long municipalityId;
  private String municipalityName;
  private String sectionNumber;
  private Integer totalPollingBoothsOpened;
  private Integer totalPollingBoothsClosed;
  private Integer total;
}
