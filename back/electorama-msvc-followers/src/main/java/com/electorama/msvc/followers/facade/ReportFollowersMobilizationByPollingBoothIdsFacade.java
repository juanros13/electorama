package com.electorama.msvc.followers.facade;

import com.electorama.msvc.followers.model.dto.ReportFollowersMarkByPollingBoothDto;
import com.electorama.msvc.followers.model.dto.ReportFollowersMobilzationDto;
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
public class ReportFollowersMobilizationByPollingBoothIdsFacade {
  @PersistenceContext EntityManager em;

  public List<ReportFollowersMarkByPollingBoothDto> reportFollowersMobilization(
      List<Long> pollingBoothIds) {
    List<ReportFollowersMarkByPollingBoothDto> result =
        new ArrayList<ReportFollowersMarkByPollingBoothDto>();
    StringBuilder queryBuilder = new StringBuilder();
    List<Object[]> objectRes = new ArrayList<>();
    String pollingBoothIdsString =
        pollingBoothIds.stream().map(Object::toString).collect(Collectors.joining(","));
    queryBuilder
        .append(
            "SELECT "
                + "follower_mark.polling_booth_id, "
                + "COUNT(follower_mark.id) filter (where follower_mark.mark_flag) as progress_mobilization, "
                + "COUNT(follower_mark.id) as goal_mobilization "
                + "FROM  follower_mark ")
        .append("WHERE follower_mark.polling_booth_id in (" + pollingBoothIdsString + ") ")
        .append("GROUP BY follower_mark.polling_booth_id ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        ReportFollowersMarkByPollingBoothDto edd = new ReportFollowersMarkByPollingBoothDto();
        ReportFollowersMobilzationDto report = new ReportFollowersMobilzationDto();

        edd.setPollingBoothId(obj[0] != null ? Integer.parseInt(obj[0].toString()) : 0);
        report.setProgress(obj[0] != null ? Integer.parseInt(obj[1].toString()) : 0);
        report.setGoal(obj[0] != null ? Integer.parseInt(obj[2].toString()) : 0);
        edd.setReport(report);
        result.add(edd);
      }
    } catch (Exception e) {
      System.err.println("Error al obtener las reportFollowersByReferent:" + e.getMessage());
    }
    return result;
  }
}
