package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyJobRepository extends JpaRepository<SurveyJobEntity, Long> {}
