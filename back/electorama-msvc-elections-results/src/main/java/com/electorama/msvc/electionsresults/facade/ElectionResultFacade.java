package com.electorama.msvc.electionsresults.facade;

import com.electorama.msvc.electionsresults.models.dto.PoliticalPartyResulDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ElectionResultFacade {

  @PersistenceContext EntityManager em;

  public List<PoliticalPartyResulDto> findByElectionsPositions(String candidatesIds) {
    List<PoliticalPartyResulDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        " SELECT   "
            + "political_party_name,  "
            + "SUM(result),   "
            + " candidate_id, "
            + "COUNT(*) AS total_casillas  "
            + "FROM election_result ");
    if (candidatesIds != null && !candidatesIds.isEmpty()) {
      queryBuilder.append(" where election_result.candidate_id IN ( " + candidatesIds + ")");
    }
    queryBuilder.append(
        " GROUP BY election_result.political_party_name, candidate_id, candidate_party_association_id ");
    queryBuilder.append(" order by candidate_party_association_id ASC ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        PoliticalPartyResulDto edd = new PoliticalPartyResulDto();
        edd.setId((long) count++);
        edd.setPoliticalPartyName(obj[0] != null ? obj[0].toString() : "");
        edd.setResult(obj[1] != null ? (Long) obj[1] : 0);
        edd.setCandidateId(obj[2] != null ? (Long) obj[2] : 0);
        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }

  public List<PoliticalPartyResulDto> findByElectionsPositionsSheets(String candidatesIds) {
    List<PoliticalPartyResulDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        " SELECT   "
            + "political_party_name,  "
            + "SUM(result),   "
            + " candidate_id, "
            + "COUNT(*) AS total_casillas  "
            + "FROM election_result_sheets ");
    if (candidatesIds != null && !candidatesIds.isEmpty()) {
      queryBuilder.append(" where election_result_sheets.candidate_id IN ( " + candidatesIds + ")");
    }
    queryBuilder.append(
        " GROUP BY election_result_sheets.political_party_name, candidate_id, candidate_party_association_id ");
    queryBuilder.append(" order by candidate_party_association_id ASC ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        PoliticalPartyResulDto edd = new PoliticalPartyResulDto();
        edd.setId((long) count++);
        edd.setPoliticalPartyName(obj[0] != null ? obj[0].toString() : "");
        edd.setResult(obj[1] != null ? (Long) obj[1] : 0);
        edd.setCandidateId(obj[2] != null ? (Long) obj[2] : 0);
        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }

  public List<PoliticalPartyResulDto> sumResultElectionsMunicipality(
      String candidatesIds, String municipalitiesIds) {
    List<PoliticalPartyResulDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        " SELECT   "
            + "political_party_name,  "
            + "SUM(result),   "
            + " candidate_id, "
            + "COUNT(*) AS total_casillas  "
            + "FROM election_result ");
    if (municipalitiesIds != null && !municipalitiesIds.isEmpty()) {
      queryBuilder.append(" where election_result.candidate_id IN ( " + candidatesIds + ")");
      queryBuilder.append(" and election_result.municipality_id IN ( " + municipalitiesIds + ")");
    }
    queryBuilder.append(
        " GROUP BY election_result.political_party_name, candidate_id, candidate_party_association_id ");
    queryBuilder.append(" order by candidate_party_association_id ASC ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        PoliticalPartyResulDto edd = new PoliticalPartyResulDto();
        edd.setId((long) count++);
        edd.setPoliticalPartyName(obj[0] != null ? obj[0].toString() : "");
        edd.setResult(obj[1] != null ? (Long) obj[1] : 0);
        edd.setCandidateId(obj[2] != null ? (Long) obj[2] : 0);
        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }

  public List<PoliticalPartyResulDto> sumResultElectionsDistrictFederal(
      String candidatesIds, String districtFederalIds) {
    List<PoliticalPartyResulDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        " SELECT   "
            + "political_party_name,  "
            + "SUM(result),   "
            + " candidate_id, "
            + "COUNT(*) AS total_casillas  "
            + "FROM election_result ");
    if (districtFederalIds != null && !districtFederalIds.isEmpty()) {
      queryBuilder.append(" where election_result.candidate_id IN ( " + candidatesIds + ")");
      queryBuilder.append(
          " and election_result.district_federal_id IN ( " + districtFederalIds + ")");
    }
    queryBuilder.append(
        " GROUP BY election_result.political_party_name, candidate_id, candidate_party_association_id ");
    queryBuilder.append(" order by candidate_party_association_id ASC ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        PoliticalPartyResulDto edd = new PoliticalPartyResulDto();
        edd.setId((long) count++);
        edd.setPoliticalPartyName(obj[0] != null ? obj[0].toString() : "");
        edd.setResult(obj[1] != null ? (Long) obj[1] : 0);
        edd.setCandidateId(obj[2] != null ? (Long) obj[2] : 0);
        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }

  public List<PoliticalPartyResulDto> sumResultElectionsDistrictLocal(
      String candidatesIds, String districtLocalIds) {
    List<PoliticalPartyResulDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        " SELECT   "
            + "political_party_name,  "
            + "SUM(result),   "
            + " candidate_id, "
            + "COUNT(*) AS total_casillas  "
            + "FROM election_result ");
    if (districtLocalIds != null && !districtLocalIds.isEmpty()) {
      queryBuilder.append(" where election_result.candidate_id IN ( " + candidatesIds + ")");
      queryBuilder.append(" and election_result.district_local_id IN ( " + districtLocalIds + ")");
    }
    queryBuilder.append(
        " GROUP BY election_result.political_party_name, candidate_id, candidate_party_association_id ");
    queryBuilder.append(" order by candidate_party_association_id ASC ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        PoliticalPartyResulDto edd = new PoliticalPartyResulDto();
        edd.setId((long) count++);
        edd.setPoliticalPartyName(obj[0] != null ? obj[0].toString() : "");
        edd.setResult(obj[1] != null ? (Long) obj[1] : 0);
        edd.setCandidateId(obj[2] != null ? (Long) obj[2] : 0);
        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }
}
