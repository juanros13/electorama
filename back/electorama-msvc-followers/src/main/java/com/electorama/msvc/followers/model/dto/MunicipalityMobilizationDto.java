package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class MunicipalityMobilizationDto {
  private Long id;

  private Long municipalityId;
  private String municipality;
  private Long goal;
  private Long progress;
}
