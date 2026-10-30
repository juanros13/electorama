package com.electorama.msvc.citizenissues.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportIssueCategoryDto {

  private String category;
  private Integer total;
}
