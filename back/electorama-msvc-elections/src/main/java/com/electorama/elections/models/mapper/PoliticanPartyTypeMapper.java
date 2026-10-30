package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PoliticalPartyTypeDto;
import com.electorama.elections.models.entity.PoliticalPartyTypeEntity;
import org.springframework.beans.BeanUtils;

public class PoliticanPartyTypeMapper
    extends BaseMapper<PoliticalPartyTypeEntity, PoliticalPartyTypeDto> {

  @Override
  public PoliticalPartyTypeEntity convertToEntity(PoliticalPartyTypeDto dto, Object... args) {
    PoliticalPartyTypeEntity electionsEntity = new PoliticalPartyTypeEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, electionsEntity);
    }
    return electionsEntity;
  }

  @Override
  public PoliticalPartyTypeDto convertToDto(PoliticalPartyTypeEntity entity, Object... args) {
    PoliticalPartyTypeDto calendar = new PoliticalPartyTypeDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
