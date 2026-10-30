package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.*;
import com.electorama.msvc.survey.model.dto.request.RequestFolios;
import com.electorama.msvc.survey.model.dto.request.RequestSections;
import com.electorama.msvc.survey.model.rest.UserProfileResponse;
import java.util.List;
import java.util.Optional;

public interface SurveyUserService {

  SurveyUserSectionListDto createUser(UserProfileResponse user);

  List<SurveyUserDto> listUsersMunicipality(String authId);

  String assignSection(RequestSections request);

  List<String> assignFolio(RequestFolios request);

  Boolean deleteSectionNumber(Long id);

  List<SurveySectionGoalDto> listSectionMunicipality(Long municipalityId);

  Optional<SurveyUserSurveyDto> listUsersSurvey(String authId);

  Optional<SurveyAnnexReportDto> listSurveyAnnex(Long id);
}
