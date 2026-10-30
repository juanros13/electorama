package com.electorama.msvc.documentmanagement.models.repository;

import com.electorama.msvc.documentmanagement.models.entity.PositionPersonStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionPersonStatusRepository
    extends JpaRepository<PositionPersonStatusEntity, Long>,
        JpaSpecificationExecutor<PositionPersonStatusEntity> {}
