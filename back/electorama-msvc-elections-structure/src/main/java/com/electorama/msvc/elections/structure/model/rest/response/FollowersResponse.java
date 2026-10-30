package com.electorama.msvc.elections.structure.model.rest.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowersResponse {
  private Long id;
  private String names;

  private String lastName;

  private String surName;

  private String curp;

  private String ine;

  private String promoterAuthId;
}
