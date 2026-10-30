package com.electorama.msvc.documentmanagement.models.rest;

import lombok.Data;

@Data
public class SeccionResponse {
  private Long id;
  private Integer seccion;
  private Long distritoLocalId;
  private Long distritoFederalId;
  private Long municipioId;
  private Long entidadId;
  private String nomMun;
  private String nomEnt;
  private Integer meta;
}
