package com.electorama.logfile.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ods")
@Getter
@Setter
@NoArgsConstructor
public class OdsEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String objective;

  private String description;

  private String image;

  private String color;
}
