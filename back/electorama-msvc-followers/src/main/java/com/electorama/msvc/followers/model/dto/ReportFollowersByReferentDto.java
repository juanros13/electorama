package com.electorama.msvc.followers.model.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportFollowersByReferentDto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer municipalityId;

  private String nomMun;

  private String referentAuthId;

  private String NombreReferenceAuth;

  private Integer numFollowers;

  private Integer numPromoters;
}
