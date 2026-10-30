package com.electorama.msvc.followers.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CandidateDto {

  @NotBlank private Long id;

  @NotBlank private String names;

  @NotBlank private String lastNames;

  @NotBlank private String surNames;

  @NotBlank private ElectionDto election;
}
