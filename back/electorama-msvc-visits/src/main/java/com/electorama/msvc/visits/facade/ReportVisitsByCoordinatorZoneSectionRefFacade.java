package com.electorama.msvc.visits.facade;

import com.electorama.msvc.visits.model.dto.ReportVisitsByCoordinatorZoneSectionRefDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportVisitsByCoordinatorZoneSectionRefFacade {

  @PersistenceContext EntityManager em;

  public List<ReportVisitsByCoordinatorZoneSectionRefDto>
      findReportVisitsByCoordinatorZoneSectionRef(
          String dateStart,
          String dateEnd,
          Integer municipalityId,
          String typeRol,
          String authIdHeader) {
    List<ReportVisitsByCoordinatorZoneSectionRefDto> result =
        new ArrayList<ReportVisitsByCoordinatorZoneSectionRefDto>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder
        .append("SELECT ")
        .append("row_number() over() as id, ")
        .append("visited.municipality_id, ")
        .append("visited.reference_auth_id, ")
        .append("visited.seccion_id, ")
        .append("visited.coordinate_zone_id, ")
        .append(
            "count(visited_people.id) filter (where visited_people.visit_number >= 1) as primera_visita, ")
        .append(
            "count(visited_people.id) filter (where visited_people.visit_number >= 2 ) as segunda_visita, ")
        .append(
            "count(visited_people.id) filter (where visited_people.visit_number = 3 ) as tercera_visita, ")
        .append(
            "count(visited_people.id) filter (where visited_people.open_the_door = 't') as visitas_abrieron_la_puerta,")
        .append(
            "count(visited_people.id) filter (where visited_people.open_the_door = 'f') as visitas_no_abrieron_la_puerta ")
        .append("from ")
        .append("visited ")
        .append("LEFT JOIN visited_people on visited_people.visited_id = visited.id ")
        .append("WHERE ")
        .append(
            "visited.coordinate_zone_id IS NOT NULL and visited.deleted is null and visited_people.deleted is null ")
        .append(
            "and visited.reference_auth_id is not null AND visited.visit_date BETWEEN "
                + dateStart
                + " AND "
                + dateEnd);

    if (typeRol.equalsIgnoreCase("CORDINATOR_MUNICIPAL")) {
      log.info("Es CORDINATOR_MUNICIPAL...");
      queryBuilder.append("and visited.reference_auth_id ='" + authIdHeader + "'");
    } else if (typeRol.equalsIgnoreCase("CORDINATOR_ZONE")) {
      log.info("Es CORDINATOR_ZONE...");
      queryBuilder.append("and visited.coordinate_zone_id ='" + authIdHeader + "'");
    }

    if (municipalityId > 0) {
      queryBuilder.append(" and visited.municipality_id = " + municipalityId);
    }

    queryBuilder
        .append(" GROUP BY ")
        .append("visited.reference_auth_id, ")
        .append("visited.seccion_id, ")
        .append("visited.coordinate_zone_id, ")
        .append("visited.municipality_id");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();

      for (Object[] obj : objectRes) {
        ReportVisitsByCoordinatorZoneSectionRefDto edd =
            new ReportVisitsByCoordinatorZoneSectionRefDto();
        edd.setId(Long.parseLong(obj[0].toString()));
        edd.setMunicipalityId(Integer.parseInt(obj[1].toString()));
        edd.setReferenceAuthId(obj[2].toString());
        edd.setSeccionId(Integer.parseInt(obj[3].toString()));
        edd.setCoordinateZoneId(obj[4].toString());
        edd.setPrimeraVisita(Integer.parseInt(obj[5].toString()));
        edd.setSegundaVisita(Integer.parseInt(obj[6].toString()));
        edd.setTerceraVisita(Integer.parseInt(obj[7].toString()));
        edd.setVisitasAbrieronLaPuerta(Integer.parseInt(obj[8] != null ? obj[8].toString() : "0"));
        edd.setVisitasNoAbrieronLaPuerta(
            Integer.parseInt(obj[9] != null ? obj[9].toString() : "0"));
        result.add(edd);
      }

    } catch (Exception e) {
      System.err.println(
          "Error al obtener las findReportVisitsByCoordinatorZoneSectionRef:" + e.getMessage());
    }
    return result;
  }
}
