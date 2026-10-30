package com.electorama.logfile.model.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IndicatorsDto {

  private Long id;

  private Long goalId;

  private String indicatorNumber;

  private String description;
}
