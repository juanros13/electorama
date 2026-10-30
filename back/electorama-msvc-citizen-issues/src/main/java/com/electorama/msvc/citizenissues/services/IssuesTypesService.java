package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueTypeEntity;
import java.util.List;

public interface IssuesTypesService {

  List<IssueTypeEntity> findAll();
}
