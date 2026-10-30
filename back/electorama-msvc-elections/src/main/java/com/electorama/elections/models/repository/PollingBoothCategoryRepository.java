package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.PollingBoothCategoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingBoothCategoryRepository
    extends JpaRepository<PollingBoothCategoryEntity, Long> {

  List<PollingBoothCategoryEntity> findAllByOrderByIdAsc();
}
