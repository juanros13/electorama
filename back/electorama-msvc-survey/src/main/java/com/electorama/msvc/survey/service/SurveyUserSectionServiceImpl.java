package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.SurveyUserDto;
import com.electorama.msvc.survey.model.dto.SurveyUserSectionDto;
import com.electorama.msvc.survey.model.mapper.SurveyUserMapper;
import com.electorama.msvc.survey.model.mapper.SurveyUserSectionMapper;
import com.electorama.msvc.survey.model.repository.SurveyUserRepository;
import com.electorama.msvc.survey.model.repository.SurveyUserSectionRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SurveyUserSectionServiceImpl implements SurveyUserSectionService {

  @Autowired private SurveyUserSectionRepository surveyUserSectionRepository;

  @Autowired private SurveyUserRepository surveyUserRepository;
  private final SurveyUserSectionMapper surveyUserSectionMapper = new SurveyUserSectionMapper();

  private final SurveyUserMapper surveyUserMapper = new SurveyUserMapper();

  @Override
  public List<SurveyUserSectionDto> getSectionByMunicipality(String authId) {

    List<SurveyUserDto> surveyUserDtos =
        surveyUserMapper.convertToDtoList(
            surveyUserRepository.findBySurveyMunicipalityAuthId(authId));

    // Crear una lista para SurveyUserSectionDto
    List<SurveyUserSectionDto> surveyUserSectionDtos = new ArrayList<>();

    // Iterar sobre la lista de SurveyUserDto y crear SurveyUserSectionDto
    for (SurveyUserDto surveyUserDto : surveyUserDtos) {
      SurveyUserSectionDto sectionDto = new SurveyUserSectionDto();

      sectionDto.setSurveyUserId(surveyUserDto.getId());
      // sectionDto.setSectionId(surveyUserDto.getSectionId());
      // sectionDto.setSectionNumber(surveyUserDto.getSectionNumber());

      // Agregar a la lista de SurveyUserSectionDto
      surveyUserSectionDtos.add(sectionDto);
    }

    // Retornar la lista de SurveyUserSectionDto
    return surveyUserSectionDtos;
  }
}
