package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class CandidatePartyAssociationDto {

  private Long id;

  private Integer sortOrder;

  private CandidateDto candidate;

  private PoliticalPartyDto politicalParty;
}
