package com.electorama.msvc.canvasandwalls.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ReportCanvasAndWallZoneEntity {

  @Id private Integer id;
  private Integer municipalityId;
  private String referenceAuthId;
  private String coordinateZoneId;
  private Integer canvasAndWallCategoryId;
  private Integer sectionId;
  private String category;
  private Integer canvasAndWallProyectId;
  private String proyect;
  private Long total;
}
