package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.entity.IssueStatusEntity;
import java.util.List;

public interface IssuesStatusService {

  List<IssueStatusEntity> findAll();
}
