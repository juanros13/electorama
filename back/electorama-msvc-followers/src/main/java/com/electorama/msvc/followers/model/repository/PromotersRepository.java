package com.electorama.msvc.followers.model.repository;

import com.electorama.msvc.followers.model.entity.PromotersEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotersRepository extends JpaRepository<PromotersEntity, Long> {

  Page<PromotersEntity> findByPromoterAuthId(String authId, Pageable pageable);

  Page<PromotersEntity> findByCordinatorZoneAuthIdAndFlagAlter(
      String authId, Boolean flagAlter, Pageable pageable);

  List<PromotersEntity> findByCordinatorZoneAuthId(String authId);

  List<PromotersEntity> findByReferentAuthId(String authId);

  List<PromotersEntity> findByReferentAuthIdAndFlagAlterIsTrue(String authId);

  List<PromotersEntity> findBySeccionIdAndCordinatorZoneAuthId(Integer sectionId, String authId);

  List<PromotersEntity> findBySeccionIdAndCordinatorZoneAuthIdAndFlagAlter(
      Integer sectionId, String authId, Boolean flagAlter);

  Optional<PromotersEntity> findByIdAndUserCreateId(Long id, String authId);

  Integer countAllByUserCreateIdAndDeletedNull(String authId);

  // Integer countAllByCatCommitteeAndDeletedNull(CatCommitteeEntity catCommittee);

  Integer countAllByUserCreateId(String userId);

  Integer countAllByUserCreateIdAndSeccionIdAndFlagAlter(
      String userId, Integer seccionId, Boolean flagAlter);

  boolean existsByCurpAndFlagAlter(String curp, Boolean flagAlter);

  PromotersEntity findFirstByPromoterAuthId(String authId);

  PromotersEntity findFirstByFlagAlterIsFalseAndCurpOrIne(String curp, String ine);

  PromotersEntity findByCurpAndFlagAlter(String curp, Boolean flagAlter);

  PromotersEntity findByIneAndFlagAlter(String ine, Boolean flagAlter);
}
