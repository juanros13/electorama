package com.electorama.geography.model.repository;

import com.electorama.geography.model.entity.NeighborhoodsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeighborhoodsRepository extends JpaRepository<NeighborhoodsEntity, Long> {

  List<NeighborhoodsEntity> findByCpAsent(String postalCodeId);
}
