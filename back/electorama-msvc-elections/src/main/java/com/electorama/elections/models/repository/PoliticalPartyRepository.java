package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.PoliticalPartyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoliticalPartyRepository extends JpaRepository<PoliticalPartyEntity, Long> {

  List<PoliticalPartyEntity> findByPoliticalPartyType_Id(Long idType);

  List<PoliticalPartyEntity> findByIdIn(List<Long> idsPartyAssociations);
}
