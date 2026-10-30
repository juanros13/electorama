package com.electorama.geography.model.repository;

import com.electorama.geography.model.entity.LocalDistrictEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalDistrictRepository extends JpaRepository<LocalDistrictEntity, Long> {

  List<LocalDistrictEntity> findByEntidadIdOrderByDistritoLocalAsc(Integer id);
}
