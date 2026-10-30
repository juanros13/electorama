package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class FollowerMarkDto {
  private Long id;

  private Long followerId;
  private Long politicalPartyId;
  private Long pollingBoothId;
  private Integer markNumber;
  private Boolean markFlag;
  private Boolean isSynchronized;

  private String fullName;

  private Long municipalityId;
  private String municipality;
  private String federalDistrict;
  private String federalDistrictName;
  private String localDistrict;
  private String seccionNumber;
}
