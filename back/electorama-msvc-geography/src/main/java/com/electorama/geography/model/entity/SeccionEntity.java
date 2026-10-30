package com.electorama.geography.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "geo_secciones")
public class SeccionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull private Integer seccion;

  @NotNull private Long distritoLocalId;

  @NotNull private Long distritoFederalId;

  @NotNull private Long municipioId;

  @NotNull private Long entidadId;

  private String nomMun;

  private String nomEnt;

  private Integer meta;

  private Long distritoFed;

  private Long distritoLoc;

  // Vigencia de la sección en el marco geográfico: true=activa, false=obsoleta/fusionada.
  // Usada para filtrar las secciones dadas de baja en la actualización de marco (p.ej. 2026).
  private Boolean status;
}
