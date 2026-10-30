package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class PollingsBoothsByMunicipalityDto {
  private Long id;

  private Long municipalityId;
  private String municipalityName;
  private Long totalPollingBoothsOpened;
  private Long totalPollingBoothsClosed;
  private Long total;
}
