package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class PollingsBoothsFederalDistrictDto {
  private Long id;

  private Long federalDistrictId;
  private String federalDistrictName;
  private Long totalPollingBoothsOpened;
  private Long totalPollingBoothsClosed;
  private Long total;
}
