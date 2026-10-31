package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.CampaignLevel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignLevelRepository extends JpaRepository<CampaignLevel, Long> {

  List<CampaignLevel> findByCampaignIdOrderByRank(Long campaignId);

  List<CampaignLevel> findAllByOrderByCampaignIdAscRankAsc();

  /**
   * Cuántos puestos (vigentes o no) tiene cada nivel de la campaña: con puestos no cambia de forma.
   */
  @Query(
      value =
          "SELECT l.id AS levelId, count(p.id) AS positions FROM campaign_level l"
              + " LEFT JOIN position p ON p.level_id = l.id"
              + " WHERE l.campaign_id = :campaignId GROUP BY l.id",
      nativeQuery = true)
  List<LevelUsage> usage(@Param("campaignId") Long campaignId);

  interface LevelUsage {
    Long getLevelId();

    Long getPositions();
  }
}
