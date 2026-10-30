package com.electorama.msvc.followers.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class UpdateVoteFlagListDto {
  private Long id;
  private List<Long> followerIds;
  private Boolean voteFlag;
}
