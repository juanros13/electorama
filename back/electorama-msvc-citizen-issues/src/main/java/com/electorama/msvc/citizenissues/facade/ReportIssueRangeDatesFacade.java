package com.electorama.msvc.citizenissues.facade;

import com.electorama.msvc.citizenissues.model.dto.ReportIssueRangeDateDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportIssueRangeDatesFacade {

  @PersistenceContext EntityManager em;

  public List<ReportIssueRangeDateDto> getReporteIssues(
      Integer municipalityId, String start_date, String end_date) {
    List<ReportIssueRangeDateDto> result = new ArrayList<ReportIssueRangeDateDto>();

    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "WITH date_range AS (  "
            + "  SELECT generate_series(  "
            + "    '"
            + start_date
            + "'::::DATE,   "
            + "    '"
            + end_date
            + "'::::DATE,  "
            + "    '1 day'::::INTERVAL  "
            + "  ) AS date  "
            + ")  "
            + "SELECT  "
            + "  TO_CHAR(date_range.date, 'YYYY-MM-DD') AS date,  "
            + "  COALESCE(COUNT(issuedet.registration_date), 0) AS total  "
            + "FROM date_range  "
            + "LEFT JOIN   "
            + "  (  "
            + "  SELECT issues_details.registration_date FROM issues  "
            + "  LEFT JOIN issues_details ON issues_details.issue_id = issues.id  ";
    if (municipalityId > 0) {
      select = select + " WHERE issues.municipality_id = " + municipalityId;
    }
    select =
        select
            + "  ) as issuedet  "
            + "ON date_range.date = issuedet.registration_date  "
            + "GROUP BY date_range.date  "
            + "ORDER BY date_range.date;";

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        ReportIssueRangeDateDto edd = new ReportIssueRangeDateDto();
        edd.setDate(obj[0].toString());
        edd.setTotal(Integer.parseInt(obj[1].toString()));
        result.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener el reporte:" + e.getMessage());
    }
    return result;
  }
}
