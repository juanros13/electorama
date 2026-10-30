package com.electorama.logfile.model.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InstrumentsDto {
  private Long id;

  private Long indicatorId;

  private String acronym;

  private String article;
  private String name;
  private String description;
}
