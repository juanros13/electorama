package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.SurveyUserSectionDto;
import java.util.List;

public interface SurveyUserSectionService {

  List<SurveyUserSectionDto> getSectionByMunicipality(String authId);
}
