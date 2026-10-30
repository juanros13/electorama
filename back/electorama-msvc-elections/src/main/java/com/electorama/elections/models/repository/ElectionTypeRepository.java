package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.ElectionTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectionTypeRepository extends JpaRepository<ElectionTypeEntity, Long> {}
