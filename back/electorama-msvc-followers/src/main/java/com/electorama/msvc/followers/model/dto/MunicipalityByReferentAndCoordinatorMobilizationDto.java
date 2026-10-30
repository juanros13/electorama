package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class MunicipalityByReferentAndCoordinatorMobilizationDto {
  private Long id;

  private String ReferentId;
  private String ReferentName;
  private String CoordinatorId;
  private String CoordinatorName;
  private Long municipalityId;
  private Long seccionId;
  private String seccionNumber;
  private String municipality;
  private Long goal;
  private Long progress;
  private String federaDistrictName;
  private String localDistrictName;
}
