package com.electorama.elections.facade;

import com.electorama.elections.models.dto.PollingBoothIssuesDescriptionDto;
import com.electorama.elections.models.dto.TotalIncidentsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PollingBoothIssuesFacade {

  @PersistenceContext EntityManager em;

  public Page<PollingBoothIssuesDescriptionDto> findByParameters(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId,
      Pageable pageable) {
    List<PollingBoothIssuesDescriptionDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "SELECT    "
            + "     V2_0.municipality, "
            + "     V2_0.federal_district, "
            + "     V2_0.local_district, "
            + "     V2_0.seccion_number, "
            + "     V2_0.id as polling_booths_id, "
            + "     cat.name as name_cat, "
            + "     subcat.name as name_sub, "
            + "     polling_booths_issues.description, "
            + "     polling_booths_issues.created, "
            + "     UPPER(concat(polling_booths_types.name,' ',V2_0.polling_booth_number)) as polling_booth_name "
            + "               FROM polling_booths_issues   "
            + "               LEFT JOIN polling_booths V2_0 ON V2_0.id = polling_booths_issues.polling_booth_id "
            + "        LEFT JOIN polling_booths_issues_sub_categories subcat ON subcat.id = polling_booths_issues.sub_category_id "
            + "        LEFT JOIN polling_booths_issues_categories cat ON cat.id = subcat.category_id "
            + "        LEFT JOIN polling_booths_types on polling_booths_types.id = V2_0.polling_booth_type_id "
            + "               where  V2_0.municipality_id IS NOT NULL  ");
    if (municipalityId != null && municipalityId > 0) {
      queryBuilder.append(" AND V2_0.municipality_id = " + municipalityId);
    }
    if (sectionNum != null && sectionNum > 0) {
      queryBuilder.append(" AND seccion_number = " + sectionNum);
    }
    if (federalDistrictId != null && federalDistrictId > 0) {
      queryBuilder.append(" AND  V2_0.federal_district = " + federalDistrictId);
    }
    if (localDistrictId != null && localDistrictId > 0) {
      queryBuilder.append(" AND V2_0.local_district = " + localDistrictId);
    }
    if (subCategoryId != null && subCategoryId > 0) {
      queryBuilder.append(" AND subcat.id = " + subCategoryId);
    }
    if (typeId != null && typeId > 0) {
      queryBuilder.append(" AND V2_0.polling_booth_type_id = " + typeId);
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
        PollingBoothIssuesDescriptionDto edd = new PollingBoothIssuesDescriptionDto();
        edd.setId(Long.valueOf(count++));
        edd.setMunicipality(obj[0] != null ? obj[0].toString() : "");
        edd.setFederalDistrict(obj[1] != null ? (long) (int) obj[1] : 0);
        edd.setLocalDistrict(obj[2] != null ? (long) (int) obj[2] : 0);
        edd.setSectionNumber(obj[3] != null ? (Integer) obj[3] : 0);
        edd.setPollingBoothsId(obj[4] != null ? (Long) obj[4] : 0);
        edd.setNameCat(obj[5] != null ? obj[5].toString() : "");
        edd.setNameSub(obj[6] != null ? obj[6].toString() : "");
        edd.setDescription(obj[7] != null ? obj[7].toString() : "");
        edd.setCreated((Date) obj[8]);
        edd.setPollingBoothName(obj[9] != null ? obj[9].toString() : "");

        resultList.add(edd);
      }

      StringBuilder queryStringBuilder = new StringBuilder();
      queryStringBuilder.append("SELECT COUNT(*) FROM polling_booths_issues ");
      queryStringBuilder.append(
          "LEFT JOIN polling_booths V2_0 ON V2_0.id = polling_booths_issues.polling_booth_id ");
      queryStringBuilder.append(
          "LEFT JOIN polling_booths_issues_sub_categories subcat ON subcat.id = polling_booths_issues.sub_category_id ");
      queryStringBuilder.append(
          "LEFT JOIN polling_booths_issues_categories cat ON cat.id = subcat.category_id ");
      queryStringBuilder.append("WHERE V2_0.municipality_id IS NOT NULL ");

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
      if (subCategoryId != null && subCategoryId > 0) {
        queryStringBuilder.append(" AND subcat.id = :subCategoryId");
      }
      if (typeId != null && typeId > 0) {
        queryStringBuilder.append(" AND V2_0.polling_booth_type_id = :typeId");
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
      if (subCategoryId != null && subCategoryId > 0) {
        countQuery.setParameter("subCategoryId", subCategoryId);
      }
      if (typeId != null && typeId > 0) {
        countQuery.setParameter("typeId", typeId);
      }
      totalCount = (Long) (countQuery.getSingleResult());

    } catch (Exception e) {
      System.err.println("Error al obtener las findByParameters:" + e.getMessage());
    }
    return new PageImpl<>(resultList, pageable, totalCount);
  }

  public List<TotalIncidentsDto> findByTotalIncidents(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId) {
    List<TotalIncidentsDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "SELECT "
            + "subcat.name AS sub_category, "
            + "count(*) AS total "
            + "FROM polling_booths_issues "
            + "LEFT JOIN polling_booths V2_0 ON V2_0.id = polling_booths_issues.polling_booth_id "
            + "LEFT JOIN polling_booths_issues_sub_categories subcat ON subcat.id = polling_booths_issues.sub_category_id "
            + "LEFT JOIN polling_booths_issues_categories cat ON cat.id = subcat.category_id "
            + "LEFT JOIN polling_booths_types on polling_booths_types.id = V2_0.polling_booth_type_id "
            + "where  V2_0.municipality_id IS NOT NULL ");
    if (municipalityId != null && municipalityId > 0) {
      queryBuilder.append(" AND V2_0.municipality_id = " + municipalityId);
    }
    if (sectionNum != null && sectionNum > 0) {
      queryBuilder.append(" AND seccion_number = " + sectionNum);
    }
    if (federalDistrictId != null && federalDistrictId > 0) {
      queryBuilder.append(" AND  V2_0.federal_district = " + federalDistrictId);
    }
    if (localDistrictId != null && localDistrictId > 0) {
      queryBuilder.append(" AND V2_0.local_district = " + localDistrictId);
    }
    if (subCategoryId != null && subCategoryId > 0) {
      queryBuilder.append(" AND subcat.id = " + subCategoryId);
    }
    if (typeId != null && typeId > 0) {
      queryBuilder.append(" AND V2_0.polling_booth_type_id = " + typeId);
    }

    queryBuilder.append(" GROUP BY subcat.name ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        TotalIncidentsDto edd = new TotalIncidentsDto();
        edd.setId(Long.valueOf(count++));
        edd.setSubCategoryName(obj[0] != null ? obj[0].toString() : "");
        edd.setTotal(obj[1] != null ? (Long) obj[1] : 0);

        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }

  public List<PollingBoothIssuesDescriptionDto> findByParametersList(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId) {
    List<PollingBoothIssuesDescriptionDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "SELECT    "
            + "     V2_0.municipality, "
            + "     V2_0.federal_district, "
            + "     V2_0.local_district, "
            + "     V2_0.seccion_number, "
            + "     V2_0.id as polling_booths_id, "
            + "     cat.name as name_cat, "
            + "     subcat.name as name_sub, "
            + "     polling_booths_issues.description, "
            + "     polling_booths_issues.created, "
            + "     UPPER(concat(polling_booths_types.name,' ',V2_0.polling_booth_number)) as polling_booth_name "
            + "               FROM polling_booths_issues   "
            + "               LEFT JOIN polling_booths V2_0 ON V2_0.id = polling_booths_issues.polling_booth_id "
            + "        LEFT JOIN polling_booths_issues_sub_categories subcat ON subcat.id = polling_booths_issues.sub_category_id "
            + "        LEFT JOIN polling_booths_issues_categories cat ON cat.id = subcat.category_id "
            + "        LEFT JOIN polling_booths_types on polling_booths_types.id = V2_0.polling_booth_type_id "
            + "               where  V2_0.municipality_id IS NOT NULL  ");
    if (municipalityId != null && municipalityId > 0) {
      queryBuilder.append(" AND V2_0.municipality_id = " + municipalityId);
    }
    if (sectionNum != null && sectionNum > 0) {
      queryBuilder.append(" AND seccion_number = " + sectionNum);
    }
    if (federalDistrictId != null && federalDistrictId > 0) {
      queryBuilder.append(" AND  V2_0.federal_district = " + federalDistrictId);
    }
    if (localDistrictId != null && localDistrictId > 0) {
      queryBuilder.append(" AND V2_0.local_district = " + localDistrictId);
    }
    if (subCategoryId != null && subCategoryId > 0) {
      queryBuilder.append(" AND subcat.id = " + subCategoryId);
    }
    if (typeId != null && typeId > 0) {
      queryBuilder.append(" AND V2_0.polling_booth_type_id = " + typeId);
    }
    log.info("Query: " + queryBuilder);
    Query query = em.createNativeQuery(queryBuilder.toString());
    long totalCount = 0;
    try {
      // Aplicar paginación
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        PollingBoothIssuesDescriptionDto edd = new PollingBoothIssuesDescriptionDto();
        edd.setId(Long.valueOf(count++));
        edd.setMunicipality(obj[0] != null ? obj[0].toString() : "");
        edd.setFederalDistrict(obj[1] != null ? (long) (int) obj[1] : 0);
        edd.setLocalDistrict(obj[2] != null ? (long) (int) obj[2] : 0);
        edd.setSectionNumber(obj[3] != null ? (Integer) obj[3] : 0);
        edd.setPollingBoothsId(obj[4] != null ? (Long) obj[4] : 0);
        edd.setNameCat(obj[5] != null ? obj[5].toString() : "");
        edd.setNameSub(obj[6] != null ? obj[6].toString() : "");
        edd.setDescription(obj[7] != null ? obj[7].toString() : "");
        edd.setCreated((Date) obj[8]);
        edd.setPollingBoothName(obj[9] != null ? obj[9].toString() : "");

        resultList.add(edd);
      }
    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }
}
