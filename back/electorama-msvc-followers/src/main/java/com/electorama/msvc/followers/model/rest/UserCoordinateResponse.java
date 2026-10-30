package com.electorama.msvc.followers.model.rest;

import lombok.Data;

@Data
public class UserCoordinateResponse {

  private Long id;

  private String referenceAuthId;

  private Integer municipalityId;

  private Integer seccionId;

  private String coordinateZoneId;

  private Integer meta;
}
