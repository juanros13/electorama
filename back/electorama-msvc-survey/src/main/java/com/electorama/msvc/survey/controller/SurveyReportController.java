package com.electorama.msvc.survey.controller;

import com.electorama.msvc.survey.service.SurveyReportService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/surveyReport")
public class SurveyReportController {
  @Autowired private SurveyReportService surveyReportService;

  @GetMapping("/getDashboardMunicipality")
  public ResponseEntity<?> getDashboardMunicipality(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put("getDashboardMunicipality", surveyReportService.getDashMunicipality());
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

  @GetMapping("/getSurveyReportMunicipality")
  public ResponseEntity<?> getSurveyReportMunicipality(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getSurveyReportMunicipality", surveyReportService.getSurveyReportMunicipality());
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

  @GetMapping("/getCoordinateMunicipality/{municipalityId}")
  public ResponseEntity<?> getSurveyReportMunicipality(
      @PathVariable Long municipalityId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getSurveyReportMunicipality",
          surveyReportService.getCoordinateMunicipality(municipalityId));
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

  @GetMapping("/getGeolocationByUser/{userId}")
  public ResponseEntity<?> getGeolocationByUser(
      @PathVariable Long userId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put("getGeolocationByUser", surveyReportService.getGeolocationByUser(userId));
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

  @GetMapping("/getSurveyReportMunicipalityId/{municipalityId}")
  public ResponseEntity<?> getSurveyReportMunicipalityId(
      @PathVariable Long municipalityId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getSurveyReportMunicipalityId",
          surveyReportService.getSurveyReportMunicipalityId(municipalityId));
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

  @GetMapping("/getSurveyReportCoordinatorMunicipality")
  public ResponseEntity<?> getSurveyReportCoordinatorMunicipality(
      @RequestParam(defaultValue = "") Integer sectionNumber,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getSurveyReportCoordinatorMunicipality",
          surveyReportService.getSurveyReportCoordinateMunicipality(authIdHeader, sectionNumber));
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

  @GetMapping("/getSurveyReportAdminCoordinator")
  public ResponseEntity<?> getSurveyReportAdminCoordinator(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      if (servletRequest.isUserInRole("ADMIN_SURVEY")) {
        response.put(
            "getSurveyReportAdminCoordinator",
            surveyReportService.getSurveyReportAdminCoordinator(
                pageNo, pageSize, sortBy, orderBy, search, authIdHeader));
      } else {
        response.put(
            "getSurveyReportAdminCoordinator", "No tienes facultades para accesar a este reporte");
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
}
