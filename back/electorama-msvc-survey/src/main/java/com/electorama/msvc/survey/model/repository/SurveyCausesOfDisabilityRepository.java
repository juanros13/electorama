package com.electorama.msvc.survey.model.repository;

import com.electorama.msvc.survey.model.entity.SurveyCausesOfDisabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyCausesOfDisabilityRepository
    extends JpaRepository<SurveyCausesOfDisabilityEntity, Long> {}
