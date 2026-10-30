package com.electorama.msvc.survey.controller;

import com.dropbox.core.DbxException;
import com.electorama.msvc.survey.model.dto.DateDto;
import com.electorama.msvc.survey.model.dto.SurveyDto;
import com.electorama.msvc.survey.model.dto.SurveyUserFolioDto;
import com.electorama.msvc.survey.model.dto.request.RequestSurveyFolio;
import com.electorama.msvc.survey.model.dto.request.RequestSurveys;
import com.electorama.msvc.survey.model.dto.request.RequestSurveysRangeFolios;
import com.electorama.msvc.survey.model.dto.response.ResponseSurveyRangeFolios;
import com.electorama.msvc.survey.service.SurveyService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(value = "/")
public class SurveyController {

  @Autowired private SurveyService surveyService;

  @PostMapping
  public ResponseEntity create(@Valid @RequestBody RequestSurveys request, BindingResult result) {
    if (result.hasErrors()) {

      return validar(result);
    }
    RequestSurveys survey = surveyService.save(request);
    return ResponseEntity.ok(survey);
  }

  private ResponseEntity<Map<String, String>> validar(BindingResult result) {
    Map<String, String> errores = new HashMap<>();
    result
        .getFieldErrors()
        .forEach(
            err -> {
              errores.put(
                  err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
            });
    return ResponseEntity.badRequest().body(errores);
  }

  @GetMapping("/listSurvey")
  public ResponseEntity<?> list(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(required = false) String deviceId,
      @RequestParam(required = false) String surveyor_name,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          Date dge_survey_date,
      @RequestParam(required = false) Boolean status_change,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "survey",
          surveyService.list(
              pageNo,
              pageSize,
              sortBy,
              orderBy,
              search,
              deviceId,
              surveyor_name,
              dge_survey_date,
              status_change));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/listSurveyFolio")
  public ResponseEntity<?> listFolio(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "survey.id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(required = false) String deviceId,
      @RequestParam(required = false) String surveyor_name,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          Date dge_survey_date,
      @RequestParam(required = false) Boolean status_change,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "survey",
          surveyService.listFolio(
              pageNo,
              pageSize,
              sortBy,
              orderBy,
              search,
              deviceId,
              surveyor_name,
              dge_survey_date,
              status_change,
              authIdHeader));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(@PathVariable Long id) {
    Optional<SurveyDto> surveyDto = surveyService.findById(id);
    if (surveyDto.isPresent()) {
      return ResponseEntity.ok(surveyDto.get());
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Survey not found");
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(
      @PathVariable Long id, @Valid @RequestBody SurveyDto request, BindingResult result) {
    if (result.hasErrors()) {
      return validar(result);
    }
    Optional<SurveyDto> updatedSurvey = surveyService.update(id, request);
    if (updatedSurvey.isPresent()) {
      return ResponseEntity.ok(updatedSurvey.get());
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Survey not found");
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> changeStatus(@PathVariable Long id) {
    try {
      surveyService.changeStatus(id);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Survey not found");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error changing survey status");
    }
  }

  @GetMapping("/reports/weekly")
  public ResponseEntity<?> getTotalSurveysPerWeek(@RequestParam Integer numWeek) {
    DateDto dateDto = surveyService.calculateWeek(numWeek);
    List<Object[]> result = surveyService.getTotalSurveysPerWeek(dateDto);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/reports/municipality")
  public ResponseEntity<?> getTotalSurveysPerMunicipality() {
    List<Object[]> result = surveyService.getTotalSurveysPerMunicipality();
    return ResponseEntity.ok(result);
  }

  @GetMapping("/reports/municipality-user")
  public ResponseEntity<?> getTotalSurveysPerMunicipalityAndUser() {
    List<Object[]> result = surveyService.getTotalSurveysPerMunicipalityAndUser();
    return ResponseEntity.ok(result);
  }

  @PostMapping("/createFolio")
  public ResponseEntity<?> createFolio(
      @RequestParam(name = "pathPhotoIdentificationAdverse", required = false)
          MultipartFile pathPhotoIdentificationAdverse,
      @RequestParam(name = "pathPhotoIdentificationReverse", required = false)
          MultipartFile pathPhotoIdentificationReverse,
      // @RequestParam(name = "pathPhotoFacade", required = true) MultipartFile pathPhotoFacade,
      @RequestParam(name = "pathPhotoFacadeQr", required = false) MultipartFile pathPhotoFacadeQr,
      @RequestParam(defaultValue = "", required = true) String deviceId,
      @RequestParam(defaultValue = "", required = true) String folio,
      @RequestParam(defaultValue = "", required = true) String folioHouse,
      @RequestParam(defaultValue = "", required = true) String surveyDate,
      @RequestParam(defaultValue = "", required = false) String latitude,
      @RequestParam(defaultValue = "", required = false) String longitude,
      @RequestParam(defaultValue = "", required = true) String userAuthId,
      @RequestParam(defaultValue = "", required = true) String sectionId,
      @RequestParam(defaultValue = "", required = true) String sectionNumber,
      @RequestParam(defaultValue = "", required = true) String municipality,
      @RequestParam(defaultValue = "", required = true) Integer municipalityId,
      @RequestParam(defaultValue = "", required = true) Long surveyUserId)
      throws IOException, DbxException {
    if (deviceId.isEmpty()
        || folio.isEmpty()
        || folioHouse.isEmpty()
        || surveyDate.isEmpty()
        // || latitude.isEmpty()
        // || longitude.isEmpty()
        || userAuthId.isEmpty()
        || sectionId.isEmpty()
        || sectionNumber.isEmpty()
        || municipality.isEmpty()
        || municipalityId.toString().isEmpty()
        || surveyUserId != null) {

      return ResponseEntity.badRequest().body("All parameters must be provided and non-empty");
    }
    folio = Integer.toString(Integer.parseInt(folio));
    RequestSurveyFolio requestSurveyFolio = new RequestSurveyFolio();
    requestSurveyFolio.setPathPhotoIdentificationAdverse(pathPhotoIdentificationAdverse);
    requestSurveyFolio.setPathPhotoIdentificationReverse(pathPhotoIdentificationReverse);
    // requestSurveyFolio.setPathPhotoFacade(pathPhotoFacade);
    requestSurveyFolio.setPathPhotoFacadeQr(pathPhotoFacadeQr);
    requestSurveyFolio.setDeviceId(deviceId);
    requestSurveyFolio.setFolioHouse(folioHouse);
    requestSurveyFolio.setSurveyDate(surveyDate);
    requestSurveyFolio.setLatitude(latitude);
    requestSurveyFolio.setLongitude(longitude);
    requestSurveyFolio.setUserAuthId(userAuthId);
    requestSurveyFolio.setSectionId(sectionId);
    requestSurveyFolio.setSectionNumber(sectionNumber);
    requestSurveyFolio.setMunicipality(municipality);
    requestSurveyFolio.setMunicipalityId(municipalityId);
    requestSurveyFolio.setFolio(folio);

    Boolean survResp = surveyService.createFolio(requestSurveyFolio);
    return ResponseEntity.ok(survResp);
  }

  @GetMapping("/listSurveyFolioComplete")
  public ResponseEntity<?> listFolioComplete(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "survey.id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(required = false) String deviceId,
      @RequestParam(required = false) String surveyor_name,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          Date dge_survey_date,
      @RequestParam(required = false) Boolean status_change,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      if (servletRequest.isUserInRole("SURVEY_MUNICIPALITY")) {
        response.put(
            "survey",
            surveyService.listFolioCompleteMunicipality(
                pageNo,
                pageSize,
                sortBy,
                orderBy,
                search,
                deviceId,
                surveyor_name,
                dge_survey_date,
                status_change,
                authIdHeader));
      } else {
        response.put(
            "survey",
            surveyService.listFolioComplete(
                pageNo,
                pageSize,
                sortBy,
                orderBy,
                search,
                deviceId,
                surveyor_name,
                dge_survey_date,
                status_change,
                authIdHeader));
      }

      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/rangeFolios")
  public ResponseEntity getRangeFolios(@Valid @RequestBody RequestSurveysRangeFolios request) {
    List<ResponseSurveyRangeFolios> survey = surveyService.getRangeFolios(request);
    return ResponseEntity.ok(survey);
  }

  @PostMapping("/rangeFoliosAvailables")
  public ResponseEntity getRangeAvailablesFolios(
      @Valid @RequestBody RequestSurveysRangeFolios request) {
    List<ResponseSurveyRangeFolios> survey = surveyService.getRangeFoliosAvailables(request);
    return ResponseEntity.ok(survey);
  }

  @PostMapping("/folios")
  public ResponseEntity getFolios(@Valid @RequestBody RequestSurveysRangeFolios request) {
    List<SurveyUserFolioDto> survey = surveyService.getFolios(request);
    return ResponseEntity.ok(survey);
  }

  @PostMapping("/foliosAvailables")
  public ResponseEntity getFoliosAvailables(@Valid @RequestBody RequestSurveysRangeFolios request) {
    List<SurveyUserFolioDto> survey = surveyService.getFoliosAvailables(request);
    return ResponseEntity.ok(survey);
  }

  @GetMapping("/external/survey/3678")
  public ResponseEntity<?> getExternalSurvey() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String response = " ";
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error al realizar solicitud");
    }
  }
}
