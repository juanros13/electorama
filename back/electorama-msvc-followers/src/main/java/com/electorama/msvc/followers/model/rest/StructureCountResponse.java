package com.electorama.msvc.followers.model.rest;

import lombok.Getter;
import lombok.Setter;

/** Una fila de reporte de msvc-structure: la clave de agrupacion y sus conteos. */
@Getter
@Setter
public class StructureCountResponse {

  private Long municipalityId;
  private Long sectionId;
  private String coordinatorZoneAuthId;
  private String referentAuthId;
  private Long followers;
  private Long promoters;
  private Long goal;
}
