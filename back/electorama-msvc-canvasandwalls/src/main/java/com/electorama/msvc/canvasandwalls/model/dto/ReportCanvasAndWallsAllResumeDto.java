package com.electorama.msvc.canvasandwalls.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportCanvasAndWallsAllResumeDto {
  private Integer id;
  private String category;
  private Integer total;
  private Integer goal;
}
