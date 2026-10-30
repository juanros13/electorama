package com.electorama.elections.models.rest;

import lombok.Data;

@Data
public class FollowerMarkProgressResponse {
  private Long id;

  private Long pollingBoothId;
  private Long progressVote;
}
