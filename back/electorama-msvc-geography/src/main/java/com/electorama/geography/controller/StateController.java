package com.electorama.geography.controller;

import com.electorama.geography.model.dto.State;
import com.electorama.geography.service.StateService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/entity")
public class StateController {

  @Autowired private StateService stateService;

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<State> stateOptional = stateService.byId(id);
    if (stateOptional.isPresent()) {
      return ResponseEntity.ok(stateOptional.get());
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/")
  public ResponseEntity<?> al(@RequestHeader("X-Auth-Id") String authIdHeader) {
    List<State> states = stateService.findAll();
    return ResponseEntity.ok(states);
  }
}
