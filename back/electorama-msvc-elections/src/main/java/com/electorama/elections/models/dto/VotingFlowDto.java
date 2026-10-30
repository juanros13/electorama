package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class VotingFlowDto {
  private Long id;
  private String Municipality;
  private Long federalDistrict;
  private Long localDistrict;
  private String categoryName;
  private Integer sectionNumber;
  private String pollingBoothName;
  private Long pollingBoothsId;
  private Long progressVote;
  private Integer votersList;
}
