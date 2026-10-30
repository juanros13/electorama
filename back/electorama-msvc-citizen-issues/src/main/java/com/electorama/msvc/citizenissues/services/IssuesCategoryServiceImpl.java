package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueCategoryEntity;
import com.electorama.msvc.citizenissues.model.repository.IssueCategoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IssuesCategoryServiceImpl implements IssuesCategoryService {
  @Autowired private IssueCategoryRepository issueCategoryRepository;

  @Override
  public List<IssueCategoryEntity> findAll() {
    return issueCategoryRepository.findAll();
  }
}
