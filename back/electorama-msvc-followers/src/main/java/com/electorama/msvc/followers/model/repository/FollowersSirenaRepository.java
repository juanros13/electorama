package com.electorama.msvc.followers.model.repository;

import com.electorama.msvc.followers.model.entity.FollowersSirenaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowersSirenaRepository extends JpaRepository<FollowersSirenaEntity, Long> {

  // Page<FollowersSirenaEntity> findByUserCreateId(String authId, Pageable pageable);

  Optional<FollowersSirenaEntity> findByFollowerId(Long id);

  Page<FollowersSirenaEntity> findBySeccionIdIn(List<Long> catSection, Pageable pageable);

  Page<FollowersSirenaEntity> findBySeccionIdAndNamesIsNotNull(Long sectionId, Pageable pageable);

  FollowersSirenaEntity findByPromotersId(Long promoterId);

  @Query(
      value =
          "SELECT "
              + "PR.*  "
              + "FROM "
              + "followers_sirena PR "
              + "WHERE "
              + " PR.seccion_id = ?1 "
              + "AND "
              + "PR.names IS NOT NULL "
              + "AND (CONCAT(PR.names, ' ',PR.last_name,' ',PR.sur_name) ILIKE %?2% "
              + "OR PR.names ILIKE %?3% "
              + "OR PR.last_name ILIKE %?4% "
              + "OR PR.sur_name ILIKE %?5% "
              + "OR PR.curp ILIKE %?6% "
              + "OR PR.ine ILIKE %?7% "
              + ")",
      nativeQuery = true)
  Page<FollowersSirenaEntity> findByParametros(
      Long sectionId,
      String nombreCompleto,
      String nombre,
      String apelidoPaterno,
      String apellidoMaterno,
      String curp,
      String ine,
      Pageable pageable);
}
