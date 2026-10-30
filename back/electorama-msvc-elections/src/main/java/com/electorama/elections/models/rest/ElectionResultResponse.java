package com.electorama.elections.models.rest;

import lombok.Data;

@Data
public class ElectionResultResponse {

  private Long id;
  private Long electionAssociationId;
  private Long candidatePartyAssociationId;
  private Integer result;
  private Long politicalPartyId;
  private String politicalPartyName;
  private String electionName;
  private String pollingBoothDescription;
  private Long seccionNumber;
  private Long seccionId;
  private Long municipalityId;
  private String municipalityName;
  private Long districtFederalId;
  private String districtFederalName;
  private Long districtLocalId;
  private String districtLocalName;
}
