package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.ElectionYearEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectionYearRepository extends JpaRepository<ElectionYearEntity, Long> {}
