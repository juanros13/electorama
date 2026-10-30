package com.electorama.elections.services;

import com.electorama.elections.models.dto.SurveyResultsDto;
import java.util.List;

public interface FastSurveyService {

  List<SurveyResultsDto> getReportFastSurvey(Integer id);
}
