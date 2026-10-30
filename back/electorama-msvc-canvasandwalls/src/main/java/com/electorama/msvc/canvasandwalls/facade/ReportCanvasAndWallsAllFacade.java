package com.electorama.msvc.canvasandwalls.facade;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallsAllDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportCanvasAndWallsAllFacade {

  @PersistenceContext EntityManager em;

  public List<ReportCanvasAndWallsAllDto> getReportCanvasAndWallsAll(
      String dateStart, String dateEnd, String typeRol, String authIdHeader) {
    List<ReportCanvasAndWallsAllDto> result = new ArrayList<ReportCanvasAndWallsAllDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT  "
            + "canvas_and_walls.reference_auth_id AS referente,  "
            + "canvas_and_walls.coordinate_zone_id AS coordinador_zona,  "
            + "canvas_and_walls.municipality_id AS municipio,  "
            + "canvas_and_walls.seccion_id AS seccion,  "
            + "canvas_and_walls.id AS canvas_id,  "
            + "'TABASCO' AS entidad,  "
            + "canvas_and_walls_proyects.\"name\" AS proyecto,  "
            + "canvas_and_walls_category.\"name\" AS categoria,  "
            + "DATE(canvas_and_walls.date_of_placement) AS fecha_puesta,  "
            + "DATE(canvas_and_walls.created) AS fecha_registro,  "
            + "canvas_and_walls.latitude AS latitud,  "
            + "canvas_and_walls.longitude AS longitud,  "
            + "canvas_and_walls.path_evidence_photo  "
            + "FROM  "
            + "canvas_and_walls  "
            + "LEFT JOIN canvas_and_walls_category ON canvas_and_walls_category.id = canvas_and_walls.canvas_and_wall_category_id  "
            + "LEFT JOIN canvas_and_walls_proyects ON canvas_and_walls_proyects.id = canvas_and_walls.canvas_and_wall_proyect_id  "
            + "WHERE canvas_and_walls.deleted IS NULL AND canvas_and_walls.created  BETWEEN "
            + dateStart
            + " AND "
            + dateEnd;

    log.info("Query: " + select);
    if (typeRol.equalsIgnoreCase("CORDINATOR_MUNICIPAL")) {
      log.info("Es CORDINATOR_MUNICIPAL...");
      select = select + " and canvas_and_walls.reference_auth_id ='" + authIdHeader + "'";
    } else if (typeRol.equalsIgnoreCase("CORDINATOR_ZONE")
        || typeRol.equalsIgnoreCase("CORDINATOR_ZONE_ALT")) {
      log.info("Es CORDINATOR_ZONE...");
      select = select + " and canvas_and_walls.coordinate_zone_id ='" + authIdHeader + "'";
    }
    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        ReportCanvasAndWallsAllDto edd = new ReportCanvasAndWallsAllDto();
        edd.setReferente(obj[0] != null ? obj[0].toString() : "");
        edd.setCoordinadorZona(obj[1] != null ? obj[1].toString() : "");
        edd.setMunicipio(obj[2] != null ? obj[2].toString() : "");
        edd.setSeccion(obj[3] != null ? obj[3].toString() : "");
        edd.setCanvasId(obj[4] != null ? obj[4].toString() : "");
        edd.setEntidad(obj[5] != null ? obj[5].toString() : "");
        edd.setProyecto(obj[6] != null ? obj[6].toString() : "");
        edd.setCategoria(obj[7] != null ? obj[7].toString() : "");
        edd.setFechaPuesta(obj[8] != null ? obj[8].toString() : "");
        edd.setFechaRegistro(obj[9] != null ? obj[9].toString() : "");
        edd.setLatitud(obj[10] != null ? obj[10].toString() : "");
        edd.setLongitud(obj[11] != null ? obj[11].toString() : "");
        edd.setPath(obj[12] != null ? obj[12].toString() : "");

        result.add(edd);
      }

    } catch (Exception e) {
      System.err.println(
          "Error al obtener las findReportVisitedByMunicipalCoordinator:" + e.getMessage());
    }
    return result;
  }
}
