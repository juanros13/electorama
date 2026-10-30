package com.electorama.msvc.followers.model.dto;

import lombok.Data;

@Data
public class FollowerMarkProgressDto {
  private Long id;

  private Long pollingBoothId;
  private Long progressVote;
}
