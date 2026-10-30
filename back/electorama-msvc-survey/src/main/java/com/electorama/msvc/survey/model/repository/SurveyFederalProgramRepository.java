package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyFederalProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyFederalProgramRepository
    extends JpaRepository<SurveyFederalProgramEntity, Long> {}
