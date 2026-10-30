package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PoliticalPartyDto;
import com.electorama.elections.models.entity.PoliticalPartyEntity;
import org.springframework.beans.BeanUtils;

public class PoliticanPartyMapper extends BaseMapper<PoliticalPartyEntity, PoliticalPartyDto> {

  private final PoliticanPartyTypeMapper politicanPartyTypeMapper = new PoliticanPartyTypeMapper();

  private final CandidateMapper candidateMapper = new CandidateMapper();

  @Override
  public PoliticalPartyEntity convertToEntity(PoliticalPartyDto dto, Object... args) {
    PoliticalPartyEntity electionsEntity = new PoliticalPartyEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, electionsEntity);
      if (dto.getPoliticalPartyType() != null) {
        electionsEntity.setPoliticalPartyType(
            politicanPartyTypeMapper.convertToEntity(dto.getPoliticalPartyType()));
      }
    }
    return electionsEntity;
  }

  @Override
  public PoliticalPartyDto convertToDto(PoliticalPartyEntity entity, Object... args) {
    PoliticalPartyDto dto = new PoliticalPartyDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      if (entity.getPoliticalPartyType() != null) {
        dto.setPoliticalPartyType(
            politicanPartyTypeMapper.convertToDto(entity.getPoliticalPartyType()));
      }
    }
    return dto;
  }
}
