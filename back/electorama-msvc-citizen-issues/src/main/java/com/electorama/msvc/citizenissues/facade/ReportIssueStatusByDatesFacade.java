package com.electorama.msvc.citizenissues.facade;

import com.electorama.msvc.citizenissues.model.dto.ReportIssueStatusDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportIssueStatusByDatesFacade {

  @PersistenceContext EntityManager em;

  public ReportIssueStatusDto getReporteIssues(
      Integer municipalityId, String start_date, String end_date) {

    String select =
        "SELECT "
            + "COUNT(issues_details.id) FILTER (WHERE issues_status.name = 'EN ESPERA' AND issues_details.deleted_date IS NULL) AS total_issues_on_hold, "
            + "COUNT(issues_details.id) FILTER (WHERE issues_status.name = 'EN PROCESO' AND issues_details.deleted_date IS NULL) AS total_issues_in_progress, "
            + "COUNT(issues_details.id) FILTER (WHERE issues_status.name = 'ATENDIDO' AND issues_details.deleted_date IS NULL) AS total_issues_completed, "
            + "COUNT(issues_details.id) FILTER (WHERE issues_details.deleted_date IS NOT NULL) AS total_issues_deleted, "
            + "COUNT(issues_details.id) FILTER (WHERE issues_details.urgent AND issues_details.deleted_date IS NULL AND issues_status.name != 'ATENDIDO') AS total_issues_urgent "
            + "FROM issues "
            + "LEFT JOIN issues_details ON issues_details.issue_id = issues.id "
            + "LEFT JOIN issues_status ON issues_status.id = issues_details.issue_status_id "
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
    ReportIssueStatusDto edd = new ReportIssueStatusDto();

    try {
      Object obj = query.getSingleResult(); // Obtiene solo el primer resultado
      if (obj != null) {
        Object[] resultArray = (Object[]) obj;
        edd.setTotalIssuesOnHold(Integer.parseInt(resultArray[0].toString()));
        edd.setTotalIssuesInProcess(Integer.parseInt(resultArray[1].toString()));
        edd.setTotalIssuesCompleted(Integer.parseInt(resultArray[2].toString()));
        edd.setTotalIssuesDeleted(Integer.parseInt(resultArray[3].toString()));
        edd.setTotalIssuesUrgent(Integer.parseInt(resultArray[4].toString()));
      }

    } catch (Exception e) {
      System.err.println("Error al obtener el reporte:" + e.getMessage());
    }
    return edd;
  }
}
