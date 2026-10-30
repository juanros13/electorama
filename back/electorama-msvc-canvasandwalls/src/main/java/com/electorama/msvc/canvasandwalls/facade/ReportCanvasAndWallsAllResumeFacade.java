package com.electorama.msvc.canvasandwalls.facade;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallsAllResumeDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallsByTimeDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportCanvasAndWallsAllResumeFacade {

  @PersistenceContext EntityManager em;

  public List<ReportCanvasAndWallsAllResumeDto> getReportCanvasAndWallsAllResume(
      //    String dateStart, String dateEnd, String typeRol, String authIdHeader
      ) {
    List<ReportCanvasAndWallsAllResumeDto> result =
        new ArrayList<ReportCanvasAndWallsAllResumeDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT id,canvas_and_walls_category.\"name\" as category, totals.total, goals.goal FROM  "
            + "canvas_and_walls_category "
            + "LEFT JOIN ( "
            + " SELECT "
            + "  canvas_and_walls.canvas_and_wall_category_id, "
            + "  COUNT(canvas_and_walls.id) as total "
            + "  FROM "
            + "  canvas_and_walls "
            + "  WHERE "
            + "  canvas_and_walls.reference_auth_id IS NOT NULL  "
            + "  AND canvas_and_walls.deleted is null  "
            + "  GROUP BY  "
            + "  canvas_and_walls.canvas_and_wall_category_id "
            + "   "
            + ") AS totals ON totals.canvas_and_wall_category_id = canvas_and_walls_category.id "
            + "LEFT JOIN ( "
            + " SELECT  "
            + "  canvas_and_walls_goals.canvas_and_wall_category_id, "
            + "  COUNT(canvas_and_walls_goals.id) as goal "
            + "  FROM canvas_and_walls_goals "
            + " GROUP BY canvas_and_walls_goals.canvas_and_wall_category_id "
            + " "
            + ")AS goals ON goals.canvas_and_wall_category_id = canvas_and_walls_category.id";

    //    log.info("Query: " + select);
    //    if (typeRol.equalsIgnoreCase("CORDINATOR_MUNICIPAL")) {
    //      log.info("Es CORDINATOR_MUNICIPAL...");
    //      select = select + " and canvas_and_walls.reference_auth_id ='" + authIdHeader + "'";
    //    } else if (typeRol.equalsIgnoreCase("CORDINATOR_ZONE")) {
    //      log.info("Es CORDINATOR_ZONE...");
    //      select = select + " and canvas_and_walls.coordinate_zone_id ='" + authIdHeader + "'";
    //    }
    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        ReportCanvasAndWallsAllResumeDto edd = new ReportCanvasAndWallsAllResumeDto();
        edd.setId(obj[0] != null ? Integer.parseInt(obj[0].toString()) : 0);
        edd.setCategory(obj[1] != null ? obj[1].toString() : "");
        edd.setTotal(obj[2] != null ? Integer.parseInt(obj[2].toString()) : 0);
        edd.setGoal(obj[3] != null ? Integer.parseInt(obj[3].toString()) : 0);

        result.add(edd);
      }

    } catch (Exception e) {
      System.err.println(
          "Error al obtener las findReportVisitedByMunicipalCoordinator:" + e.getMessage());
    }
    return result;
  }

  public List<ReportCanvasAndWallsByTimeDto> findAllVisitsByTime(
      String dateStart, String dateEnd, Integer category) {
    List<ReportCanvasAndWallsByTimeDto> result = new ArrayList<ReportCanvasAndWallsByTimeDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT"
            + " date_trunc('day', canvas_and_walls.created)::::date AS date_canvas, "
            + " COUNT(canvas_and_walls.id) as total_canvas "
            + " FROM "
            + " canvas_and_walls "
            + " WHERE "
            + " canvas_and_walls.reference_auth_id IS NOT NULL "
            + " AND canvas_and_walls.deleted is null "
            + " AND canvas_and_walls.canvas_and_wall_category_id = "
            + category
            + " AND canvas_and_walls.created  BETWEEN "
            + dateStart
            + " AND "
            + dateEnd
            + " GROUP BY date_trunc('day', canvas_and_walls.created) "
            + " ORDER BY date_trunc('day', canvas_and_walls.created) ";

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();

      for (Object[] obj : objectRes) {
        ReportCanvasAndWallsByTimeDto edd = new ReportCanvasAndWallsByTimeDto();
        edd.setDateCanvas(obj[0].toString());
        edd.setTotalCanvas(obj[1].toString());

        result.add(edd);
      }

    } catch (Exception e) {
      System.err.println(
          "Error al obtener las findReportVisitedByMunicipalCoordinator:" + e.getMessage());
    }
    return result;
  }
}
