package com.electorama.msvc.followers.model.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportFollowersByMunicipalityDto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer municipalityId;

  private Integer numFollowers;

  private String nomMun;

  private Integer numPromoters;
}
