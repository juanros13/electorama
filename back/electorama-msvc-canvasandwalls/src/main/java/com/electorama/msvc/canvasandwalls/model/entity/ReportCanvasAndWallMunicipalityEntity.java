package com.electorama.msvc.canvasandwalls.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ReportCanvasAndWallMunicipalityEntity {

  @Id private Integer id;
  private Integer municipalityId;
  private Integer canvasAndWallCategoryId;
  private String category;
  private Integer canvasAndWallProyectId;
  private String proyect;
  private Long total;
  private Integer goal;
}
