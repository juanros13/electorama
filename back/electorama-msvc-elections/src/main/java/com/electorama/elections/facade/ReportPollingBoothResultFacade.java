package com.electorama.elections.facade;

import com.electorama.elections.models.dto.ReportPollingBoothResultsByPollingBoothIdsDto;
import com.electorama.elections.models.dto.ReportProgressDto;
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
public class ReportPollingBoothResultFacade {

  @PersistenceContext EntityManager em;

  public List<ReportPollingBoothResultsByPollingBoothIdsDto> getReportResults(
      List<Long> pollingBoothIds) {
    List<ReportPollingBoothResultsByPollingBoothIdsDto> result =
        new ArrayList<ReportPollingBoothResultsByPollingBoothIdsDto>();
    StringBuilder queryBuilder = new StringBuilder();
    List<Object[]> objectRes = new ArrayList<>();
    String pollingBoothIdsString =
        pollingBoothIds.stream().map(Object::toString).collect(Collectors.joining(","));
    queryBuilder
        .append(
            "SELECT "
                + "polling_booths_election_association.polling_booth_id, "
                + "COUNT(polling_booths_election_association.id) AS goal, "
                + "COUNT(polling_booths_election_association.id) FILTER (WHERE polling_booths_election_association.is_synchronized) AS progress "
                + "FROM polling_booths_election_association ")
        .append(
            "WHERE polling_booths_election_association.polling_booth_id IN ("
                + pollingBoothIdsString
                + ") ")
        .append("GROUP BY polling_booths_election_association.polling_booth_id ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        ReportPollingBoothResultsByPollingBoothIdsDto edd =
            new ReportPollingBoothResultsByPollingBoothIdsDto();
        ReportProgressDto report = new ReportProgressDto();

        edd.setPollingBoothId(obj[0] != null ? Integer.parseInt(obj[0].toString()) : 0);
        report.setGoal((obj[1] != null ? Integer.parseInt(obj[1].toString()) : 0));
        report.setProgress((obj[2] != null ? Integer.parseInt(obj[2].toString()) : 0));
        edd.setReport(report);
        result.add(edd);
      }
    } catch (Exception e) {
      System.err.println("Error al obtener las reportFollowersByReferent:" + e.getMessage());
    }
    return result;
  }
}
