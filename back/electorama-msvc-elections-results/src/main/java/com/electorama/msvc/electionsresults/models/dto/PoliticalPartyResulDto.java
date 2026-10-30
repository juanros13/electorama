package com.electorama.msvc.electionsresults.models.dto;

import lombok.Data;

@Data
public class PoliticalPartyResulDto {

  private Long id;
  private String politicalPartyName;
  private Long candidateId;
  private Long result;
}
