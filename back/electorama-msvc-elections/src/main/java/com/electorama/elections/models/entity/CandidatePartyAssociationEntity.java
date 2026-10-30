package com.electorama.elections.models.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "candidates_party_associations")
public class CandidatePartyAssociationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer sortOrder;

  @ManyToOne
  @JoinColumn(name = "candidate_id")
  private CandidateEntity candidate;

  @ManyToOne
  @JoinColumn(name = "political_party_id")
  private PoliticalPartyEntity politicalParty;
}
