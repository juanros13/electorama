package com.electorama.geography.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "geo_distritos_federales")
public class FederalDistrictEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(name = "distrito_federal")
  private Integer distritoFederal;

  @NotBlank private Integer tipo;

  @NotBlank private Integer entidadId;
}
