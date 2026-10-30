package com.electorama.msvc.survey.controller;

import com.electorama.msvc.survey.model.dto.SurveyUserSectionDto;
import com.electorama.msvc.survey.service.CsvService;
import com.electorama.msvc.survey.service.HttpService;
import com.electorama.msvc.survey.service.SurveyUserSectionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/surveyUserSection")
public class SurveyUserSectionController {
  @Autowired private SurveyUserSectionService surveyUserSectionService;

  @Autowired private CsvService csvService;

  @Autowired private HttpService httpService;

  @GetMapping("/getSectionByMunicipality")
  public ResponseEntity<?> getSectionByMunicipality(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {

    List<SurveyUserSectionDto> userSectionDtos = new ArrayList<>();
    userSectionDtos = surveyUserSectionService.getSectionByMunicipality(authIdHeader);
    if (!userSectionDtos.isEmpty()) {
      return ResponseEntity.ok(userSectionDtos);
    }
    return new ResponseEntity<>(
        "No se encontraron datos con datos asociados el id: " + authIdHeader, HttpStatus.NOT_FOUND);
  }
}
