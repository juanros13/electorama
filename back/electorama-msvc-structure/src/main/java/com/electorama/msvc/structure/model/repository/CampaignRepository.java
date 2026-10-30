package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Campaign;
import com.electorama.msvc.structure.model.enums.CampaignStatus;
import com.electorama.msvc.structure.model.projection.CampaignElectionRow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

  /** La campaña en curso. V6 garantiza que hay a lo más una por territorio. */
  Optional<Campaign> findFirstByStatusOrderByIdDesc(CampaignStatus status);

  List<Campaign> findAllByOrderByYearDesc();

  /** Qué se elige en cada campaña (V6, {@code campaign_election}). */
  @Query(
      value =
          "SELECT e.campaign_id AS campaignId, e.office AS office, e.scope AS scope,"
              + "       e.seats AS seats, e.election_day AS electionDay"
              + "  FROM campaign_election e ORDER BY e.campaign_id, e.seats",
      nativeQuery = true)
  List<CampaignElectionRow> elections();
}
