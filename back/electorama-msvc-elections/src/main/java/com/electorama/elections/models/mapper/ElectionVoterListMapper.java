package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.ElectionVoterListDto;
import com.electorama.elections.models.entity.ElectionVoterListEntity;
import org.springframework.beans.BeanUtils;

public class ElectionVoterListMapper
    extends BaseMapper<ElectionVoterListEntity, ElectionVoterListDto> {

  @Override
  public ElectionVoterListEntity convertToEntity(ElectionVoterListDto dto, Object... args) {
    ElectionVoterListEntity electionsEntity = new ElectionVoterListEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, electionsEntity);
    }
    return electionsEntity;
  }

  @Override
  public ElectionVoterListDto convertToDto(ElectionVoterListEntity entity, Object... args) {
    ElectionVoterListDto electionDto = new ElectionVoterListDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, electionDto);
    }
    return electionDto;
  }
}
