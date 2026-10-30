package com.electorama.elections.models.rest;

import lombok.Data;

@Data
public class PoliticalPartyResulResponse {

  private Long id;
  private String politicalPartyName;
  private Long result;
  private Long candidateId;
}
