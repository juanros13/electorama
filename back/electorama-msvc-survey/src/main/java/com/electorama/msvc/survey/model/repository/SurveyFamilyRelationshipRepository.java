package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyFamilyRelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyFamilyRelationshipRepository
    extends JpaRepository<SurveyFamilyRelationshipEntity, Long> {}
