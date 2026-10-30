package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.*;
import java.util.List;
import org.springframework.data.domain.Page;

public interface SurveyReportService {

  List<MunicipalityDashDto> getDashMunicipality();

  List<MunicipalitySummaryDto> getSurveyReportMunicipality();

  List<CoordinateMunicipalityDto> getCoordinateMunicipality(Long municipalityId);

  List<SurveyUserGeoDto> getGeolocationByUser(Long userId);

  List<MunicipalityGoalDto> getSurveyReportMunicipalityId(Long municipalityId);

  List<AuthIdMunicipalityDto> getSurveyReportCoordinateMunicipality(
      String autId, Integer sectionNumber);

  Page<AuthIdMunicipalityDto> getSurveyReportAdminCoordinator(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search, String autId);
}
