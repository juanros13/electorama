package com.electorama.msvc.followers.controller;

import com.electorama.msvc.followers.model.dto.FollowersSirenaDto;
import com.electorama.msvc.followers.service.FollowersSirenaService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/followersSirena")
public class FollowersSirenaController {

  @Autowired private FollowersSirenaService followersSirenaService;

  @GetMapping()
  public ResponseEntity<?> list(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      if (servletRequest.isUserInRole("ADMIN")) {
        response.put(
            "list",
            followersSirenaService.listAdmin(
                pageNo, pageSize, sortBy, orderBy, search, authIdHeader));
        response.put("success", "true");
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
      } else {
        response.put(
            "list",
            followersSirenaService.list(pageNo, pageSize, sortBy, orderBy, search, authIdHeader));
        response.put("success", "true");
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
      }
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<>(
          "No se encontraron datos con datos asociados el id: " + authIdHeader,
          HttpStatus.NOT_FOUND);
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

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    // Optional<FollowersSirenaDto> PromotersSirenaDtoOptional = promotersService.byId(id,
    // authIdHeader);
    Optional<FollowersSirenaDto> PromotersSirenaDtoOptional;

    PromotersSirenaDtoOptional = followersSirenaService.byIdAdmin(id, authIdHeader);

    if (PromotersSirenaDtoOptional.isPresent()) {
      return ResponseEntity.ok(PromotersSirenaDtoOptional.get());
    }
    return new ResponseEntity<>(
        "No se encontraron datos con datos asociados el id: " + id, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/getFollowersSirenaBySection")
  public ResponseEntity<?> getFollowersSirenaBySection(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "") Long sectionId,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    Map<String, Object> response = new HashMap<>();
    try {

      response.put(
          "list",
          followersSirenaService.listBySection(
              pageNo, pageSize, sortBy, orderBy, search, authIdHeader, sectionId));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);

    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<>(
          "No se encontraron datos con datos asociados el id: " + authIdHeader,
          HttpStatus.NOT_FOUND);
    }
  }
}
