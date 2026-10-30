package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyIndigenousLanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyIndigenousLanguageRepository
    extends JpaRepository<SurveyIndigenousLanguageEntity, Long> {}
