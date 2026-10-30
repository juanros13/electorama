package com.electorama.geography.model.repository;

import com.electorama.geography.model.entity.StateEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateRepository extends JpaRepository<StateEntity, Long> {

  Optional<StateEntity> findById(Long id);
}
