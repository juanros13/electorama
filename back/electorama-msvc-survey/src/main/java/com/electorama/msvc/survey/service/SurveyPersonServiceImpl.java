package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.SurveyPersonDto;
import com.electorama.msvc.survey.model.mapper.SurveyPersonMapper;
import com.electorama.msvc.survey.model.repository.SurveyPersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SurveyPersonServiceImpl implements SurveyPersonService {

  @Autowired SurveyPersonRepository surveyPersonRepository;

  private final SurveyPersonMapper surveyPersonMapper = new SurveyPersonMapper();

  @Override
  public void createSurveyPerson(SurveyPersonDto request) {
    log.info("Creating survey person: {}", request);
    surveyPersonRepository.save(surveyPersonMapper.convertToEntity(request));
  }
}
