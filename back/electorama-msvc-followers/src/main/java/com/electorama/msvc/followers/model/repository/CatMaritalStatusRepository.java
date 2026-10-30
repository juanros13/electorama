package com.electorama.msvc.followers.model.repository;

import com.electorama.msvc.followers.model.entity.CatMaritalStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatMaritalStatusRepository extends JpaRepository<CatMaritalStatusEntity, Long> {}
