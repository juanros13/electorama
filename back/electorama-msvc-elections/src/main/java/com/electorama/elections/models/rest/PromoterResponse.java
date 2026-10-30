package com.electorama.elections.models.rest;

import lombok.Data;

@Data
public class PromoterResponse {

  private String email;

  private String password;

  private String identification;

  private String firstName;

  private String lastName;
}
