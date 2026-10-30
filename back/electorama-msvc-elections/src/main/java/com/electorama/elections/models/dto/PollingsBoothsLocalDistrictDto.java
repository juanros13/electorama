package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class PollingsBoothsLocalDistrictDto {
  private Long id;

  private Long localDistrictId;
  private Long totalPollingBoothsOpened;
  private Long totalPollingBoothsClosed;
  private Long total;
}
