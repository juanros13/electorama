package com.electorama.msvc.followers.model.repository;

import com.electorama.msvc.followers.model.entity.CatSexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatSexRepository extends JpaRepository<CatSexEntity, Long> {}
