package com.electorama.elections.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "political_parties")
public class PoliticalPartyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String name;

  @NotBlank private String acronym;

  @NotBlank private String logo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "political_party_type_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PoliticalPartyTypeEntity politicalPartyType;

  private Integer softorder;
}
