package com.electorama.elections.models.dto;

import java.util.List;
import lombok.Data;

@Data
public class PollingBoothIssuesListDto {
  private Long id;

  private Long pollingBoothId;

  private List<PollingBoothIssuesDetailDto> detailsIssues;
}
