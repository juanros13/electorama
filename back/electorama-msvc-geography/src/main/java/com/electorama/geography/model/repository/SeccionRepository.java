package com.electorama.geography.model.repository;

import com.electorama.geography.model.entity.SeccionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeccionRepository extends JpaRepository<SeccionEntity, Long> {

  List<SeccionEntity> findByEntidadId(Integer id);

  List<SeccionEntity> findByDistritoFederalId(Integer id);

  List<SeccionEntity> findByEntidadIdAndDistritoFedOrderBySeccionAsc(Integer entidadId, Integer id);

  List<SeccionEntity> findByDistritoLocalId(Integer id);

  List<SeccionEntity> findByEntidadIdAndDistritoLocOrderBySeccionAsc(Integer entidadId, Integer id);

  List<SeccionEntity> findByMunicipioIdOrderBySeccionAsc(Integer id);

  List<SeccionEntity> findByMunicipioIdIn(List<Long> municipalitiesIds);
}
