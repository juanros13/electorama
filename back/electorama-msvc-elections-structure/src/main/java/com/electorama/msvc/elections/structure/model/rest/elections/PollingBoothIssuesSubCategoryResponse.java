package com.electorama.msvc.elections.structure.model.rest.elections;

import lombok.Data;

@Data
public class PollingBoothIssuesSubCategoryResponse {
  private Long id;
  private String name;
  private PollingBoothIssuesCategoryResponse pollingBoothIssuesCategory;
}
