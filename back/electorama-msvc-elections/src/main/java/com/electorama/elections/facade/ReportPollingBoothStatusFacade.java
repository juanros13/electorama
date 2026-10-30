package com.electorama.elections.facade;

import com.electorama.elections.models.dto.PollingBoothStatusDto;
import com.electorama.elections.models.dto.ReportPollingBoothStatusOpenByPollingBoothIdsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportPollingBoothStatusFacade {

  @PersistenceContext EntityManager em;

  public List<ReportPollingBoothStatusOpenByPollingBoothIdsDto> getReportStatusOpen(
      List<Long> pollingBoothIds) {
    List<ReportPollingBoothStatusOpenByPollingBoothIdsDto> result =
        new ArrayList<ReportPollingBoothStatusOpenByPollingBoothIdsDto>();
    StringBuilder queryBuilder = new StringBuilder();
    List<Object[]> objectRes = new ArrayList<>();
    String pollingBoothIdsString =
        pollingBoothIds.stream().map(Object::toString).collect(Collectors.joining(","));
    queryBuilder
        .append(
            "SELECT "
                + "polling_booths.id, "
                + "polling_booths_status.id as id_status, "
                + "polling_booths_status.name, "
                + "polling_booths.date_status_open ,"
                + "polling_booths.date_closed "
                + "FROM  polling_booths "
                + "LEFT JOIN polling_booths_status ON polling_booths_status.id = polling_booths.polling_booth_status_id "
                + "            ")
        .append("WHERE polling_booths.id in (" + pollingBoothIdsString + ") ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        ReportPollingBoothStatusOpenByPollingBoothIdsDto edd =
            new ReportPollingBoothStatusOpenByPollingBoothIdsDto();
        PollingBoothStatusDto report = new PollingBoothStatusDto();

        edd.setPollingBoothId(obj[0] != null ? Integer.parseInt(obj[0].toString()) : 0);
        edd.setOpenDate(obj[3] != null ? obj[3].toString() : "");
        edd.setCloseDate(obj[4] != null ? obj[4].toString() : "");
        report.setId((long) (obj[1] != null ? Integer.parseInt(obj[1].toString()) : 0));
        report.setName(obj[2] != null ? obj[2].toString() : "");
        edd.setStatus(report);
        result.add(edd);
      }
    } catch (Exception e) {
      System.err.println("Error al obtener las reportFollowersByReferent:" + e.getMessage());
    }
    return result;
  }
}
