package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class PollingBoothStatusResultDto {
  private Long id;
  private String state;
  private String municipality;
  private Long federalDistrict;
  private Long localDistrict;
  private Integer sectionNumber;
  private Long pollingBoothId;
  private String status;
  private String pollingBoothName;
}
