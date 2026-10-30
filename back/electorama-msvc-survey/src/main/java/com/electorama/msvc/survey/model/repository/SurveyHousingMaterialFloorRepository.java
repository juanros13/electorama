package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyHousingMaterialFloorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyHousingMaterialFloorRepository
    extends JpaRepository<SurveyHousingMaterialFloorEntity, Long> {}
