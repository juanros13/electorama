package com.electorama.geography.model.dto;

import lombok.Data;

@Data
public class Seccion {

  private Long id;

  private Integer seccion;

  private Long distritoLocalId;

  private Long distritoFederalId;

  private Long municipioId;

  private Long entidadId;

  private String nomMun;

  private String nomEnt;

  private Integer meta;

  private Long distritoFed;

  private Long distritoLoc;
}
