package com.electorama.elections.facade;

import com.electorama.elections.models.dto.ResponseMunicipalityReportDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportMunicipalityFacade {

  @PersistenceContext EntityManager em;

  public List<ResponseMunicipalityReportDto> getReportMunicipality() {
    List<ResponseMunicipalityReportDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "SELECT "
            + "municipality_id, "
            + "municipality as municipality_name, "
            + "count(polling_booths.id) filter (where polling_booths_status.name ilike 'Abierta%') as total_polling_booths_opened, "
            + "count(polling_booths.id) filter (where polling_booths_status.name ilike 'Cerrada%') as total_polling_booths_closed, "
            + "count(*) AS total "
            + "FROM polling_booths "
            + "LEFT JOIN polling_booths_status ON polling_booths_status.id = polling_booths.polling_booth_status_id "
            + "GROUP BY municipality_id, municipality");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      // Aplicar paginación
      objectRes = query.getResultList();

      for (Object[] obj : objectRes) {
        ResponseMunicipalityReportDto edd = new ResponseMunicipalityReportDto();
        edd.setMunicipalityId(Long.parseLong(obj[0].toString()));
        edd.setMunicipalityName(obj[1] != null ? obj[1].toString() : "");
        edd.setTotalPollingBoothsOpened(Integer.parseInt(obj[2].toString()));
        edd.setTotalPollingBoothsClosed(Integer.parseInt(obj[3].toString()));
        edd.setTotal(Integer.parseInt(obj[4].toString()));

        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByCordinatorAuthId:" + e.getMessage());
    }
    return resultList;
  }
}
