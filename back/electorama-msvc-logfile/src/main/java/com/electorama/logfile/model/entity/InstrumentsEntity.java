package com.electorama.logfile.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "instruments")
@Getter
@Setter
@NoArgsConstructor
public class InstrumentsEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long indicatorId;

  private String acronym;

  private String article;
  private String name;
  private String description;
}
