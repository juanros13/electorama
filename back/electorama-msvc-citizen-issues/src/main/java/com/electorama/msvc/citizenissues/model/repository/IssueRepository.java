package com.electorama.msvc.citizenissues.model.repository;

import com.electorama.msvc.citizenissues.model.entity.IssueEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<IssueEntity, Long> {

  @Query(
      value =
          "SELECT MAX(CAST(SUBSTRING(i.invoice FROM '[0-9]+') AS integer))  FROM issues i WHERE i.municipality_id = :municipalityId",
      nativeQuery = true)
  Integer countByMunicipalityId(Integer municipalityId);

  Page<IssueEntity> findAllByDeletedDateIsNull(Pageable pageable);

  Page<IssueEntity> findAllByDeletedDateIsNullAndInvoiceContainingIgnoreCase(
      Pageable pageable, String invoice);
}
