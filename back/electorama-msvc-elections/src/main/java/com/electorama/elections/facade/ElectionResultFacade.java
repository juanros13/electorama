package com.electorama.elections.facade;

import com.electorama.elections.models.dto.ElectionsAndCandidatesDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ElectionResultFacade {

  @PersistenceContext EntityManager em;

  public List<ElectionsAndCandidatesDto> findByElectionsPositions(Integer electionsPositionsId) {
    List<ElectionsAndCandidatesDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "SELECT  "
            + "candidates.id, "
            + "elections.name as election_name, "
            + "elections_positions.name, "
            + "candidates.names, "
            + "candidates.last_names, "
            + "candidates.sur_names, "
            + "elections.id as election_id, "
            + "candidates.candidate_path "
            + "FROM elections_positions "
            + "LEFT JOIN elections ON elections.election_position_id = elections_positions.id "
            + "LEFT JOIN candidates ON candidates.election_id = elections.id ");
    if (electionsPositionsId != null && electionsPositionsId > 0) {
      queryBuilder.append(" WHERE elections_positions.id = " + electionsPositionsId);
    }
    queryBuilder.append(" order by elections.name ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        ElectionsAndCandidatesDto edd = new ElectionsAndCandidatesDto();
        edd.setCandidateId(obj[0] != null ? (Long) obj[0] : 0);
        edd.setElectionName(obj[1] != null ? obj[1].toString() : "");
        edd.setElectionPositionName(obj[2] != null ? obj[2].toString() : "");
        edd.setNames(obj[3] != null ? obj[3].toString() : "");
        edd.setLastNames(obj[4] != null ? obj[4].toString() : "");
        edd.setSurNames(obj[5] != null ? obj[5].toString() : "");
        edd.setElectionId(obj[6] != null ? (Long) obj[6] : 0);
        edd.setCandidatePath(obj[7] != null ? obj[7].toString() : "");
        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }

  public List<ElectionsAndCandidatesDto> findByElectionsFederalDistrict(
      Integer electionsPositionsId, List<Long> federalDistrictIds) {
    List<ElectionsAndCandidatesDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    StringJoiner joiner = new StringJoiner(", ");
    for (Long id : federalDistrictIds) {
      joiner.add(id.toString());
    }

    queryBuilder.append(
        "SELECT  "
            + "candidates.id, "
            + "elections.name as election_name, "
            + "elections_positions.name, "
            + "candidates.names, "
            + "candidates.last_names, "
            + "candidates.sur_names, "
            + "elections.id as election_id, "
            + "candidates.candidate_path "
            + "FROM elections_positions "
            + "LEFT JOIN elections ON elections.election_position_id = elections_positions.id "
            + "LEFT JOIN candidates ON candidates.election_id = elections.id "
            + "LEFT JOIN elections_federal_district ef ON ef.candidate_id = candidates.id ");
    if (electionsPositionsId != null && electionsPositionsId > 0) {
      queryBuilder.append(" WHERE elections_positions.id = " + electionsPositionsId);
    }
    if (federalDistrictIds != null && federalDistrictIds.size() > 0) {
      queryBuilder.append(" AND ef.federal_district_id in (" + joiner + ")");
    }
    queryBuilder.append(" order by elections.name ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        ElectionsAndCandidatesDto edd = new ElectionsAndCandidatesDto();
        edd.setCandidateId(obj[0] != null ? (Long) obj[0] : 0);
        edd.setElectionName(obj[1] != null ? obj[1].toString() : "");
        edd.setElectionPositionName(obj[2] != null ? obj[2].toString() : "");
        edd.setNames(obj[3] != null ? obj[3].toString() : "");
        edd.setLastNames(obj[4] != null ? obj[4].toString() : "");
        edd.setSurNames(obj[5] != null ? obj[5].toString() : "");
        edd.setCandidatePath(obj[7] != null ? obj[7].toString() : "");
        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }

  public List<ElectionsAndCandidatesDto> findByElectionsLocalDistrict(
      Integer electionsPositionsId, List<Long> localDistrictIds) {
    List<ElectionsAndCandidatesDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    StringJoiner joiner = new StringJoiner(", ");
    for (Long id : localDistrictIds) {
      joiner.add(id.toString());
    }

    queryBuilder.append(
        "SELECT  "
            + "candidates.id, "
            + "elections.name as election_name, "
            + "elections_positions.name, "
            + "candidates.names, "
            + "candidates.last_names, "
            + "candidates.sur_names, "
            + "elections.id as election_id, "
            + "candidates.candidate_path "
            + "FROM elections_positions "
            + "LEFT JOIN elections ON elections.election_position_id = elections_positions.id "
            + "LEFT JOIN candidates ON candidates.election_id = elections.id "
            + "LEFT JOIN elections_local_district ef ON ef.candidate_id = candidates.id ");
    if (electionsPositionsId != null && electionsPositionsId > 0) {
      queryBuilder.append(" WHERE elections_positions.id = " + electionsPositionsId);
    }
    if (localDistrictIds != null && localDistrictIds.size() > 0) {
      queryBuilder.append(" AND ef.local_district_id in (" + joiner + ")");
    }
    queryBuilder.append(" order by elections.name ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        ElectionsAndCandidatesDto edd = new ElectionsAndCandidatesDto();
        edd.setCandidateId(obj[0] != null ? (Long) obj[0] : 0);
        edd.setElectionName(obj[1] != null ? obj[1].toString() : "");
        edd.setElectionPositionName(obj[2] != null ? obj[2].toString() : "");
        edd.setNames(obj[3] != null ? obj[3].toString() : "");
        edd.setLastNames(obj[4] != null ? obj[4].toString() : "");
        edd.setSurNames(obj[5] != null ? obj[5].toString() : "");
        edd.setElectionId(obj[6] != null ? (Long) obj[0] : 6);
        edd.setCandidatePath(obj[7] != null ? obj[7].toString() : "");
        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }

  public List<ElectionsAndCandidatesDto> findByElectionsMunicipality(
      Integer electionsPositionsId, List<Long> municipalitiesIds) {
    List<ElectionsAndCandidatesDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    StringJoiner joiner = new StringJoiner(", ");
    for (Long id : municipalitiesIds) {
      joiner.add(id.toString());
    }

    queryBuilder.append(
        "SELECT  "
            + "candidates.id, "
            + "elections.name as election_name, "
            + "elections_positions.name, "
            + "candidates.names, "
            + "candidates.last_names, "
            + "candidates.sur_names, "
            + "elections.id as election_id, "
            + "candidates.candidate_path "
            + "FROM elections_positions "
            + "LEFT JOIN elections ON elections.election_position_id = elections_positions.id "
            + "LEFT JOIN candidates ON candidates.election_id = elections.id "
            + "LEFT JOIN elections_municipality ef ON ef.candidate_id = candidates.id ");
    if (electionsPositionsId != null && electionsPositionsId > 0) {
      queryBuilder.append(" WHERE elections_positions.id = " + electionsPositionsId);
    }
    if (municipalitiesIds != null && municipalitiesIds.size() > 0) {
      queryBuilder.append(" AND ef.municipality_id in (" + joiner + ")");
    }
    queryBuilder.append(" order by elections.name ");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {
      objectRes = query.getResultList();
      int count = 1;
      for (Object[] obj : objectRes) {
        ElectionsAndCandidatesDto edd = new ElectionsAndCandidatesDto();
        edd.setCandidateId(obj[0] != null ? (Long) obj[0] : 0);
        edd.setElectionName(obj[1] != null ? obj[1].toString() : "");
        edd.setElectionPositionName(obj[2] != null ? obj[2].toString() : "");
        edd.setNames(obj[3] != null ? obj[3].toString() : "");
        edd.setLastNames(obj[4] != null ? obj[4].toString() : "");
        edd.setSurNames(obj[5] != null ? obj[5].toString() : "");
        edd.setElectionId(obj[6] != null ? (Long) obj[0] : 6);
        edd.setCandidatePath(obj[7] != null ? obj[7].toString() : "");
        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }
}
