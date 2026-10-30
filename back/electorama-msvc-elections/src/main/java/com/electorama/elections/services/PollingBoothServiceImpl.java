package com.electorama.elections.services;

import com.electorama.elections.facade.PollingBoothFacade;
import com.electorama.elections.facade.PollingBoothStatusFacade;
import com.electorama.elections.facade.ReportMunicipalityFacade;
import com.electorama.elections.facade.ReportSectionsFacade;
import com.electorama.elections.models.dto.*;
import com.electorama.elections.models.entity.PollingBoothEntity;
import com.electorama.elections.models.entity.PollingBoothIssuesEntity;
import com.electorama.elections.models.mapper.*;
import com.electorama.elections.models.repository.*;
import com.electorama.elections.services.rest.GeographyRestClient;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class PollingBoothServiceImpl implements PollingBoothService {

  @Autowired PollingBoothRepository pollingBoothRepository;

  @Autowired PollingBoothIssuesRepository pollingBoothIssuesRepository;
  @Autowired PollingBoothStatusRepository pollingBoothStatusRepository;
  @Autowired PollingBoothAddressTypeRepository pollingBoothAddressTypeRepository;
  @Autowired PollingBoothCategoryRepository pollingBoothCategoryRepository;
  @Autowired PollingBoothTypeRepository pollingBoothTypeRepository;

  @Autowired ReportMunicipalityFacade reportMunicipalityFacade;

  @Autowired ReportSectionsFacade reportSectionsFacade;

  @Autowired GeographyRestClient geographyRestClient;
  @Autowired ElectionRepository electionRepository;

  @Autowired StatusValidateRepository statusValidateRepository;

  @Autowired PoliticalPartyRepository politicalPartyRepository;
  @Autowired PollingBoothElectionAssociationRepository pollingBoothElectionAssociationRepository;
  private final PollingBoothMapper pollingBoothMapper = new PollingBoothMapper();
  private final PollingBoothStatusMapper pollingBoothStatusMapper = new PollingBoothStatusMapper();
  private final PollingBoothAddressTypeMapper pollingBoothAddressTypeMapper =
      new PollingBoothAddressTypeMapper();
  private final PollingBoothCategoryMapper pollingBoothCategoryMapper =
      new PollingBoothCategoryMapper();

  private final PollingBoothTypeMapper pollingBoothTypeMapper = new PollingBoothTypeMapper();

  private final StatusCandidateMapper statusCandidateMapper = new StatusCandidateMapper();

  @Autowired CandidatePartyAssociationRepository candidatePartyAssociationRepository;
  @Autowired CandidateRepository candidateRepository;

  @Autowired PollingBoothFacade pollingBoothFacade;

  @Autowired PollingBoothStatusFacade pollingBoothStatusFacade;

  @Override
  public Page<PollingBoothDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      Integer status,
      Integer categoryId,
      Integer typeId,
      Integer addressTypeId,
      String authId) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<PollingBoothDto> dtoPage = null;

    if (search.length() >= 1) {
      if (StringUtils.hasText(search) && Pattern.matches("\\d+", search)) {
        log.info("por numero de seccion...");
        dtoPage =
            pollingBoothMapper.convertToDtoPage(
                pollingBoothRepository.findBySectionNumber(Integer.parseInt(search), paging));
      } else {
        log.info("busqueda por cadena...");
        dtoPage =
            pollingBoothMapper.convertToDtoPage(
                pollingBoothRepository.findAllByParametros(search, search, search, paging));
      }
    } else if (status > 0 && search.isEmpty()) {
      log.info("por status...");
      dtoPage =
          pollingBoothMapper.convertToDtoPage(
              pollingBoothRepository.findByPollingBoothStatus_Id(status, paging));
    } else if (categoryId > 0 && search.isEmpty()) {
      log.info("por categoria...");
      dtoPage =
          pollingBoothMapper.convertToDtoPage(
              pollingBoothRepository.findByPollingBoothCategory_Id(categoryId, paging));
    } else if (typeId > 0 && search.isEmpty()) {
      log.info("por tipo...");
      dtoPage =
          pollingBoothMapper.convertToDtoPage(
              pollingBoothRepository.findByPollingBoothType_Id(typeId, paging));
    } else if (addressTypeId > 0 && search.isEmpty()) {
      log.info("por tipo de direccion...");
      dtoPage =
          pollingBoothMapper.convertToDtoPage(
              pollingBoothRepository.findByPollingBoothAddressType_Id(addressTypeId, paging));
    } else {
      log.info("otra...");
      dtoPage = pollingBoothMapper.convertToDtoPage(pollingBoothRepository.findAll(paging));
    }

    return dtoPage;
  }

  @Override
  public List<PollingBoothDto> findAllPollingBooth() {
    return new ArrayList<>(pollingBoothMapper.convertToDto(pollingBoothRepository.findAll()));
  }

  @Override
  public Optional<PollingBoothDto> findById(Long pollingBoothId) {
    return pollingBoothRepository.findById(pollingBoothId).map(pollingBoothMapper::convertToDto);
  }

  @Override
  public List<PollingBoothStatusDto> catPollingBoothStatus() {
    return new ArrayList<>(
        pollingBoothStatusMapper.convertToDto(
            pollingBoothStatusRepository.findAllByOrderByIdAsc()));
  }

  @Override
  public List<PollingBoothAddressTypeDto> catPollingBoothAddressType() {
    return new ArrayList<>(
        pollingBoothAddressTypeMapper.convertToDto(
            pollingBoothAddressTypeRepository.findAllByOrderByIdAsc()));
  }

  @Override
  public List<PollingBoothCategoryDto> catPollingBoothCategory() {
    return new ArrayList<>(
        pollingBoothCategoryMapper.convertToDto(
            pollingBoothCategoryRepository.findAllByOrderByIdAsc()));
  }

  @Override
  public List<PollingBoothTypeDto> catPollingBoothType() {
    return new ArrayList<>(
        pollingBoothTypeMapper.convertToDto(pollingBoothTypeRepository.findAllByOrderByIdAsc()));
  }

  @Override
  public Page<PollingBoothDto> findByFederalAndLocalDistrictPaging(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      Integer federalDistrictId,
      Integer localDistrictId,
      String authId) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<PollingBoothDto> dtoPage = null;

    if (search.length() >= 1) {
      log.info("por busqueda federal district...");
      dtoPage =
          pollingBoothMapper.convertToDtoPage(
              pollingBoothRepository.findByFederalDistrictPaging(
                  search, search, search, federalDistrictId, paging));
    }

    if (federalDistrictId != null && federalDistrictId > 0) {
      log.info("federal district...");
      dtoPage =
          pollingBoothMapper.convertToDtoPage(
              pollingBoothRepository.findByFederalDistrict(federalDistrictId, paging));
    } else if (localDistrictId != null && localDistrictId > 0) {
      log.info("local district...");
      dtoPage =
          pollingBoothMapper.convertToDtoPage(
              pollingBoothRepository.findByLocalDistrict(localDistrictId, paging));
    }

    return dtoPage;
  }

  @Override
  public List<PollingBoothDto> findByFederalDistrict(Integer federalDistricId) {
    return new ArrayList<>(
        pollingBoothMapper.convertToDto(
            pollingBoothRepository.findByFederalDistrict(federalDistricId)));
  }

  @Override
  public Page<PollingBoothDto> findByMunicipalityPaging(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      Integer municipalityId,
      String authId) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<PollingBoothDto> dtoPage = null;

    if (search.length() >= 1) {
      log.info("por busqueda municipality...");
      dtoPage =
          pollingBoothMapper.convertToDtoPage(
              pollingBoothRepository.findByMunicipalityPaging(
                  search, search, search, municipalityId, paging));
    } else {
      log.info("solo municipality...");
      dtoPage =
          pollingBoothMapper.convertToDtoPage(
              pollingBoothRepository.findByMunicipalityId(municipalityId, paging));
    }

    return dtoPage;
  }

  @Override
  public List<PollingBoothDto> findByMunicipality(Integer municipalityId) {
    return new ArrayList<>(
        pollingBoothMapper.convertToDto(
            pollingBoothRepository.findByMunicipalityId(municipalityId)));
  }

  @Override
  public Optional<PollingBoothDto> byId(Long id, String authId) {
    return Optional.of(
        pollingBoothMapper.convertToDto(
            pollingBoothRepository
                .findById(id)
                .orElseThrow(
                    () ->
                        new EntityNotFoundException("PollingBooth no encontrado con ID: " + id))));
  }

  public PollingBoothDto save(PollingBoothDto pollingBoothDto, String authId) {

    PollingBoothEntity pollingBoothEntity =
        pollingBoothMapper.convertToEntity(pollingBoothDto, authId);

    PollingBoothIssuesEntity issuesEntity =
        pollingBoothIssuesRepository.findFirstByPollingBoothId(pollingBoothEntity.getId());

    if (issuesEntity != null) {
      if (pollingBoothEntity.getPollingBoothStatus().getId() == 1L) {
        pollingBoothEntity.getPollingBoothStatus().setId(3L);
      } else if (pollingBoothEntity.getPollingBoothStatus().getId() == 2L) {
        pollingBoothEntity.getPollingBoothStatus().setId(4L);
      }
    }

    PollingBoothDto dto =
        pollingBoothMapper.convertToDto(pollingBoothRepository.save(pollingBoothEntity));

    return dto;
  }

  @Override
  public List<ResponseMunicipalityReportDto> getReportMunicipalitiesByEntity() {
    return reportMunicipalityFacade.getReportMunicipality();
  }

  @Override
  public List<ResponseSectionReportDto> getReportSectionByMunicipalityId(Long municipalityId) {
    return reportSectionsFacade.getReportSectionByMunicipalityId(municipalityId);
  }

  @Override
  public List<ResponseSectionReportByDistrictFederalDto> getReportSectionByDistrictFederalId(
      Long districtFederalId) {
    return reportSectionsFacade.getReportSectionByDistrictFederalId(districtFederalId);
  }

  @Override
  public List<PollingsBoothsFederalDistrictDto> getPollingsBoothsByFederalDistrict() {
    List<PollingsBoothsFederalDistrictDto> result = new ArrayList<>();

    List<Object[]> objects = pollingBoothRepository.getPollingsBoothsByFederalDistrict();

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long federalDistrict = ((Number) objArray[0]).longValue();
      String federalDistrictName = objArray[1].toString();
      long pollingBoothOpen = ((Number) objArray[2]).longValue();
      long pollingBoothClosed = ((Number) objArray[3]).longValue();
      long total = ((Number) objArray[4]).longValue();
      PollingsBoothsFederalDistrictDto dto = new PollingsBoothsFederalDistrictDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setFederalDistrictId(federalDistrict);
      dto.setFederalDistrictName(federalDistrictName);
      dto.setTotalPollingBoothsOpened(pollingBoothOpen);
      dto.setTotalPollingBoothsClosed(pollingBoothClosed);
      dto.setTotal(total);

      result.add(dto);
    }
    return result;
  }

  @Override
  public List<PollingsBoothsLocalDistrictDto> getPollingsBoothsByLocalDistrict() {
    List<PollingsBoothsLocalDistrictDto> result = new ArrayList<>();

    List<Object[]> objects = pollingBoothRepository.getPollingsBoothsByLocalDistrict();

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long localDistrict = ((Number) objArray[0]).longValue();
      long pollingBoothOpen = ((Number) objArray[1]).longValue();
      long pollingBoothClosed = ((Number) objArray[2]).longValue();
      long total = ((Number) objArray[3]).longValue();
      PollingsBoothsLocalDistrictDto dto = new PollingsBoothsLocalDistrictDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setLocalDistrictId(localDistrict);
      dto.setTotalPollingBoothsOpened(pollingBoothOpen);
      dto.setTotalPollingBoothsClosed(pollingBoothClosed);
      dto.setTotal(total);

      result.add(dto);
    }
    return result;
  }

  @Override
  public List<PollingsBoothsByMunicipalityDto> getPollingsBoothsByMunicipality() {
    List<PollingsBoothsByMunicipalityDto> result = new ArrayList<>();

    List<Object[]> objects = pollingBoothRepository.getPollingsBoothsByMunicipality();

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long municipality = ((Number) objArray[0]).longValue();
      String municipalityName = objArray[1].toString();
      long pollingBoothOpen = ((Number) objArray[2]).longValue();
      long pollingBoothClosed = ((Number) objArray[3]).longValue();
      long total = ((Number) objArray[4]).longValue();
      PollingsBoothsByMunicipalityDto dto = new PollingsBoothsByMunicipalityDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setMunicipalityId(municipality);
      dto.setMunicipalityName(municipalityName);
      dto.setTotalPollingBoothsOpened(pollingBoothOpen);
      dto.setTotalPollingBoothsClosed(pollingBoothClosed);
      dto.setTotal(total);

      result.add(dto);
    }
    return result;
  }

  @Override
  public List<PollingsBoothsSectionAndMunicipalityByLocalDistrictReportDto>
      getReportSectionByDistrictLocalId(Long localDistrictId) {
    List<PollingsBoothsSectionAndMunicipalityByLocalDistrictReportDto> result = new ArrayList<>();

    List<Object[]> objects =
        pollingBoothRepository.getReportSectionByDistrictLocalId(localDistrictId);

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long localDistrict = ((Number) objArray[0]).longValue();
      long municipality = ((Number) objArray[1]).longValue();
      String municipalityName = objArray[2].toString();
      long sectionNumber = ((Number) objArray[3]).longValue();
      long pollingBoothOpen = ((Number) objArray[4]).longValue();
      long pollingBoothClosed = ((Number) objArray[5]).longValue();
      long total = ((Number) objArray[6]).longValue();
      PollingsBoothsSectionAndMunicipalityByLocalDistrictReportDto dto =
          new PollingsBoothsSectionAndMunicipalityByLocalDistrictReportDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setLocalDistrictId(localDistrict);
      dto.setMunicipalityId(municipality);
      dto.setMunicipalityName(municipalityName);
      dto.setSectionNumber(sectionNumber);
      dto.setTotalPollingBoothsOpened(pollingBoothOpen);
      dto.setTotalPollingBoothsClosed(pollingBoothClosed);
      dto.setTotal(total);

      result.add(dto);
    }
    return result;
  }

  @Override
  public List<PollingBoothDto> getPollingBoothBySection(Long sectionNumber) {
    return new ArrayList<>(pollingBoothMapper.convertToDto(pollingBoothRepository.findAll()));
  }

  @Override
  public List<StatusValidateDto> getAllStatusValidate() {
    return statusCandidateMapper.convertToDtoList(statusValidateRepository.findAll());
  }

  @Override
  public Page<PollingBoothResultDto> getPollingBooths(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long electionTypeId,
      Long electionPositionId,
      Long statusValidateId) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<PollingBoothResultDto> result = null;

    result =
        pollingBoothFacade.findByParameters(
            municipalityId,
            sectionNum,
            federalDistrictId,
            localDistrictId,
            electionTypeId,
            electionPositionId,
            statusValidateId,
            paging);

    return result;
  }

  @Override
  public Page<PollingBoothStatusResultDto> getPollingBoothsStatus(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long statusId) {
    Pageable paging;
    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<PollingBoothStatusResultDto> result = null;

    result =
        pollingBoothStatusFacade.findByParameters(
            municipalityId, sectionNum, federalDistrictId, localDistrictId, statusId, paging);

    return result;
  }

  @Override
  public CountPollingBoothDto getCountPollingsBooths(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long statusId) {
    CountPollingBoothDto result = new CountPollingBoothDto();

    result =
        pollingBoothFacade.getCountPollingsBooths(
            municipalityId, sectionNum, federalDistrictId, localDistrictId, statusId);

    return result;
  }

  @Override
  public ValidateSheetDto getValideSheet(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long statusValidateId,
      Long electionPositionId) {
    ValidateSheetDto result = new ValidateSheetDto();

    result =
        pollingBoothFacade.getValideSheet(
            municipalityId,
            sectionNum,
            federalDistrictId,
            localDistrictId,
            statusValidateId,
            electionPositionId);

    return result;
  }

  @Override
  public List<PollingBoothDto> findAllPollingBoothImproved() {
    List<PollingBoothDto> resultList = new ArrayList<>();
    resultList = pollingBoothFacade.findAllPollingBoothImproved();
    return resultList;
  }
}
