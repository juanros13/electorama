package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyDto;
import com.electorama.msvc.survey.model.dto.SurveyHousingServicesDto;
import com.electorama.msvc.survey.model.dto.SurveyPersonDto;
import com.electorama.msvc.survey.model.entity.SurveyEntity;
import com.electorama.msvc.survey.model.entity.SurveyHousingServicesEntity;
import com.electorama.msvc.survey.model.entity.SurveyPersonEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;

public class SurveyMapper extends BaseMapper<SurveyEntity, SurveyDto> {
  private final SurveyPersonMapper surveyPersonMapper = new SurveyPersonMapper();
  private final SurveyUserFolioMapper surveyUserFolioMapper = new SurveyUserFolioMapper();
  private final SurveyHousingConditionMapper surveyHousingConditionMapper =
      new SurveyHousingConditionMapper();
  private final SurveyHousingDocumentMapper surveyHousingDocumentMapper =
      new SurveyHousingDocumentMapper();
  private final SurveyHousingMaterialRoofMapper surveyHousingMaterialRoofMapper =
      new SurveyHousingMaterialRoofMapper();
  private final SurveyHousingMaterialFloorMapper surveyHousingMaterialFloorMapper =
      new SurveyHousingMaterialFloorMapper();
  private final SurveyHousingMaterialWallMapper surveyHousingMaterialWallMapper =
      new SurveyHousingMaterialWallMapper();
  private final SurveyHousingMapper surveyHousingMapper = new SurveyHousingMapper();

  private final SurveyCulturalActivityMapper surveyCulturalActivityMapper =
      new SurveyCulturalActivityMapper();
  private final SurveyUserMapper surveyUserMapper = new SurveyUserMapper();
  private final SurveyHousingServiceMapper surveyHousingServicesMapper =
      new SurveyHousingServiceMapper();

  @Override
  public SurveyEntity convertToEntity(SurveyDto dto, Object... args) {
    SurveyEntity entity = new SurveyEntity();
    if (dto != null) {
      if (dto.getSurveyHousing() != null) {
        entity.setSurveyHousing(surveyHousingMapper.convertToEntity(dto.getSurveyHousing()));
      }
      BeanUtils.copyProperties(dto, entity);
      if (dto.getSurveyUserFolio() != null) {
        entity.setSurveyUserFolio(surveyUserFolioMapper.convertToEntity(dto.getSurveyUserFolio()));
      }
      if (dto.getSurveyHousingCondition() != null) {
        entity.setSurveyHousingCondition(
            surveyHousingConditionMapper.convertToEntity(dto.getSurveyHousingCondition()));
      }
      if (dto.getSurveyHousingDocument() != null) {
        entity.setSurveyHousingDocument(
            surveyHousingDocumentMapper.convertToEntity(dto.getSurveyHousingDocument()));
      }
      if (dto.getSurveyHousingMaterialRoof() != null) {
        entity.setSurveyHousingMaterialRoof(
            surveyHousingMaterialRoofMapper.convertToEntity(dto.getSurveyHousingMaterialRoof()));
      }
      if (dto.getSurveyHousingMaterialFloor() != null) {
        entity.setSurveyHousingMaterialFloor(
            surveyHousingMaterialFloorMapper.convertToEntity(dto.getSurveyHousingMaterialFloor()));
      }
      if (dto.getSurveyHousingMaterialWall() != null) {
        entity.setSurveyHousingMaterialWall(
            surveyHousingMaterialWallMapper.convertToEntity(dto.getSurveyHousingMaterialWall()));
      }
      if (dto.getSurveyUser() != null) {
        entity.setSurveyUser(surveyUserMapper.convertToEntity(dto.getSurveyUser()));
      }
      if (dto.getSurveyHousingServicesEntities() != null) {
        List<SurveyHousingServicesEntity> surveyHousingServicesEntities = new ArrayList<>();
        for (SurveyHousingServicesDto surveyHousingServicesDto :
            dto.getSurveyHousingServicesEntities()) {
          surveyHousingServicesEntities.add(
              surveyHousingServicesMapper.convertToEntity(surveyHousingServicesDto));
        }
        entity.setSurveyHousingServicesEntities(surveyHousingServicesEntities);
      }
      if (dto.getSurveyPersonEntityList() != null) {
        for (SurveyPersonDto surveyPersonDto : dto.getSurveyPersonEntityList()) {
          SurveyPersonEntity surveyPersonEntity =
              surveyPersonMapper.convertToEntity(surveyPersonDto);
          surveyPersonEntity.setSurvey(entity); // Establecer la referencia bidireccional
          entity.addSurveyPerson(surveyPersonEntity);
        }
      }
      if (dto.getSurveyCulturalActivity() != null) {
        entity.setSurveyCulturalActivity(
            surveyCulturalActivityMapper.convertToEntity(dto.getSurveyCulturalActivity()));
      }
    }
    return entity;
  }

