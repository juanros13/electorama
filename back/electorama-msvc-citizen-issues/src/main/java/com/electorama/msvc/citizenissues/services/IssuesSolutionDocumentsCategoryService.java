package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionDocumentsCategoryEntity;
import java.util.List;

public interface IssuesSolutionDocumentsCategoryService {

  List<IssueSolutionDocumentsCategoryEntity> findAll();
}
