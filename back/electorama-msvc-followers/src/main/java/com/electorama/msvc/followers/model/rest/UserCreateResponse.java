package com.electorama.msvc.followers.model.rest;

import java.util.Date;
import lombok.Data;

@Data
public class UserCreateResponse {
  public int id;
  public String email;
  public String password;
  public String authId;
  public Boolean enabled;
  public String firstName;
  public String lastName;
  public String avatar;
  public String authIdReference;
  public Integer meta;
  public Long userSeccionId;
  public Long municipalityId;
  public Date deleted;
}
