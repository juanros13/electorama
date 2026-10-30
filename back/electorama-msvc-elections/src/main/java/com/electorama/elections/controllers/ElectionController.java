package com.electorama.elections.controllers;

import com.electorama.elections.services.ElectionService;
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
@RequestMapping(value = "/election")
public class ElectionController {

  @Autowired ElectionService electionService;

  @GetMapping("/")
  public ResponseEntity<?> findAllYears(
      @RequestParam(defaultValue = "0") Long electionYear,
      @RequestParam(defaultValue = "0") Long electionType,
      @RequestParam(defaultValue = "0") Long electionPosition,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "elections", electionService.getElections(electionYear, electionPosition, electionType));

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

  @GetMapping("/findAllElections")
  public ResponseEntity<?> findAllElections() {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put("findAllElections", electionService.findAllElections());
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

  @GetMapping("/findByStatus/{status}")
  public ResponseEntity<?> findByStatus(@PathVariable String status) {

    Map<String, Object> response = new HashMap<>();

    try {
      response.put("findByStatus", electionService.findByStatus(status));
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

  @GetMapping("/findByAnioElection/{anio}")
  public ResponseEntity<?> findByAnioElection(@PathVariable String anio) {
    Map<String, Object> response = new HashMap<>();

    try {
      response.put("findByAnioElection", electionService.findByAnioElection(anio));
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

  @GetMapping("/findByPosition/{positionId}")
  public ResponseEntity<?> findByPosition(@PathVariable Integer positionId) {

    Map<String, Object> response = new HashMap<>();

    try {
      response.put("findByPosition", electionService.findByPosition(positionId));
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

  @GetMapping("/findByType/{typeId}")
  public ResponseEntity<?> findByType(@PathVariable Integer typeId) {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put("findByType", electionService.findByType(typeId));
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

  @GetMapping("/electionResult/{electionsPositionsId}")
  public ResponseEntity<?> electionResult(
      @PathVariable Integer electionsPositionsId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    Map<String, Object> response = new HashMap<>();

    try {
      response.put(
          "electionResult",
          electionService.electionResult(electionsPositionsId, authIdHeader, servletRequest));
      response.put(
          "voters_list",
          electionService.electionResultVoterList(
              electionsPositionsId, authIdHeader, servletRequest));
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

  @GetMapping("/electionResultSheets/{electionsPositionsId}")
  public ResponseEntity<?> electionResultSheets(
      @PathVariable Integer electionsPositionsId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    Map<String, Object> response = new HashMap<>();

    try {
      response.put(
          "electionResultSheets",
          electionService.electionResultSheets(electionsPositionsId, authIdHeader, servletRequest));
      response.put(
          "voters_list",
          electionService.electionResultVoterListSheets(
              electionsPositionsId, authIdHeader, servletRequest));
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
