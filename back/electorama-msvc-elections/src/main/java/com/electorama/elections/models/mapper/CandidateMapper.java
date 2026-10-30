package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.CandidateDto;
import com.electorama.elections.models.entity.CandidateEntity;
import org.springframework.beans.BeanUtils;

public class CandidateMapper extends BaseMapper<CandidateEntity, CandidateDto> {

  private final ElectionMapper electionMapper = new ElectionMapper();

  @Override
  public CandidateEntity convertToEntity(CandidateDto dto, Object... args) {
    CandidateEntity entity = new CandidateEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      if (dto.getElection() != null) {
        entity.setElection(electionMapper.convertToEntity(dto.getElection()));
      }
    }
    return entity;
  }

  @Override
  public CandidateDto convertToDto(CandidateEntity entity, Object... args) {
    CandidateDto dto = new CandidateDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      if (entity.getElection() != null) {
        dto.setElection(electionMapper.convertToDto(entity.getElection()));
      }
    }
    return dto;
  }
}
