package com.electorama.geography.controller;

import com.electorama.geography.model.dto.FederalDistrict;
import com.electorama.geography.service.FederalDistrictService;
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
@RequestMapping(value = "/federalDistrict")
public class FederalDistrictController {

  @Autowired FederalDistrictService federalDistrictService;

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<FederalDistrict> stateOptional = federalDistrictService.byId(id);
    if (stateOptional.isPresent()) {
      return ResponseEntity.ok(stateOptional.get());
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/entity/{id}")
  public Map<String, Object> findByEntity(
      @PathVariable Integer id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("federalDistricts", federalDistrictService.findByEntityId(id));
    return body;
  }

  @GetMapping(value = "")
  public ResponseEntity readFederalDistrics(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest request) {

    if (request.isUserInRole("ADMIN")) {
      log.info("Es Administrador");
      return ResponseEntity.ok(federalDistrictService.findByFederalDistricts());
    } else {
      log.info("otro");
      return ResponseEntity.notFound().build();
    }
  }
}
