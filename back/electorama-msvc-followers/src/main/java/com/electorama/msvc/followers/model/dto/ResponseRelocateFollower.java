package com.electorama.msvc.followers.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class ResponseRelocateFollower {

  public List<Integer> followersList;
  public String promoterAuthId;
}
