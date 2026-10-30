package com.electorama.msvc.citizenissues.facade;

import com.electorama.msvc.citizenissues.model.dto.ReportIssueTypeDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportIssueTypesByDatesFacade {

  @PersistenceContext EntityManager em;

  public ReportIssueTypeDto getReporteIssues(
      Integer municipalityId, String start_date, String end_date) {

    String select =
        "SELECT   "
            + "COUNT(issues_details.id) FILTER (WHERE issues_types.name = 'FISICO' ) AS total_issues_physical,  "
            + "COUNT(issues_details.id) FILTER (WHERE issues_types.name = 'MEDIOS' ) AS total_issues_media,  "
            + "COUNT(issues_details.id) FILTER (WHERE issues_types.name = 'DIGITAL' ) AS total_issues_digital  "
            + "FROM issues  "
            + "LEFT JOIN issues_details ON issues_details.issue_id = issues.id  "
            + "LEFT JOIN issues_types ON issues_types.id = issues_details.issue_type_id  "
            + "WHERE true ";

    if (municipalityId > 0) {
      select = select + " AND issues.municipality_id = " + municipalityId;
    }
    if (end_date != null && !end_date.equals("")) {
      select =
          select
              + " AND issues_details.registration_date BETWEEN '"
              + start_date
              + "' AND '"
              + end_date
              + "'";
    }
    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    ReportIssueTypeDto edd = new ReportIssueTypeDto();

    try {
      Object obj = query.getSingleResult(); // Obtiene solo el primer resultado
      if (obj != null) {
        Object[] resultArray = (Object[]) obj;
        edd.setTotalIssuesPhysical(Integer.parseInt(resultArray[0].toString()));
        edd.setTotalIssuesMedia(Integer.parseInt(resultArray[1].toString()));
        edd.setTotalIssuesDigital(Integer.parseInt(resultArray[2].toString()));
      }

    } catch (Exception e) {
      System.err.println("Error al obtener el reporte:" + e.getMessage());
    }
    return edd;
  }
}
