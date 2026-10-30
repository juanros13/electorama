package com.electorama.logfile.model.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OdsDto {
  private Long id;

  private String objective;

  private String description;

  private String image;

  private String color;
}
