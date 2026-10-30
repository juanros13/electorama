package com.electorama.msvc.users.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class UserSeccionDto {
  private Long id;

  private String userAuthId;

  private String seccionNumber;

  private Long seccionGeoId;

  private Integer meta;

  private Date deleted;

  private Long municipalityId;
}
