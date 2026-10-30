package com.electorama.msvc.visits.model.repository;

import com.electorama.msvc.visits.model.entity.VisitedNumberEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitedNumberRepository extends JpaRepository<VisitedNumberEntity, Long> {

  Optional<VisitedNumberEntity> findFirstByVisitedPeopleIdOrderByVisitedNumberDesc(
      Long visitedPeopleId);

  List<VisitedNumberEntity> findByVisitedPeopleId(Long visitedPeopleId);
}
