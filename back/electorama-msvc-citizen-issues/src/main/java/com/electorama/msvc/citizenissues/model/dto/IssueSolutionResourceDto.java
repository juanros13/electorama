package com.electorama.msvc.citizenissues.model.dto;

import lombok.Data;

@Data
public class IssueSolutionResourceDto {
  Long id;
  String name;
  private IssueCategoryDto issueCategory;
}
