package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.CatCommitteeResponseDto;
import com.electorama.msvc.followers.model.dto.ReportCommitteesGoalsDto;
import org.springframework.data.domain.Page;

public interface CommitteesService {

  Page<CatCommitteeResponseDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId);

  Page<CatCommitteeResponseDto> listAdmin(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId);

  ReportCommitteesGoalsDto getReportCommitteesGoals(String authId);

  ReportCommitteesGoalsDto getReportCommitteesGoalsByAdmin();
}
