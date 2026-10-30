package com.electorama.elections.controllers;

import com.electorama.elections.models.dto.PollingBoothsIdsDto;
import com.electorama.elections.services.PollingBoothElectionAssociationService;
import com.electorama.elections.services.ScheduleService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(value = "/pollingBoothElectionAssociation")
public class PollingBoothElectionAssociationController {

  @Autowired PollingBoothElectionAssociationService pollingBoothElectionAssociationService;
  @Autowired private ScheduleService scheduleService;

  @GetMapping("/v2.0/getElectionsByPollingBooths")
  public ResponseEntity<?> getElectionsByPollingBooths(
      @RequestBody PollingBoothsIdsDto pollingBoothsIdsDto,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getElectionsByPollingBooths",
          pollingBoothElectionAssociationService.getElectionsByPollingBooths(
              pollingBoothsIdsDto.getPollingBoothsIds()));
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

  @PostMapping("/updateSynchronized/{id}")
  public ResponseEntity<?> updateSynchronized(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    JSONObject jsonResponse = new JSONObject();

    try {
      pollingBoothElectionAssociationService.updateSynchronized(id);

      jsonResponse.put("message", "Results updated successfully");

      return ResponseEntity.ok(jsonResponse.toString());
    } catch (Exception e) {

      JSONObject errorResponse = new JSONObject();
      errorResponse.put("error", "Failed to update results");
      errorResponse.put("details", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse.toString());
    }
  }

  @PostMapping("/v2.0")
  public ResponseEntity create(
      @RequestParam("electionAssociationId") Long electionAssociationId,
      @RequestParam(name = "pathSheet", required = true) MultipartFile pathSheet,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    JSONObject jsonResponse = new JSONObject();

    if (scheduleService.isExecutionAllowed()) {
      try {
        pollingBoothElectionAssociationService.save(electionAssociationId, pathSheet);

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

  @GetMapping("/getPollingBoothsElectionAssociation/{id}")
  public ResponseEntity<?> getPollingBoothsElectionAssociation(@PathVariable Long id) {
    return ResponseEntity.ok(
        pollingBoothElectionAssociationService.getPollingBoothsElectionAssociation(id));
  }

  @PostMapping("/updateValidate/{id}/{statusId}")
  public ResponseEntity<?> updateValidate(
      @PathVariable Long id,
      @PathVariable Integer statusId,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    JSONObject jsonResponse = new JSONObject();

    try {
      pollingBoothElectionAssociationService.updateValidate(id, statusId);

      jsonResponse.put("message", "Results updated successfully");

      return ResponseEntity.ok(jsonResponse.toString());
    } catch (Exception e) {

      JSONObject errorResponse = new JSONObject();
      errorResponse.put("error", "Failed to update results");
      errorResponse.put("details", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse.toString());
    }
  }
}
