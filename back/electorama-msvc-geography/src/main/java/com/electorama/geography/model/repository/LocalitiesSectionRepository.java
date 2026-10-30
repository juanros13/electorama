package com.electorama.geography.model.repository;

import com.electorama.geography.model.entity.LocalitiesSectionEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalitiesSectionRepository extends JpaRepository<LocalitiesSectionEntity, Long> {

  List<LocalitiesSectionEntity> findBySeccionId(Long id);

  Page<LocalitiesSectionEntity> findAll(Pageable pageable);

  Page<LocalitiesSectionEntity>
      findByLocalidadIsContainingIgnoreCaseOrSeccionOrMunicipioContainingIgnoreCase(
          String search, String searchDos, String searchTres, Pageable pageable);

  List<LocalitiesSectionEntity> findByMunicipioIgnoreCase(String municipio);
}
