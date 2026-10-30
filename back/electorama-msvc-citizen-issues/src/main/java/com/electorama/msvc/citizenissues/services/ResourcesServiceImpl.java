package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionResourceEntity;
import com.electorama.msvc.citizenissues.model.repository.IssueResourcesRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourcesServiceImpl implements ResourcesService {

  @Autowired private IssueResourcesRepository issueResourcesRepository;

  @Override
  public List<IssueSolutionResourceEntity> findAll() {
    return issueResourcesRepository.findAll();
  }

  @Override
  public List<IssueSolutionResourceEntity> findByCategoryId(Long id) {
    return issueResourcesRepository.findByIssueCategoryId(id);
  }

  @Override
  public IssueSolutionResourceEntity findById(Long id) {
    return issueResourcesRepository.findById(id).orElse(null);
  }

  @Override
  public IssueSolutionResourceEntity save(IssueSolutionResourceEntity resourcesEntity) {
    return issueResourcesRepository.save(resourcesEntity);
  }
}
