package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ElectionVoterListDto {

  private Long id;

  private String userAuthId;

  private Integer electionId;

  private Integer voterList;
}
