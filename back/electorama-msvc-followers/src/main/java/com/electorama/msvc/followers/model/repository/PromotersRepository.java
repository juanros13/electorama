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

  Page<PromotersEntity> findByCordinatorZoneAuthIdAndFlagAlter(
      String authId, Boolean flagAlter, Pageable pageable);

  // Integer countAllByCatCommitteeAndDeletedNull(CatCommitteeEntity catCommittee);
}
