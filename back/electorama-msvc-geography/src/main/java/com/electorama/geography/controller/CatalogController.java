package com.electorama.geography.controller;

import com.electorama.geography.model.dto.catalog.CatalogDistrict;
import com.electorama.geography.model.dto.catalog.CatalogMunicipality;
import com.electorama.geography.model.dto.catalog.CatalogPage;
import com.electorama.geography.model.dto.catalog.CatalogSection;
import com.electorama.geography.model.dto.catalog.CatalogSummary;
import com.electorama.geography.model.dto.catalog.GoalUpdateRequest;
import com.electorama.geography.model.dto.catalog.MunicipalityUpdateRequest;
import com.electorama.geography.model.dto.catalog.SectionUpdateRequest;
import com.electorama.geography.model.repository.CatalogQueryRepository.SectionChange;
import com.electorama.geography.model.repository.CatalogQueryRepository.SectionFilter;
import com.electorama.geography.service.CatalogService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * La administración del catálogo territorial: secciones, municipios y distritos.
 *
 * <p>Es para administradores; quien ocupa un puesto de la estructura sólo lee secciones y
 * municipios (el organigrama los usa para el nombre del municipio y la meta de cada sección). Los
 * endpoints viejos ({@code /seccion}, {@code /municipality}, {@code /federalDistrict}…) siguen
 * igual para los microservicios que los consumen por Feign.
 */
@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CatalogController {

  /** Lectura del catálogo que también necesita el organigrama del referente. */
  private static final String READ_WITH_REFERENT =
      "hasAnyRole('ADMIN', 'CORDINATOR_MUNICIPAL', 'CORDINATOR_MUNICIPAL_ALT', 'CORDINATOR_ZONE', 'CORDINATOR_ZONE_ALT', 'PROMOTER')";

  private final CatalogService catalogService;

  @GetMapping("/sections")
  @PreAuthorize(READ_WITH_REFERENT)
  public CatalogPage<CatalogSection> sections(
      @RequestParam(defaultValue = "27") Integer stateId,
      @RequestParam(required = false) Long municipalityId,
      @RequestParam(required = false) Integer federalDistrict,
      @RequestParam(required = false) Integer localDistrict,
      @RequestParam(required = false) Boolean status,
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    return catalogService.sections(
        new SectionFilter(stateId, municipalityId, federalDistrict, localDistrict, status, q),
        page,
        size);
  }

  /** Los totales de las tarjetas de arriba, con el mismo filtro que la lista. */
  @GetMapping("/sections/summary")
  public CatalogSummary sectionsSummary(
      @RequestParam(defaultValue = "27") Integer stateId,
      @RequestParam(required = false) Long municipalityId,
      @RequestParam(required = false) Integer federalDistrict,
      @RequestParam(required = false) Integer localDistrict,
      @RequestParam(required = false) Boolean status,
      @RequestParam(required = false) String q) {
    return catalogService.summarizeSections(
        new SectionFilter(stateId, municipalityId, federalDistrict, localDistrict, status, q));
  }

  @GetMapping("/sections/{id}")
  public CatalogSection section(@PathVariable Long id) {
    return catalogService.section(id);
  }

  /** El polígono de la sección (GeoJSON) para dibujarla en el mapa de captura. */
  @GetMapping(value = "/sections/{id}/geometry", produces = "application/geo+json")
  @PreAuthorize(READ_WITH_REFERENT)
  public String sectionGeometry(@PathVariable Long id) {
    return catalogService.sectionGeoJson(id);
  }

  /**
   * Si el punto cae dentro de la sección o a no más de {@code tolerance} metros de su borde (100
   * por omisión: el error del GPS). {@code inside} es null cuando la sección no tiene geometría. Lo
   * usa msvc-structure al capturar un convencido con ubicación.
   */
  @GetMapping("/sections/{id}/contains")
  @PreAuthorize(READ_WITH_REFERENT)
  public Map<String, Object> sectionContains(
      @PathVariable Long id,
      @RequestParam double lat,
      @RequestParam double lng,
      @RequestParam(defaultValue = "100") double tolerance) {
    Map<String, Object> out = new HashMap<>();
    out.put("sectionId", id);
    out.put("inside", catalogService.sectionCovers(id, lat, lng, Math.max(0, tolerance)));
    out.put("toleranceMeters", tolerance);
    return out;
  }

  @PatchMapping("/sections/{id}")
  public CatalogSection updateSection(
      @PathVariable Long id, @Valid @RequestBody SectionUpdateRequest request) {
    return catalogService.updateSection(
        new SectionChange(
            id, request.meta(), request.nominalList(), request.goalPercentage(), request.status()));
  }

  /** El % de meta de una campaña: lo aplica a todas las secciones de su estado o municipio. */
  @PatchMapping("/goal")
  public CatalogSummary applyGoal(@Valid @RequestBody GoalUpdateRequest request) {
    return catalogService.applyGoalPercentage(request);
  }

  @GetMapping("/municipalities")
  @PreAuthorize(READ_WITH_REFERENT)
  public List<CatalogMunicipality> municipalities(
      @RequestParam(defaultValue = "27") Integer stateId) {
    return catalogService.municipalities(stateId);
  }

  @PatchMapping("/municipalities/{id}")
  public ResponseEntity<Void> renameMunicipality(
      @PathVariable Long id, @Valid @RequestBody MunicipalityUpdateRequest request) {
    catalogService.renameMunicipality(id, request.name());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/federal-districts")
  public List<CatalogDistrict> federalDistricts(
      @RequestParam(defaultValue = "27") Integer stateId) {
    return catalogService.federalDistricts(stateId);
  }

  @GetMapping("/local-districts")
  public List<CatalogDistrict> localDistricts(@RequestParam(defaultValue = "27") Integer stateId) {
    return catalogService.localDistricts(stateId);
  }
}
