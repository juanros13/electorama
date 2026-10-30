package com.electorama.elections.models.rest;

import lombok.Data;

@Data
public class LocalityResponse {

  private Long id;

  private String municipio;

  private String seccion;

  private String localidad;

  private Integer seccionId;
}
