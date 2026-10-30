package com.electorama.geography.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FederalDistrict {

  private Long id;

  @NotBlank private Integer distritoFederal;

  @NotBlank private Integer tipo;

  @NotBlank private Integer entidadId;
}
