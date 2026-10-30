package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueStatusEntity;
import com.electorama.msvc.citizenissues.model.repository.IssuesStatusRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IssuesStatusServiceImpl implements IssuesStatusService {
  @Autowired private IssuesStatusRepository issuesStatusRepository;

  @Override
  public List<IssueStatusEntity> findAll() {
    return issuesStatusRepository.findAll();
  }
}
