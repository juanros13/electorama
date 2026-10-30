package com.electorama.msvc.electionsresults.models.rest;

import lombok.Data;

@Data
public class PollingBoothsElectionAssociationResponse {
  private Long id;

  private Long pollingBoothId;

  private Long electionId;

  private Boolean isSynchronized;
}
