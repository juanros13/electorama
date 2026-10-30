package com.electorama.logfile.model.repository;

import com.electorama.logfile.model.entity.GoalsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalsRepository extends JpaRepository<GoalsEntity, Long> {

  List<GoalsEntity> findByOdsId(Long odsId);
}
