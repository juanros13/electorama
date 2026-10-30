package com.electorama.msvc.survey.controller;

import com.dropbox.core.DbxException;
import com.electorama.msvc.survey.model.dto.SurveyAnnexDto;
import com.electorama.msvc.survey.model.dto.SurveyAnnexListDto;
import com.electorama.msvc.survey.model.dto.request.RequestSurveyAnexAllFile;
import com.electorama.msvc.survey.model.dto.request.RequestSurveyAnexFile;
import com.electorama.msvc.survey.service.SurveyAnnexService;
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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(value = "/surveyAnnex")
public class SurveyAnnexController {

  @Autowired private SurveyAnnexService surveyAnnexService;

  @PostMapping("")
  public ResponseEntity create(
      @Valid @RequestBody SurveyAnnexListDto request, BindingResult result) {
    if (result.hasErrors()) {

      return validar(result);
    }
    SurveyAnnexListDto survey = surveyAnnexService.save(request);
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
          surveyAnnexService.list(
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
          surveyAnnexService.listFolio(
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
    Optional<SurveyAnnexDto> SurveyAnnexDto = surveyAnnexService.findById(id);
    if (SurveyAnnexDto.isPresent()) {
      return ResponseEntity.ok(SurveyAnnexDto.get());
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Survey not found");
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(
      @PathVariable Long id, @Valid @RequestBody SurveyAnnexDto request, BindingResult result) {
    if (result.hasErrors()) {
      return validar(result);
    }
    Optional<SurveyAnnexDto> updatedSurvey = surveyAnnexService.update(id, request);
    if (updatedSurvey.isPresent()) {
      return ResponseEntity.ok(updatedSurvey.get());
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Survey not found");
    }
  }

  @GetMapping("/survey/{surveyId}/annexes")
  public ResponseEntity<List<SurveyAnnexDto>> getAnnexesBySurveyId(@PathVariable Long surveyId) {
    List<SurveyAnnexDto> annexes = surveyAnnexService.findBySurveyId(surveyId);
    if (annexes.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
    }
    return ResponseEntity.ok(annexes);
  }

  @PostMapping("/uploadFile")
  public ResponseEntity<?> createFolio(
      @RequestParam(name = "file", required = true) MultipartFile file,
      @RequestParam(defaultValue = "", required = true) String category,
      @RequestParam(defaultValue = "", required = true) Long surveyPersonId)
      throws IOException, DbxException {
    if (category.isEmpty() && surveyPersonId != null) {
      return ResponseEntity.badRequest().body("All parameters must be provided and non-empty");
    }
    RequestSurveyAnexFile requestSurveyAnexFile = new RequestSurveyAnexFile();
    requestSurveyAnexFile.setCategory(category);
    requestSurveyAnexFile.setSurveyPersonId(surveyPersonId);
    requestSurveyAnexFile.setFile(file);
    Boolean survResp = surveyAnnexService.uploadFile(requestSurveyAnexFile);
    return ResponseEntity.ok(survResp);
  }

  @PostMapping("/uploadFileAll")
  public ResponseEntity<?> uploadFileAll(
      @RequestParam(name = "identificationApplicantFile", required = false)
          MultipartFile identificationApplicantFile,
      @RequestParam(name = "birthCertificateApplicantFile", required = false)
          MultipartFile birthCertificateApplicantFile,
      @RequestParam(name = "curpApplicantFile", required = false) MultipartFile curpApplicantFile,
      @RequestParam(name = "addressApplicantFile", required = false)
          MultipartFile addressApplicantFile,
      @RequestParam(name = "identificationAssistantFile", required = false)
          MultipartFile identificationAssistantFile,
      @RequestParam(name = "birthCertificateAssistantFile", required = false)
          MultipartFile birthCertificateAssistantFile,
      @RequestParam(name = "curpAssistantFile", required = false) MultipartFile curpAssistantFile,
      @RequestParam(name = "addressAssistantFile", required = false)
          MultipartFile addressAssistantFile,
      @RequestParam(defaultValue = "", required = true) Long surveyId,
      @RequestParam(defaultValue = "", required = true) Long surveyPersonId)
      throws IOException, DbxException {
    if (surveyPersonId == null && surveyId == null) {
      return ResponseEntity.badRequest().body("All parameters must be provided and non-empty");
    }
    RequestSurveyAnexAllFile requestSurveyAnexFile = new RequestSurveyAnexAllFile();

    requestSurveyAnexFile.setIdentificationApplicantFile(identificationApplicantFile);
    requestSurveyAnexFile.setBirthCertificateApplicantFile(birthCertificateApplicantFile);
    requestSurveyAnexFile.setCurpApplicantFile(curpApplicantFile);
    requestSurveyAnexFile.setAddressApplicantFile(addressApplicantFile);
    requestSurveyAnexFile.setIdentificationAssistantFile(identificationAssistantFile);
    requestSurveyAnexFile.setBirthCertificateAssistantFile(birthCertificateAssistantFile);
    requestSurveyAnexFile.setCurpAssistantFile(curpAssistantFile);
    requestSurveyAnexFile.setAddressAssistantFile(addressAssistantFile);

    requestSurveyAnexFile.setSurveyPersonId(surveyPersonId);
    Boolean survResp = surveyAnnexService.uploadFileAll(requestSurveyAnexFile);
    return ResponseEntity.ok(survResp);
  }
}
