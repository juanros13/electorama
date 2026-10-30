package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.CanvasAndWallsGoalDto;
import java.util.List;

public interface CanvasAndWallsGoalService {
  CanvasAndWallsGoalDto save(CanvasAndWallsGoalDto canvasAndWallsGoalDto);

  List<CanvasAndWallsGoalDto> findByReferenceAuthId(String referenceAuthId);

  List<CanvasAndWallsGoalDto> findByMunicipalityId(Integer municipalityId);

  List<CanvasAndWallsGoalDto> findByMunicipalityIdAndReferenceAuthId(
      Integer municipalityId, String referenceAuthId);

  List<CanvasAndWallsGoalDto> findByAll();
}
