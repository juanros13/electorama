package com.electorama.geography.controller;

import com.electorama.geography.model.dto.LocalitiesSection;
import com.electorama.geography.service.LocalitiesSectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/localitiesSection")
public class LocalitiesSectionController {

  @Autowired LocalitiesSectionService localitiesSectionService;

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<LocalitiesSection> stateOptional = localitiesSectionService.byId(id);
    if (stateOptional.isPresent()) {
      return ResponseEntity.ok(stateOptional.get());
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/getAll")
  public ResponseEntity<?> getAll(@RequestHeader("X-Auth-Id") String authIdHeader) {
    List<LocalitiesSection> stateOptional = localitiesSectionService.getAll();
    if (!stateOptional.isEmpty()) {
      return ResponseEntity.ok(stateOptional);
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/section/{id}")
  public Map<String, Object> findBySeccion(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("LocalitiesSections", localitiesSectionService.findBySectionId(id));
    return body;
  }

  @PostMapping(value = "/register")
  public ResponseEntity createLocalities(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @Valid @RequestBody LocalitiesSection localitiesSection,
      HttpServletRequest request,
      BindingResult result) {

    if (request.isUserInRole("ADMIN")) {
      log.info("Es Administrador");

      if (result.hasErrors()) {
        return validar(result);
      }

      log.info("Creating user with {}", localitiesSection.toString());
      LocalitiesSection localities = localitiesSectionService.createLocation(localitiesSection);
      return ResponseEntity.ok(localities);
    } else {
      log.info("No eres Administrador");
      return ResponseEntity.notFound().build();
    }
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/editLocation/{id}")
  public ResponseEntity<?> editLocation(
      @Valid @RequestBody LocalitiesSection localitiesSection,
      BindingResult result,
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    if (result.hasErrors()) {
      return validar(result);
    }

    Optional<LocalitiesSection> o = localitiesSectionService.byId(id);
    if (o.isPresent()) {
      LocalitiesSection locationDB = o.get();

      locationDB.setMunicipio(localitiesSection.getMunicipio());
      locationDB.setSeccion(localitiesSection.getSeccion());
      locationDB.setLocalidad(localitiesSection.getLocalidad());
      locationDB.setSeccionId(localitiesSection.getSeccionId());

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(localitiesSectionService.save(locationDB));
    }
    return ResponseEntity.notFound().build();
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

  @DeleteMapping("/deleteLocation/{id}")
  public ResponseEntity<?> deleteLocation(
      @PathVariable Long id,
      HttpServletRequest request,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    if (request.isUserInRole("ADMIN")) {
      log.info("Es Administrador");

      Optional<LocalitiesSection> o = localitiesSectionService.byId(id);
      if (o.isPresent()) {
        LocalitiesSection localitiesSectionDB = o.get();

        long miliseconds = System.currentTimeMillis();
        java.sql.Date date = new Date(miliseconds);

        localitiesSectionDB.setDeleted(date);

        localitiesSectionService.save(localitiesSectionDB);
        return ResponseEntity.noContent().build();
      }
      return ResponseEntity.notFound().build();

    } else {
      log.info("No eres Administrador");
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/localitiesSect/{id}")
  public Map<String, Object> findByLocalitiesSection(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("LocalitiesSections", localitiesSectionService.findBySection(id));
    return body;
  }
}
