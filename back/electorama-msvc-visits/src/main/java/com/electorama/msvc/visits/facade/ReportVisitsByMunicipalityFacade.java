package com.electorama.msvc.visits.facade;

import com.electorama.msvc.visits.model.dto.ReportVisitsByMunicipalityDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportVisitsByMunicipalityFacade {

  @PersistenceContext EntityManager em;

  public List<ReportVisitsByMunicipalityDto> findReportVisitsByMunicipality(
      String dateStart, String dateEnd) {
    List<ReportVisitsByMunicipalityDto> result = new ArrayList<ReportVisitsByMunicipalityDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT "
            + "row_number() over() as id, "
            + "visited.municipality_id, "
            + "count(visited_people.id) filter (where visited_people.visit_number >= 1) as primera_visita, "
            + "count(visited_people.id) filter (where visited_people.visit_number >= 2 ) as segunda_visita, "
            + "count(visited_people.id) filter (where visited_people.visit_number = 3 ) as tercera_visita,"
            + "count(visited_people.id) filter (where visited_people.open_the_door = 't') as visitas_abrieron_la_puerta, "
            + "count(visited_people.id) filter (where visited_people.open_the_door = 'f') as visitas_no_abrieron_la_puerta "
            + "from visited "
            + "LEFT JOIN visited_people on visited_people.visited_id = visited.id "
            + "WHERE "
            + "visited.reference_auth_id IS NOT NULL and visited.deleted is null and "
            + "visited_people.deleted is null AND visited.visit_date BETWEEN "
            + dateStart
            + " AND "
            + dateEnd
            + "GROUP BY visited.municipality_id";

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();

      for (Object[] obj : objectRes) {
        ReportVisitsByMunicipalityDto edd = new ReportVisitsByMunicipalityDto();
        edd.setId(Long.parseLong(obj[0].toString()));
        edd.setMunicipalityId(Integer.parseInt(obj[1].toString()));
        edd.setPrimeraVisita(Integer.parseInt(obj[2].toString()));
        edd.setSegundaVisita(Integer.parseInt(obj[3].toString()));
        edd.setTerceraVisita(Integer.parseInt(obj[4].toString()));
        edd.setVisitasAbrieronLaPuerta(Integer.parseInt(obj[5].toString()));
        edd.setVisitasNoAbrieronLaPuerta(Integer.parseInt(obj[6].toString()));
        result.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findReportVisitsByMunicipality:" + e.getMessage());
    }
    return result;
  }
}
