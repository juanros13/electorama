package com.electorama.msvc.electionsresults.controllers;

import com.electorama.msvc.electionsresults.models.dto.*;
import com.electorama.msvc.electionsresults.service.ElectionResultService;
import com.electorama.msvc.electionsresults.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/electionResult")
public class ElectionResultController {

  @Autowired ElectionResultService electionResultService;
  @Autowired private ScheduleService scheduleService;
  /**********
   * @GetMapping("/")
   * public ResponseEntity<?> findAllYears(
   * @RequestParam(defaultValue = "0") Long electionYear,
   * @RequestParam(defaultValue = "0") Long electionType,
   * @RequestParam(defaultValue = "0") Long electionPosition,
   * @RequestHeader("X-Auth-Id") String authIdHeader) {
   * Map<String, Object> response = new HashMap<>();
   * try {
   * response.put(
   * "elections", electionResultService.getElections(electionYear, electionPosition, electionType));
   *
   * response.put("success", "true");
   * return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
   * } catch (DataAccessException e) {
   * response.put("mensaje", "Error al realizar la consulta a base de datos");
   * response.put(
   * "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
   * response.put("success", "false");
   * return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
   * }
   * }
   *
   *
   * @GetMapping("/findByStatus/{status}")
   * public ResponseEntity<?> findByStatus(@PathVariable String status) {
   *
   * Map<String, Object> response = new HashMap<>();
   *
   * try {
   * response.put("findByStatus", electionResultService.findByStatus(status));
   * response.put("success", "true");
   * return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
   *
   * } catch (DataAccessException e) {
   * response.put("mensaje", "Error al realizar la consulta a base de datos");
   * response.put(
   * "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
   * response.put("success", "false");
   * return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
   * }
   * }
   *
   * @GetMapping("/findByAnioElection/{anio}")
   * public ResponseEntity<?> findByAnioElection(@PathVariable String anio) {
   * Map<String, Object> response = new HashMap<>();
   *
   * try {
   * response.put("findByAnioElection", electionResultService.findByAnioElection(anio));
   * response.put("success", "true");
   * return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
   *
   * } catch (DataAccessException e) {
   * response.put("mensaje", "Error al realizar la consulta a base de datos");
   * response.put(
   * "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
   * response.put("success", "false");
   * return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
   * }
   * }
   *
   * @GetMapping("/findByPosition/{positionId}")
   * public ResponseEntity<?> findByPosition(@PathVariable Integer positionId) {
   *
   * Map<String, Object> response = new HashMap<>();
   *
   * try {
   * response.put("findByPosition", electionResultService.findByPosition(positionId));
   * response.put("success", "true");
   * return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
   *
   * } catch (DataAccessException e) {
   * response.put("mensaje", "Error al realizar la consulta a base de datos");
   * response.put(
   * "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
   * response.put("success", "false");
   * return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
   * }
   * }
   *
   ***********/
  @PostMapping("/v2.0/updateElectionsResults")
  public ResponseEntity<?> updateElectionsResults(
      @RequestBody List<UpdateElectionResultsDto> datesRequestDto,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    JSONObject jsonResponse = new JSONObject();

    if (scheduleService.isExecutionAllowed()) {
      try {
        electionResultService.updateElectionsResults(
            (List<UpdateElectionResultsDto>) datesRequestDto);
        jsonResponse.put("message", "Results updated successfully");
        return ResponseEntity.ok(jsonResponse);
      } catch (Exception e) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", "Failed to update results");
        errorResponse.put("details", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
      }
    } else {
      // No ejecutar la lógica del negocio
      jsonResponse.put("message", "Tarea no permitida en este horario.");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonResponse);
    }
  }

  @PostMapping("/findElectionsAssociation")
  public List<ElectionResultDto> findElectionsAssociation(
      @RequestBody ElectionAssociationIdsDto electionAssociationIds) {
    return electionResultService.findElectionsAssociation(electionAssociationIds);
  }

  @PostMapping("/findElectionsAssociationB")
  public List<ResultDto> findElectionsAssociationB(
      @RequestBody ElectionAssociationIdsDto electionAssociationIds) {
    return electionResultService.findElectionsAssociationB(electionAssociationIds);
  }

  @PostMapping("/sumResultElections")
  public List<PoliticalPartyResulDto> sumResultElections(
      @RequestBody ElectionAssociationIdsDto electionAssociationIds) {
    return electionResultService.sumResultElections(electionAssociationIds);
  }

  @PostMapping("/sumResultElectionsSheets")
  public List<PoliticalPartyResulDto> sumResultElectionsSheets(
      @RequestBody ElectionAssociationIdsDto electionAssociationIds) {
    return electionResultService.sumResultElectionsSheets(electionAssociationIds);
  }

  @PostMapping("/sumResultElectionsMunicipality")
  public List<PoliticalPartyResulDto> sumResultElectionsMunicipality(
      @RequestBody MunicipalitiesIdsDto municipalitiesIdsDto) {
    return electionResultService.sumResultElectionsMunicipality(municipalitiesIdsDto);
  }

  @PostMapping("/sumResultElectionsDistrictFederal")
  public List<PoliticalPartyResulDto> sumResultElectionsDistrictFederal(
      @RequestBody FederalDistrictIdsDto federalDistrictIdsDto) {
    return electionResultService.sumResultElectionsDistrictFederal(federalDistrictIdsDto);
  }

  @PostMapping("/sumResultElectionsDistrictLocal")
  public List<PoliticalPartyResulDto> sumResultElectionsDistrictLocal(
      @RequestBody LocalDistrictIdsDto localDistrictIdsDto) {
    return electionResultService.sumResultElectionsDistrictLocal(localDistrictIdsDto);
  }
}
