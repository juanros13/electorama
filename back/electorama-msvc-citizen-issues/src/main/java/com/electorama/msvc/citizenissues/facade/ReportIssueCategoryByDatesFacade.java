package com.electorama.msvc.citizenissues.facade;

import com.electorama.msvc.citizenissues.model.dto.ReportIssueCategoryDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportIssueCategoryByDatesFacade {

  @PersistenceContext EntityManager em;

  public List<ReportIssueCategoryDto> getReporteIssues(
      Integer municipalityId, String start_date, String end_date) {
    List<ReportIssueCategoryDto> result = new ArrayList<ReportIssueCategoryDto>();

    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT  "
            + "issues_categories.name as category,  "
            + "COALESCE(COUNT(*),0) AS total "
            + "FROM issues "
            + "LEFT JOIN issues_details ON issues_details.issue_id = issues.id "
            + "LEFT JOIN issues_status ON issues_status.id = issues_details.issue_status_id "
            + "LEFT JOIN issues_categories ON issues_categories.id = issues_details.issue_category_id "
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
    select = select + " GROUP BY issues_categories.name";

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        ReportIssueCategoryDto edd = new ReportIssueCategoryDto();
        edd.setCategory(obj[0].toString());
        edd.setTotal(Integer.parseInt(obj[1].toString()));
        result.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener el reporte:" + e.getMessage());
    }
    return result;
  }
}
