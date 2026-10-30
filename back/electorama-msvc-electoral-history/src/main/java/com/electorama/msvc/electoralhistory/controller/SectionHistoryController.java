package com.electorama.msvc.electoralhistory.controller;

import com.electorama.msvc.electoralhistory.dto.SectionHistory;
import com.electorama.msvc.electoralhistory.service.ElectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * El histórico de una sección: cómo votó en cada elección cargada. Lo usa quien trabaja la sección
 * (movilizador, promotor, referente) para ver la suya; el tablero y el análisis por territorio
 * siguen siendo sólo ADMIN. Son resultados públicos del INE y el IEPCT.
 */
@RestController
@RequestMapping("/v1/sections")
@RequiredArgsConstructor
@PreAuthorize(
    "hasAnyRole('ADMIN', 'CORDINATOR_MUNICIPAL', 'CORDINATOR_MUNICIPAL_ALT', 'CORDINATOR_ZONE',"
        + " 'CORDINATOR_ZONE_ALT', 'PROMOTER')")
public class SectionHistoryController {

  private final ElectionService elections;

  /**
   * @param section número de sección (único en el estado)
   * @param groupBy bloc (bloque del año, por omisión), candidacy o party
   */
  @GetMapping("/{section}/history")
  public SectionHistory history(
      @PathVariable int section, @RequestParam(defaultValue = "bloc") String groupBy) {
    return elections.sectionHistory(section, groupBy);
  }
}
