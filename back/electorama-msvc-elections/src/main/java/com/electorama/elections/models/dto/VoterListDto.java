package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class VoterListDto {
  private String pollingtBoothIds;
  private Integer progressVote;
}
