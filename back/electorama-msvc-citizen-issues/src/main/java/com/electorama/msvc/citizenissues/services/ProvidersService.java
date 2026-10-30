package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionProviderEntity;
import java.util.List;

public interface ProvidersService {

  List<IssueSolutionProviderEntity> findAll();

  IssueSolutionProviderEntity findById(Long id);

  IssueSolutionProviderEntity save(IssueSolutionProviderEntity providersEntity);
}
