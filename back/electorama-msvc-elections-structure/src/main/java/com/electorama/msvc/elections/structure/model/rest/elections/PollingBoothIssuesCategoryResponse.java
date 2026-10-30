package com.electorama.msvc.elections.structure.model.rest.elections;

import lombok.Data;

@Data
public class PollingBoothIssuesCategoryResponse {
  private Long id;
  private String name;
  private PollingBoothIssuesSubCategoryResponse pollingBoothIssuesSubCategory;
}
