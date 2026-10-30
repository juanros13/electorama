package com.electorama.elections.facade;

import com.electorama.elections.models.dto.*;
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
public class VotingFlowFacade {

  @PersistenceContext EntityManager em;

  public Page<VotingFlowDto> getVotingFlow(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long electionTypeId,
      Long electionPositionId,
      Pageable pageable) {
    List<VotingFlowDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "    SELECT "
            + "        V2_0.municipality, "
            + "        V2_0.federal_district, "
            + "        V2_0.local_district, "
            + "        cat.name as category_name, "
            + "        V2_0.seccion_number, "
            + "        UPPER(concat(polling_booths_types.name,' ',V2_0.polling_booth_number)) as polling_booth_name, "
            + "        V2_0.id as polling_booths_id, "
            + "        V2_0.voters_list               "
            + "    FROM "
            + "        polling_booths V2_0                   "
            + "    LEFT JOIN "
            + "        polling_booths_types  "
            + "            on polling_booths_types.id = V2_0.polling_booth_type_id           "
            + "    LEFT JOIN "
            + "        polling_booths_categories cat  "
            + "            on cat.id = V2_0.polling_booth_category_id               "
            + "    where "
            + "        V2_0.municipality_id IS NOT NULL ");
    if (municipalityId != null && municipalityId > 0) {
      queryBuilder.append(" AND V2_0.municipality_id = " + municipalityId);
    }
    if (sectionNum != null && sectionNum > 0) {
      queryBuilder.append(" AND V2_0.seccion_number = " + sectionNum);
    }
    if (federalDistrictId != null && federalDistrictId > 0) {
      queryBuilder.append(" AND  V2_0.federal_district = " + federalDistrictId);
    }
    if (localDistrictId != null && localDistrictId > 0) {
      queryBuilder.append(" AND V2_0.local_district = " + localDistrictId);
    }
    if (electionTypeId != null && electionTypeId > 0) {
      queryBuilder.append(" AND et.id = " + electionTypeId);
    }
    if (electionPositionId != null && electionPositionId > 0) {
      queryBuilder.append(" AND ep.id = " + electionPositionId);
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
        VotingFlowDto edd = new VotingFlowDto();
        edd.setId(Long.valueOf(count++));
        edd.setMunicipality(obj[0] != null ? obj[0].toString() : "");
        edd.setFederalDistrict(obj[1] != null ? (long) (int) obj[1] : 0);
        edd.setLocalDistrict(obj[2] != null ? (long) (int) obj[2] : 0);
        edd.setCategoryName(obj[3] != null ? obj[3].toString() : "");
        edd.setSectionNumber(obj[4] != null ? (Integer) obj[4] : 0);
        edd.setPollingBoothName(obj[5] != null ? obj[5].toString() : "");
        edd.setPollingBoothsId(obj[6] != null ? (Long) obj[6] : 0);
        edd.setVotersList(obj[7] != null ? (Integer) obj[7] : 0);
        resultList.add(edd);
      }

      StringBuilder queryStringBuilder = new StringBuilder();
      queryStringBuilder.append(
          "    SELECT COUNT(*) "
              + "    FROM "
              + "        polling_booths V2_0                   "
              + "    LEFT JOIN "
              + "        polling_booths_types  "
              + "            on polling_booths_types.id = V2_0.polling_booth_type_id           "
              + "    LEFT JOIN "
              + "        polling_booths_categories cat  "
              + "            on cat.id = V2_0.polling_booth_category_id               "
              + "    where "
              + "        V2_0.municipality_id IS NOT NULL ");

      if (municipalityId != null && municipalityId > 0) {
        queryStringBuilder.append(" AND V2_0.municipality_id = :municipalityId");
      }

      if (sectionNum != null && sectionNum > 0) {
        queryStringBuilder.append(" AND seccion_number = :sectionNum ");
      }
      if (federalDistrictId != null && federalDistrictId > 0) {
        queryStringBuilder.append(" AND  V2_0.federal_district = :federalDistrictId");
      }
      if (localDistrictId != null && localDistrictId > 0) {
        queryStringBuilder.append(" AND V2_0.local_district = :localDistrictId");
      }
      if (electionTypeId != null && electionTypeId > 0) {
        queryBuilder.append(" AND et.id = :electionTypeId ");
      }
      if (electionPositionId != null && electionPositionId > 0) {
        queryStringBuilder.append("  AND ep.id = :electionPositionId");
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
      if (electionTypeId != null && electionTypeId > 0) {
        countQuery.setParameter("electionTypeId", electionTypeId);
      }
      if (electionPositionId != null && electionPositionId > 0) {
        countQuery.setParameter("electionPositionId", electionPositionId);
      }
      totalCount = (Long) (countQuery.getSingleResult());

    } catch (Exception e) {
      System.err.println("Error al obtener las findByParameters:" + e.getMessage());
    }
    return new PageImpl<>(resultList, pageable, totalCount);
  }

  public VoterListDto getProgressVote(
      Long municipalityId, Integer sectionNum, Long federalDistrictId, Long localDistrictId) {

    StringBuilder queryBuilder = new StringBuilder();
    List<Object[]> objectRes = new ArrayList<>();
    VoterListDto report = new VoterListDto();
    queryBuilder.append(
        "SELECT "
            + "  STRING_AGG(V2_0.id::::text, ',') AS polling_booths_id, "
            + " SUM(V2_0.voters_list) as total "
            + "FROM "
            + "  polling_booths V2_0                   "
            + "LEFT JOIN "
            + "  polling_booths_types  "
            + "    on polling_booths_types.id = V2_0.polling_booth_type_id           "
            + "LEFT JOIN "
            + "  polling_booths_categories cat  "
            + "    on cat.id = V2_0.polling_booth_category_id               "
            + "where "
            + "  V2_0.municipality_id IS NOT NULL ");

    if (municipalityId != null && municipalityId > 0) {
      queryBuilder.append(" AND V2_0.municipality_id =" + municipalityId);
    }
    if (sectionNum != null && sectionNum > 0) {
      queryBuilder.append(" AND seccion_number =" + sectionNum);
    }
    if (federalDistrictId != null && federalDistrictId > 0) {
      queryBuilder.append(" AND  V2_0.federal_district =" + federalDistrictId);
    }
    if (localDistrictId != null && localDistrictId > 0) {
      queryBuilder.append(" AND V2_0.local_district =" + localDistrictId);
    }

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());

    try {

      objectRes = query.getResultList();
      if (objectRes != null && !objectRes.isEmpty()) {
        Object[] obj = objectRes.get(0);

        report.setPollingtBoothIds((obj[0] != null ? obj[0].toString() : ""));
        report.setProgressVote((obj[1] != null ? Integer.parseInt(obj[1].toString()) : 0));
      }
    } catch (Exception e) {
      System.err.println("Error al obtener las getProgressVote:" + e.getMessage());
    }
    return report;
  }
}
