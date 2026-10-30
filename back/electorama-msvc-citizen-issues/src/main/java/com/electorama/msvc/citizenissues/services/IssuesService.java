package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.dto.IssueDetailDto;
import com.electorama.msvc.citizenissues.model.dto.IssueDto;
import com.electorama.msvc.citizenissues.model.dto.ReportIssueStatusDto;
import com.electorama.msvc.citizenissues.model.entity.IssueEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface IssuesService {

  Page<IssueDto> findAll(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search);

  Page<IssueDetailDto> findAllDetails(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      Integer municpalityId,
      String userCreateId,
      Integer categoryId,
      Integer statusId,
      Integer invoice,
      String isUrgent);

  Page<IssueEntity> findByCreator(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId);

  IssueDto findById(Long id);

  IssueDetailDto findDetailById(Long id);

  Page<IssueDetailDto> findDetailByUserCreateId(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId,
      Integer municipalityId,
      Integer categoryId,
      Integer statusId,
      Integer invoice,
      String isUrgent);

  IssueEntity save(IssueDto issue);

  IssueEntity deleteById(Long id);

  IssueDetailDto deleteIssueDetailById(Long id);

  IssueDetailDto hardDeleteIssueDetailById(Long id);

  IssueDetailDto updateDetail(IssueDetailDto issueDetail);

  ReportIssueStatusDto getReportIssueCategory(
      Integer municipalityId,
      String userCreateId,
      Integer categoryId,
      Integer statusId,
      String search,
      Boolean isAdmin,
      Integer invoice,
      String isUrgent);

  String uploadFile(
      MultipartFile file, Long issueDetailId, Integer issueSolutionDocumentsCategory, String uuid);
}
