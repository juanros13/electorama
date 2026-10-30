package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class TotalIncidentsDto {
  private Long id;

  private String subCategoryName;
  private Long total;
}
