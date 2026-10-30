package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.PollingBoothIssuesSubCategoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingBoothIssuesSubCategoryRepository
    extends JpaRepository<PollingBoothIssuesSubCategoryEntity, Long> {

  List<PollingBoothIssuesSubCategoryEntity> findAll();
}
