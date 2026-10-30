package com.electorama.msvc.followers.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ElectionTypeDto {
  private Long id;

  @NotBlank private String name;
}
