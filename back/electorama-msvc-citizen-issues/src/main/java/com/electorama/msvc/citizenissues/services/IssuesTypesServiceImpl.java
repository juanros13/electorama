package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueTypeEntity;
import com.electorama.msvc.citizenissues.model.repository.IssuesTypesRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IssuesTypesServiceImpl implements IssuesTypesService {
  @Autowired private IssuesTypesRepository issuesTypesRepository;

  @Override
  public List<IssueTypeEntity> findAll() {
    return issuesTypesRepository.findAll();
  }
}
