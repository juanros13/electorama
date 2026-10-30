package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.DateDto;
import com.electorama.msvc.survey.model.dto.SurveyDto;
import com.electorama.msvc.survey.model.dto.SurveyUserFolioDto;
import com.electorama.msvc.survey.model.dto.request.RequestSurveyFolio;
import com.electorama.msvc.survey.model.dto.request.RequestSurveys;
import com.electorama.msvc.survey.model.dto.request.RequestSurveysRangeFolios;
import com.electorama.msvc.survey.model.dto.response.ResponseSurveyRangeFolios;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface SurveyService {

  RequestSurveys save(RequestSurveys requestSurveys);

  Page<SurveyDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange);

  Page<SurveyDto> listFolio(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange,
      String authIdHeader);

  Page<SurveyDto> list(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search);

  Optional<SurveyDto> findById(Long id);

  Optional<SurveyDto> update(Long id, SurveyDto requestSurveys);

  boolean changeStatus(Long id);

  List<Object[]> getTotalSurveysPerWeek(DateDto dateDto);

  List<Object[]> getTotalSurveysPerMunicipality();

  List<Object[]> getTotalSurveysPerMunicipalityAndUser();

  DateDto calculateWeek(Integer numWeek);

  Boolean createFolio(RequestSurveyFolio requestSurveyFolio);

  List<ResponseSurveyRangeFolios> getRangeFolios(
      RequestSurveysRangeFolios requestSurveysRangeFolios);

  List<ResponseSurveyRangeFolios> getRangeFoliosAvailables(
      RequestSurveysRangeFolios requestSurveysRangeFolios);

  List<SurveyUserFolioDto> getFolios(RequestSurveysRangeFolios requestSurveysRangeFolios);

  List<SurveyUserFolioDto> getFoliosAvailables(RequestSurveysRangeFolios requestSurveysRangeFolios);

  Page<SurveyDto> listFolioComplete(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange,
      String authIdHeader);

  Page<SurveyDto> listFolioCompleteMunicipality(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange,
      String authIdHeader);
}
