package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyDisabilityInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyDisabilityInformationRepository
    extends JpaRepository<SurveyDisabilityInformationEntity, Long> {}
