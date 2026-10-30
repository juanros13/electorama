package com.electorama.msvc.citizenissues.model.repository;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionDocumentsCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueSolutionDocumentsCategoryRepository
    extends JpaRepository<IssueSolutionDocumentsCategoryEntity, Long> {}
