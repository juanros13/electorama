package com.electorama.geography.model.rest;

import java.util.Date;
import lombok.Data;

@Data
public class UserMunicipalityResponse {
  private Long id;

  private String userAuthId;

  private String municipalityId;

  private Long rejuntadoId;

  private String name;

  private Date deleted;
}
