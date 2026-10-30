package com.electorama.elections.models.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ElectionPositionDto {
  private Long id;

  @NotBlank private String name;

  private Integer sortOrder;
}
