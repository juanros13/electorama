package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.ElectionDto;
import com.electorama.elections.models.entity.ElectionEntity;
import org.springframework.beans.BeanUtils;

public class ElectionMapper extends BaseMapper<ElectionEntity, ElectionDto> {

  private final ElectionTypeMapper electionTypeMapper = new ElectionTypeMapper();
  private final ElectionPositionMapper electionPositionMapper = new ElectionPositionMapper();
  private final ElectionYearMapper electionYearMapper = new ElectionYearMapper();

  @Override
  public ElectionEntity convertToEntity(ElectionDto dto, Object... args) {
    ElectionEntity electionsEntity = new ElectionEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, electionsEntity);
      electionsEntity.setElectionPosition(
          electionPositionMapper.convertToEntity(dto.getElectionPosition()));
      electionsEntity.setElectionYear(electionYearMapper.convertToEntity(dto.getElectionYear()));
      electionsEntity.setElectionType(electionTypeMapper.convertToEntity(dto.getElectionType()));
    }
    return electionsEntity;
  }

  @Override
  public ElectionDto convertToDto(ElectionEntity entity, Object... args) {
    ElectionDto electionDto = new ElectionDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, electionDto);
      electionDto.setElectionPosition(
          electionPositionMapper.convertToDto(entity.getElectionPosition()));
      electionDto.setElectionType(electionTypeMapper.convertToDto(entity.getElectionType()));
      electionDto.setElectionYear(electionYearMapper.convertToDto(entity.getElectionYear()));
    }
    return electionDto;
  }
}
