package com.electorama.geography.model.repository;

import com.electorama.geography.model.entity.FederalDistrictEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FederalDistrictRepository extends JpaRepository<FederalDistrictEntity, Long> {

  List<FederalDistrictEntity> findByEntidadId(Integer id);
}
