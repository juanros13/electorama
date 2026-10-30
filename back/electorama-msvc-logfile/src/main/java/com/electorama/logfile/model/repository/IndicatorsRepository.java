package com.electorama.logfile.model.repository;

import com.electorama.logfile.model.entity.IndicatorsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicatorsRepository extends JpaRepository<IndicatorsEntity, Long> {

  List<IndicatorsEntity> findByGoalId(Long goalId);
}
