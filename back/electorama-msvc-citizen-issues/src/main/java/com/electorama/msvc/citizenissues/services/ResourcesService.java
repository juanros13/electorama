package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionResourceEntity;
import java.util.List;

public interface ResourcesService {

  List<IssueSolutionResourceEntity> findAll();

  List<IssueSolutionResourceEntity> findByCategoryId(Long id);

  IssueSolutionResourceEntity findById(Long id);

  IssueSolutionResourceEntity save(IssueSolutionResourceEntity resourcesEntity);
}
