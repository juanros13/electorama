package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallMunicipalityDto;
import java.util.List;

public interface ReportCanvasAndWallMunicipalityService {

  List<ReportCanvasAndWallMunicipalityDto> findAllByMunicipality(ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallMunicipalityDto> findByCategoryMunicipality(
      ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallMunicipalityDto> findByProjectMunicipality(
      ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallMunicipalityDto> findByProjectAndCategoryMunicipality(
      ReportCanvasAndWallDto reportVO);
}
