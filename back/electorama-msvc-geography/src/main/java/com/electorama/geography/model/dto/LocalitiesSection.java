package com.electorama.geography.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class LocalitiesSection {

  private Long id;

  private String municipio;

  private String seccion;

  private String localidad;

  private Integer seccionId;

  private Date deleted;
}
