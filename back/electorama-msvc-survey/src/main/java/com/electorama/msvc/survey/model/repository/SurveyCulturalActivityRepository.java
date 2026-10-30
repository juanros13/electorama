package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyCulturalActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyCulturalActivityRepository
    extends JpaRepository<SurveyCulturalActivityEntity, Long> {}
