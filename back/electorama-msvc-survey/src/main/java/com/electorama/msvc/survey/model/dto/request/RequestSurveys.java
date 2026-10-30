package com.electorama.msvc.survey.model.dto.request;

import com.electorama.msvc.survey.model.dto.SurveyDto;
import java.util.List;
import lombok.Data;

@Data
public class RequestSurveys {

  List<SurveyDto> surveyDtos;

  public List<SurveyDto> getSurveyDtos() {
    return surveyDtos;
  }

  public void setSurveyDtos(List<SurveyDto> surveyDtos) {
    this.surveyDtos = surveyDtos;
  }
}
