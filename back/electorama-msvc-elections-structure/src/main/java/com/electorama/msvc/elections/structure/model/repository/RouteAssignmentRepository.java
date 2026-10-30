package com.electorama.msvc.elections.structure.model.repository;

import com.electorama.msvc.elections.structure.model.entity.RouteAssignmentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteAssignmentRepository extends JpaRepository<RouteAssignmentEntity, Long> {

  List<RouteAssignmentEntity> findByRoute_Id(Long routeId);
}
