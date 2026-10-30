package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class SectionByMunicipalityMobilizationDto {
  private Long id;

  private Long municipalityId;
  private String municipality;
  private String sectionNumber;
  private Long goal;
  private Long progress;
}
