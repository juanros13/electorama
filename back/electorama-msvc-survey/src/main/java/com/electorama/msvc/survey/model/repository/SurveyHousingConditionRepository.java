package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyHousingConditionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyHousingConditionRepository
    extends JpaRepository<SurveyHousingConditionEntity, Long> {}
