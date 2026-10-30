package com.electorama.msvc.citizenissues.model.repository;

import com.electorama.msvc.citizenissues.model.entity.IssueCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueCategoryRepository extends JpaRepository<IssueCategoryEntity, Long> {}
