package com.electorama.googlecalendar.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Calendar {

  private Long id;

  @NotBlank private String title;

  @NotBlank private String color;

  private Boolean visible;
}
