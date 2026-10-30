package com.electorama.msvc.structure.controller;

import com.electorama.msvc.structure.model.dto.RotateRequest;
import com.electorama.msvc.structure.model.projection.AssignmentHistory;
import com.electorama.msvc.structure.model.projection.DailyActivity;
import com.electorama.msvc.structure.model.projection.MunicipalityPosition;
import com.electorama.msvc.structure.model.projection.MunicipalitySummary;
import com.electorama.msvc.structure.model.projection.PositionSummary;
import com.electorama.msvc.structure.model.projection.SectionGoal;
import com.electorama.msvc.structure.model.projection.SectionOverview;
import com.electorama.msvc.structure.model.projection.TerritoryScope;
import com.electorama.msvc.structure.security.BranchGuard;
import com.electorama.msvc.structure.service.AssignmentService;
import com.electorama.msvc.structure.service.PositionService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** El organigrama: puestos, su gente y su ámbito. */
@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {

  private final PositionService positionService;
  private final AssignmentService assignmentService;
  private final BranchGuard branch;

  @GetMapping("/{id}")
  @PreAuthorize("@branch.canRead(#id)")
  public PositionSummary get(@PathVariable Long id) {
    return positionService.get(id);
  }

  @GetMapping("/{id}/children")
  @PreAuthorize("@branch.canRead(#id)")
  public List<PositionSummary> children(@PathVariable Long id) {
    return positionService.children(id);
  }

  /** La raíz del árbol de una estructura: sus referentes. */
  /** Los referentes de la estructura; un referente sólo ve el suyo. */
  @GetMapping("/roots")
  public List<PositionSummary> roots(@RequestParam Long structureId) {
    return positionService.roots(structureId);
  }

  /**
   * Si la cuenta es titular de un puesto de la rama de quien llama (siempre true para ADMIN). Lo
   * consulta msvc-users antes de enseñarle una cuenta a un referente o dejarle cambiar su
   * contraseña.
   */
  @GetMapping("/holders/{authId}/in-branch")
  public Map<String, Object> holderInBranch(@PathVariable String authId) {
    return Map.of("authId", authId, "inBranch", branch.holderInBranch(authId));
  }

  /** Los puestos vigentes sobre una sección del catálogo, con su titular y su meta. */
  @GetMapping("/by-section/{sectionGeoId}")
  public List<PositionSummary> bySection(
      @PathVariable Integer sectionGeoId, @RequestParam(required = false) Long campaignId) {
    return positionService.bySection(sectionGeoId, campaignId);
  }

  /**
   * La meta repartida entre coordinadores, por sección. La pantalla de catálogos la pone junto al
   * techo de la sección para avisar cuando no cuadran.
   */
  @GetMapping("/section-goals")
  public List<SectionGoal> sectionGoals(
      @RequestParam List<Integer> sectionGeoIds, @RequestParam(required = false) Long campaignId) {
    return positionService.sectionGoals(sectionGeoIds, campaignId);
  }

  /**
   * Todas las secciones con estructura principal en una llamada: meta repartida, coordinadores (con
   * nombre), promotores y convencidos. El análisis electoral la cruza con las secciones a vigilar.
   */
  @GetMapping("/section-overview")
  public List<SectionOverview> sectionOverview(@RequestParam(required = false) Long campaignId) {
    return positionService.sectionOverview(campaignId);
  }

  /** La estructura principal por municipio: gente, meta repartida y convencidos (tablero). */
  @GetMapping("/municipality-summary")
  public List<MunicipalitySummary> municipalitySummary(
      @RequestParam(required = false) Long campaignId) {
    return positionService.municipalitySummary(campaignId);
  }

  /**
   * El desglose de un municipio para el tablero: sus puestos de los tres niveles con titular y
   * convencidos directos. Sólo ADMIN, como el tablero.
   */
  @GetMapping("/municipality/{municipalityGeoId}/breakdown")
  @PreAuthorize("hasRole('ADMIN')")
  public List<MunicipalityPosition> municipalityBreakdown(
      @PathVariable Integer municipalityGeoId, @RequestParam(required = false) Long campaignId) {
    return positionService.municipalityPositions(municipalityGeoId, campaignId);
  }

  /** El avance: convencidos de todo el subárbol contra la meta del puesto. */
  @GetMapping("/{id}/progress")
  @PreAuthorize("@branch.canRead(#id)")
  public Map<String, Object> progress(@PathVariable Long id) {
    PositionSummary position = positionService.get(id);
    long total = positionService.subtreeMemberships(id);
    return Map.of(
        "positionId", id,
        "goal", position.getGoal() == null ? 0 : position.getGoal(),
        "memberships", total);
  }

  /**
   * La actividad por día del subárbol: convencidos capturados cada día, con los días vacíos en
   * cero. Sin {@code from}/{@code to}, del primer al último día con captura.
   */
  @GetMapping("/{id}/activity")
  @PreAuthorize("@branch.canRead(#id)")
  public List<DailyActivity> activity(
      @PathVariable Long id,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return positionService.activity(id, from, to);
  }

  /** El ámbito territorial de una cuenta: las secciones que cubre y desde qué puesto. */
  @GetMapping("/scope/{authId}")
  public List<TerritoryScope> scope(
      @PathVariable String authId, @RequestParam(required = false) Long campaignId) {
    return positionService.scopeOf(authId, campaignId);
  }

  @GetMapping("/{id}/assignments")
  @PreAuthorize("@branch.canRead(#id)")
  public List<AssignmentHistory> assignments(@PathVariable Long id) {
    return assignmentService.history(id);
  }

  /**
   * Rota al titular. Devuelve los convencidos del subárbol después de rotar, que tienen que ser los
   * mismos que antes: es la propiedad que justifica todo el modelo.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{id}/rotate")
  public Map<String, Object> rotate(@PathVariable Long id, @RequestBody RotateRequest request) {
    long memberships = assignmentService.rotate(id, request);
    return Map.of("positionId", id, "memberships", memberships);
  }

  /** Deja el puesto sin titular: ADMIN, o el referente en un puesto de su rama (no el suyo). */
  @PreAuthorize("@branch.canVacate(#id)")
  @DeleteMapping("/{id}/holder")
  public ResponseEntity<Void> vacate(
      @PathVariable Long id, @RequestParam(required = false) String reason) {
    assignmentService.vacate(id, reason);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/goal")
  public PositionSummary updateGoal(
      @PathVariable Long id,
      @RequestParam(required = false) Integer goal,
      @RequestParam(required = false) Integer childLimit) {
    return positionService.updateGoal(id, goal, childLimit);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> close(@PathVariable Long id) {
    positionService.close(id);
    return ResponseEntity.noContent().build();
  }
}
