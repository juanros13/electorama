package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.PollingBoothStatusEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingBoothStatusRepository
    extends JpaRepository<PollingBoothStatusEntity, Long> {

  List<PollingBoothStatusEntity> findAllByOrderByIdAsc();
}
