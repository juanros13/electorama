package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PollingBoothDto;
import com.electorama.elections.models.entity.PollingBoothEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

public class PollingBoothMapper extends BaseMapper<PollingBoothEntity, PollingBoothDto> {

  private final PollingBoothAddressTypeMapper pollingBoothAddressTypeMapper =
      new PollingBoothAddressTypeMapper();
  private final PollingBoothCategoryMapper pollingBoothCategoryMapper =
      new PollingBoothCategoryMapper();
  private final PollingBoothStatusMapper pollingBoothStatusMapper = new PollingBoothStatusMapper();
  private final PollingBoothTypeMapper pollingBoothTypeMapper = new PollingBoothTypeMapper();

  private final PollingBoothIssuesMapper pollingBoothIssuesMapper = new PollingBoothIssuesMapper();

  @Override
  public PollingBoothEntity convertToEntity(PollingBoothDto dto, Object... args) {
    PollingBoothEntity pollingBoothEntity = new PollingBoothEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, pollingBoothEntity);

      pollingBoothEntity.setPollingBoothAddressType(
          pollingBoothAddressTypeMapper.convertToEntity(dto.getPollingBoothAddressType()));
      pollingBoothEntity.setPollingBoothCategory(
          pollingBoothCategoryMapper.convertToEntity(dto.getPollingBoothCategory()));
      pollingBoothEntity.setPollingBoothStatus(
          pollingBoothStatusMapper.convertToEntity(dto.getPollingBoothStatus()));
      pollingBoothEntity.setPollingBoothType(
          pollingBoothTypeMapper.convertToEntity(dto.getPollingBoothType()));
      pollingBoothEntity.setPollingBoothIssuesList(
          pollingBoothIssuesMapper.convertToEntityList(dto.getPollingBoothIssuesList()));
    }
    return pollingBoothEntity;
  }

  @Override
  public PollingBoothDto convertToDto(PollingBoothEntity entity, Object... args) {
    PollingBoothDto PollingBoothDto = new PollingBoothDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, PollingBoothDto);
      PollingBoothDto.setPollingBoothAddressType(
          pollingBoothAddressTypeMapper.convertToDto(entity.getPollingBoothAddressType()));
      PollingBoothDto.setPollingBoothCategory(
          pollingBoothCategoryMapper.convertToDto(entity.getPollingBoothCategory()));
      PollingBoothDto.setPollingBoothStatus(
          pollingBoothStatusMapper.convertToDto(entity.getPollingBoothStatus()));
      PollingBoothDto.setPollingBoothType(
          pollingBoothTypeMapper.convertToDto(entity.getPollingBoothType()));
      PollingBoothDto.setPollingBoothIssuesList(
          pollingBoothIssuesMapper.convertToDtoList(entity.getPollingBoothIssuesList()));
    }
    return PollingBoothDto;
  }

  public Optional<PollingBoothDto> convertToDtoOptional(
      Optional<PollingBoothEntity> entity, Object... args) {
    Optional<PollingBoothDto> calendar = Optional.of(new PollingBoothDto());

    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
      calendar.get().setId(entity.get().getId());
      calendar.get().setVotersList(entity.get().getVotersList());
      calendar.get().setVotersRegistry(entity.get().getVotersRegistry());
      calendar.get().setPollingBoothKey(entity.get().getPollingBoothKey());
    }
    return calendar;
  }
}
