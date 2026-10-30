package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ValidateSheetDto {
  private Integer totalDigitalValidate;
  private Integer totalFisicalValidate;
  private Integer totalSheets;
}
