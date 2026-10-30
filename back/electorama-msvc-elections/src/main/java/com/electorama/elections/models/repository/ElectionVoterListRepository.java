package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.ElectionVoterListEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectionVoterListRepository extends JpaRepository<ElectionVoterListEntity, Long> {

  List<ElectionVoterListEntity> findByUserAuthId(String userAuthId);
}
