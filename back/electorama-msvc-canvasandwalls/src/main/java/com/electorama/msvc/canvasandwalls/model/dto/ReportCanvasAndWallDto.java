package com.electorama.msvc.canvasandwalls.model.dto;

import java.util.Date;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ReportCanvasAndWallDto {
  private Integer category;
  private Integer proyect;
  private Integer municipality;
  private Date startDate;
  private Date endDate;
  private String referenceAuthId;
  private String coordinatorZoneAuthId;
}
