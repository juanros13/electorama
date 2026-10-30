package com.electorama.msvc.citizenissues.model.repository;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueSolutionRepository extends JpaRepository<IssueSolutionEntity, Long> {

  @NotNull
  Page<IssueSolutionEntity> findAll(Pageable pageable);

  IssueSolutionEntity findById(long id);
}
