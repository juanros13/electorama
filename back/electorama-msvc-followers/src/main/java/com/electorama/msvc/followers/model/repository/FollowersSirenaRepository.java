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
}
