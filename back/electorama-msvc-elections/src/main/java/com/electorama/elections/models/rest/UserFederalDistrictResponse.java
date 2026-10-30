package com.electorama.elections.models.rest;

import java.util.Date;
import lombok.Data;

@Data
public class UserFederalDistrictResponse {
  private Long id;

  private String userAuthId;

  private String federalDistrictId;

  private String name;

  private Date deleted;
}
