package com.electorama.msvc.citizenissues.model.repository;

import com.electorama.msvc.citizenissues.model.entity.IssueTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssuesTypesRepository extends JpaRepository<IssueTypeEntity, Long> {}