  @Override
  public SurveyDto convertToDto(SurveyEntity entity, Object... args) {
    SurveyDto dto = new SurveyDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      if (entity.getSurveyUserFolio() != null) {
        dto.setSurveyUserFolio(surveyUserFolioMapper.convertToDto(entity.getSurveyUserFolio()));
      }
      if (entity.getSurveyHousing() != null) {
        dto.setSurveyHousing(surveyHousingMapper.convertToDto(entity.getSurveyHousing()));
      }
      if (entity.getSurveyHousingCondition() != null) {
        dto.setSurveyHousingCondition(
            surveyHousingConditionMapper.convertToDto(entity.getSurveyHousingCondition()));
      }
      if (entity.getSurveyHousingDocument() != null) {
        dto.setSurveyHousingDocument(
            surveyHousingDocumentMapper.convertToDto(entity.getSurveyHousingDocument()));
      }
      if (entity.getSurveyHousingMaterialRoof() != null) {
        dto.setSurveyHousingMaterialRoof(
            surveyHousingMaterialRoofMapper.convertToDto(entity.getSurveyHousingMaterialRoof()));
      }
      if (entity.getSurveyHousingMaterialFloor() != null) {
        dto.setSurveyHousingMaterialFloor(
            surveyHousingMaterialFloorMapper.convertToDto(entity.getSurveyHousingMaterialFloor()));
      }
      if (entity.getSurveyHousingMaterialWall() != null) {
        dto.setSurveyHousingMaterialWall(
            surveyHousingMaterialWallMapper.convertToDto(entity.getSurveyHousingMaterialWall()));
      }
      if (entity.getSurveyUser() != null) {
        dto.setSurveyUser(surveyUserMapper.convertToDto(entity.getSurveyUser()));
      }
      if (entity.getSurveyHousingServicesEntities() != null) {
        List<SurveyHousingServicesDto> surveyHousingServicesDtos = new ArrayList<>();
        for (SurveyHousingServicesEntity surveyHousingServicesEntity :
            entity.getSurveyHousingServicesEntities()) {
          surveyHousingServicesDtos.add(
              surveyHousingServicesMapper.convertToDto(surveyHousingServicesEntity));
        }
        dto.setSurveyHousingServicesEntities(surveyHousingServicesDtos);
      }
      if (entity.getSurveyPersonEntityList() != null) {
        List<SurveyPersonDto> surveyPersonDtos = new ArrayList<>();
        for (SurveyPersonEntity surveyPersonEntity : entity.getSurveyPersonEntityList()) {
          surveyPersonDtos.add(surveyPersonMapper.convertToDto(surveyPersonEntity));
        }
        dto.setSurveyPersonEntityList(surveyPersonDtos);
      }
      if (entity.getSurveyCulturalActivity() != null) {
        dto.setSurveyCulturalActivity(
            surveyCulturalActivityMapper.convertToDto(entity.getSurveyCulturalActivity()));
      }
    }
    return dto;
  }

  public List<SurveyPersonEntity> convertToEntityList(List<SurveyPersonDto> surveyPersonDtoList) {
    return surveyPersonDtoList.stream().map(this::convertToEntity).collect(Collectors.toList());
  }

  public SurveyPersonEntity convertToEntity(SurveyPersonDto surveyPersonDto) {
    SurveyPersonEntity entity = new SurveyPersonEntity();
    entity.setId(surveyPersonDto.getId());
    entity.setAfroMexicanGroup(surveyPersonDto.getAfroMexicanGroup());
    entity.setCurp(surveyPersonDto.getCurp());
    entity.setDateOfBirth(surveyPersonDto.getDateOfBirth());
    entity.setEthnicGroup(surveyPersonDto.getEthnicGroup());
    entity.setGender(surveyPersonDto.getGender());
    entity.setIdentificationDocument(surveyPersonDto.getIdentificationDocument());
    entity.setIncome(surveyPersonDto.getIncome());
    entity.setIsDisabled(surveyPersonDto.getIsDisabled());
    entity.setIsHeadOfHousehold(surveyPersonDto.getIsHeadOfHousehold());
    entity.setLandlinePhone(surveyPersonDto.getLandlinePhone());
    entity.setLastName(surveyPersonDto.getLastName());
    entity.setMobilePhone(surveyPersonDto.getMobilePhone());
    entity.setNames(surveyPersonDto.getNames());
    entity.setNoIdentificationDocument(surveyPersonDto.getNoIdentificationDocument());
    entity.setOwnBusiness(surveyPersonDto.getOwnBusiness());
    entity.setSurName(surveyPersonDto.getSurName());
    entity.setTimeActivity(surveyPersonDto.getTimeActivity());
    SurveyEntity survey = new SurveyEntity();
    survey.setId(surveyPersonDto.getSurveyId());
    entity.setSurvey(survey);
    return entity;
  }
}
