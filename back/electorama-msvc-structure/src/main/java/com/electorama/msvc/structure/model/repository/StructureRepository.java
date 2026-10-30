package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Structure;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureRepository extends JpaRepository<Structure, Long> {

  List<Structure> findByCampaignIdOrderById(Long campaignId);
}
