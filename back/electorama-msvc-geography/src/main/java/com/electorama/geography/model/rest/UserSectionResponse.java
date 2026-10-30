package com.electorama.geography.model.rest;

import java.util.Date;
import lombok.Data;

@Data
public class UserSectionResponse {
  private Long id;

  private String userAuthId;

  private String seccionNumber;

  private Long seccionGeoId;

  private Integer meta;

  private Date deleted;

  private Long municipalityId;
}
