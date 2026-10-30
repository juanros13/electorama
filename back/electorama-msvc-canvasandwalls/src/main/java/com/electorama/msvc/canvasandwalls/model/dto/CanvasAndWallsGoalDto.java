package com.electorama.msvc.canvasandwalls.model.dto;

import lombok.Data;

@Data
public class CanvasAndWallsGoalDto {
  private Long id;

  private Long municipalityId;

  private String referenceAuthId;

  private String municipalityName;

  private CanvasAndWallProyectDto canvasAndWallsProyect;

  private CanvasAndWallCategoryDto canvasAndWallsCategory;

  private String referenceAuthName;

  private Integer goal;
}
