package com.electorama.logfile.controller;

import com.electorama.logfile.model.dto.LogfileDto;
import com.electorama.logfile.service.LogfileService;
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
@RequestMapping(value = "/")
public class LogfileController {

  @Autowired LogfileService logfileService;

  @PostMapping
  public ResponseEntity create(
      @Valid @RequestBody LogfileDto request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    if (result.hasErrors()) {
      return validar(result);
    }

    log.info("Creating log with {}", request.toString());
    LogfileDto followers = logfileService.save(request, authIdHeader);
    return ResponseEntity.ok(followers);
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
}
