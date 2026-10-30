package com.electorama.elections.models.rest;

import java.util.Date;
import lombok.Data;

@Data
public class UserLocalDistrictResponse {
  private Long id;

  private String userAuthId;

  private String localDistrictId;

  private String name;

  private Date deleted;
}
