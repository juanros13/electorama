package com.electorama.elections.models.repository;

import com.electorama.elections.models.entity.PollingBoothElectionAssociationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollingBoothElectionAssociationRepository
    extends JpaRepository<PollingBoothElectionAssociationEntity, Long> {

  List<PollingBoothElectionAssociationEntity> findByPollingBoothIdIn(List<Long> pollingBooths);

  /*********
   * @Query(
   * value =
   * "select " +
   * "     v1_0. * " +
   * "     from " +
   * "         polling_booths_election_association v1_0  " +
   * " LEFT JOIN elections V2_0 ON V2_0.id = v1_0.election_id " +
   * "     where V1_0.polling_booth_id in :pollingBooths " +
   * " order by polling_booth_id asc",
   * nativeQuery = true)
   * List<PollingBoothElectionAssociationEntity> findByPollingBoothIdIn(
   * @Param("pollingBooths") List<Long> pollingBooths);
   ********/
}
