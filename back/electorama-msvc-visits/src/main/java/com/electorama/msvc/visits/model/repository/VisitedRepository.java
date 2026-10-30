package com.electorama.msvc.visits.model.repository;

import com.electorama.msvc.visits.model.entity.VisitedEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitedRepository extends JpaRepository<VisitedEntity, Long> {

  List<VisitedEntity> findBySeccionId(Long seccionId);

  List<VisitedEntity> findByCoordinateZoneId(Long seccionId);

  Page<VisitedEntity> findAll(Pageable pageable);

  Page<VisitedEntity> findBySeccionId(Integer idSection, Pageable pageable);
}
