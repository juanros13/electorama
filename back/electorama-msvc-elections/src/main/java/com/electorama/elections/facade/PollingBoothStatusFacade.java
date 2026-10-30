package com.electorama.elections.facade;

import com.electorama.elections.models.dto.PollingBoothStatusResultDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PollingBoothStatusFacade {

  @PersistenceContext EntityManager em;

  public Page<PollingBoothStatusResultDto> findByParameters(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long statusId,
      Pageable pageable) {
    List<PollingBoothStatusResultDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "SELECT     "
            + " polling_booths.state, "
            + " polling_booths.municipality,  "
            + " polling_booths.federal_district,  "
            + " polling_booths.local_district,  "
            + " polling_booths.seccion_number,  "
            + " polling_booths.id as polling_booths_id, "
            + " st.name as status, "
            + " UPPER(concat(polling_booths_types.name,' ',polling_booths.polling_booth_number)) as polling_booth_name  "
            + " FROM polling_booths    "
            + " LEFT JOIN polling_booths_status st ON st.id = polling_booths.polling_booth_status_id "
            + " LEFT JOIN polling_booths_types ON polling_booths_types.id = polling_booths.polling_booth_type_id "
            + " WHERE  polling_booths.municipality_id IS NOT NULL ");
    if (municipalityId != null && municipalityId > 0) {
      queryBuilder.append(" AND polling_booths.municipality_id = " + municipalityId);
    }
    if (sectionNum != null && sectionNum > 0) {
      queryBuilder.append(" AND polling_booths.seccion_number = " + sectionNum);
    }
    if (federalDistrictId != null && federalDistrictId > 0) {
      queryBuilder.append(" AND  polling_booths.federal_district = " + federalDistrictId);
    }
    if (localDistrictId != null && localDistrictId > 0) {
      queryBuilder.append(" AND polling_booths.local_district = " + localDistrictId);
    }
    if (statusId != null && statusId > 0) {
      queryBuilder.append(" AND st.id = " + statusId);
    }

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    long totalCount = 0;
    try {
      // Aplicar paginación
      query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
      query.setMaxResults(pageable.getPageSize());

      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        PollingBoothStatusResultDto edd = new PollingBoothStatusResultDto();
        edd.setId(Long.valueOf(count++));
        edd.setState(obj[0] != null ? obj[0].toString() : "");
        edd.setMunicipality(obj[1] != null ? obj[1].toString() : "");
        edd.setFederalDistrict(obj[2] != null ? (long) (int) obj[2] : 0);
        edd.setLocalDistrict(obj[3] != null ? (long) (int) obj[3] : 0);
        edd.setSectionNumber(obj[4] != null ? (Integer) obj[4] : 0);
        edd.setPollingBoothId(obj[5] != null ? (long) obj[5] : 0);
        edd.setStatus(obj[6] != null ? obj[6].toString() : "");
        edd.setPollingBoothName(obj[7] != null ? obj[7].toString() : "");
        resultList.add(edd);
      }

      StringBuilder queryStringBuilder = new StringBuilder();
      queryStringBuilder.append(
          "SELECT  COUNT(*)  "
              + " FROM polling_booths    "
              + " LEFT JOIN polling_booths_status st ON st.id = polling_booths.polling_booth_status_id "
              + " LEFT JOIN polling_booths_types ON polling_booths_types.id = polling_booths.polling_booth_type_id "
              + " where  polling_booths.municipality_id IS NOT NULL ");

      if (municipalityId != null && municipalityId > 0) {
        queryStringBuilder.append(" AND polling_booths.municipality_id = :municipalityId");
      }

      if (sectionNum != null && sectionNum > 0) {
        queryStringBuilder.append(" AND seccion_number = :sectionNum ");
      }
      if (federalDistrictId != null && federalDistrictId > 0) {
        queryStringBuilder.append(" AND  polling_booths.federal_district = :federalDistrictId");
      }
      if (localDistrictId != null && localDistrictId > 0) {
        queryStringBuilder.append(" AND polling_booths.local_district = :localDistrictId");
      }
      if (statusId != null && statusId > 0) {
        queryStringBuilder.append(" AND st.id = :statusId");
      }

      Query countQuery = em.createNativeQuery(queryStringBuilder.toString());

      if (municipalityId != null && municipalityId > 0) {
        countQuery.setParameter("municipalityId", municipalityId);
      }
      if (sectionNum != null && sectionNum > 0) {
        countQuery.setParameter("sectionNum", sectionNum);
      }
      if (federalDistrictId != null && federalDistrictId > 0) {
        countQuery.setParameter("federalDistrictId", federalDistrictId);
      }
      if (localDistrictId != null && localDistrictId > 0) {
        countQuery.setParameter("localDistrictId", localDistrictId);
      }
      if (statusId != null && statusId > 0) {
        countQuery.setParameter("statusId", statusId);
      }
      log.info("Query: " + queryStringBuilder);

      totalCount = (Long) (countQuery.getSingleResult());

    } catch (Exception e) {
      System.err.println("Error al obtener las findByParameters:" + e.getMessage());
    }
    return new PageImpl<>(resultList, pageable, totalCount);
  }
}
