package com.electorama.msvc.documentmanagement.models.repository;

import com.electorama.msvc.documentmanagement.models.entity.DepartamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartamentRepository
    extends JpaRepository<DepartamentEntity, Long>, JpaSpecificationExecutor<DepartamentEntity> {}
