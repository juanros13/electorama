package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class CatCommitteeResponseDto {
  private Long id;

  private String electoralBox;

  private String noCommittee;

  private String sectionNumber;

  private Integer goal;

  private Integer progress;
}
