package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class VotersParamsDto {
  private Integer municipalityId;
  private String municipality;
  private Integer numVoters;
  private Integer numVotersPending;
  private Integer federalDistrict;
  private String federalDistrictName;
  private Integer localDistrict;
  private String seccionNumber;
}
