package com.electorama.geography.model.repository;

import com.electorama.geography.model.entity.SectionLocalitiesEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionLocalitiesRepository extends JpaRepository<SectionLocalitiesEntity, Long> {

  List<SectionLocalitiesEntity> findByIdSeccion(Long id);
}
