package com.electorama.msvc.elections.structure.model.repository;

import com.electorama.msvc.elections.structure.model.entity.PollingRepresentativeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingRepresentativeRepository
    extends JpaRepository<PollingRepresentativeEntity, Long> {

  Optional<PollingRepresentativeEntity> findByAuthId(String id);

  List<PollingRepresentativeEntity> findByPollingBoothId(Long id);
}
