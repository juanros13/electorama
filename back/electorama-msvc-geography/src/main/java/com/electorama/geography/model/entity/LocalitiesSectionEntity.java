package com.electorama.geography.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "geo_localidades_juntos")
public class LocalitiesSectionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull private String municipio;

  @NotNull private String seccion;

  @NotNull private String localidad;

  @NotNull private Integer seccionId;

  private Date deleted;
}
