package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallZoneDto;
import java.util.List;

public interface ReportCanvasAndWallZoneService {

  List<ReportCanvasAndWallZoneDto> findAllByZone(ReportCanvasAndWallDto ReportVO);

  List<ReportCanvasAndWallZoneDto> findByMunicipality(ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallZoneDto> findByCategory(ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallZoneDto> findByProject(ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallZoneDto> findByProjectAndCategory(ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallZoneDto> findByCategoryAndMunicipality(ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallZoneDto> findByProjectAndMunicipality(ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallZoneDto> findByProjectAndCategoryAndMunicipality(
      ReportCanvasAndWallDto reportVO);
}
