package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionDocumentsCategoryEntity;
import com.electorama.msvc.citizenissues.model.repository.IssueSolutionDocumentsCategoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IssuesSolutionDocumentsCategoryServiceImpl
    implements IssuesSolutionDocumentsCategoryService {
  @Autowired
  private IssueSolutionDocumentsCategoryRepository issueSolutionDocumentsCategoryRepository;

  @Override
  public List<IssueSolutionDocumentsCategoryEntity> findAll() {
    return issueSolutionDocumentsCategoryRepository.findAll();
  }
}
