package com.electorama.elections.services;

import com.electorama.elections.models.dto.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface PollingBoothService {

  Page<PollingBoothDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      Integer status,
      Integer categoryId,
      Integer typeId,
      Integer addressTypeId,
      String authId);

  List<PollingBoothDto> findAllPollingBooth();

  Optional<PollingBoothDto> findById(Long pollingBoothId);

  List<PollingBoothStatusDto> catPollingBoothStatus();

  List<PollingBoothAddressTypeDto> catPollingBoothAddressType();

  List<PollingBoothCategoryDto> catPollingBoothCategory();

  List<PollingBoothTypeDto> catPollingBoothType();

  Page<PollingBoothDto> findByFederalAndLocalDistrictPaging(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      Integer federalDistrictId,
      Integer localDistrictId,
      String authId);

  List<PollingBoothDto> findByFederalDistrict(Integer federalDistricId);

  Page<PollingBoothDto> findByMunicipalityPaging(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      Integer municipalityId,
      String authId);

  List<PollingBoothDto> findByMunicipality(Integer municipalityId);

  Optional<PollingBoothDto> byId(Long id, String authId);

  PollingBoothDto save(PollingBoothDto pollingBoothDto, String authId);

  List<ResponseMunicipalityReportDto> getReportMunicipalitiesByEntity();

  List<ResponseSectionReportDto> getReportSectionByMunicipalityId(Long municipalityId);

  List<ResponseSectionReportByDistrictFederalDto> getReportSectionByDistrictFederalId(
      Long municipalityId);

  List<PollingsBoothsFederalDistrictDto> getPollingsBoothsByFederalDistrict();

  List<PollingsBoothsLocalDistrictDto> getPollingsBoothsByLocalDistrict();

  List<PollingsBoothsByMunicipalityDto> getPollingsBoothsByMunicipality();

  List<PollingsBoothsSectionAndMunicipalityByLocalDistrictReportDto>
      getReportSectionByDistrictLocalId(Long localDistrictId);

  List<PollingBoothDto> getPollingBoothBySection(Long sectionNumber);

  List<StatusValidateDto> getAllStatusValidate();

  Page<PollingBoothResultDto> getPollingBooths(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId,
      Long statusId);

  Page<PollingBoothStatusResultDto> getPollingBoothsStatus(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long statusId);

  CountPollingBoothDto getCountPollingsBooths(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long statusId);

  ValidateSheetDto getValideSheet(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long statusValidateId,
      Long electionPositionId);

  List<PollingBoothDto> findAllPollingBoothImproved();
}
