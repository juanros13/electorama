package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyRequestingOrAuxiliaryDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRequestingOrAuxiliaryDocumentRepository
    extends JpaRepository<SurveyRequestingOrAuxiliaryDocumentEntity, Long> {}
