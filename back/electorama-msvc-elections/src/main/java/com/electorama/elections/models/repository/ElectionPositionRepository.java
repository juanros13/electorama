package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.ElectionPositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectionPositionRepository extends JpaRepository<ElectionPositionEntity, Long> {}
