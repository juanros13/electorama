package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallReferrerDto;
import java.util.List;

public interface ReportCanvasAndWallReferrerService {

  List<ReportCanvasAndWallReferrerDto> findAllByMunicipality(ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallReferrerDto> findByCategory(ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallReferrerDto> findByProject(ReportCanvasAndWallDto reportVO);

  List<ReportCanvasAndWallReferrerDto> findByProjectAndCategory(ReportCanvasAndWallDto reportVO);
}
