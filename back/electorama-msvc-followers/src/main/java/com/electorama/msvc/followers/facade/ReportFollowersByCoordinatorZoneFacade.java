package com.electorama.msvc.followers.facade;

import com.electorama.msvc.followers.model.dto.ReportFollowersByCoordinatorZoneDto;
import com.electorama.msvc.followers.model.rest.StructureCountResponse;
import com.electorama.msvc.followers.service.rest.StructureRestClient;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Convencidos, promotores y meta por coordinador de zona y seccion.
 *
 * <p>De los tres reportes, este era el unico que unia bien —por {@code promoter_auth_id}— y el
 * unico cuyas ramas de rol nombraban las columnas correctas. Aun asi se mueve al modelo nuevo por
 * dos razones: la jerarquia sale del organigrama en vez de cruzarse por authId, y el convencido
 * cuyo puntero de promotor estaba roto deja de desaparecer del reporte (el join viejo lo perdia).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReportFollowersByCoordinatorZoneFacade {

  private final StructureRestClient structure;

  public List<ReportFollowersByCoordinatorZoneDto> findReportFollowersByCoordinatorZone(
      String dateStart, String dateEnd, String typeRol, String authIdHeader) {

    List<StructureCountResponse> rows =
        structure.reportFollowers(
            "COORDINATOR",
            ReportScope.from(dateStart),
            ReportScope.to(dateEnd),
            ReportScope.scope(typeRol, authIdHeader));

    List<ReportFollowersByCoordinatorZoneDto> result = new ArrayList<>(rows.size());
    long id = 1;
    for (StructureCountResponse row : rows) {
      if (row.getCoordinatorZoneAuthId() == null) {
        continue;
      }
      ReportFollowersByCoordinatorZoneDto dto = new ReportFollowersByCoordinatorZoneDto();
      dto.setId(id++);
      dto.setMunicipalityId(
          row.getMunicipalityId() == null ? null : row.getMunicipalityId().intValue());
      dto.setCordinatorZoneAuthId(row.getCoordinatorZoneAuthId());
      dto.setReferentAuthId(row.getReferentAuthId());
      dto.setSeccionId(row.getSectionId() == null ? null : row.getSectionId().intValue());
      dto.setNumFollowers(row.getFollowers() == null ? 0 : row.getFollowers().intValue());
      dto.setNumPromoters(row.getPromoters() == null ? 0 : row.getPromoters().intValue());
      dto.setGoal(row.getGoal() == null ? 0 : row.getGoal().intValue());
      result.add(dto);
    }
    return result;
  }
}
