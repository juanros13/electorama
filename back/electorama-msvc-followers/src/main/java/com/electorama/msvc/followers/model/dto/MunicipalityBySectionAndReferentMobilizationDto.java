package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class MunicipalityBySectionAndReferentMobilizationDto {
  private Long id;

  private String ReferentId;
  private String ReferentName;
  private Long municipalityId;
  private String municipality;
  private String sectionNumber;
  private Long sectionId;
  private String localDistrictName;
  private Long localDistrictId;
  private String federalDistrictName;
  private Long federalDistrictId;
  private Long goal;
  private Long progress;
}
