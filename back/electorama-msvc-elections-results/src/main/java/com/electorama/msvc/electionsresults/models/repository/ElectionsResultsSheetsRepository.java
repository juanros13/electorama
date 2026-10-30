package com.electorama.msvc.electionsresults.models.repository;

import com.electorama.msvc.electionsresults.models.entity.ElectionResultsSheetsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectionsResultsSheetsRepository
    extends JpaRepository<ElectionResultsSheetsEntity, Long> {

  List<ElectionResultsSheetsEntity> findByElectionAssociationIdIn(
      List<Long> electionAssociationIds);
}
