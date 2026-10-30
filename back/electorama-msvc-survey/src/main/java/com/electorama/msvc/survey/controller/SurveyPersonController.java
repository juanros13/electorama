package com.electorama.msvc.survey.controller;

import com.electorama.msvc.survey.model.dto.SurveyPersonDto;
import com.electorama.msvc.survey.service.SurveyPersonService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/person")
public class SurveyPersonController {

  @Autowired private SurveyPersonService surveyPersonService;

  @PostMapping
  public ResponseEntity<?> create(
      @Valid @RequestBody SurveyPersonDto request, BindingResult result) {

    surveyPersonService.createSurveyPerson(request);
    if (result.hasErrors()) {
      log.error("Error in the request: {}", result.getAllErrors());
      return ResponseEntity.badRequest().body(result.getAllErrors());
    }
    return ResponseEntity.ok().build();
  }
}
