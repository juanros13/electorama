package com.electorama.elections.controllers;

import com.electorama.elections.services.VotingFlowService;
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
@RequestMapping(value = "/votingFlow")
public class VotingFlowController {

  @Autowired VotingFlowService votingFlowService;

  @GetMapping("/votingFlow")
  public ResponseEntity<?> getVotingFlow(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "") Long municipalityId,
      @RequestParam(defaultValue = "") Integer sectionNum,
      @RequestParam(defaultValue = "") Long federalDistrictId,
      @RequestParam(defaultValue = "") Long localDistrictId,
      @RequestParam(defaultValue = "") Long electionTypeId,
      @RequestParam(defaultValue = "") Long electionPositionId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getVotingFlow",
          votingFlowService.getVotingFlow(
              pageNo,
              pageSize,
              municipalityId,
              sectionNum,
              federalDistrictId,
              localDistrictId,
              electionTypeId,
              electionPositionId));
      //   }
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

  @GetMapping("/getProgressVote")
  public ResponseEntity<?> getProgressVote(
      @RequestParam(defaultValue = "") Long municipalityId,
      @RequestParam(defaultValue = "") Integer sectionNum,
      @RequestParam(defaultValue = "") Long federalDistrictId,
      @RequestParam(defaultValue = "") Long localDistrictId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getProgressVote",
          votingFlowService.getProgressVote(
              municipalityId, sectionNum, federalDistrictId, localDistrictId));
      //   }
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
