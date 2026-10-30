package com.electorama.msvc.electionsresults.models.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PoliticalPartyResponse {

  private Long id;

  @NotBlank private String name;

  @NotBlank private String acronym;

  @NotBlank private String logo;
  private Integer softorder;

  private Integer result;
}
