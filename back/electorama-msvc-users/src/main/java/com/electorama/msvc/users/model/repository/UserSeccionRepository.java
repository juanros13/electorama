package com.electorama.msvc.users.model.repository;

import com.electorama.msvc.users.model.entity.UserSeccionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSeccionRepository extends JpaRepository<UserSeccionEntity, Long> {

  List<UserSeccionEntity> findByUserAuthId(String authId);
}
