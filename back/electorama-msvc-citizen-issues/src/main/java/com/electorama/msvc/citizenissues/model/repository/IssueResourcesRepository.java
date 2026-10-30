package com.electorama.msvc.citizenissues.model.repository;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionResourceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueResourcesRepository extends JpaRepository<IssueSolutionResourceEntity, Long> {

  List<IssueSolutionResourceEntity> findByIssueCategoryId(Long id);
}
