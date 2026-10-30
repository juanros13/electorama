package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class CatCommitteeDto {
  private Long id;

  private String name;

  private String referenceAuthId;

  private String referenceAuthName;

  private String electoralBox;

  private Integer promotersGoal;

  private Integer promotersCount;

  private Long catSection;
}
