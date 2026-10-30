package com.electorama.msvc.documentmanagement.models.repository;

import com.electorama.msvc.documentmanagement.models.entity.PositionPersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionPersonRepository
    extends JpaRepository<PositionPersonEntity, Long>,
        JpaSpecificationExecutor<PositionPersonEntity> {}
