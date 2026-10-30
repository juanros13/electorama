package com.electorama.geography.model.rest;

import lombok.Data;

@Data
public class UserResponse {

  private Long id;

  private String referenceAuthId;

  private Integer municipalityId;

  private Integer coordinadores;

  private Integer meta;
}
