package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyTypeOfDisabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyTypeOfDisabilityRepository
    extends JpaRepository<SurveyTypeOfDisabilityEntity, Long> {}
