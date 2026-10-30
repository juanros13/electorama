package com.electorama.msvc.electionsresults.models.dto;

import lombok.Data;

@Data
public class UpdateElectionsResultsSheetsDto {
  private Long id;
  private Long politicalPartyId;
  private Long electionAssociationId;
  private Integer result;
}
