package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionProviderEntity;
import com.electorama.msvc.citizenissues.model.repository.IssuesProvidersRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProvidersServiceImpl implements ProvidersService {

  @Autowired private IssuesProvidersRepository issuesProvidersRepository;

  @Override
  public List<IssueSolutionProviderEntity> findAll() {
    return issuesProvidersRepository.findAll();
  }

  @Override
  public IssueSolutionProviderEntity findById(Long id) {
    return issuesProvidersRepository.findById(id).orElse(null);
  }

  @Override
  public IssueSolutionProviderEntity save(IssueSolutionProviderEntity resourcesEntity) {
    return issuesProvidersRepository.save(resourcesEntity);
  }
}
