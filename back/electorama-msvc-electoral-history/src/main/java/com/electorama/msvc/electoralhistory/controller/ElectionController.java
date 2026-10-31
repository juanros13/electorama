package com.electorama.msvc.electoralhistory.controller;

import com.electorama.msvc.electoralhistory.dto.ElectionSummary;
import com.electorama.msvc.electoralhistory.dto.SectionReport;
import com.electorama.msvc.electoralhistory.dto.SectionTotals;
import com.electorama.msvc.electoralhistory.dto.TerritoryLevel;
import com.electorama.msvc.electoralhistory.dto.TerritoryReport;
import com.electorama.msvc.electoralhistory.service.ElectionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resultados oficiales por elección. Por el gateway: {@code /api/electoral-history/v1/...}.
 *
 * <p>Todo es de un estado ({@code stateId}, clave INE; Tabasco si no se dice): de una elección
 * federal sólo sus casillas, y las locales sólo existen para los estados cargados.
 *
 * <p>Sólo ADMIN: es análisis de campaña, aunque los datos sean públicos.
 */
@RestController
@RequestMapping("/v1/elections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ElectionController {

  private final ElectionService elections;

  /** Las elecciones del estado, con su fuente y fecha de corte (para los filtros del tablero). */
  @GetMapping
  public List<ElectionSummary> elections(@RequestParam(defaultValue = "27") int stateId) {
    return elections.elections(stateId);
  }

  /**
   * Una elección por territorio, más el total estatal.
   *
   * @param office PRESIDENT, SENATOR, FEDERAL_DEPUTY, GOVERNOR, LOCAL_DEPUTY o MAYOR
   * @param level MUNICIPALITY, LOCAL_DISTRICT o FEDERAL_DISTRICT
   * @param groupBy candidacy (la coalición real), party (un solo emblema) o bloc (bloque del año)
   */
  @GetMapping("/{year}/{office}/territories")
  public TerritoryReport byTerritory(
      @PathVariable int year,
      @PathVariable String office,
      @RequestParam(defaultValue = "MUNICIPALITY") TerritoryLevel level,
      @RequestParam(defaultValue = "candidacy") String groupBy,
      @RequestParam(defaultValue = "27") int stateId) {
    return elections.byTerritory(stateId, year, office, level, groupBy);
  }

  /** Todas las secciones del estado, con sus votos por bloque (para cruzar con la estructura). */
  @GetMapping("/{year}/{office}/sections")
  public List<SectionTotals> allSections(
      @PathVariable int year,
      @PathVariable String office,
      @RequestParam(defaultValue = "27") int stateId) {
    return elections.allSections(stateId, year, office);
  }

  /** Las secciones de un territorio (clave de municipio o número de distrito, en el estado). */
  @GetMapping("/{year}/{office}/territories/{territory}/sections")
  public SectionReport bySection(
      @PathVariable int year,
      @PathVariable String office,
      @PathVariable int territory,
      @RequestParam(defaultValue = "MUNICIPALITY") TerritoryLevel level,
      @RequestParam(defaultValue = "candidacy") String groupBy,
      @RequestParam(defaultValue = "27") int stateId) {
    return elections.bySection(stateId, year, office, level, territory, groupBy);
  }
}
