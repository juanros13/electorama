package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.CandidatePartyAssociationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatePartyAssociationRepository
    extends JpaRepository<CandidatePartyAssociationEntity, Long> {

  List<CandidatePartyAssociationEntity> findByCandidate_IdIn(List<Long> idsCandidates);
}
