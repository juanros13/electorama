package com.electorama.msvc.survey.controller;

import com.electorama.msvc.survey.model.dto.*;
import com.electorama.msvc.survey.model.dto.request.RequestFolios;
import com.electorama.msvc.survey.model.dto.request.RequestSections;
import com.electorama.msvc.survey.model.rest.UserProfileResponse;
import com.electorama.msvc.survey.service.CsvService;
import com.electorama.msvc.survey.service.HttpService;
import com.electorama.msvc.survey.service.SurveyUserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/surveyUser")
public class SurveyUserController {
  @Autowired private SurveyUserService surveyUserService;

  @Autowired private CsvService csvService;

  @Autowired private HttpService httpService;

  @PostMapping(value = "/register")
  public ResponseEntity createUser(
      @RequestBody UserProfileResponse request, HttpServletRequest servletRequest) {
    SurveyUserSectionListDto user = surveyUserService.createUser(request);
    return ResponseEntity.ok(user);
  }

  @GetMapping("/send-surveys")
  public String sendSurveys() {
    String url = "http://localhost:8090/api/survey/surveyUser/register";
    List<CsvDto> users =
        csvService.readCsv("/home/angelalonso/Documents/JVP/encuesta/usuarios_admin.csv");

    for (CsvDto user : users) {
      httpService.sendRequest(user, url);
    }
    return "Requests sent successfully!";
  }

  @GetMapping("/getUsersMunicipality")
  public ResponseEntity<?> getUsersMunicipality(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {

    List<SurveyUserDto> surveyUserDtos = new ArrayList<>();
    surveyUserDtos = surveyUserService.listUsersMunicipality(authIdHeader);
    if (!surveyUserDtos.isEmpty()) {
      return ResponseEntity.ok(surveyUserDtos);
    }
    return new ResponseEntity<>(
        "No se encontraron datos con datos asociados el id: " + authIdHeader, HttpStatus.NOT_FOUND);
  }

  @PostMapping(value = "/assignSection")
  public ResponseEntity assignSection(
      @RequestBody RequestSections request, HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();

    String range = surveyUserService.assignSection(request);
    response.put("success", "true");
    return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
  }

  @PostMapping(value = "/assignFolio")
  public ResponseEntity assignFolio(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @RequestBody RequestFolios request,
      HttpServletRequest servletRequest) {

    if (servletRequest.isUserInRole("SURVEY_MUNICIPALITY")) {
      Map<String, Object> response = new HashMap<>();
      List<String> range = surveyUserService.assignFolio(request);
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping(value = "/deleteSection/{id}")
  public ResponseEntity<Map<String, Object>> deleteSection(@PathVariable("id") Long id) {
    Map<String, Object> response = new HashMap<>();
    boolean success = surveyUserService.deleteSectionNumber(id);

    if (success) {
      response.put("success", true);
      response.put("message", "Section deleted successfully.");
      return new ResponseEntity<>(response, HttpStatus.OK);
    } else {
      response.put("success", false);
      response.put("message", "Section deletion failed.");
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/getSectionMunicipality/{municipalityId}")
  public ResponseEntity<?> getSectionMunicipality(
      @PathVariable Long municipalityId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    List<SurveySectionGoalDto> surveySectionGoalDtos = new ArrayList<>();
    surveySectionGoalDtos = surveyUserService.listSectionMunicipality(municipalityId);
    if (!surveySectionGoalDtos.isEmpty()) {
      return ResponseEntity.ok(surveySectionGoalDtos);
    }
    return new ResponseEntity<>(
        "No se encontraron datos con datos asociados el id: " + authIdHeader, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/getStateServer/{authId}")
  public ResponseEntity<?> getStateServer(
      @PathVariable("authId") String authId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    Optional<SurveyUserSurveyDto> surveyUserSurveyDtos = surveyUserService.listUsersSurvey(authId);

    if (!surveyUserSurveyDtos.isEmpty()) {
      return ResponseEntity.ok(surveyUserSurveyDtos);
    }
    return new ResponseEntity<>(
        "No se encontraron datos con datos asociados el id: " + authIdHeader, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/listSurveyAnnex/{id}")
  public ResponseEntity<?> listSurveyAnnex(
      @PathVariable("id") Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    Optional<SurveyAnnexReportDto> surveyUserSurveyDtos = surveyUserService.listSurveyAnnex(id);

    if (!surveyUserSurveyDtos.isEmpty()) {
      return ResponseEntity.ok(surveyUserSurveyDtos);
    }
    return new ResponseEntity<>(
        "No se encontraron datos con datos asociados el id: " + authIdHeader, HttpStatus.NOT_FOUND);
  }
}
