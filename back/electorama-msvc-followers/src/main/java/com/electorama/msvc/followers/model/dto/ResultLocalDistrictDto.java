package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class ResultLocalDistrictDto {
  private Long id;

  private String ReferentId;
  private String ReferentName;
  private Long municipalityId;
  private String municipality;
  private Long goal;
  private Long progress;
  private Long localDistrict;
}
