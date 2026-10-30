package com.electorama.msvc.documentmanagement.models.repository;

import com.electorama.msvc.documentmanagement.models.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository
    extends JpaRepository<PersonEntity, Long>, JpaSpecificationExecutor<PersonEntity> {}
