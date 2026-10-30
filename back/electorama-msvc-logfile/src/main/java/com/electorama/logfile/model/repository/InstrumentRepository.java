package com.electorama.logfile.model.repository;

import com.electorama.logfile.model.entity.InstrumentsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<InstrumentsEntity, Long> {

  List<InstrumentsEntity> findByIndicatorId(Long indicatorId);
}
