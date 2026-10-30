package com.electorama.msvc.citizenissues.model.mapper;

import com.electorama.msvc.citizenissues.model.dto.IssueDetailDto;
import com.electorama.msvc.citizenissues.model.dto.IssueDto;
import com.electorama.msvc.citizenissues.model.entity.IssueDetailEntity;
import com.electorama.msvc.citizenissues.model.entity.IssueEntity;
import org.springframework.beans.BeanUtils;

public class IssueDetailMapper extends BaseMapper<IssueDetailEntity, IssueDetailDto> {

  private final IssueCategoryMapper issueCategoryMapper = new IssueCategoryMapper();

  private final IssueStatusMapper issueStatusMapper = new IssueStatusMapper();

  private final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();

  private final IssueSolutionMapper issueSolutionMapper = new IssueSolutionMapper();

  private final IssueSolutionResourceMapper issueSolutionResourceMapper =
      new IssueSolutionResourceMapper();

  @Override
  public IssueDetailEntity convertToEntity(IssueDetailDto dto, Object... args) {
    IssueDetailEntity entity = new IssueDetailEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      entity.setId(dto.getId());
      entity.setIssueCategory(issueCategoryMapper.convertToEntity(dto.getIssueCategory()));
      entity.setIssueStatus(issueStatusMapper.convertToEntity(dto.getIssueStatus()));
      entity.setIssueType(issueTypeMapper.convertToEntity(dto.getIssueType()));
      entity.setRegistrationDate(dto.getRegistrationDate());
      entity.setDeletedDate(dto.getDeletedDate());
      entity.setName(dto.getName());
      entity.setLastName(dto.getLastName());
      entity.setSurName(dto.getSurName());
      entity.setCurp(dto.getCurp());
      entity.setBirtDay(dto.getBirtDay());
      entity.setUrgent(dto.getUrgent());
      entity.setIssue(new IssueEntity());
      entity.setPathFile(dto.getPathFile());
      entity.setUuid(dto.getUuid());
      // entity.setIssueSolutionResourceTmp(issueSolutionResourceMapper.convertToEntity(dto.getIssueSolutionResourceTmp()));
      if (dto.getIssueSolution() != null) {
        entity.setIssueSolution(issueSolutionMapper.convertToEntity(dto.getIssueSolution()));
      }
      if (dto.getIssueSolutionResourceTmp() != null) {
        entity.setIssueSolutionResourceTmp(
            issueSolutionResourceMapper.convertToEntity(dto.getIssueSolutionResourceTmp()));
      }
      if (dto.getIssue() != null) {
        entity.getIssue().setId(dto.getIssue().getId());
        entity.getIssue().setInvoice(dto.getIssue().getInvoice());
        entity.getIssue().setRegistrationDate(dto.getIssue().getRegistrationDate());
        entity.getIssue().setDeletedDate(dto.getIssue().getDeletedDate());
        entity.getIssue().setFullName(dto.getIssue().getFullName());
        entity.getIssue().setPhone(dto.getIssue().getPhone());
        entity.getIssue().setMunicipalityId(dto.getIssue().getMunicipalityId());
        entity.getIssue().setLocalityId(dto.getIssue().getLocalityId());
        entity.getIssue().setLocalityOther(dto.getIssue().getLocalityOther());
      }
      entity.setUserCreateId(dto.getUserCreateId());
    }
    return entity;
  }

  @Override
  public IssueDetailDto convertToDto(IssueDetailEntity entity, Object... args) {
    IssueDetailDto dto = new IssueDetailDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      dto.setId(entity.getId());
      dto.setIssueCategory(issueCategoryMapper.convertToDto(entity.getIssueCategory()));
      dto.setIssueStatus(issueStatusMapper.convertToDto(entity.getIssueStatus()));
      dto.setIssueType(issueTypeMapper.convertToDto(entity.getIssueType()));
      dto.setPathFile(entity.getPathFile());
      dto.setName(entity.getName());
      dto.setLastName(entity.getLastName());
      dto.setSurName(entity.getSurName());
      dto.setCurp(entity.getCurp());
      dto.setBirtDay(entity.getBirtDay());
      dto.setUrgent(entity.getUrgent());
      if (entity.getIssueSolution() != null) {
        dto.setIssueSolution(issueSolutionMapper.convertToDto(entity.getIssueSolution()));
      }
      if (entity.getIssueSolutionResourceTmp() != null) {
        dto.setIssueSolutionResourceTmp(
            issueSolutionResourceMapper.convertToDto(entity.getIssueSolutionResourceTmp()));
      }
      dto.setIssue(
          new IssueDto(
              entity.getIssue().getId(),
              entity.getIssue().getInvoice(),
              entity.getIssue().getRegistrationDate(),
              entity.getIssue().getDeletedDate(),
              entity.getIssue().getFullName(),
              entity.getIssue().getPhone(),
              entity.getIssue().getMunicipalityId(),
              "",
              entity.getIssue().getLocalityId(),
              "",
              entity.getIssue().getLocalityOther()));
      dto.setUserCreateId(entity.getUserCreateId());
      dto.setRegistrationDate(entity.getRegistrationDate());
      dto.setDeletedDate(entity.getDeletedDate());

      return dto;
    }

    return dto;
  }
}
