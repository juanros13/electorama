package com.electorama.msvc.visits.facade;

import com.electorama.msvc.visits.model.dto.ReportAllVisitsDto;
import com.electorama.msvc.visits.model.dto.ReportVisitAllDto;
import com.electorama.msvc.visits.model.dto.ReportVisitMapDto;
import com.electorama.msvc.visits.model.dto.ReportVisitsByTimeDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportAllVisitsFacade {

  @PersistenceContext EntityManager em;

  public ReportAllVisitsDto getReporteAllVisitsByAdmin() {
    ReportAllVisitsDto result = new ReportAllVisitsDto();
    List<Object[]> objectRes = null;
    String select =
        "SELECT \n"
            + "COUNT(visited_people.id) FILTER (WHERE visited_people.deleted IS NULL) as total_visits,\n"
            + "COUNT(visited_people.id) FILTER (WHERE visited_people.deleted IS NULL AND visited_people.visit_number >=2) as total_visits_second,\n"
            + "COUNT(visited_people.id) FILTER (WHERE visited_people.deleted IS NOT NULL) as total_visits_deleted,\n"
            + "COUNT(visited_people.id) FILTER (WHERE visited_people.deleted IS NULL AND visited_people.visit_number >=3) as total_visits_third\n"
            + "FROM visited_people\n";

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    ReportAllVisitsDto edd = new ReportAllVisitsDto();

    try {
      objectRes = query.getResultList();
      for (Object[] obj : objectRes) {
        edd.setTotalVisits(Integer.parseInt(obj[0].toString()));
        edd.setTotalVisitsDeleted(Integer.parseInt(obj[2].toString()));
        edd.setTotalVisitsSecond(Integer.parseInt(obj[1].toString()));
      }

    } catch (Exception e) {
      System.err.println("Error al obtener el reporte:" + e.getMessage());
    }
    return edd;
  }

  public List<ReportVisitMapDto> reportVisitsMap(String typeRol, String authIdHeader) {
    List<ReportVisitMapDto> result = new ArrayList<ReportVisitMapDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT visited_people.latitude, visited_people.longitude  FROM visited "
            + "LEFT JOIN visited_people ON visited_people.visited_id = visited.id "
            + "WHERE visited.deleted is null and visited_people.deleted is null ";
    if (typeRol.equalsIgnoreCase("CORDINATOR_MUNICIPAL")) {
      log.info("Es CORDINATOR_MUNICIPAL...");
      select = select + " and visited.reference_auth_id ='" + authIdHeader + "'";
    } else if (typeRol.equalsIgnoreCase("CORDINATOR_ZONE")) {
      log.info("Es CORDINATOR_ZONE...");
      select = select + " and visited.coordinate_zone_id ='" + authIdHeader + "'";
    }

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();

      for (Object[] obj : objectRes) {
        ReportVisitMapDto edd = new ReportVisitMapDto();
        if (obj[0] != null
            && obj[1] != null
            && !obj[0].toString().equals("")
            && !obj[1].toString().equals("")) {
          edd.setLatitude(obj[0].toString());
          edd.setLongitude(obj[1].toString());
          result.add(edd);
        }
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las visitas map:" + e.getMessage());
    }
    return result;
  }

  public List<ReportVisitAllDto> reportVisitsAll(
      String dateStart, String dateEnd, String typeRol, String authIdHeader) {
    List<ReportVisitAllDto> result = new ArrayList<ReportVisitAllDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT "
            + "visited.reference_auth_id AS referente, "
            + "visited.coordinate_zone_id AS coordinador_zona, "
            + "visited.municipality_id AS municipio, "
            + "visited.seccion_id AS seccion, "
            + "visited_people.\"id\" AS visitado_id, "
            + "visited.visit_date AS fecha_visita, "
            + "DATE(visited.created) AS fecha_sincronizacion, "
            + "CASE visited_people.open_the_door WHEN 't' THEN 'SI' ELSE 'NO' END abrio_puerta, "
            + "CASE visited_people.is_related WHEN 't' THEN 'SI' ELSE 'NO' END es_afin, "
            + "CASE  WHEN concat(visited_people.\"names\",' ',visited_people.lASt_name,' ',visited_people.sur_name)  = '  ' THEN 'NO' ELSE 'SI' END acepta_mAS_info, "
            + "visited_people.\"names\" AS visitado_nombre, "
            + "visited_people.last_name AS visitado_paterno, "
            + "visited_people.sur_name AS visitado_materno, "
            + "visited_people.phone AS telefono, "
            + "visited_people.age_of_the_person_who_opens_the_door AS edad_persona_abre_puerta, "
            + "visited_people.sex_of_the_one_who_opens_the_door AS sexo_persona_abre_puerta, "
            + "visited_people.street AS calle, "
            + "visited_people.external_number AS numero_externo, "
            + "visited_people.latitude AS latitud, "
            + "visited_people.longitude AS longitud "
            + "FROM "
            + "visited "
            + "LEFT JOIN visited_people ON visited.id = visited_people.visited_id "
            + "WHERE visited_people.deleted IS NULL "
            + "AND visited.visit_date BETWEEN "
            + dateStart
            + " AND "
            + dateEnd;

    log.info("Query: " + select);
    if (typeRol.equalsIgnoreCase("CORDINATOR_MUNICIPAL")) {
      log.info("Es CORDINATOR_MUNICIPAL...");
      select = select + " and visited.reference_auth_id ='" + authIdHeader + "'";
    } else if (typeRol.equalsIgnoreCase("CORDINATOR_ZONE")) {
      log.info("Es CORDINATOR_ZONE...");
      select = select + " and visited.coordinate_zone_id ='" + authIdHeader + "'";
    }

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();

      for (Object[] obj : objectRes) {
        ReportVisitAllDto edd = new ReportVisitAllDto();
        edd.setReferente(obj[0] != null ? obj[0].toString() : "");
        edd.setCoordinadorZona(obj[1] != null ? obj[1].toString() : "");
        edd.setMunicipio(obj[2] != null ? obj[2].toString() : "");
        edd.setSeccion(obj[3] != null ? obj[3].toString() : "");
        edd.setVisitadoId(obj[4] != null ? obj[4].toString() : "");
        edd.setFechaVisita(obj[5] != null ? obj[5].toString() : "");
        edd.setFechaSincronizacion(obj[6] != null ? obj[6].toString() : "");
        edd.setAbrioPuerta(obj[7] != null ? obj[7].toString() : "");
        edd.setEsAfin(obj[8] != null ? obj[8].toString() : "");
        edd.setAceptaMasInfo(obj[9] != null ? obj[9].toString() : "");
        edd.setVisitadoNombre(obj[10] != null ? obj[10].toString() : "");
        edd.setVisitadoPaterno(obj[11] != null ? obj[11].toString() : "");
        edd.setVisitadoMaterno(obj[12] != null ? obj[12].toString() : "");
        edd.setTelefono(obj[13] != null ? obj[13].toString() : "");
        edd.setEdadPersonaAbrePuerta(obj[14] != null ? obj[14].toString() : "");
        edd.setSexoPersonaAbrePuerta(obj[15] != null ? obj[15].toString() : "");
        edd.setCalle(obj[16] != null ? obj[16].toString() : "");
        edd.setNumeroExterno(obj[17] != null ? obj[17].toString() : "");
        edd.setLatitud(obj[18] != null ? obj[18].toString() : "");
        edd.setLongitud(obj[19] != null ? obj[19].toString() : "");

        result.add(edd);
      }

    } catch (Exception e) {
      System.err.println(
          "Error al obtener las findReportVisitedByMunicipalCoordinator:" + e.getMessage());
    }
    return result;
  }

  public List<ReportVisitsByTimeDto> findAllVisitsByTime(String dateStart, String dateEnd) {
    List<ReportVisitsByTimeDto> result = new ArrayList<ReportVisitsByTimeDto>();
    List<Object[]> objectRes = new ArrayList<>();
    String select =
        "SELECT\n"
            + "date_trunc('day', visited.visit_date)::::date AS date_visits,\n"
            + "COUNT(visited_people.id) as total_visits\n"
            + "FROM\n"
            + "visited\n"
            + "LEFT JOIN visited_people on visited_people.visited_id = visited.id\n"
            + "WHERE\n"
            + "visited.reference_auth_id IS NOT NULL \n"
            + "AND visited.deleted is null and visited_people.deleted is null \n"
            + "AND visited.visit_date BETWEEN "
            + dateStart
            + " AND "
            + dateEnd
            + " "
            + "GROUP BY date_trunc('day', visited.visit_date) \n"
            + "ORDER BY date_trunc('day', visited.visit_date) ";

    log.info("Query: " + select);

    Query query = em.createNativeQuery(select);
    try {
      objectRes = query.getResultList();

      for (Object[] obj : objectRes) {
        ReportVisitsByTimeDto edd = new ReportVisitsByTimeDto();
        edd.setDateVisits(obj[0].toString());
        edd.setTotalVisits(obj[1].toString());

        result.add(edd);
      }

    } catch (Exception e) {
      System.err.println(
          "Error al obtener las findReportVisitedByMunicipalCoordinator:" + e.getMessage());
    }
    return result;
  }
}
