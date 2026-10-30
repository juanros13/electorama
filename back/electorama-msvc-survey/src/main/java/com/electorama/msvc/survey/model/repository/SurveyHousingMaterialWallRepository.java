package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyHousingMaterialWallEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyHousingMaterialWallRepository
    extends JpaRepository<SurveyHousingMaterialWallEntity, Long> {}
