package com.electorama.msvc.canvasandwalls.model.rest;

import lombok.Data;

@Data
public class UserProfileResponse {

  private Long id;

  private String email;

  private String password;

  private String authId;

  private Boolean enabled;

  private String firstName;

  private String lastName;

  private String avatar;

  private String authIdReference;

  private Integer meta;

  private Long userSeccionId;

  private Long municipalityId;
}
