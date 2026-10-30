package com.electorama.geography.controller;

import com.electorama.geography.model.dto.SafeHouse;
import com.electorama.geography.service.SafeHouseService;
import com.electorama.geography.service.SeccionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/safeHouse")
@RequiredArgsConstructor
public class SafeHouseController {

  @Autowired SafeHouseService safeHouseService;

  @Autowired SeccionService seccionService;

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    if (servletRequest.isUserInRole("ADMIN")
        || servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      Optional<SafeHouse> safeHouseOptional = safeHouseService.byId(id);
      if (safeHouseOptional.isPresent()) {
        return ResponseEntity.ok(safeHouseOptional.get());
      } else {
        return ResponseEntity.notFound().build();
      }

    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/getByMunicipality")
  public ResponseEntity<?> detail(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {
    if (servletRequest.isUserInRole("ADMIN")
        || servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      List<SafeHouse> listSafeHouse = safeHouseService.findByMunicipalities();
      return ResponseEntity.ok(listSafeHouse);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping()
  public ResponseEntity<?> create(
      @Valid @RequestBody SafeHouse request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    if (result.hasErrors()) {
      return validar(result);
    }
    if (servletRequest.isUserInRole("ADMIN")
        || servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      SafeHouse safeHouse = safeHouseService.save(request);
      return ResponseEntity.ok(safeHouse);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping()
  public ResponseEntity<?> update(
      @Valid @RequestBody SafeHouse request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    if (result.hasErrors()) {
      return validar(result);
    }
    if (servletRequest.isUserInRole("ADMIN")
        || servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      SafeHouse safeHouse = safeHouseService.save(request);
      return ResponseEntity.ok(safeHouse);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    if (servletRequest.isUserInRole("ADMIN")
        || servletRequest.isUserInRole("CORDINATOR_MUNICIPAL")) {
      try {
        SafeHouse safeHouse = safeHouseService.delete(id);
        return ResponseEntity.ok(safeHouse);
      } catch (Exception e) {
        return new ResponseEntity<>(e, HttpStatus.CONFLICT);
      }
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
