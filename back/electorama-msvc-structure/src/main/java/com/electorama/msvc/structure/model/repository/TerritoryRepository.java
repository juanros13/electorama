package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Territory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerritoryRepository extends JpaRepository<Territory, Long> {}
