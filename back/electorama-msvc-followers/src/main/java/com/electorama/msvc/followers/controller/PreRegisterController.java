package com.electorama.msvc.followers.controller;

import com.electorama.msvc.followers.model.dto.PreregisterDto;
import com.electorama.msvc.followers.service.PreregisterService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/preregistration")
public class PreRegisterController {

  @Autowired private PreregisterService preregisterService;

  @PostMapping(value = "/livestock")
  public ResponseEntity create(
      @Valid @RequestBody PreregisterDto livestock,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    if (result.hasErrors()) {
      return validar(result);
    }

    log.info("Creating PreRegistrer with {}", livestock.toString());
    PreregisterDto pre = preregisterService.save(livestock);
    try {
      return ResponseEntity.ok("PreRegistrer successfully created with id: " + pre.getId());
    } catch (Exception e) {
      return ResponseEntity.unprocessableEntity()
          .body("Error creating PreRegistrer " + e.getCause());
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

  @GetMapping(value = "/livestock/{id}")
  public ResponseEntity findById(@PathVariable Long id) {
    log.info("Finding PreRegistrer with id {}", id);
    PreregisterDto pre = preregisterService.findById(id);
    return ResponseEntity.ok(pre);
  }

  @PutMapping(value = "/livestock/{id}")
  public ResponseEntity update(
      @PathVariable Long id, @Valid @RequestBody PreregisterDto livestock, BindingResult result) {
    if (result.hasErrors()) {
      return validar(result);
    }
    log.info("Updating PreRegistrer with id {}", id);
    PreregisterDto pre = preregisterService.save(livestock);
    return ResponseEntity.ok(pre);
  }
}
