package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.CandidatePartyAssociationDto;
import com.electorama.elections.models.entity.CandidatePartyAssociationEntity;
import org.springframework.beans.BeanUtils;

public class CandidatePartyAssociationMapper
    extends BaseMapper<CandidatePartyAssociationEntity, CandidatePartyAssociationDto> {

  private final PoliticanPartyMapper politicanPartyMapper = new PoliticanPartyMapper();

  private final CandidateMapper candidateMapper = new CandidateMapper();

  @Override
  public CandidatePartyAssociationEntity convertToEntity(
      CandidatePartyAssociationDto dto, Object... args) {
    CandidatePartyAssociationEntity entity = new CandidatePartyAssociationEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      entity.setCandidate(candidateMapper.convertToEntity(dto.getCandidate()));
      entity.setPoliticalParty(politicanPartyMapper.convertToEntity(dto.getPoliticalParty()));
    }
    return entity;
  }

  @Override
  public CandidatePartyAssociationDto convertToDto(
      CandidatePartyAssociationEntity entity, Object... args) {
    CandidatePartyAssociationDto dto = new CandidatePartyAssociationDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      dto.setCandidate(candidateMapper.convertToDto(entity.getCandidate()));
      dto.setPoliticalParty(politicanPartyMapper.convertToDto(entity.getPoliticalParty()));
    }
    return dto;
  }
}
