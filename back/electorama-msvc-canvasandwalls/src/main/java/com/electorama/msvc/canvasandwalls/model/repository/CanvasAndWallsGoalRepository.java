package com.electorama.msvc.canvasandwalls.model.repository;

import com.electorama.msvc.canvasandwalls.model.entity.CanvasAndWallsGoalEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanvasAndWallsGoalRepository
    extends JpaRepository<CanvasAndWallsGoalEntity, Long> {

  List<CanvasAndWallsGoalEntity> findByReferenceAuthId(String referenceAuthId);

  List<CanvasAndWallsGoalEntity> findByMunicipalityId(Integer municipalityId);

  List<CanvasAndWallsGoalEntity> findByMunicipalityIdAndReferenceAuthId(
      Integer municipalityId, String referenceAuthId);
}
