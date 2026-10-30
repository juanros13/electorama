package com.electorama.logfile.controller;

import com.electorama.logfile.service.OdsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/ods")
public class OdsController {

  @Autowired OdsService odsService;

  @GetMapping("")
  public ResponseEntity getAllOds(@RequestHeader("X-Auth-Id") String authIdHeader) {

    return ResponseEntity.ok(odsService.getOdsList());
  }

  @GetMapping("/{id}")
  public ResponseEntity getOdsById(@PathVariable("id") Long id) {

    return ResponseEntity.ok(odsService.getOdsById(id));
  }

  @GetMapping("/goals")
  public ResponseEntity getAllGoals() {

    return ResponseEntity.ok(odsService.getGoalsList());
  }

  @GetMapping("/goals/{id}")
  public ResponseEntity getGoalsById(@PathVariable("id") Long id) {

    return ResponseEntity.ok(odsService.getGoalsById(id));
  }

  @GetMapping("/goals/byOdsId/{id}")
  public ResponseEntity getGoalsByOdsId(@PathVariable("id") Long id) {

    return ResponseEntity.ok(odsService.getGoalsByOdsId(id));
  }

  @GetMapping("/indicators")
  public ResponseEntity getAllIndicators() {

    return ResponseEntity.ok(odsService.getIndicatorsList());
  }

  @GetMapping("/indicators/{id}")
  public ResponseEntity getIndicatorsById(@PathVariable("id") Long id) {

    return ResponseEntity.ok(odsService.getIndicatorsById(id));
  }

  @GetMapping("/indicators/byGoalId/{id}")
  public ResponseEntity getIndicatorsByGoalsId(@PathVariable("id") Long id) {

    return ResponseEntity.ok(odsService.getIndicatorsByGoalsId(id));
  }

  @GetMapping("/instruments")
  public ResponseEntity getAllInstrument() {

    return ResponseEntity.ok(odsService.getInstrumentList());
  }

  @GetMapping("/instruments/{id}")
  public ResponseEntity getInstrumentById(@PathVariable("id") Long id) {

    return ResponseEntity.ok(odsService.getInstrumentsById(id));
  }

  @GetMapping("/instruments/byIndicatorsId/{id}")
  public ResponseEntity getInstrumentByIndicatorId(@PathVariable("id") Long id) {

    return ResponseEntity.ok(odsService.getInstrumentsByIndicatorsId(id));
  }
}
