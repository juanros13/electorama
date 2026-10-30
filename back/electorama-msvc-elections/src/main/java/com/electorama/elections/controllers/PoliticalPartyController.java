package com.electorama.elections.controllers;

import com.electorama.elections.services.PoliticalPartyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/political-party")
public class PoliticalPartyController {

  @Autowired PoliticalPartyService politicalPartyService;

  @GetMapping("/findAllPoliticalParties")
  public ResponseEntity<?> findAllElections() {
    return ResponseEntity.ok(politicalPartyService.findAllPoliticalParties());
  }

  @GetMapping("/finByType/{idType}")
  public ResponseEntity<?> findByType(@PathVariable Long idType) {
    return ResponseEntity.ok(politicalPartyService.findByType(idType));
  }

  @GetMapping("/{idPoliticalParty}")
  public ResponseEntity<?> findById(@PathVariable Long idPoliticalParty) {
    return ResponseEntity.ok(politicalPartyService.findById(idPoliticalParty));
  }
}
