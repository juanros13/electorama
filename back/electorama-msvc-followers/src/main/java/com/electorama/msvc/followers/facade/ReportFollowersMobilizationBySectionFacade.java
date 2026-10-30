package com.electorama.msvc.followers.facade;

import com.electorama.msvc.followers.model.dto.ReportFollowersMobilzationDto;
import com.electorama.msvc.followers.service.rest.StructureRestClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Movilizacion de una seccion: convencidos con marca de voto contra el total.
 *
 * <p>Lo que llega del front es el NUMERO impreso de la seccion, no el id del catalogo: la consulta
 * anterior lo comparaba contra {@code followers.section_number}. Se conserva ese contrato, pero
 * ahora el numero se resuelve contra el codigo del territorio, que sale del catalogo —la copia
 * vieja estaba equivocada en 840 de las parejas medidas.
 *
 * <p>Tambien se fue un filtro que sobraba: el SQL viejo exigia {@code residence_municipality_id IS
 * NOT NULL AND promoter_auth_id IS NOT NULL}, asi que los convencidos cuyo puntero de promotor
 * estaba roto no contaban ni para la meta ni para el avance. Ahora cuentan los de la seccion, que
 * es lo que el reporte dice medir.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReportFollowersMobilizationBySectionFacade {

  private final StructureRestClient structure;

  public ReportFollowersMobilzationDto reportFollowersMobilization(Long sectionNumber) {
    ReportFollowersMobilzationDto result = new ReportFollowersMobilzationDto();

    Map<String, Long> counts =
        structure.reportMobilization(sectionNumber == null ? null : sectionNumber.intValue());

    result.setProgress(counts.getOrDefault("progress", 0L).intValue());
    result.setGoal(counts.getOrDefault("goal", 0L).intValue());
    return result;
  }
}
