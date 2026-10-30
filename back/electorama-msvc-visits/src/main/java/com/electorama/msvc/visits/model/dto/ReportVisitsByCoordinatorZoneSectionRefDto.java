package com.electorama.msvc.visits.model.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportVisitsByCoordinatorZoneSectionRefDto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Integer municipalityId;

  private String referenceAuthId;

  private String NombreReferenceAuth;

  private Integer seccionId;

  private Integer seccionNumber;

  private String coordinateZoneId;

  private String NombreCoordinateZone;

  private Integer primeraVisita;

  private Integer segundaVisita;

  private Integer terceraVisita;

  private Integer meta;

  private String nomMun;

  private Integer visitasAbrieronLaPuerta;

  private Integer visitasNoAbrieronLaPuerta;
}
