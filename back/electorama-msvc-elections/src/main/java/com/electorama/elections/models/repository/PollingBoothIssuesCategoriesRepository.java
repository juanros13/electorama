package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.PollingBoothIssuesCategoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingBoothIssuesCategoriesRepository
    extends JpaRepository<PollingBoothIssuesCategoryEntity, Long> {

  List<PollingBoothIssuesCategoryEntity> findAll();
}
