package com.electorama.msvc.canvasandwalls.controller;

import com.electorama.msvc.canvasandwalls.model.dto.CanvasAndWallsGoalDto;
import com.electorama.msvc.canvasandwalls.service.CanvasAndWallsGoalService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping(value = "/canvasAndWallGoals")
public class CanvasAndWallsGoalController {

  @Autowired CanvasAndWallsGoalService goalService;

  @PostMapping
  public ResponseEntity create(
      @Valid @RequestBody CanvasAndWallsGoalDto request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    if (result.hasErrors()) {
      return validar(result);
    }

    log.info("Creating  with {}", request.toString());
    CanvasAndWallsGoalDto valueObj = goalService.save(request);
    return ResponseEntity.ok(valueObj);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> editCanvasAndWallsGoal(
      @Valid @RequestBody CanvasAndWallsGoalDto request,
      BindingResult result,
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    if (result.hasErrors()) {
      return validar(result);
    }
    log.info("Creating  with {}", request.toString());
    request.setId(id);
    CanvasAndWallsGoalDto valueObj = goalService.save(request);
    return ResponseEntity.ok(valueObj);
  }

  @GetMapping("/findByReferenceAuthId/{id}")
  public ResponseEntity<?> findByReferenceAuthId(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest,
      @PathVariable String id) {
    if (servletRequest.isUserInRole("ADMIN")) {
      return ResponseEntity.ok(goalService.findByReferenceAuthId(id));
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/findByReferenceIdAndMunicipalityId/{referenceId}/{municipalityId}")
  public ResponseEntity<?> findByReferenceIdAndMunicipalityId(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest,
      @PathVariable String referenceId,
      @PathVariable Integer municipalityId) {
    if (servletRequest.isUserInRole("ADMIN")) {
      return ResponseEntity.ok(
          goalService.findByMunicipalityIdAndReferenceAuthId(municipalityId, referenceId));
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("")
  public ResponseEntity<?> findByAll(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {
    if (servletRequest.isUserInRole("ADMIN")) {
      return ResponseEntity.ok(goalService.findByAll());
    } else {
      return ResponseEntity.notFound().build();
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
}
