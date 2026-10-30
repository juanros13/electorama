package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.PollingBoothTypeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingBoothTypeRepository extends JpaRepository<PollingBoothTypeEntity, Long> {

  List<PollingBoothTypeEntity> findAllByOrderByIdAsc();
}
