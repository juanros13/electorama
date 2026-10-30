package com.electorama.msvc.electionsresults.models.repository;

import com.electorama.msvc.electionsresults.models.entity.ElectionResultEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectionResultRepository extends JpaRepository<ElectionResultEntity, Long> {

  List<ElectionResultEntity> findByElectionAssociationIdIn(List<Long> electionAssociationIds);
}
