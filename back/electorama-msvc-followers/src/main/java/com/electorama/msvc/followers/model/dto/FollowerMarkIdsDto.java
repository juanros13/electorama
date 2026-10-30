package com.electorama.msvc.followers.model.dto;

import com.electorama.msvc.followers.model.rest.PoliticalPartyResponse;
import lombok.Data;

@Data
public class FollowerMarkIdsDto {
  private Long id;

  private FollowersListDto followers;
  private PoliticalPartyResponse politicalParty;
  private Integer markNumber;
  private Boolean markFlag;
  private Boolean isSynchronized;
}
