package com.electorama.msvc.followers.facade;

import com.electorama.msvc.followers.model.dto.ReportFollowersByReferentDto;
import com.electorama.msvc.followers.model.rest.StructureCountResponse;
import com.electorama.msvc.followers.service.rest.StructureRestClient;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Convencidos y promotores por referente.
 *
 * <p>Tenia los dos mismos defectos que el reporte por municipio: el join por {@code
 * promoters.follower_id} y la columna inexistente en la rama de rol. Contaba 86 convencidos de
 * 1,067,484 y quedaba en blanco para los dos roles de coordinador. Ahora el referente sale del
 * organigrama: es el titular del puesto abuelo del promotor.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReportFollowersByReferentFacade {

  private final StructureRestClient structure;

  public List<ReportFollowersByReferentDto> reportFollowersByReferent(
      String dateStart, String dateEnd, String typeRol, String authIdHeader) {

    List<StructureCountResponse> rows =
        structure.reportFollowers(
            "REFERENT",
            ReportScope.from(dateStart),
            ReportScope.to(dateEnd),
            ReportScope.scope(typeRol, authIdHeader));

    List<ReportFollowersByReferentDto> result = new ArrayList<>(rows.size());
    long id = 1;
    for (StructureCountResponse row : rows) {
      if (row.getReferentAuthId() == null) {
        continue;
      }
      ReportFollowersByReferentDto dto = new ReportFollowersByReferentDto();
      dto.setId(id++);
      dto.setMunicipalityId(
          row.getMunicipalityId() == null ? null : row.getMunicipalityId().intValue());
      dto.setReferentAuthId(row.getReferentAuthId());
      dto.setNumFollowers(row.getFollowers() == null ? 0 : row.getFollowers().intValue());
      dto.setNumPromoters(row.getPromoters() == null ? 0 : row.getPromoters().intValue());
      result.add(dto);
    }
    return result;
  }
}
