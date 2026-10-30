package com.electorama.msvc.citizenissues.model.repository;

import com.electorama.msvc.citizenissues.model.entity.IssueDetailEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueDetailRepository
    extends JpaRepository<IssueDetailEntity, Long>, JpaSpecificationExecutor<IssueDetailEntity> {

  List<IssueDetailEntity> findByIssueId(Long issueId);

  Page<IssueDetailEntity>
      findByUserCreateIdIsNullAndUserCreateIdContainingIgnoreCaseAndDeletedDateIsNull(
          String userCreateId, Pageable pageable);
}
