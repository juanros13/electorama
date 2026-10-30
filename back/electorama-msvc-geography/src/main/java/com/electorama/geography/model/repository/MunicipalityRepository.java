package com.electorama.geography.model.repository;

import com.electorama.geography.model.entity.MunicipalityEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MunicipalityRepository extends JpaRepository<MunicipalityEntity, Long> {

  Optional<MunicipalityEntity> findById(Long id);

  List<MunicipalityEntity> findByEntidadIdOrderByNomMunAsc(Integer id);
}
