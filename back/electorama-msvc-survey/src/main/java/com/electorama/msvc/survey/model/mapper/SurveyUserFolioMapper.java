package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyUserFolioDto;
import com.electorama.msvc.survey.model.entity.SurveyUserFolioEntity;
import org.springframework.beans.BeanUtils;

public class SurveyUserFolioMapper extends BaseMapper<SurveyUserFolioEntity, SurveyUserFolioDto> {

  @Override
  public SurveyUserFolioEntity convertToEntity(SurveyUserFolioDto dto, Object... args) {
    SurveyUserFolioEntity entity = new SurveyUserFolioEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyUserFolioDto convertToDto(SurveyUserFolioEntity entity, Object... args) {
    SurveyUserFolioDto dto = new SurveyUserFolioDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
