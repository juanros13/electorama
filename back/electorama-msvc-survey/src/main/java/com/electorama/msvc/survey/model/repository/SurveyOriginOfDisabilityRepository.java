package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyOriginOfDisabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyOriginOfDisabilityRepository
    extends JpaRepository<SurveyOriginOfDisabilityEntity, Long> {}
