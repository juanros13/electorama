package com.electorama.msvc.electionsresults.models.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "election_result")
public class ElectionResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long candidatePartyAssociationId;
  private Long districtFederalId;
  private String districtFederalName;
  private Long districtLocalId;
  private String districtLocalName;
  private Long electionAssociationId;
  private String electionName;
  private Long municipalityId;
  private String municipalityName;
  private Long politicalPartyId;
  private String politicalPartyName;
  private String pollingBoothDescription;
  private Integer result;
  private Long seccionNumber;
  private Long seccionId;
  private Long candidateId;
}
