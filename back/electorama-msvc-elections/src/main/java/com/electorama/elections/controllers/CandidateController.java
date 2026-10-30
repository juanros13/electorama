package com.electorama.elections.controllers;

import com.electorama.elections.services.CandidateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/candidates")
public class CandidateController {

  @Autowired CandidateService candidateService;

  @GetMapping("/findAllCandidates")
  public ResponseEntity<?> findAllCandidates(@RequestParam(defaultValue = "") String name) {
    try {
      if (name.isEmpty()) {
        return ResponseEntity.ok(candidateService.findAllCandidates());
      }
      return ResponseEntity.ok(candidateService.findAllCandidatesByName(name));
    } catch (DataAccessException e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }
  }

  @GetMapping("/getByElection/{electionId}")
  public ResponseEntity<?> findByElection(@PathVariable Long electionId) {
    try {
      return ResponseEntity.ok(candidateService.findByElection(electionId));
    } catch (DataAccessException e) {
      return new ResponseEntity<>(e, HttpStatus.CONFLICT);
    }
  }
}
