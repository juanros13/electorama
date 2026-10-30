package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ElectionByPollingBoothsDto {
  private Long id;

  private Long pollingBoothId;

  private Integer votersList;

  private Integer votersRegistry;

  private Long electionAssociationId;

  private Boolean isSynchronized;

  private ElectionDto election;
}
