package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ResponseSectionReportDto {
  private Long id;
  private Long municipalityId;
  private String municipalityName;
  private Long sectionId;
  private String sectionNumber;
  private Integer totalPollingBoothsOpened;
  private Integer totalPollingBoothsClosed;
  private Integer total;
}
