package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.DatesRequestDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallsAllDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallsAllResumeDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallsByTimeDto;
import java.util.List;

public interface ReportCanvasAndWallsAllService {

  List<ReportCanvasAndWallsAllDto> reportVisitsAll(
      DatesRequestDto fechasDto, String typeRol, String authIdHeader);

  List<ReportCanvasAndWallsAllResumeDto> reportVisitsAllResume();

  List<ReportCanvasAndWallsByTimeDto> findAllVisitsByTime(
      DatesRequestDto datesRequestDto, Integer category);
}
