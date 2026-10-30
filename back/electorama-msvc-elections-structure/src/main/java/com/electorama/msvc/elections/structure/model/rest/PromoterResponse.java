package com.electorama.msvc.elections.structure.model.rest;

import lombok.Data;

@Data
public class PromoterResponse {

  private String email;

  private String password;

  private String identification;

  private String firstName;

  private String lastName;
}
