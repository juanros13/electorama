package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.DateDto;
import com.electorama.msvc.survey.model.dto.SurveyDto;
import com.electorama.msvc.survey.model.dto.SurveyPersonDto;
import com.electorama.msvc.survey.model.dto.SurveyUserFolioDto;
import com.electorama.msvc.survey.model.dto.request.RequestSurveyFolio;
import com.electorama.msvc.survey.model.dto.request.RequestSurveys;
import com.electorama.msvc.survey.model.dto.request.RequestSurveysRangeFolios;
import com.electorama.msvc.survey.model.dto.response.ResponseSurveyRangeFolios;
import com.electorama.msvc.survey.model.entity.SurveyEntity;
import com.electorama.msvc.survey.model.entity.SurveyPersonEntity;
import com.electorama.msvc.survey.model.entity.SurveyUserEntity;
import com.electorama.msvc.survey.model.entity.SurveyUserFolioEntity;
import com.electorama.msvc.survey.model.mapper.*;
import com.electorama.msvc.survey.model.repository.SurveyRepository;
import com.electorama.msvc.survey.model.repository.SurveyUserFolioRepository;
import com.electorama.msvc.survey.model.repository.SurveyUserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SurveyServiceImpl implements SurveyService {

  @Autowired private SurveyRepository surveyRepository;

  @Autowired private DropboxService dropboxService;

  @Autowired private SurveyUserFolioRepository surveyUserFolioRepository;

  @Autowired private SurveyUserRepository surveyUserRepository;

  private final SurveyMapper surveyMapper = new SurveyMapper();
  private final SurveyCulturalActivityMapper surveyCulturalActivityMapper =
      new SurveyCulturalActivityMapper();
  private final SurveyHousingServiceMapper surveyHousingServiceMapper =
      new SurveyHousingServiceMapper();
  private final SurveyUserFolioMapper surveyUserFolioMapper = new SurveyUserFolioMapper();
  private final SurveyPersonMapper surveyPersonMapper = new SurveyPersonMapper();
  private final SurveyUserMapper surveyUserMapper = new SurveyUserMapper();
  private final SurveyAnnexMapper surveyAnnexMapper = new SurveyAnnexMapper();
  private final SurveyHousingMapper surveyHousingMapper = new SurveyHousingMapper();
  private final SurveyHousingConditionMapper surveyHousingConditionMapper =
      new SurveyHousingConditionMapper();
  private final SurveyHousingDocumentMapper surveyHousingDocumentMapper =
      new SurveyHousingDocumentMapper();
  private final SurveyHousingMaterialWallMapper surveyHousingMaterialWallMapper =
      new SurveyHousingMaterialWallMapper();
  private final SurveyHousingMaterialRoofMapper surveyHousingMaterialRoofMapper =
      new SurveyHousingMaterialRoofMapper();
  private final SurveyHousingMaterialFloorMapper surveyHousingMaterialFloorMapper =
      new SurveyHousingMaterialFloorMapper();

  private final SurveyJobMapper surveyJobMapper = new SurveyJobMapper();

  private final SurveyProfessionMapper surveyProfessionMapper = new SurveyProfessionMapper();

  @Override
  public RequestSurveys save(RequestSurveys requestSurveys) {

    List<SurveyDto> savedSurveys =
        requestSurveys.getSurveyDtos().stream()
            .map(
                surveyDto -> {
                  SurveyEntity surveyEntity = surveyMapper.convertToEntity(surveyDto);
                  SurveyEntity savedEntity = surveyRepository.save(surveyEntity);
                  return surveyMapper.convertToDto(savedEntity);
                })
            .collect(Collectors.toList());

    requestSurveys.setSurveyDtos(savedSurveys);
    return requestSurveys;
  }

  public Page<SurveyDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange) {
    Pageable paging =
        PageRequest.of(
            pageNo,
            pageSize,
            orderBy.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending());
    String formattedDate = "";
    if (sgeSurveyDate != null) {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      formattedDate = formatter.format(sgeSurveyDate);
    }

    Page<SurveyEntity> entityPage = surveyRepository.searchByMultipleParameters(deviceId, paging);
    return entityPage.map(surveyMapper::convertToDto);
  }

  @Override
  public Page<SurveyDto> listFolio(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange,
      String authIdHeader) {
    Pageable paging =
        PageRequest.of(
            pageNo,
            pageSize,
            orderBy.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending());
    String formattedDate = "";
    if (sgeSurveyDate != null) {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      formattedDate = formatter.format(sgeSurveyDate);
    }
    if (!search.isEmpty()) {
      log.info("search...");
      if (authIdHeader.equals("103607ac-9852-43d7-825a-4217c8f7a088")) {
        // nacajuca
        Page<SurveyEntity> entityPage =
            surveyRepository.getSurveyNacajucaWithSearch(search, paging);
        return entityPage.map(surveyMapper::convertToDto);

      } else if (authIdHeader.equals("46720757-b2c5-43ee-98fa-9096dde6001f")) {
        // centro
        Page<SurveyEntity> entityPage = surveyRepository.getSurveyCentroWithSearch(search, paging);
        return entityPage.map(surveyMapper::convertToDto);
      } else {
        // otros
        Page<SurveyEntity> entityPage =
            surveyRepository.findBySurveyUser_AuthIdAndSurveyUserFolio_FolioAndStreetIsNull(
                authIdHeader, search, paging);
        return entityPage.map(surveyMapper::convertToDto);
      }

    } else {
      if (authIdHeader.equals("103607ac-9852-43d7-825a-4217c8f7a088")) {
        // nacajuca
        paging = PageRequest.of(pageNo, pageSize);
        Page<SurveyEntity> entityPage = surveyRepository.searchSurveyNacajucaWithoutSearch(paging);
        return entityPage.map(surveyMapper::convertToDto);

      } else if (authIdHeader.equals("46720757-b2c5-43ee-98fa-9096dde6001f")) {
        // centro
        Page<SurveyEntity> entityPage = surveyRepository.getSurveyCentroWithoutSearch(paging);
        return entityPage.map(surveyMapper::convertToDto);
      } else {
        Page<SurveyEntity> entityPage =
            surveyRepository.searchByAuthIdAndNotStreet(authIdHeader, paging);
        return entityPage.map(surveyMapper::convertToDto);
      }
    }
  }

  @Override
  public Page<SurveyDto> listFolioComplete(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange,
      String authIdHeader) {
    Pageable paging =
        PageRequest.of(
            pageNo,
            pageSize,
            orderBy.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending());
    String formattedDate = "";
    if (sgeSurveyDate != null) {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      formattedDate = formatter.format(sgeSurveyDate);
    }
    if (!search.isEmpty()) {
      if (authIdHeader.equals("103607ac-9852-43d7-825a-4217c8f7a088")) {
        // nacajuca
        Page<SurveyEntity> entityPage =
            surveyRepository.getSurveyNacajucaWithSearchComplete(search, paging);
        return entityPage.map(surveyMapper::convertToDto);

      } else if (authIdHeader.equals("46720757-b2c5-43ee-98fa-9096dde6001f")) {
        // centro
        Page<SurveyEntity> entityPage =
            surveyRepository.getSurveyCentroWithSearchComplete(search, paging);
        return entityPage.map(surveyMapper::convertToDto);
      } else {
        Page<SurveyEntity> entityPage =
            surveyRepository.findBySurveyUser_AuthIdAndSurveyUserFolio_FolioAndStreetIsNotNull(
                authIdHeader, search, paging);
        return entityPage.map(surveyMapper::convertToDto);
      }

    } else {
      log.info("search complete...");
      if (authIdHeader.equals("103607ac-9852-43d7-825a-4217c8f7a088")) {
        // nacajuca
        paging = PageRequest.of(pageNo, pageSize);
        Page<SurveyEntity> entityPage =
            surveyRepository.searchSurveyNacajucaWithoutSearchComplete(paging);
        return entityPage.map(surveyMapper::convertToDto);

      } else if (authIdHeader.equals("46720757-b2c5-43ee-98fa-9096dde6001f")) {
        // centro
        Page<SurveyEntity> entityPage =
            surveyRepository.getSurveyCentroWithoutSearchComplete(paging);
        return entityPage.map(surveyMapper::convertToDto);
      } else {
        Page<SurveyEntity> entityPage =
            surveyRepository.searchByAuthIdAndStreet(authIdHeader, paging);
        return entityPage.map(surveyMapper::convertToDto);
      }
    }
  }

  @Override
  public Page<SurveyDto> listFolioCompleteMunicipality(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String deviceId,
      String surveyorName,
      Date sgeSurveyDate,
      Boolean statusChange,
      String authIdHeader) {
    Pageable paging =
        PageRequest.of(
            pageNo,
            pageSize,
            orderBy.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending());
    String formattedDate = "";
    if (sgeSurveyDate != null) {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      formattedDate = formatter.format(sgeSurveyDate);
    }
    if (!search.isEmpty()) {
      Page<SurveyEntity> entityPage =
          surveyRepository
              .findBySurveyUser_SurveyMunicipalityAuthIdAndSurveyUserFolio_FolioAndStreetIsNotNull(
                  authIdHeader, search, paging);
      return entityPage.map(surveyMapper::convertToDto);
    } else {
      log.info("search complete...");
      Page<SurveyEntity> entityPage =
          surveyRepository.searchBySurveyMunicipalityAuthIdAndStreet(authIdHeader, paging);
      return entityPage.map(surveyMapper::convertToDto);
    }
  }

  @Override
  public Page<SurveyDto> list(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<SurveyDto> dtoPage = null;

    if (search.length() >= 1) {
      dtoPage =
          surveyMapper.convertToDtoPage(
              surveyRepository.findByParameters(search, search, search, paging));
    } else {
      dtoPage = surveyMapper.convertToDtoPage(surveyRepository.findAll(paging));
    }

    dtoPage =
        new PageImpl<>(
            dtoPage.stream()
                .map(
                    page -> {
                      try {
                      } catch (Exception e) {

                      }
                      return page;
                    })
                .collect(Collectors.toList()),
            dtoPage.getPageable(),
            dtoPage.getTotalElements());

    return dtoPage;
  }

  @Override
  public Optional<SurveyDto> findById(Long id) {
    return surveyRepository.findById(id).map(surveyMapper::convertToDto);
  }

  @Override
  public Optional<SurveyDto> update(Long id, SurveyDto requestSurveys) {
    return surveyRepository
        .findById(id)
        .map(
            existingSurvey -> {
              SurveyDto surveyDto = requestSurveys;

              if (surveyDto.getDeviceId() != null) {
                existingSurvey.setDeviceId(surveyDto.getDeviceId());
              }
              if (surveyDto.getSurveyUserFolio() != null) {
                existingSurvey.setSurveyUserFolio(
                    surveyUserFolioMapper.convertToEntity(surveyDto.getSurveyUserFolio()));
              }
              if (surveyDto.getFolioHouse() != null) {
                existingSurvey.setFolioHouse(surveyDto.getFolioHouse());
              }
              if (surveyDto.getSurveyDate() != null) {
                existingSurvey.setSurveyDate(surveyDto.getSurveyDate());
              }
              if (surveyDto.getLatitude() != null) {
                existingSurvey.setLatitude(surveyDto.getLatitude());
              }
              if (surveyDto.getLongitude() != null) {
                existingSurvey.setLongitude(surveyDto.getLongitude());
              }
              if (surveyDto.getSurveyUser() != null) {
                existingSurvey.setSurveyUser(
                    surveyUserMapper.convertToEntity(surveyDto.getSurveyUser()));
              }

              // existingSurvey.setPathPhotoIneAdverse(surveyDto.getPathPhotoIneAdverse());
              // existingSurvey.setPathPhotoIneReverse(surveyDto.getPathPhotoIneReverse());
              // existingSurvey.setPathPhotoFacade(surveyDto.getPathPhotoFacade());
              // existingSurvey.setPathPhotoFacadeQr(surveyDto.getPathPhotoFacadeQr());
              if (surveyDto.getLocalityId() != null) {
                existingSurvey.setLocalityId(surveyDto.getLocalityId());
              }
              if (surveyDto.getLocality() != null) {
                existingSurvey.setLocality(surveyDto.getLocality());
              }
              if (surveyDto.getLocalityOther() != null) {
                existingSurvey.setLocalityOther(surveyDto.getLocalityOther());
              }
              if (surveyDto.getStreet() != null) {
                existingSurvey.setStreet(surveyDto.getStreet());
              }
              if (surveyDto.getExteriorNumber() != null) {
                existingSurvey.setExteriorNumber(surveyDto.getExteriorNumber());
              }
              if (surveyDto.getInteriorNumber() != null) {
                existingSurvey.setInteriorNumber(surveyDto.getInteriorNumber());
              }
              if (surveyDto.getPostalCode() != null) {
                existingSurvey.setPostalCode(surveyDto.getPostalCode());
              }
              if (surveyDto.getReference() != null) {
                existingSurvey.setReference(surveyDto.getReference());
              }
              if (surveyDto.getSurveyPersonEntityList() != null) {
                List<SurveyPersonEntity> newList = new ArrayList<>();

                surveyDto
                    .getSurveyPersonEntityList()
                    .forEach(
                        person -> {
                          if (person.getId() != null) {
                            SurveyPersonEntity surveyPersonEntity =
                                surveyPersonMapper.convertToEntity(person);
                            surveyPersonEntity.setId(person.getId());
                            surveyPersonEntity.setCurp(person.getCurp());
                            surveyPersonEntity.setNames(person.getNames());
                            surveyPersonEntity.setLastName(person.getLastName());
                            surveyPersonEntity.setSurName(person.getSurName());
                            surveyPersonEntity.setGender(person.getGender());
                            surveyPersonEntity.setIncome(person.getIncome());
                            surveyPersonEntity.setLandlinePhone(person.getLandlinePhone());
                            surveyPersonEntity.setMobilePhone(person.getMobilePhone());
                            surveyPersonEntity.setOwnBusiness(person.getOwnBusiness());
                            surveyPersonEntity.setIsHeadOfHousehold(person.getIsHeadOfHousehold());
                            surveyPersonEntity.setIsDisabled(person.getIsDisabled());
                            surveyPersonEntity.setAfroMexicanGroup(person.getAfroMexicanGroup());
                            surveyPersonEntity.setEthnicGroup(person.getEthnicGroup());
                            surveyPersonEntity.setIdentificationDocument(
                                person.getIdentificationDocument());
                            surveyPersonEntity.setNoIdentificationDocument(
                                person.getNoIdentificationDocument());
                            surveyPersonEntity.setDateOfBirth(person.getDateOfBirth());
                            surveyPersonEntity.setDeleted(person.getDeleted());
                            if (person.getSurveyAnnex() != null) {
                              surveyPersonEntity.setSurveyAnnex(
                                  person.getSurveyAnnex().stream()
                                      .map(surveyAnnexMapper::convertToEntity)
                                      .collect(Collectors.toList()));
                              surveyPersonEntity.setSurvey(existingSurvey);
                            }
                            newList.add(surveyPersonEntity);
                          } else {
                            SurveyPersonEntity surveyPersonEntity =
                                surveyPersonMapper.convertToEntity(person);
                            surveyPersonEntity.setSurvey(existingSurvey);
                            newList.add(surveyPersonEntity);
                          }
                        });

                existingSurvey.setSurveyPersonEntityList(newList);
              }
              if (surveyDto.getFamilyNumbers() != null) {
                existingSurvey.setFamilyNumbers(surveyDto.getFamilyNumbers());
              }
              if (surveyDto.getHousingOther() != null) {
                existingSurvey.setHousingOther(surveyDto.getHousingOther());
              }
              if (surveyDto.getSurveyHousing() != null) {
                existingSurvey.setSurveyHousing(
                    surveyHousingMapper.convertToEntity(surveyDto.getSurveyHousing()));
              }
              if (surveyDto.getSurveyHousingCondition() != null) {
                existingSurvey.setSurveyHousingCondition(
                    surveyHousingConditionMapper.convertToEntity(
                        surveyDto.getSurveyHousingCondition()));
              }
              if (surveyDto.getSurveyHousingDocument() != null) {
                existingSurvey.setSurveyHousingDocument(
                    surveyHousingDocumentMapper.convertToEntity(
                        surveyDto.getSurveyHousingDocument()));
              }
              if (surveyDto.getSurveyHousingMaterialWall() != null) {
                existingSurvey.setSurveyHousingMaterialWall(
                    surveyHousingMaterialWallMapper.convertToEntity(
                        surveyDto.getSurveyHousingMaterialWall()));
              }
              if (surveyDto.getSurveyHousingMaterialRoof() != null) {
                existingSurvey.setSurveyHousingMaterialRoof(
                    surveyHousingMaterialRoofMapper.convertToEntity(
                        surveyDto.getSurveyHousingMaterialRoof()));
              }
              if (surveyDto.getSurveyHousingMaterialFloor() != null) {
                existingSurvey.setSurveyHousingMaterialFloor(
                    surveyHousingMaterialFloorMapper.convertToEntity(
                        surveyDto.getSurveyHousingMaterialFloor()));
              }

              existingSurvey.setSurveyHousingServicesEntities(
                  surveyDto.getSurveyHousingServicesEntities() != null
                      ? surveyHousingServiceMapper.convertToEntityList(
                          surveyDto.getSurveyHousingServicesEntities())
                      : Collections.emptyList() // O null, según lo que necesites
                  );

              if (surveyDto.getCulturalActivityParticipation() != null) {
                existingSurvey.setCulturalActivityParticipation(
                    surveyDto.getCulturalActivityParticipation());
              }

              if (surveyDto.getKnowledgeCulturalActivity() != null) {
                existingSurvey.setKnowledgeCulturalActivity(
                    surveyDto.getKnowledgeCulturalActivity());
              }

              if (surveyDto.getSurveyCulturalActivity() != null) {
                existingSurvey.setSurveyCulturalActivity(
                    surveyCulturalActivityMapper.convertToEntity(
                        surveyDto.getSurveyCulturalActivity()));
              }

              if (surveyDto.getOtherTypeCulturalActivity() != null) {
                existingSurvey.setOtherTypeCulturalActivity(
                    surveyDto.getOtherTypeCulturalActivity());
              }

              if (surveyDto.getStatusChange() != null) {
                existingSurvey.setStatusChange(surveyDto.getStatusChange());
              }

              List<SurveyPersonEntity> surveyPersonEntities =
                  surveyPersonMapper.convertToEntityList(surveyDto.getSurveyPersonEntityList());

              // Establece la relación bidireccional
              surveyPersonEntities.forEach(person -> person.setSurvey(existingSurvey));

              existingSurvey.setSurveyPersonEntityList(surveyPersonEntities);

              SurveyEntity updatedEntity = surveyRepository.save(existingSurvey);
              SurveyDto updatedRequest = new SurveyDto();
              updatedRequest = surveyMapper.convertToDto(updatedEntity);
              List<SurveyPersonEntity> surveyPerson =
                  updatedRequest.getSurveyPersonEntityList().stream()
                      .map(surveyPersonMapper::convertToEntity)
                      .collect(Collectors.toList());

              surveyPerson.removeIf(person -> person.getDeleted() != null);

              List<SurveyPersonDto> surveyPersonDtos =
                  surveyPerson.stream()
                      .map(surveyPersonMapper::convertToDto)
                      .collect(Collectors.toList());

              updatedRequest.setSurveyPersonEntityList(surveyPersonDtos);
              return updatedRequest;
            });
  }

  @Override
  public boolean changeStatus(Long id) {
    Optional<SurveyEntity> optionalSurveyEntity = surveyRepository.findById(id);
    if (optionalSurveyEntity.isPresent()) {
      SurveyEntity surveyEntity = optionalSurveyEntity.get();
      Integer currentStatus = surveyEntity.getStatusChange();
      surveyEntity.setStatusChange(currentStatus == null || currentStatus == 0 ? 1 : 0);
      surveyRepository.save(surveyEntity);
      return true;
    } else {
      throw new EntityNotFoundException("Survey not found with id: " + id);
    }
  }

  @Override
  public List<Object[]> getTotalSurveysPerWeek(DateDto dateDto) {
    return List.of();
  }

  @Override
  public List<Object[]> getTotalSurveysPerMunicipality() {
    return List.of();
  }

  @Override
  public List<Object[]> getTotalSurveysPerMunicipalityAndUser() {
    return List.of();
  }

  public DateDto calculateWeek(Integer numWeek) {
    DateDto dto = new DateDto();
    LocalDate now = LocalDate.now();

    LocalDate monday = null;
    LocalDate sunday = null;

    if (numWeek == 0) {
      // semana actual
      monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
      sunday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    } else if (numWeek == 1) {
      // semana pasada

    } else if (numWeek == 2) {
      // semana antepasada

    }

    dto.setStartDate(monday);
    dto.setEndDate(sunday);
    return dto;
  }

  @Override
  public Boolean createFolio(RequestSurveyFolio requestSurveyFolio) {

    Optional<SurveyUserEntity> surveyUserEntity =
        surveyUserRepository.findByAuthId(requestSurveyFolio.getUserAuthId());

    if (surveyUserEntity.isEmpty()) {
      return false;
    }
    // Checar que no el folio este asignado en esa seccion
    Optional<SurveyUserFolioEntity> checkIfFolioExist =
        surveyUserFolioRepository.findByFolioAndSurveyUserId(
            requestSurveyFolio.getFolio(), surveyUserEntity.get().getId());

    if (checkIfFolioExist.isPresent()) {
      Optional<SurveyEntity> checkIfFolioAssaigment =
          surveyRepository.findBySurveyUserFolio_FolioAndSurveyUserFolio_SurveyUserId(
              requestSurveyFolio.getFolio(), Long.valueOf(surveyUserEntity.get().getId()));
      if (!checkIfFolioAssaigment.isEmpty()) {
        return false;
      }
      Optional<SurveyUserEntity> checkIfUserExist =
          surveyUserRepository.findByAuthId(requestSurveyFolio.getUserAuthId());
      if (!checkIfUserExist.isPresent()) {
        return false;
      }
      SurveyEntity surveyEntity = new SurveyEntity();
      surveyEntity.setSurveyUserFolio(checkIfFolioExist.get());
      surveyEntity.setSurveyUser(checkIfUserExist.get());

      String path = "/CENSO2024/ARCHIVOS/";
      LocalDate currentDate = LocalDate.now();
      Random random = new Random();

      List<String> contentTypes =
          Arrays.asList("image/jpg", "image/jpeg", "image/png", "application/pdf");

      int randomWithNextInt = random.nextInt() & Integer.MAX_VALUE;
      String pathIneAdverse = "";
      String ext = "";
      String fileName = "";
      String pathFacadeQr = "";
      if (requestSurveyFolio.getPathPhotoIdentificationAdverse() == null) {
        surveyEntity.setPathPhotoIneAdverse("");
      } else {
        // INE ADVERSO
        ext =
            requestSurveyFolio
                .getPathPhotoIdentificationAdverse()
                .getOriginalFilename()
                .substring(
                    requestSurveyFolio
                        .getPathPhotoIdentificationAdverse()
                        .getOriginalFilename()
                        .lastIndexOf("."));
        fileName = "IDENTIFICATIONADVERSE_" + currentDate + "_" + randomWithNextInt + ext;

        try {
          pathIneAdverse =
              dropboxService.uploadFile(
                  requestSurveyFolio.getPathPhotoIdentificationAdverse(),
                  fileName,
                  path,
                  contentTypes);
        } catch (Exception e) {
          try {
            pathIneAdverse =
                dropboxService.uploadFile(
                    requestSurveyFolio.getPathPhotoIdentificationAdverse(),
                    fileName,
                    path,
                    contentTypes);
          } catch (Exception e2) {
            try {
              pathIneAdverse =
                  dropboxService.uploadFile(
                      requestSurveyFolio.getPathPhotoIdentificationAdverse(),
                      fileName,
                      path,
                      contentTypes);
            } catch (Exception e3) {
              try {
                pathIneAdverse =
                    dropboxService.uploadFile(
                        requestSurveyFolio.getPathPhotoIdentificationAdverse(),
                        fileName,
                        path,
                        contentTypes);
              } catch (Exception e4) {
                try {
                  pathIneAdverse =
                      dropboxService.uploadFile(
                          requestSurveyFolio.getPathPhotoIdentificationAdverse(),
                          fileName,
                          path,
                          contentTypes);
                } catch (Exception e5) {
                  return false;
                }
              }
            }
          }
        }
      }
      surveyEntity.setPathPhotoIneAdverse(pathIneAdverse);
      if (requestSurveyFolio.getPathPhotoIdentificationReverse() == null) {
        surveyEntity.setPathPhotoIneReverse("");
      } else {
        // INE REVERSO
        ext =
            requestSurveyFolio
                .getPathPhotoIdentificationReverse()
                .getOriginalFilename()
                .substring(
                    requestSurveyFolio
                        .getPathPhotoIdentificationReverse()
                        .getOriginalFilename()
                        .lastIndexOf("."));
        fileName = "IDENTIFICATIONREVERSE_" + currentDate + "_" + randomWithNextInt + ext;
        String pathIneReverse = "";
        try {
          pathIneReverse =
              dropboxService.uploadFile(
                  requestSurveyFolio.getPathPhotoIdentificationReverse(),
                  fileName,
                  path,
                  contentTypes);
          surveyEntity.setPathPhotoIneReverse(pathIneReverse);
        } catch (Exception e) {
          try {
            pathIneReverse =
                dropboxService.uploadFile(
                    requestSurveyFolio.getPathPhotoIdentificationReverse(),
                    fileName,
                    path,
                    contentTypes);
            surveyEntity.setPathPhotoIneReverse(pathIneReverse);
          } catch (Exception e2) {
            try {
              pathIneReverse =
                  dropboxService.uploadFile(
                      requestSurveyFolio.getPathPhotoIdentificationReverse(),
                      fileName,
                      path,
                      contentTypes);
              surveyEntity.setPathPhotoIneReverse(pathIneReverse);
            } catch (Exception e3) {
              try {
                pathIneReverse =
                    dropboxService.uploadFile(
                        requestSurveyFolio.getPathPhotoIdentificationReverse(),
                        fileName,
                        path,
                        contentTypes);
                surveyEntity.setPathPhotoIneReverse(pathIneReverse);
              } catch (Exception e4) {
                try {
                  pathIneReverse =
                      dropboxService.uploadFile(
                          requestSurveyFolio.getPathPhotoIdentificationReverse(),
                          fileName,
                          path,
                          contentTypes);
                  surveyEntity.setPathPhotoIneReverse(pathIneReverse);
                } catch (Exception e5) {
                  return false;
                }
              }
            }
          }
        }

        surveyEntity.setPathPhotoIneReverse(pathIneReverse);
      }

      // FOTO FACHADA QR

      if (requestSurveyFolio.getPathPhotoIdentificationReverse() == null) {
        surveyEntity.setPathPhotoIneReverse("");
      } else {
        ext =
            requestSurveyFolio
                .getPathPhotoFacadeQr()
                .getOriginalFilename()
                .substring(
                    requestSurveyFolio
                        .getPathPhotoFacadeQr()
                        .getOriginalFilename()
                        .lastIndexOf("."));
        fileName = "FACADEQR_" + currentDate + "_" + randomWithNextInt + ext;
        pathFacadeQr = "";
        try {
          pathFacadeQr =
              dropboxService.uploadFile(
                  requestSurveyFolio.getPathPhotoFacadeQr(), fileName, path, contentTypes);
        } catch (Exception e) {
          try {
            pathFacadeQr =
                dropboxService.uploadFile(
                    requestSurveyFolio.getPathPhotoFacadeQr(), fileName, path, contentTypes);
          } catch (Exception e2) {
            try {
              pathFacadeQr =
                  dropboxService.uploadFile(
                      requestSurveyFolio.getPathPhotoFacadeQr(), fileName, path, contentTypes);
            } catch (Exception e3) {
              try {
                pathFacadeQr =
                    dropboxService.uploadFile(
                        requestSurveyFolio.getPathPhotoFacadeQr(), fileName, path, contentTypes);
              } catch (Exception e4) {
                try {
                  pathFacadeQr =
                      dropboxService.uploadFile(
                          requestSurveyFolio.getPathPhotoFacadeQr(), fileName, path, contentTypes);
                } catch (Exception e5) {
                  return false;
                }
              }
            }
          }
        }
      }
      surveyEntity.setPathPhotoFacadeQr(pathFacadeQr);

      surveyEntity.setDeviceId(requestSurveyFolio.getDeviceId());
      surveyEntity.setFolioHouse(requestSurveyFolio.getFolioHouse());
      surveyEntity.setSurveyDate(Timestamp.valueOf(requestSurveyFolio.getSurveyDate()));
      surveyEntity.setLatitude(requestSurveyFolio.getLatitude());
      surveyEntity.setLongitude(requestSurveyFolio.getLongitude());
      surveyEntity.setMunicipality(requestSurveyFolio.getMunicipality());
      surveyEntity.setMunicipalityId(requestSurveyFolio.getMunicipalityId());
      surveyEntity.setSectionId(Long.valueOf(requestSurveyFolio.getSectionId()));
      surveyEntity.setSectionNumber(Long.valueOf(requestSurveyFolio.getSectionNumber()));

      surveyRepository.save(surveyEntity);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<ResponseSurveyRangeFolios> getRangeFolios(
      RequestSurveysRangeFolios requestSurveysRangeFolios) {

    List<ResponseSurveyRangeFolios> responseSurveyRangeFolios = new ArrayList<>();

    List<Object[]> objects =
        surveyRepository.getRangeFolioByAuthId(requestSurveysRangeFolios.getUserId());
    for (Object[] obj : objects) {
      String folioRange = (String) obj[0]; // Assuming the range is in the first element
      ResponseSurveyRangeFolios responseSurveyRangeFolio = new ResponseSurveyRangeFolios();
      responseSurveyRangeFolio.setRange(folioRange);
      responseSurveyRangeFolios.add(responseSurveyRangeFolio);
    }
    return responseSurveyRangeFolios;
  }

  @Override
  public List<ResponseSurveyRangeFolios> getRangeFoliosAvailables(
      RequestSurveysRangeFolios requestSurveysRangeFolios) {
    List<ResponseSurveyRangeFolios> responseSurveyRangeFolios = new ArrayList<>();

    List<Object[]> objects =
        surveyRepository.getRangeFolioAvailablesByAuthId(requestSurveysRangeFolios.getUserId());
    for (Object[] obj : objects) {
      String folioRange = (String) obj[0]; // Assuming the range is in the first element
      ResponseSurveyRangeFolios responseSurveyRangeFolio = new ResponseSurveyRangeFolios();
      responseSurveyRangeFolio.setRange(folioRange);
      responseSurveyRangeFolios.add(responseSurveyRangeFolio);
    }
    return responseSurveyRangeFolios;
  }

  @Override
  public List<SurveyUserFolioDto> getFolios(RequestSurveysRangeFolios requestSurveysRangeFolios) {
    return surveyUserFolioMapper.convertToDtoList(
        surveyUserFolioRepository.searchByAuthId(requestSurveysRangeFolios.getUserId()));
  }

  @Override
  public List<SurveyUserFolioDto> getFoliosAvailables(
      RequestSurveysRangeFolios requestSurveysRangeFolios) {
    return surveyUserFolioMapper.convertToDtoList(
        surveyUserFolioRepository.searchAvailablesByAuthId(requestSurveysRangeFolios.getUserId()));
  }
}
