package com.electorama.msvc.citizenissues.model.mapper;

import com.electorama.msvc.citizenissues.model.dto.IssueDto;
import com.electorama.msvc.citizenissues.model.entity.IssueEntity;
import org.springframework.beans.BeanUtils;

public class IssueMapper extends BaseMapper<IssueEntity, IssueDto> {

  private final IssueDetailMapper issueDetailMapper = new IssueDetailMapper();

  @Override
  public IssueEntity convertToEntity(IssueDto dto, Object... args) {
    IssueEntity entity = new IssueEntity();
    if (dto != null) {
      // BeanUtils.copyProperties(dto, entity);
      assert dto != null;
      entity.setId(dto.getId());
      entity.setInvoice(dto.getInvoice());
      entity.setPhone(dto.getPhone());
      entity.setFullName(dto.getFullName());
      entity.setRegistrationDate(dto.getRegistrationDate());
      entity.setMunicipalityId(dto.getMunicipalityId());
      entity.setLocalityId(dto.getLocalityId());
      entity.setLocalityOther(dto.getLocalityOther());
      if (dto.getIssueDetailEntities() != null) {
        entity.setIssueDetailEntities(
            issueDetailMapper.convertToEntity(dto.getIssueDetailEntities()).stream().toList());
      }
    }
    return entity;
  }

  @Override
  public IssueDto convertToDto(IssueEntity entity, Object... args) {
    IssueDto dto = new IssueDto();
    if (entity != null) BeanUtils.copyProperties(entity, dto);
    if (dto.getIssueDetailEntities() != null) {
      dto.getIssueDetailEntities().stream()
          .map(
              detailDto -> {
                if (detailDto.getIssueSolution().getId() == null) {
                  detailDto.setIssueSolution(null);
                }
                if (detailDto.getIssueSolutionResourceTmp().getId() == null) {
                  detailDto.setIssueSolutionResourceTmp(null);
                }
                return detailDto;
              });
    }

    return dto;
  }
}
