package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyProfessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyProfessionRepository extends JpaRepository<SurveyProfessionEntity, Long> {}
