package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class PollingBoothIssuesSubCategoryDto {
  private Long id;
  private String name;
  private PollingBoothIssuesCategoryDto pollingBoothIssuesCategory;
}
