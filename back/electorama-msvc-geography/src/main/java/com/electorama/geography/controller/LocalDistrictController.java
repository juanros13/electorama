package com.electorama.geography.controller;

import com.electorama.geography.model.dto.LocalDistrict;
import com.electorama.geography.service.LocalDistrictService;
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
@RequestMapping(value = "/localDistrict")
public class LocalDistrictController {

  @Autowired LocalDistrictService localDistrictService;

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<LocalDistrict> stateOptional = localDistrictService.byId(id);
    if (stateOptional.isPresent()) {
      return ResponseEntity.ok(stateOptional.get());
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/entity/{id}")
  public Map<String, Object> findByEntity(
      @PathVariable Integer id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("localDistricts", localDistrictService.findByEntityId(id));
    return body;
  }

  @GetMapping(value = "")
  public ResponseEntity readLocalDistrics(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest request) {

    if (request.isUserInRole("ADMIN")) {
      log.info("Es Administrador");
      return ResponseEntity.ok(localDistrictService.findByLocalDistricts());
    } else {
      log.info("otro");
      return ResponseEntity.notFound().build();
    }
  }
}
