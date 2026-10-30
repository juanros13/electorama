package com.electorama.msvc.users.model.repository;

import com.electorama.msvc.users.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

  UserEntity findByAuthId(String authId);
}
