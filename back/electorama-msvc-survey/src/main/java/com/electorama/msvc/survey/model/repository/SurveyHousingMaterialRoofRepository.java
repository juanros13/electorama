package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyHousingMaterialRoofEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyHousingMaterialRoofRepository
    extends JpaRepository<SurveyHousingMaterialRoofEntity, Long> {}
