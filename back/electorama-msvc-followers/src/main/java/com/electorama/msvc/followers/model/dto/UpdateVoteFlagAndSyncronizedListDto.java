package com.electorama.msvc.followers.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class UpdateVoteFlagAndSyncronizedListDto {
  private Long id;
  private List<Long> ids;
  private Boolean markFlag;
  private Boolean isSynchronized;
}
