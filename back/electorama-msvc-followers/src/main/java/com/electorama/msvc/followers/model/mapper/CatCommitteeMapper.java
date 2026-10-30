package com.electorama.msvc.followers.model.mapper;

import com.electorama.msvc.followers.model.dto.CatCommitteeDto;
import com.electorama.msvc.followers.model.entity.CatCommitteeEntity;

// create class CatCommitteeMaper here
public class CatCommitteeMapper extends BaseMapper<CatCommitteeEntity, CatCommitteeDto> {
  @Override
  public CatCommitteeEntity convertToEntity(CatCommitteeDto dto, Object... args) {
    CatCommitteeEntity catCommitteeEntity = new CatCommitteeEntity();
    if (dto != null) {
      catCommitteeEntity.setId(dto.getId());
      catCommitteeEntity.setName(dto.getName());
      catCommitteeEntity.setReferenceAuthId(dto.getReferenceAuthId());
      catCommitteeEntity.setElectoralBox(dto.getElectoralBox());
      catCommitteeEntity.setPromotersGoal(dto.getPromotersGoal());
      catCommitteeEntity.setCatSection(dto.getCatSection());
    }
    return catCommitteeEntity;
  }

  @Override
  public CatCommitteeDto convertToDto(CatCommitteeEntity entity, Object... args) {
    CatCommitteeDto catCommitteeDto = new CatCommitteeDto();
    if (entity != null) {
      catCommitteeDto.setId(entity.getId());
      catCommitteeDto.setName(entity.getName());
      catCommitteeDto.setReferenceAuthId(entity.getReferenceAuthId());
      catCommitteeDto.setElectoralBox(entity.getElectoralBox());
      catCommitteeDto.setPromotersGoal(entity.getPromotersGoal());
      catCommitteeDto.setCatSection(entity.getCatSection());
    }
    return catCommitteeDto;
  }
}
