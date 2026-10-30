package com.electorama.msvc.followers.model.repository;

import com.electorama.msvc.followers.model.entity.PreregisterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreRegisterRepository extends JpaRepository<PreregisterEntity, Long> {

  Page<PreregisterEntity> findAll(org.springframework.data.domain.Pageable pageable);

  Page<PreregisterEntity> findByDeletedIsNull(org.springframework.data.domain.Pageable pageable);

  Page<PreregisterEntity> findByDeletedIsNotNull(org.springframework.data.domain.Pageable pageable);
}
