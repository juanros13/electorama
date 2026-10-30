package com.electorama.geography.controller;

import com.electorama.geography.model.dto.Municipality;
import com.electorama.geography.service.MunicipalityService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/municipality")
public class MunicipalityController {

  @Autowired private MunicipalityService municipalityService;

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<Municipality> municipalityOptional = municipalityService.byId(id);
    if (municipalityOptional.isPresent()) {
      return ResponseEntity.ok(municipalityOptional.get());
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/entity/{id}")
  public Map<String, Object> findByEntity(
      @PathVariable Integer id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("municipios", municipalityService.findByEntityId(id));
    return body;
  }

  @GetMapping("/byEntity/{id}")
  public ResponseEntity byEntity(
      @PathVariable Integer id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    return ResponseEntity.ok(municipalityService.findByEntityId(id));
  }

  @GetMapping(value = "")
  public ResponseEntity readMunicipalities(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest request) {

    if (request.isUserInRole("ADMIN")) {
      log.info("Es Administrador");
      return ResponseEntity.ok(municipalityService.findByMunicipalities());
    } else {
      log.info("otro");
      return ResponseEntity.notFound().build();
    }
  }
}
