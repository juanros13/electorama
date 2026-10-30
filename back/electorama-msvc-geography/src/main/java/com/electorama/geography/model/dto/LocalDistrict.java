package com.electorama.geography.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocalDistrict {

  private Long id;

  @NotBlank private Integer distritoLocal;

  @NotBlank private Integer entidadId;
}
