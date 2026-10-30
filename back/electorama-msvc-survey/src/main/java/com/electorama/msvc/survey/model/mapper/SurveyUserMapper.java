package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyUserDto;
import com.electorama.msvc.survey.model.entity.SurveyUserEntity;
import org.springframework.beans.BeanUtils;

public class SurveyUserMapper extends BaseMapper<SurveyUserEntity, SurveyUserDto> {

  private final SurveyUserSectionMapper surveyUserSectionMapper = new SurveyUserSectionMapper();

  @Override
  public SurveyUserEntity convertToEntity(SurveyUserDto dto, Object... args) {
    SurveyUserEntity entity = new SurveyUserEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      if (dto.getSurveyUserSection() != null) {
        entity.setSurveyUserSection(
            surveyUserSectionMapper.convertToEntityList(dto.getSurveyUserSection()));
      }
    }
    return entity;
  }

  @Override
  public SurveyUserDto convertToDto(SurveyUserEntity entity, Object... args) {
    SurveyUserDto dto = new SurveyUserDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);

      if (entity.getSurveyUserSection() != null) {
        dto.setSurveyUserSection(
            surveyUserSectionMapper.convertToDtoList(entity.getSurveyUserSection()));
      }
    }
    return dto;
  }
}
