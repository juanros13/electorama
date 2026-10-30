package com.electorama.logfile.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "indicators")
@Getter
@Setter
@NoArgsConstructor
public class IndicatorsEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long goalId;

  private String indicatorNumber;

  private String description;
}
