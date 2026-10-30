package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.CandidateEntity;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CandidateRepository extends JpaRepository<CandidateEntity, Long> {

  @Query(
      value =
          "SELECT * FROM candidates c "
              + "WHERE "
              + "CONCAT(c.names, ' ', c.last_names, ' ', c.sur_names) ilike CONCAT('%',:name,'%')  ",
      nativeQuery = true)
  List<CandidateEntity> findAllByParametros(@Param("name") String name);

  List<CandidateEntity> findByElection_Id(Long idElection);

  List<CandidateEntity> findByElectionIdIn(Set<Long> electionIds);

  List<CandidateEntity> findByIdIn(Set<Long> candidateIds);
}
