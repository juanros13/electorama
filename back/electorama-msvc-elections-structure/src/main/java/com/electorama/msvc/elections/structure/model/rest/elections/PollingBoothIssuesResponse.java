package com.electorama.msvc.elections.structure.model.rest.elections;

import lombok.Data;

@Data
public class PollingBoothIssuesResponse {
  private Long id;

  private Long pollingBoothId;

  private String Description;

  private PollingBoothIssuesSubCategoryResponse pollingBoothIssuesSubCategory;
}
