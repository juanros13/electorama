package com.electorama.msvc.users.model.repository;

import com.electorama.msvc.users.model.entity.StructureRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureRoleRepository extends JpaRepository<StructureRoleEntity, String> {}
