package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class MobilizationAltDto {

  private String referentAuthId;
  private String referentName;
  private Long goal;
  private Long progress;
}
