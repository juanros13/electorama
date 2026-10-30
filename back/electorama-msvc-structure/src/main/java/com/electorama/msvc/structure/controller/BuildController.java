package com.electorama.msvc.structure.controller;

import com.electorama.msvc.structure.model.dto.BuildRequests.Appoint;
import com.electorama.msvc.structure.model.dto.BuildRequests.Appointed;
import com.electorama.msvc.structure.model.dto.BuildRequests.CreatePosition;
import com.electorama.msvc.structure.model.dto.BuildRequests.LinkPlan;
import com.electorama.msvc.structure.model.dto.BuildRequests.SectionSeat;
import com.electorama.msvc.structure.model.dto.BuildRequests.Vacancies;
import com.electorama.msvc.structure.model.dto.BuildRequests.VacancyResult;
import com.electorama.msvc.structure.model.projection.PositionSummary;
import com.electorama.msvc.structure.service.BuildService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Armar la estructura: crear puestos, generar vacantes y nombrar titulares, sólo en una campaña que
 * no esté cerrada (la base también lo impide).
 *
 * <p>Crear puestos y vacantes es sólo ADMIN. Nombrar titular también lo puede hacer el referente en
 * los puestos de su rama ({@link com.electorama.msvc.structure.security.BranchGuard}).
 */
@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class BuildController {

  private final BuildService build;

  /** Un referente, un promotor (coordinador de zona) o un movilizador (promotor). */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PositionSummary> create(@RequestBody CreatePosition request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(build.create(request));
  }

  /**
   * Las vacantes que pide la meta: bajo un referente, promotores por sección (1 por cada 200); bajo
   * un promotor, movilizadores (1 por cada 20 de su meta). Sólo crea lo que falta.
   */
  @PostMapping("/{id}/vacancies")
  @PreAuthorize("hasRole('ADMIN')")
  public List<VacancyResult> vacancies(
      @PathVariable Long id, @RequestBody(required = false) Vacancies request) {
    return build.vacancies(id, request == null ? null : request.sections());
  }

  /**
   * Nombra al titular; con {@code account.mode = CREATE} le crea su cuenta con el rol del nivel.
   */
  /**
   * Qué pasaría al ligar la cuenta al puesto: el rol que recibe y, si ya ocupa otros puestos en la
   * misma rama, cuáles quedan vacantes y qué roles pierde. El front lo enseña antes de confirmar.
   */
  @GetMapping("/{id}/link-preview")
  @PreAuthorize("@branch.canAppoint(#id)")
  public LinkPlan linkPreview(@PathVariable Long id, @RequestParam String authId) {
    return build.linkPlan(id, authId);
  }

  /** Los demás puestos vacantes de promotor de la sección: los que tomaría «toda la sección». */
  @GetMapping("/{id}/section-vacancies")
  @PreAuthorize("@branch.canAppoint(#id)")
  public List<SectionSeat> sectionVacancies(@PathVariable Long id) {
    return build.sectionVacancies(id);
  }

  @PostMapping("/{id}/appoint")
  @PreAuthorize("@branch.canAppoint(#id)")
  public Appointed appoint(@PathVariable Long id, @RequestBody Appoint request) {
    return build.appoint(id, request);
  }
}
