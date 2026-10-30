package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyHousingDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyHousingDocumentRepository
    extends JpaRepository<SurveyHousingDocumentEntity, Long> {}
