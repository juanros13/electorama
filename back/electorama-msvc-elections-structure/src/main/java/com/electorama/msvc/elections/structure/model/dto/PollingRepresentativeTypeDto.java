package com.electorama.msvc.elections.structure.model.dto;

import jakarta.persistence.*;
import lombok.Data;

@Data
public class PollingRepresentativeTypeDto {
  private Long id;

  private String name;
}
