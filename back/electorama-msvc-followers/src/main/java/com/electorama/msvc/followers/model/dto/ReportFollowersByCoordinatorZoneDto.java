package com.electorama.msvc.followers.model.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportFollowersByCoordinatorZoneDto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer municipalityId;

  private String cordinatorZoneAuthId;

  private String referentAuthId;

  private Integer seccionId;

  private Integer numFollowers;

  private String nombreReferenceAuth;

  private Integer seccionNumber;

  private String nombreCoordinateZone;

  private String nomMun;

  private Integer numPromoters;

  private Integer goal;
}
