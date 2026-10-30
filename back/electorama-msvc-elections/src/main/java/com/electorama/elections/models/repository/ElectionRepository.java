package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.ElectionEntity;
import com.electorama.elections.models.enums.ElectionStatusEnum;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectionRepository extends JpaRepository<ElectionEntity, Long> {

  List<ElectionEntity> findByElectionPosition_Id(Long electionPositionId);

  List<ElectionEntity> findByElectionType_Id(Long electionYearId);

  List<ElectionEntity> findByElectionYear_Id(Long electionYearId);

  List<ElectionEntity> findByElectionYear_IdAndElectionType_Id(
      Long electionYearId, Long electionTypeId);

  List<ElectionEntity> findByElectionPosition_IdAndElectionType_Id(
      Long electionYearId, Long electionTypeId);

  List<ElectionEntity> findByElectionYear_IdAndElectionPosition_Id(
      Long electionYearId, Long electionPositionId);

  List<ElectionEntity> findByElectionYear_IdAndElectionPosition_IdAndElectionType_Id(
      Long electionYearId, Long electionPositionId, Long electionTypeId);

  List<ElectionEntity> findByStatus(ElectionStatusEnum status);

  List<ElectionEntity> findByElectionYear_Name(String anio);

  List<ElectionEntity> findByElectionPosition_Id(Integer id);

  List<ElectionEntity> findByElectionType_Id(Integer id);

  List<ElectionEntity> findByIdIn(Set<Long> idsElections);

  @Query(
      value =
          "SELECT  "
              + " polling_booths_election_association.election_id, "
              + " COUNT(polling_booths_election_association.id) filter (where polling_booths_election_association.is_validated_sheet =6) as progress,  "
              + " COUNT(polling_booths_election_association.id) as total "
              + " FROM polling_booths_election_association "
              + " GROUP BY polling_booths_election_association.election_id",
      nativeQuery = true)
  List<Object[]> getSheetsProgress();
}
