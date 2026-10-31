package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.CampaignElection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignElectionRepository extends JpaRepository<CampaignElection, Long> {

  List<CampaignElection> findByCampaignIdOrderById(Long campaignId);

  void deleteByCampaignId(Long campaignId);
}
