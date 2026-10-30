package com.electorama.elections.facade;

import com.electorama.elections.models.dto.ResponseSectionReportByDistrictFederalDto;
import com.electorama.elections.models.dto.ResponseSectionReportDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportSectionsFacade {

  @PersistenceContext EntityManager em;

  public List<ResponseSectionReportDto> getReportSectionByMunicipalityId(Long municipalityId) {
    List<ResponseSectionReportDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "SELECT "
            + "municipality_id, "
            + "municipality as municipality_name, "
            + "seccion_id, "
            + "seccion_number, "
            + "count(polling_booths.id) filter (where polling_booths_status.name ilike 'Abierta%') as total_polling_booths_opened, "
            + "count(polling_booths.id) filter (where polling_booths_status.name ilike 'Cerrada%') as total_polling_booths_closed, "
            + "count(*) AS total "
            + "FROM polling_booths "
            + "LEFT JOIN polling_booths_status ON polling_booths_status.id = polling_booths.polling_booth_status_id ");
    queryBuilder.append("WHERE polling_booths.municipality_id=" + municipalityId + " ");
    queryBuilder.append("GROUP BY  municipality_id, municipality, seccion_id, seccion_number ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      // Aplicar paginación
      objectRes = query.getResultList();
      int idCounter = 1;
      for (Object[] obj : objectRes) {
        ResponseSectionReportDto edd = new ResponseSectionReportDto();
        edd.setId(Long.valueOf(idCounter++));
        edd.setMunicipalityId(Long.parseLong(obj[0].toString()));
        edd.setMunicipalityName(obj[1] != null ? obj[1].toString() : "");
        edd.setSectionId(Long.parseLong(obj[2].toString()));
        edd.setSectionNumber(obj[3] != null ? obj[3].toString() : "");
        edd.setTotalPollingBoothsOpened(Integer.parseInt(obj[4].toString()));
        edd.setTotalPollingBoothsClosed(Integer.parseInt(obj[5].toString()));
        edd.setTotal(Integer.parseInt(obj[6].toString()));

        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByCordinatorAuthId:" + e.getMessage());
    }
    return resultList;
  }

  public List<ResponseSectionReportByDistrictFederalDto> getReportSectionByDistrictFederalId(
      Long districtFederalId) {
    List<ResponseSectionReportByDistrictFederalDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "SELECT "
            + "federal_district, "
            + "federal_district_name, "
            + "municipality_id,"
            + "municipality, "
            + "seccion_number, "
            + "count(polling_booths.id) filter (where polling_booths_status.name ilike 'Abierta%') as total_polling_booths_opened, "
            + "count(polling_booths.id) filter (where polling_booths_status.name ilike 'Cerrada%') as total_polling_booths_closed, "
            + "count(*) AS total "
            + "FROM polling_booths "
            + "LEFT JOIN polling_booths_status ON polling_booths_status.id = polling_booths.polling_booth_status_id ");
    queryBuilder.append("WHERE polling_booths.federal_district=" + districtFederalId + " ");
    queryBuilder.append(
        "GROUP BY  federal_district, federal_district_name, municipality_id, municipality, seccion_number "
            + "ORDER BY federal_district");
    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      // Aplicar paginación
      objectRes = query.getResultList();
      int idCounter = 1;
      for (Object[] obj : objectRes) {
        ResponseSectionReportByDistrictFederalDto edd =
            new ResponseSectionReportByDistrictFederalDto();
        edd.setId(Long.valueOf(idCounter++));
        edd.setFederalDistrict(Long.parseLong(obj[0].toString()));
        edd.setFederalDistrictName(obj[1] != null ? obj[1].toString() : "");
        edd.setMunicipalityId(Long.parseLong(obj[2].toString()));
        edd.setMunicipalityName(obj[3] != null ? obj[3].toString() : "");
        edd.setSectionNumber(obj[4] != null ? obj[4].toString() : "");
        edd.setTotalPollingBoothsOpened(Integer.parseInt(obj[5].toString()));
        edd.setTotalPollingBoothsClosed(Integer.parseInt(obj[6].toString()));
        edd.setTotal(Integer.parseInt(obj[7].toString()));

        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByCordinatorAuthId:" + e.getMessage());
    }
    return resultList;
  }
}
