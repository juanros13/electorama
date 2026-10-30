package com.electorama.msvc.followers.facade;

import com.electorama.msvc.followers.model.dto.ReportFollowersByMunicipalityDto;
import com.electorama.msvc.followers.model.rest.StructureCountResponse;
import com.electorama.msvc.followers.service.rest.StructureRestClient;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Convencidos y promotores por municipio.
 *
 * <p>La consulta anterior estaba rota de dos maneras y nadie lo veia. Agrupaba uniendo {@code
 * promoters.follower_id = followers.id}, y ese puntero resuelve en 86 de sus 27,680 filas: medido
 * contra las tablas congeladas, el reporte contaba <b>86 convencidos de 1,067,484</b>. Y en la rama
 * de rol nombraba {@code promoters.reference_auth_id}, columna que no existe, asi que para un
 * coordinador municipal o de zona la consulta fallaba y el {@code catch} devolvia lista vacia.
 *
 * <p>Ahora lo resuelve msvc-structure por el organigrama. El mismo rango de fechas devuelve
 * 1,057,007 convencidos en 17 municipios (mas una fila sin municipio: el puesto sin ubicar, que el
 * modelo deja visible en vez de esconderlo).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReportFollowersByMunicipalityFacade {

  private final StructureRestClient structure;

  public List<ReportFollowersByMunicipalityDto> reportFollowersByMunicipality(
      String dateStart, String dateEnd, String typeRol, String authIdHeader) {

    List<StructureCountResponse> rows =
        structure.reportFollowers(
            "MUNICIPALITY",
            ReportScope.from(dateStart),
            ReportScope.to(dateEnd),
            ReportScope.scope(typeRol, authIdHeader));

    List<ReportFollowersByMunicipalityDto> result = new ArrayList<>(rows.size());
    long id = 1;
    for (StructureCountResponse row : rows) {
      if (row.getMunicipalityId() == null) {
        continue;
      }
      ReportFollowersByMunicipalityDto dto = new ReportFollowersByMunicipalityDto();
      dto.setId(id++);
      dto.setMunicipalityId(row.getMunicipalityId().intValue());
      dto.setNumFollowers(row.getFollowers() == null ? 0 : row.getFollowers().intValue());
      dto.setNumPromoters(row.getPromoters() == null ? 0 : row.getPromoters().intValue());
      result.add(dto);
    }
    return result;
  }
}
