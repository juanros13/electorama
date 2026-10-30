package com.electorama.geography.controller;

import com.electorama.geography.model.dto.Seccion;
import com.electorama.geography.model.repository.CatalogQueryRepository.SectionChange;
import com.electorama.geography.service.CatalogService;
import com.electorama.geography.service.SeccionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/seccion")
@RequiredArgsConstructor
public class SeccionController {

  @Autowired SeccionService seccionService;

  @Autowired CatalogService catalogService;

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<Seccion> municipalityOptional = seccionService.byId(id);
    if (municipalityOptional.isPresent()) {
      return ResponseEntity.ok(municipalityOptional.get());
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/entity/{id}")
  public Map<String, Object> findByEntity(
      @PathVariable Integer id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("seccions", seccionService.findByEntityId(id));
    return body;
  }

  @GetMapping("/byEntity/{id}")
  public ResponseEntity byEntity(
      @PathVariable Integer id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    return ResponseEntity.ok(seccionService.findByEntityId(id));
  }

  @GetMapping("/federalDistrict/{id}")
  public Map<String, Object> findByFederalDistrict(
      @PathVariable Integer id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("seccions", seccionService.findByFederalDistrictId(id));
    return body;
  }

  @GetMapping("/findSectionsByFederalDistrict/{federalDistrictId}")
  public Map<String, Object> findSectionsByFederalDistrict(
      @PathVariable Integer federalDistrictId, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put(
        "findBySectionsByFederalDistrict",
        seccionService.findBySectionsByFederalDistrict(federalDistrictId));
    return body;
  }

  @GetMapping("/findSectionByLocalDistrict/{LocalDistrictId}")
  public Map<String, Object> findSectionByLocalDistrict(
      @PathVariable Integer LocalDistrictId, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("seccions", seccionService.findSectionByLocalDistrictId(LocalDistrictId));
    return body;
  }

  @GetMapping("/localDistrict/{id}")
  public Map<String, Object> findByLocalDistrict(
      @PathVariable Integer id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("seccions", seccionService.findByLocalDistrictId(id));
    return body;
  }

  @GetMapping("/municipality/{id}")
  public Map<String, Object> findByMunicipality(
      @PathVariable Integer id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("seccions", seccionService.findByMunicipality(id));
    return body;
  }

  @GetMapping("/municipalityReference")
  public Map<String, Object> findByMunicipalityReference(
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("seccions", seccionService.findByMunicipalityReference());
    return body;
  }

  @GetMapping(value = "")
  public ResponseEntity readSections(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest request) {

    if (request.isUserInRole("ADMIN")) {
      log.info("Es Administrador");
      return ResponseEntity.ok(seccionService.findBySections());
    } else {
      log.info("otro");
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Sólo cambia la meta. Antes guardaba la entidad entera armada desde el DTO, que no trae {@code
   * status}: cada edición dejaba la sección con vigencia NULL.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/editMeta/{id}")
  public ResponseEntity<?> editMeta(
      @Valid @RequestBody Seccion seccion,
      BindingResult result,
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    if (result.hasErrors()) {
      return validar(result);
    }
    if (seccion.getMeta() == null || seccion.getMeta() < 0) {
      return ResponseEntity.badRequest().body(Map.of("meta", "La meta debe ser 0 o mayor"));
    }

    // con LN la meta sale del porcentaje y esto responde 400 (ver CatalogService.updateSection)
    catalogService.updateSection(new SectionChange(id, seccion.getMeta(), null, null, null));
    return ResponseEntity.status(HttpStatus.CREATED).body(seccionService.byId(id).get());
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
