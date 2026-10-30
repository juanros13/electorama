package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueCategoryEntity;
import java.util.List;

public interface IssuesCategoryService {

  List<IssueCategoryEntity> findAll();
}
