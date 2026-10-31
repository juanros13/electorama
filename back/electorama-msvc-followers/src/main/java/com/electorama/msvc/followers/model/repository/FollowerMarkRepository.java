package com.electorama.msvc.followers.model.repository;

import com.electorama.msvc.followers.model.entity.FollowerMarkEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowerMarkRepository extends JpaRepository<FollowerMarkEntity, Long> {
}
