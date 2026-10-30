package com.electorama.msvc.followers.controller;

import com.electorama.msvc.followers.model.dto.UpdateVoteFlagAndSyncronizedListDto;
import com.electorama.msvc.followers.service.MobilizationService;
import com.electorama.msvc.followers.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/mobilization")
public class MobilizationController {

  @Autowired private MobilizationService mobilizationService;

  @Autowired private ScheduleService scheduleService;

  @GetMapping("/getStateMobilization")
  public ResponseEntity<?> getStateMobilization(@RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put("getStateMobilization", mobilizationService.getStateMobilization());
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

  @GetMapping("/getMobilizationByMunicipality")
  public ResponseEntity<?> getMobilizationByMunicipality(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getMobilizationByMunicipality", mobilizationService.getMunicipalityMobilization());
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

  @PostMapping("/updateVoteFlagAndSyncronizedList")
  public ResponseEntity<?> updateVoteFlagAndSyncronizedList(
      @RequestBody UpdateVoteFlagAndSyncronizedListDto request,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    JSONObject jsonResponse = new JSONObject();
    boolean updated = mobilizationService.updateVoteFlagAndSyncronizedList(request);
    try {
      if (updated) {
        jsonResponse.put("message", "Results updated successfully");
      }
    } catch (Exception e) {
      JSONObject errorResponse = new JSONObject();
      errorResponse.put("error", "Failed to update results");
      errorResponse.put("details", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    return ResponseEntity.ok(jsonResponse);
  }

  @GetMapping("/getMunicipalityByReferentMobilization")
  public ResponseEntity<?> getMunicipalityByReferentMobilization(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getMobilizationByMunicipality",
          mobilizationService.getMunicipalityByReferentMobilization(authIdHeader));
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

  @GetMapping("/getFederalDistrictByReferentMobilization")
  public ResponseEntity<?> getFederalDistrictByReferentMobilization(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getFederalDistrictByReferentMobilization",
          mobilizationService.getFederalDistrictByReferentMobilization());
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

  @GetMapping("/getMobilizationSectionByMunicipality")
  public ResponseEntity<?> getMobilizationSectionByMunicipality(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getMobilizationSectionByMunicipality",
          mobilizationService.getMobilizationSectionByMunicipality());
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

  @GetMapping("/getMunicipalityByReferentAndCoordinatorMobilization")
  public ResponseEntity<?> getMunicipalityByReferentAndCoordinatorMobilization(
      @RequestParam(defaultValue = "") Integer municipalityId,
      @RequestParam(defaultValue = "") String referentId,
      @RequestParam(defaultValue = "") Integer federalDistrict,
      @RequestParam(defaultValue = "") Integer localDistrict,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getMunicipalityByReferentAndCoordinatorMobilization",
          mobilizationService.getMunicipalityByReferentAndCoordinatorMobilization(
              servletRequest,
              authIdHeader,
              municipalityId,
              referentId,
              federalDistrict,
              localDistrict));
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

  @GetMapping("/getMunicipalityBySectionAndReferentMobilization/{municipalityId}")
  public ResponseEntity<?> getMunicipalityBySectionAndReferentMobilization(
      @PathVariable Long municipalityId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getMunicipalityBySectionAndReferentMobilization",
          mobilizationService.getMunicipalityBySectionAndReferentMobilization(municipalityId));
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

  @GetMapping("/getFederalDistrictMobilization")
  public ResponseEntity<?> getFederalDistrictMobilization(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getFederalDistrictMobilization", mobilizationService.getFederalDistrictMobilization());
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

  @GetMapping("/getLocalDistrictMobilization")
  public ResponseEntity<?> getLocalDistrictMobilization(
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getLocalDistrictMobilization", mobilizationService.getLocalDistrictMobilization());
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

  @GetMapping("/getReferentsByFederalDistrictMobilization/{federalDistrictId}")
  public ResponseEntity<?> getReferentsByFederalDistrictMobilization(
      @PathVariable Long federalDistrictId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getReferentsByFederalDistrictMobilization",
          mobilizationService.getReferentsByFederalDistrictMobilization(federalDistrictId));
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

  @GetMapping("/getReferentsByLocalDistrictMobilization/{localDistrictId}")
  public ResponseEntity<?> getReferentsByLocalDistrictMobilization(
      @PathVariable Long localDistrictId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getReferentsByLocalDistrictMobilization",
          mobilizationService.getReferentsByLocalDistrictMobilization(localDistrictId));
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

  @GetMapping("/getReferentAlt")
  public ResponseEntity<?> getReferentAlt(
      @RequestParam(defaultValue = "") String referentId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getReferentAlt", mobilizationService.getReferentAlt(servletRequest, authIdHeader));
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

  @GetMapping("/getMunicipalityByReferentAltAndCoordinatorMobilization")
  public ResponseEntity<?> getMunicipalityByReferentAltAndCoordinatorMobilization(
      HttpServletRequest servletRequest,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @RequestParam(defaultValue = "") String referentAltId) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getMunicipalityByReferentAltAndCoordinatorMobilization",
          mobilizationService.getMunicipalityByReferentAltAndCoordinatorMobilization(
              servletRequest, authIdHeader, referentAltId));
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

  @GetMapping("/getMobilizationAlt")
  public ResponseEntity<?> getMobilizationAlt(
      HttpServletRequest servletRequest, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getMobilizationAlt",
          mobilizationService.getMobilizationAlt(servletRequest, authIdHeader));
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
