package com.electorama.elections.facade;

import com.electorama.elections.models.dto.*;
import com.electorama.elections.models.entity.*;
import com.electorama.elections.models.mapper.*;
import com.electorama.elections.models.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PollingBoothFacade {

  @PersistenceContext EntityManager em;
  @Autowired PollingBoothAddressTypeRepository pollingBoothAddressTypeRepository;
  @Autowired PollingBoothTypeRepository pollingBoothTypeRepository;
  @Autowired PollingBoothCategoryRepository pollingBoothCategoryRepository;

  @Autowired PollingBoothIssuesRepository pollingBoothIssuesRepository;
  @Autowired PollingBoothStatusRepository pollingBoothStatusRepository;
  private final PollingBoothAddressTypeMapper pollingBoothAddressTypeMapper =
      new PollingBoothAddressTypeMapper();
  private final PollingBoothTypeMapper pollingBoothTypeMapper = new PollingBoothTypeMapper();
  private final PollingBoothCategoryMapper pollingBoothCategoryMapper =
      new PollingBoothCategoryMapper();
  private final PollingBoothStatusMapper pollingBoothStatusMapper = new PollingBoothStatusMapper();

  private final PollingBoothIssuesMapper pollingBoothIssuesMapper = new PollingBoothIssuesMapper();

  public Page<PollingBoothResultDto> findByParameters(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long electionTypeId,
      Long electionPositionId,
      Long statusValidateId,
      Pageable pageable) {
    List<PollingBoothResultDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "SELECT     "
            + "elections.name as election_name, "
            + "ey.name as election_year, "
            + "et.name as election_type, "
            + "V2_0.state, "
            + "V2_0.municipality,  "
            + "V2_0.federal_district,  "
            + "V2_0.local_district,  "
            + "V2_0.seccion_number,  "
            + "V2_0.id as polling_booths_id, "
            + "st.name as status_polling_booth, "
            + "sv.name as status, "
            + "pb.id as polling_booths_election_association_id, "
            + " UPPER(concat(polling_booths_types.name,' ',V2_0.polling_booth_number)) as polling_booth_name,  "
            + " V2_0.voters_list "
            + "FROM polling_booths V2_0    "
            + "LEFT JOIN polling_booths_election_association pb ON pb.polling_booth_id = V2_0.id "
            + "LEFT JOIN elections ON elections.id = pb.election_id "
            + "LEFT JOIN elections_years ey ON ey.id = elections.election_year_id "
            + "LEFT JOIN elections_types et ON et.id = elections.election_type_id "
            + "LEFT JOIN polling_booths_status st ON st.id = V2_0.polling_booth_status_id "
            + "LEFT JOIN elections_positions ep ON ep.id = elections.election_position_id "
            + "LEFT JOIN  status_validate sv ON sv.id = pb.is_validated_sheet "
            + "LEFT JOIN polling_booths_types on polling_booths_types.id = V2_0.polling_booth_type_id  "
            + "where  V2_0.municipality_id IS NOT NULL ");
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

    if (statusValidateId != null && statusValidateId > 0) {
      queryBuilder.append(" AND sv.id = " + statusValidateId);
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
        PollingBoothResultDto edd = new PollingBoothResultDto();
        edd.setId(Long.valueOf(count++));
        edd.setElectionName(obj[0] != null ? obj[0].toString() : "");
        edd.setElectionYear(obj[1] != null ? obj[1].toString() : "");
        edd.setElectionType(obj[2] != null ? obj[2].toString() : "");
        edd.setState(obj[3] != null ? obj[3].toString() : "");
        edd.setMunicipality(obj[4] != null ? obj[4].toString() : "");
        edd.setFederalDistrict(obj[5] != null ? (long) (int) obj[5] : 0);
        edd.setLocalDistrict(obj[6] != null ? (long) (int) obj[6] : 0);
        edd.setSectionNumber(obj[7] != null ? (Integer) obj[7] : 0);
        edd.setPollingBoothsId(obj[8] != null ? (Long) obj[8] : 0);
        edd.setStatusPollingBooth(obj[9] != null ? obj[9].toString() : "");
        edd.setStatus(obj[10] != null ? obj[10].toString() : "");
        edd.setPollingBoothsElectionAssociationId(obj[11] != null ? (Long) obj[11] : 0);
        edd.setPollingBoothName(obj[12] != null ? obj[12].toString() : "");
        edd.setVotersList(obj[13] != null ? (Integer) obj[13] : 0);
        resultList.add(edd);
      }

      StringBuilder queryStringBuilder = new StringBuilder();
      queryStringBuilder.append("SELECT COUNT(*) FROM polling_booths V2_0 ");
      queryStringBuilder.append(
          "LEFT JOIN polling_booths_election_association pb ON pb.polling_booth_id = V2_0.id ");
      queryStringBuilder.append("LEFT JOIN elections ON elections.id = pb.election_id ");
      queryStringBuilder.append(
          "LEFT JOIN elections_years ey ON ey.id = elections.election_year_id ");
      queryStringBuilder.append(
          "LEFT JOIN elections_types et ON et.id = elections.election_type_id ");
      queryStringBuilder.append(
          "LEFT JOIN polling_booths_status st ON st.id = V2_0.polling_booth_status_id ");
      queryStringBuilder.append(
          "LEFT JOIN elections_positions ep ON ep.id = elections.election_position_id ");
      queryStringBuilder.append("LEFT JOIN  status_validate sv ON sv.id = pb.is_validated_sheet  ");
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
      if (electionTypeId != null && electionTypeId > 0) {
        queryBuilder.append(" AND et.id = :electionTypeId ");
      }
      if (electionPositionId != null && electionPositionId > 0) {
        queryStringBuilder.append("  AND ep.id = :electionPositionId");
      }
      if (statusValidateId != null && statusValidateId > 0) {
        queryStringBuilder.append(" AND sv.id = " + statusValidateId);
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

  public CountPollingBoothDto getCountPollingsBooths(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long statusId) {

    StringBuilder queryBuilder = new StringBuilder();
    List<Object[]> objectRes = new ArrayList<>();
    CountPollingBoothDto report = new CountPollingBoothDto();
    queryBuilder.append(
        "SELECT       "
            + "   COUNT(polling_booths.id) filter (where st.id IN (1,3)) as total_open,    "
            + "   COUNT(polling_booths.id) filter (where st.id IN (2,4)) as total_close  "
            + " FROM polling_booths      "
            + " LEFT JOIN polling_booths_status st ON st.id = polling_booths.polling_booth_status_id   "
            + " LEFT JOIN polling_booths_types ON polling_booths_types.id = polling_booths.polling_booth_address_type_id  "
            + " where  polling_booths.municipality_id IS NOT NULL ");

    if (municipalityId != null && municipalityId > 0) {
      queryBuilder.append(" AND municipality_id = " + municipalityId);
    }

    if (sectionNum != null && sectionNum > 0) {
      queryBuilder.append(" AND seccion_number = " + sectionNum);
    }
    if (federalDistrictId != null && federalDistrictId > 0) {
      queryBuilder.append(" AND  federal_district =" + federalDistrictId);
    }
    if (localDistrictId != null && localDistrictId > 0) {
      queryBuilder.append(" AND local_district =" + localDistrictId);
    }
    if (statusId != null && statusId > 0) {
      queryBuilder.append(" AND st.id = " + statusId);
    }

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());

    try {

      objectRes = query.getResultList();
      if (objectRes != null && !objectRes.isEmpty()) {
        Object[] obj = objectRes.get(0);

        report.setTotalOpen((obj[0] != null ? Integer.parseInt(obj[0].toString()) : 0));
        report.setTotalClose((obj[1] != null ? Integer.parseInt(obj[1].toString()) : 0));
      }
    } catch (Exception e) {
      System.err.println("Error al obtener las getCountPollingsBooths:" + e.getMessage());
    }
    return report;
  }

  public ValidateSheetDto getValideSheet(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long statusValidateId,
      Long electionPositionId) {

    StringBuilder queryBuilder = new StringBuilder();
    List<Object[]> objectRes = new ArrayList<>();
    ValidateSheetDto report = new ValidateSheetDto();
    queryBuilder.append(
        "SELECT   "
            + "    COUNT(polling_booths_election_association.id) filter (where polling_booths_election_association.is_validated_sheet IN (2,3)) as total_digital_validate,     "
            + "    COUNT(polling_booths_election_association.id) filter (where polling_booths_election_association.is_validated_sheet = 6) as total_fisical_validate,   "
            + "        COUNT(*) AS total_sheets   "
            + " FROM polling_booths_election_association   "
            + " LEFT JOIN polling_booths p ON p.id =  polling_booths_election_association.polling_booth_id   "
            + "LEFT JOIN elections ON elections.id = polling_booths_election_association.election_id "
            + "LEFT JOIN elections_positions ep ON ep.id = elections.election_position_id "
            + "LEFT JOIN  status_validate sv ON sv.id = polling_booths_election_association.is_validated_sheet "
            + " WHERE p.municipality_id IS NOT NULL ");

    if (municipalityId != null && municipalityId > 0) {
      queryBuilder.append(" AND p.municipality_id = " + municipalityId);
    }

    if (sectionNum != null && sectionNum > 0) {
      queryBuilder.append(" AND seccion_number = " + sectionNum);
    }
    if (federalDistrictId != null && federalDistrictId > 0) {
      queryBuilder.append(" AND  federal_district =" + federalDistrictId);
    }
    if (localDistrictId != null && localDistrictId > 0) {
      queryBuilder.append(" AND local_district =" + localDistrictId);
    }
    if (electionPositionId != null && electionPositionId > 0) {
      queryBuilder.append(" AND ep.id = " + electionPositionId);
    }

    if (statusValidateId != null && statusValidateId > 0) {
      queryBuilder.append(" AND sv.id = " + statusValidateId);
    }
    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());

    try {
      objectRes = query.getResultList();
      if (objectRes != null && !objectRes.isEmpty()) {
        Object[] obj = objectRes.get(0);

        report.setTotalDigitalValidate((obj[0] != null ? Integer.parseInt(obj[0].toString()) : 0));
        report.setTotalFisicalValidate((obj[1] != null ? Integer.parseInt(obj[1].toString()) : 0));
        report.setTotalSheets((obj[2] != null ? Integer.parseInt(obj[2].toString()) : 0));
      }
    } catch (Exception e) {
      System.err.println("Error al obtener las getValideSheet:" + e.getMessage());
    }
    return report;
  }

  public List<PollingBoothDto> findAllPollingBoothImproved() {
    List<PollingBoothDto> resultList = new ArrayList<>();
    List<Object[]> objectRes = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append(
        "    select "
            + "        p1_0.id, "
            + "    p1_0.seccion_number, "
            + "    p1_0.seccion_id, "
            + "    p1_0.polling_booth_id, "
            + "    p1_0.state, "
            + "        p1_0.state_id, "
            + "        p1_0.municipality, "
            + "        p1_0.municipality_id, "
            + "        p1_0.federal_district, "
            + "        p1_0.federal_district_name, "
            + "        p1_0.local_district,     "
            + "    p1_0.address, "
            + "        p1_0.reference_address,         "
            + "        p1_0.voters_registry,  "
            + "        p1_0.voters_list, "
            + "        p1_0.approved_board, "
            + "        p1_0.approved_committee, "
            + "        p1_0.polling_booth_key, "
            + "        p1_0.polling_booth_number,     "
            + "    p1_0.polling_booth_address_type_id, "
            + "    p1_0.polling_booth_type_id, "
            + "        p1_0.polling_booth_category_id, "
            + "        p1_0.date_status_open,     "
            + "        p1_0.date_closed, "
            + "        p1_0.polling_booth_status_id, "
            + "        p1_0.latitude, "
            + "        p1_0.longitude "
            + "    from "
            + "        polling_booths p1_0 "
            + "  order by id ASC");

    log.info("Query: " + queryBuilder);

    Query query = em.createNativeQuery(queryBuilder.toString());
    try {

      List<PollingBoothAddressTypeEntity> pollingBoothAddressTypeEntities =
          pollingBoothAddressTypeRepository.findAll();

      List<PollingBoothTypeEntity> pollingBoothTypeEntities = pollingBoothTypeRepository.findAll();

      List<PollingBoothCategoryEntity> pollingBoothCategoryEntities =
          pollingBoothCategoryRepository.findAll();

      List<PollingBoothStatusEntity> pollingBoothStatusEntities =
          pollingBoothStatusRepository.findAll();

      objectRes = query.getResultList();

      List<PollingBoothIssuesEntity> pollingBoothIssuesEntities =
          pollingBoothIssuesRepository.findAll();
      int count = 1;
      for (Object[] obj : objectRes) {
        PollingBoothDto edd = new PollingBoothDto();
        edd.setId(obj[0] != null ? (Long) obj[0] : 0);
        edd.setSeccionNumber(obj[1] != null ? (Integer) obj[1] : null);
        edd.setSeccionId(obj[2] != null ? (Integer) obj[2] : null);
        edd.setPollingBoothId(obj[3] != null ? obj[3].toString() : "");
        edd.setState(obj[4] != null ? obj[4].toString() : "");
        edd.setStateId(obj[5] != null ? (Integer) obj[5] : null);
        edd.setMunicipality(obj[6] != null ? obj[6].toString() : "");
        edd.setMunicipalityId(obj[7] != null ? (Integer) obj[7] : null);
        edd.setFederalDistrict(obj[8] != null ? (Integer) obj[8] : null);
        edd.setFederalDistrictName(obj[9] != null ? obj[9].toString() : "");
        edd.setLocalDistrict(obj[10] != null ? (Integer) obj[10] : null);
        edd.setAddress(obj[11] != null ? obj[11].toString() : "");
        edd.setReferenceAddress(obj[12] != null ? obj[12].toString() : "");
        edd.setVotersRegistry(obj[13] != null ? (Integer) obj[13] : null);
        edd.setVotersList(obj[14] != null ? (Integer) obj[14] : null);
        edd.setApprovedBoard(obj[15] != null ? obj[15].toString() : "");
        edd.setApprovedCommittee(obj[16] != null ? obj[16].toString() : "");
        edd.setPollingBoothKey(obj[17] != null ? obj[17].toString() : "");
        edd.setPollingBoothNumber(obj[18] != null ? obj[18].toString() : "");
        Long pollingBoothAddressTypeId = obj[19] != null ? (Long) obj[19] : 0;

        PollingBoothAddressTypeDto pollingBoothAddressTypeDto =
            findPollingBoothAddressTypeDtoById(
                pollingBoothAddressTypeEntities, pollingBoothAddressTypeId);
        edd.setPollingBoothAddressType(pollingBoothAddressTypeDto);

        Long pollingBoothTypeId = obj[20] != null ? (Long) obj[20] : 0;

        PollingBoothTypeDto pollingBoothTypeDto =
            findPollingBoothTypeDtoById(pollingBoothTypeEntities, pollingBoothTypeId);
        edd.setPollingBoothType(pollingBoothTypeDto);

        Long pollingBoothCategoryId = obj[21] != null ? (Long) obj[21] : 0;

        PollingBoothCategoryDto pollingBoothCategoryDto =
            findPollingBoothCategoryDtoById(pollingBoothCategoryEntities, pollingBoothCategoryId);
        edd.setPollingBoothCategory(pollingBoothCategoryDto);

        Timestamp dateTimestamp = (Timestamp) obj[22];
        edd.setDateStatusOpen(dateTimestamp != null ? dateTimestamp.toLocalDateTime() : null);

        Timestamp dateClosed = (Timestamp) obj[23];
        edd.setDateClosed(dateClosed != null ? dateClosed.toLocalDateTime() : null);

        Long pollingBoothStatusId = obj[24] != null ? (Long) obj[24] : 0;

        PollingBoothStatusDto pollingBoothStatusDto =
            findPollingBoothStatusDtoById(pollingBoothStatusEntities, pollingBoothStatusId);
        edd.setPollingBoothStatus(pollingBoothStatusDto);

        edd.setLatitude(obj[25] != null ? obj[25].toString() : "");
        edd.setLongitude(obj[26] != null ? obj[26].toString() : "");

        // List<PollingBoothIssuesDto> pollingBoothIssuesDto =
        // findPollingBoothIssuesById(pollingBoothIssuesEntities, edd.getId());
        // edd.setPollingBoothIssuesList(pollingBoothIssuesDto);

        resultList.add(edd);
      }

    } catch (Exception e) {
      System.err.println("Error al obtener las findByTotalIncidents:" + e.getMessage());
    }
    return resultList;
  }

  private PollingBoothAddressTypeDto findPollingBoothAddressTypeDtoById(
      List<PollingBoothAddressTypeEntity> entities, Long id) {
    PollingBoothAddressTypeDto foundDto = new PollingBoothAddressTypeDto();
    List<PollingBoothAddressTypeDto> dtos =
        pollingBoothAddressTypeMapper.convertToDtoList(entities);

    Optional<PollingBoothAddressTypeDto> optionalDto =
        dtos.stream().filter(dto -> dto.getId() == id).findFirst();

    if (optionalDto.isPresent()) {
      foundDto = optionalDto.get();
    } else {
      System.out.println("No se encontró un DTO con el ID especificado.");
    }
    return foundDto;
  }

  private PollingBoothTypeDto findPollingBoothTypeDtoById(
      List<PollingBoothTypeEntity> entities, Long id) {
    PollingBoothTypeDto foundDto = new PollingBoothTypeDto();
    List<PollingBoothTypeDto> dtos = pollingBoothTypeMapper.convertToDtoList(entities);

    Optional<PollingBoothTypeDto> optionalDto =
        dtos.stream().filter(dto -> dto.getId() == id).findFirst();

    if (optionalDto.isPresent()) {
      foundDto = optionalDto.get();
    } else {
      System.out.println("No se encontró un DTO con el ID especificado.");
    }
    return foundDto;
  }

  private PollingBoothCategoryDto findPollingBoothCategoryDtoById(
      List<PollingBoothCategoryEntity> entities, Long id) {
    PollingBoothCategoryDto foundDto = new PollingBoothCategoryDto();
    List<PollingBoothCategoryDto> dtos = pollingBoothCategoryMapper.convertToDtoList(entities);

    Optional<PollingBoothCategoryDto> optionalDto =
        dtos.stream().filter(dto -> dto.getId() == id).findFirst();

    if (optionalDto.isPresent()) {
      foundDto = optionalDto.get();
    } else {
      System.out.println("No se encontró un DTO con el ID especificado.");
    }
    return foundDto;
  }

  private PollingBoothStatusDto findPollingBoothStatusDtoById(
      List<PollingBoothStatusEntity> entities, Long id) {
    PollingBoothStatusDto foundDto = new PollingBoothStatusDto();
    List<PollingBoothStatusDto> dtos = pollingBoothStatusMapper.convertToDtoList(entities);

    Optional<PollingBoothStatusDto> optionalDto =
        dtos.stream().filter(dto -> dto.getId() == id).findFirst();

    if (optionalDto.isPresent()) {
      foundDto = optionalDto.get();
    } else {
      System.out.println("No se encontró un DTO con el ID especificado.");
    }
    return foundDto;
  }

  private List<PollingBoothIssuesDto> findPollingBoothIssuesById(
      List<PollingBoothIssuesEntity> entities, Long id) {
    List<PollingBoothIssuesDto> dtos = new ArrayList<>();

    dtos = pollingBoothIssuesMapper.convertToDtoList(entities);

    List<PollingBoothIssuesDto> filteredDtos =
        dtos.stream()
            .filter(dto -> dto.getPollingBoothId().equals(id))
            .collect(Collectors.toList());

    return filteredDtos;
  }
}
