package com.electorama.msvc.canvasandwalls.model.dto;

import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ReportCanvasAndWallMunicipalityDto {

  @Id private Integer id;
  private String municipalityId;
  private Integer canvasAndWallCategoryId;
  private String category;
  private Integer canvasAndWallProyectId;
  private String proyect;
  private Long total;
  private Integer goal;
  private Integer coordinators;
}
