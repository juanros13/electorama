package com.electorama.msvc.followers.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class MarkByPollingBoothsDto {
  private Long id;

  private Long pollingBoothId;

  private List<FollowerMarkIdsDto> markList;
}
