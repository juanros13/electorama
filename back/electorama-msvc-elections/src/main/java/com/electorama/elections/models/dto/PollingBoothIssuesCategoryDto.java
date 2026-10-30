package com.electorama.elections.models.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PollingBoothIssuesCategoryDto {
  private Long id;
  private String name;
  private List<PollingBoothIssuesSubCategoryDto> subCategories;

  public void addSubCategory(PollingBoothIssuesSubCategoryDto subCategory) {
    if (subCategories == null) {
      subCategories = new ArrayList<>();
    }
    subCategories.add(subCategory);
  }
}
