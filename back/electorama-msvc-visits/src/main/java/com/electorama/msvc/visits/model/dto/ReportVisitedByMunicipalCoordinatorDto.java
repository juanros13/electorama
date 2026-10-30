package com.electorama.msvc.visits.model.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportVisitedByMunicipalCoordinatorDto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String referenceAuthId;

  private Integer municipalityId;

  private Integer primeraVisita;

  private Integer segundaVisita;

  private Integer terceraVisita;

  private Integer meta;

  private String firstName;

  private String lastName;

  private String nomMun;

  private Integer visitasAbrieronLaPuerta;

  private Integer visitasNoAbrieronLaPuerta;
}
