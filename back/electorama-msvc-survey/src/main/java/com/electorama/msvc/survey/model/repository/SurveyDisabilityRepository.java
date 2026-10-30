package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyDisabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyDisabilityRepository extends JpaRepository<SurveyDisabilityEntity, Long> {}
