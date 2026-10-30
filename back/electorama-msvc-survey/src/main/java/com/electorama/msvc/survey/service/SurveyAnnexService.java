package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.SurveyAnnexDto;
import com.electorama.msvc.survey.model.dto.SurveyAnnexListDto;
import com.electorama.msvc.survey.model.dto.request.RequestSurveyAnexAllFile;
import com.electorama.msvc.survey.model.dto.request.RequestSurveyAnexFile;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface SurveyAnnexService {

  SurveyAnnexListDto save(SurveyAnnexListDto surveyAnnexListDto);

  Page<SurveyAnnexDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange);

  Page<SurveyAnnexDto> listFolio(
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

  Page<SurveyAnnexDto> list(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search);

  Optional<SurveyAnnexDto> findById(Long id);

  Boolean uploadFile(RequestSurveyAnexFile requestSurveyAnexFile);

  Boolean uploadFileAll(RequestSurveyAnexAllFile requestSurveyAnexFile);

  Optional<SurveyAnnexDto> update(Long id, SurveyAnnexDto requestSurveys);

  List<SurveyAnnexDto> findBySurveyId(Long surveyId);
}
