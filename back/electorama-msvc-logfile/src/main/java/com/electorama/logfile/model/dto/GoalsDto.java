package com.electorama.logfile.model.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoalsDto {

  private Long id;

  private Long odsId;

  private String goalNumber;

  private String description;
}
