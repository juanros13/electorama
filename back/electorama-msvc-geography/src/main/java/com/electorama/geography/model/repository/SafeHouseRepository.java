package com.electorama.geography.model.repository;

import com.electorama.geography.model.entity.SafeHouseEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafeHouseRepository extends JpaRepository<SafeHouseEntity, Long> {

  List<SafeHouseEntity> findByMunicipalityIdIn(List<Long> municipalitiesIds);
}
