package com.electorama.msvc.followers.model.repository;

import com.electorama.msvc.followers.model.entity.CatCommitteeEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatCommitteeRepository extends JpaRepository<CatCommitteeEntity, Long> {

  List<CatCommitteeEntity> findByCatSection(Long sectionId);

  Page<CatCommitteeEntity> findByCatSectionIn(List<Long> listSection, Pageable pageable);
}
