package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.dto.IssueSolutionDto;
import org.springframework.data.domain.Page;

public interface IssuesSolutionService {
  Page<IssueSolutionDto> findAll(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search);

  IssueSolutionDto findById(Long id);

  IssueSolutionDto save(IssueSolutionDto resourcesEntity);

  IssueSolutionDto update(IssueSolutionDto resourcesEntity);

  IssueSolutionDto delete(Long id);
}
