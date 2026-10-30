package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.*;
import com.electorama.msvc.survey.model.dto.request.RequestFolios;
import com.electorama.msvc.survey.model.dto.request.RequestSections;
import com.electorama.msvc.survey.model.entity.*;
import com.electorama.msvc.survey.model.mapper.*;
import com.electorama.msvc.survey.model.repository.*;
import com.electorama.msvc.survey.model.rest.UserProfileResponse;
import com.electorama.msvc.survey.service.rest.UsersRestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SurveyUserServiceImpl implements SurveyUserService {

  @Autowired private SurveyUserRepository surveyUserRepository;
  @Autowired private SurveyUserSectionRepository surveyUserSectionRepository;

  @Autowired private SurveyUserFolioRepository surveyUserFolioRepository;

  @Autowired private SurveySectionGoalRepository surveySectionGoalRepository;
  @Autowired private UsersRestClient usuariosRestClient;

  @Autowired private SurveyRepository surveyRepository;

  @Autowired private SurveyAnnexRepository surveyAnnexRepository;
  private final SurveyUserMapper surveyUserMapper = new SurveyUserMapper();

  private final SurveySectionGoalMapper surveySectionGoalMapper = new SurveySectionGoalMapper();

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

  private final SurveyHousingServiceMapper surveyHousingServiceMapper =
      new SurveyHousingServiceMapper();

  private final SurveyCulturalActivityMapper surveyCulturalActivityMapper =
      new SurveyCulturalActivityMapper();

  private final SurveyFamilyRelationshipMapper surveyFamilyRelationshipMapper =
      new SurveyFamilyRelationshipMapper();

  private final SurveyIndigenousLanguageMapper surveyIndigenousLanguageMapper =
      new SurveyIndigenousLanguageMapper();

  private final SurveyOriginOfDisabilityMapper surveyOriginOfDisabilityMapper =
      new SurveyOriginOfDisabilityMapper();

  private final SurveyDisabilityInformationMapper surveyDisabilityInformationMapper =
      new SurveyDisabilityInformationMapper();
  private final SurveyTypeOfDisabilityMapper surveyTypeOfDisabilityMapper =
      new SurveyTypeOfDisabilityMapper();
  private final SurveyCausesOfDisabilityMapper surveyCausesOfDisabilityMapper =
      new SurveyCausesOfDisabilityMapper();
  private final SurveyTutorOrAssistantMapper surveyTutorOrAssistantMapper =
      new SurveyTutorOrAssistantMapper();
  private final SurveyRequestingOrAuxiliaryDocumentMapper
      surveyRequestingOrAuxiliaryDocumentMapper = new SurveyRequestingOrAuxiliaryDocumentMapper();

  @Override
  public SurveyUserSectionListDto createUser(UserProfileResponse user) {
    SurveyUserEntity entity = new SurveyUserEntity();
    SurveyUserSectionListDto dto = new SurveyUserSectionListDto();

    // vamos a verificar si existe
    Optional<SurveyUserEntity> findUsr = surveyUserRepository.findByUserName(user.getEmail());

    if (findUsr.isPresent()) {
      List<SurveyUserSectionEntity> findSection =
          surveyUserSectionRepository.findBySurveyUserId(findUsr.get().getId());
      if (findSection.size() > 0) {
        List<SurveyUserSectionDto> userSectionDtos = new ArrayList<>();

        for (SurveyUserSectionEntity section : findSection) {
          SurveyUserSectionDto userSectionDto = new SurveyUserSectionDto();
          // Asignar valores de section a userSectionDto según corresponda
          userSectionDto.setId(section.getId());
          userSectionDto.setSectionId(section.getSectionId());
          userSectionDto.setSurveyUserId(section.getSurveyUserId());
          userSectionDto.setSectionNumber(section.getSectionNumber());
          // Añadir userSectionDto a la lista de DTOs
          userSectionDtos.add(userSectionDto);
        }

        dto.setSurveyUserSection(userSectionDtos);
      }

      dto.setId(findUsr.get().getId());
      dto.setUserName(findUsr.get().getUserName());
      dto.setName(findUsr.get().getName());
      dto.setDeviceId(findUsr.get().getDeviceId());
      dto.setAuthId(findUsr.get().getAuthId());

    } else {
      UserProfileResponse registerUser = usuariosRestClient.registerUser(user);

      entity.setUserName(registerUser.getEmail());
      entity.setAuthId(registerUser.getAuthId());
      entity.setDeviceId(user.getDeviceId());
      entity.setMunicipalyId(user.getMunicipalityId());
      entity.setName(registerUser.getFirstName() + " " + registerUser.getLastName());
      entity.setMunicipalityName(user.getMunicipalityName());
      SurveyUserEntity savedEntity = surveyUserRepository.save(entity);

      dto.setId(savedEntity.getId());
      dto.setUserName(registerUser.getEmail());
      dto.setName(registerUser.getFirstName() + " " + registerUser.getLastName());
      dto.setDeviceId(user.getDeviceId());
      dto.setAuthId(registerUser.getAuthId());
    }

    return dto;
  }

  @Override
  public List<SurveyUserDto> listUsersMunicipality(String authId) {
    List<SurveyUserDto> surveyUserDtos =
        surveyUserMapper.convertToDtoList(
            surveyUserRepository.findBySurveyMunicipalityAuthId(authId));

    return surveyUserDtos;
  }

  @Override
  public String assignSection(RequestSections request) {

    Optional<SurveyUserEntity> surveyUserOutEntity =
        surveyUserRepository.findByAuthIdAndSurveyUserSection_SectionNumber(
            request.getSurveyUserAuthOuthId(), request.getSectionOutId());

    if (!surveyUserOutEntity.isPresent()) {
      throw new RuntimeException("El usuario y la sección no existen");
    }

    Optional<SurveyUserEntity> surveyUserInEntity =
        surveyUserRepository.findByAuthId(request.getSurveyUserAuthInId());

    Optional<SurveyUserSectionEntity> section =
        surveyUserSectionRepository.findBySurveyUserIdAndSectionNumber(
            surveyUserOutEntity.get().getId(), request.getSectionOutId());

    if (!section.isPresent()) {
      throw new IllegalArgumentException("El Usuario Id no se puede asignar a esta sección");
    }

    if (!surveyUserInEntity.isPresent()) {
      throw new RuntimeException("El usuario no existe");
    }

    Optional<SurveyUserSectionEntity> section1 =
        surveyUserSectionRepository.findBySurveyUserIdAndSectionNumber(
            surveyUserInEntity.get().getId(), request.getSectionOutId());

    if (section1.isPresent()) {
      throw new IllegalArgumentException("El Usuario ya tiene asignada esta sección");
    }

    SurveyUserSectionEntity surveyUserSection = new SurveyUserSectionEntity();

    surveyUserSection.setSurveyUserId(surveyUserInEntity.get().getId());
    surveyUserSection.setSectionNumber(section.get().getSectionNumber());
    surveyUserSection.setSectionId(section.get().getSectionId());

    surveyUserSectionRepository.save(surveyUserSection);

    return "OK";
  }

  @Override
  public List<String> assignFolio(RequestFolios request) {

    if (request.getFolioStart() > request.getFolioEnd()) {
      throw new IllegalArgumentException("El folio inicial debe ser menor o igual al folio final.");
    }
    List<String> range =
        IntStream.rangeClosed(request.getFolioStart(), request.getFolioEnd())
            .boxed()
            .map(String::valueOf) // Convert Integer to String
            .collect(Collectors.toList());

    List<SurveyEntity> surveyEntity = surveyRepository.findBySurveyUserFolio_FolioIn(range);

    if (surveyEntity.size() > 0) {
      throw new IllegalArgumentException(
          "El rango de los folios proporcionado ya esta asignado a una o varias encuesta ");
    }

    Optional<SurveyUserEntity> survey = surveyUserRepository.findById(request.getSurveyUserId());

    if (survey.isPresent()) {
      List<SurveyUserFolioEntity> surveyUserFolioEntity =
          surveyUserFolioRepository.findBySurveyUserIdAndFolioIn(survey.get().getId(), range);
      /****
       * if (surveyUserFolioEntity.size() != range.size()) {
       * throw new IllegalArgumentException("Existe algún folio no asignado al usuario.");
       * }***/
    } else {
      throw new IllegalArgumentException("El usuario para asignar folios no existe ");
    }
    Optional<SurveyUserEntity> surveyUserInEntity =
        surveyUserRepository.findById(request.getSurveyUserId());

    if (!surveyUserInEntity.isPresent()) {
      throw new RuntimeException("El usuario no existe");
    }
    surveyUserFolioRepository.updateFolio(
        surveyUserInEntity.get().getId(),
        request.getFolioStart().toString(),
        request.getFolioEnd().toString());

    return range;
  }

  public Boolean deleteSectionNumber(Long id) {
    Optional<SurveyUserSectionEntity> surveyUserSection = surveyUserSectionRepository.findById(id);

    if (surveyUserSection.get().getIsOwner() == true) {
      throw new RuntimeException("Esta sección no se puede borrar ya que tiene dueño");
    }
    if (surveyUserSection != null) {
      surveyUserSectionRepository.deleteById(id);
    }

    return true;
  }

  @Override
  public List<SurveySectionGoalDto> listSectionMunicipality(Long municipalityId) {
    List<SurveySectionGoalDto> surveyUserDtos =
        surveySectionGoalMapper.convertToDtoList(
            surveySectionGoalRepository.findByMunicipalityId(municipalityId));

    return surveyUserDtos;
  }

  @Override
  public Optional<SurveyUserSurveyDto> listUsersSurvey(String authId) {
    SurveyUserSurveyDto surveyUserDtos = new SurveyUserSurveyDto();

    // Buscar al usuario por authId
    Optional<SurveyUserEntity> surveyUserEntityOpt = surveyUserRepository.findByAuthId(authId);

    // Verificar si el usuario fue encontrado
    if (!surveyUserEntityOpt.isPresent()) {
      return Optional.empty();
    }

    SurveyUserEntity surveyUserEntity = surveyUserEntityOpt.get();

    // Llenar los datos de SurveyUserSurveyDto con la información de SurveyUserEntity
    surveyUserDtos.setId(surveyUserEntity.getId());
    surveyUserDtos.setAuthId(surveyUserEntity.getAuthId());
    surveyUserDtos.setDeviceId(surveyUserEntity.getDeviceId());
    surveyUserDtos.setName(surveyUserEntity.getName());
    surveyUserDtos.setUserName(surveyUserEntity.getUserName());
    surveyUserDtos.setUserPersonId(surveyUserEntity.getUserPersonId());
    surveyUserDtos.setMunicipalyId(surveyUserEntity.getMunicipalyId());
    surveyUserDtos.setMunicipalityName(surveyUserEntity.getMunicipalityName());

    // Buscar las encuestas asociadas al usuario
    List<SurveyEntity> surveyEntities =
        surveyRepository.findBySurveyUser_Id(surveyUserEntity.getId());

    // Convertir la lista de SurveyEntity a SurveyDto
    List<SurveyDto> surveyDtos =
        surveyEntities.stream()
            .map(
                surveyEntity -> {
                  SurveyDto surveyDtoSalida = new SurveyDto();

                  surveyDtoSalida.setId(surveyEntity.getId());
                  surveyDtoSalida.setDeviceId(surveyEntity.getDeviceId());
                  //               surveyDto.setSurveyUserFolio(
                  //
                  // surveyUserFolioMapper.convertToEntity(surveyDto.getSurveyUserFolio()));
                  surveyDtoSalida.setFolioHouse(surveyEntity.getFolioHouse());
                  surveyDtoSalida.setSurveyDate(surveyEntity.getSurveyDate());
                  surveyDtoSalida.setLatitude(surveyEntity.getLatitude());
                  surveyDtoSalida.setLongitude(surveyEntity.getLongitude());
                  surveyDtoSalida.setPathPhotoIneAdverse(surveyEntity.getPathPhotoIneAdverse());
                  surveyDtoSalida.setPathPhotoIneReverse(surveyEntity.getPathPhotoIneReverse());
                  surveyDtoSalida.setPathPhotoFacadeQr(surveyEntity.getPathPhotoFacadeQr());
                  surveyDtoSalida.setPathPhotoFacadeQr(surveyEntity.getPathPhotoFacadeQr());
                  surveyDtoSalida.setMunicipalityId(surveyEntity.getMunicipalityId());
                  surveyDtoSalida.setMunicipality(surveyEntity.getMunicipality());
                  surveyDtoSalida.setLocalityId(surveyEntity.getLocalityId());
                  surveyDtoSalida.setLocality(surveyEntity.getLocality());
                  surveyDtoSalida.setLocalityOther(surveyEntity.getLocalityOther());
                  surveyDtoSalida.setStreet(surveyEntity.getStreet());
                  surveyDtoSalida.setExteriorNumber(surveyEntity.getExteriorNumber());
                  surveyDtoSalida.setInteriorNumber(surveyEntity.getInteriorNumber());
                  surveyDtoSalida.setPostalCode(surveyEntity.getPostalCode());
                  surveyDtoSalida.setReference(surveyEntity.getReference());
                  surveyDtoSalida.setFamilyNumbers(surveyEntity.getFamilyNumbers());
                  surveyDtoSalida.setHousingOther(surveyEntity.getHousingOther());
                  surveyDtoSalida.setSurveyHousing(
                      surveyHousingMapper.convertToDto(surveyEntity.getSurveyHousing()));
                  surveyDtoSalida.setSurveyHousingCondition(
                      surveyHousingConditionMapper.convertToDto(
                          surveyEntity.getSurveyHousingCondition()));
                  surveyDtoSalida.setSurveyHousingDocument(
                      surveyHousingDocumentMapper.convertToDto(
                          surveyEntity.getSurveyHousingDocument()));
                  surveyDtoSalida.setSurveyHousingMaterialWall(
                      surveyHousingMaterialWallMapper.convertToDto(
                          surveyEntity.getSurveyHousingMaterialWall()));
                  surveyDtoSalida.setSurveyHousingMaterialRoof(
                      surveyHousingMaterialRoofMapper.convertToDto(
                          surveyEntity.getSurveyHousingMaterialRoof()));
                  surveyDtoSalida.setSurveyHousingMaterialFloor(
                      surveyHousingMaterialFloorMapper.convertToDto(
                          surveyEntity.getSurveyHousingMaterialFloor()));
                  surveyDtoSalida.setSurveyHousingServicesEntities(
                      surveyHousingServiceMapper.convertToDtoList(
                          surveyEntity.getSurveyHousingServicesEntities()));
                  surveyDtoSalida.setCulturalActivityParticipation(
                      surveyDtoSalida.getCulturalActivityParticipation());
                  surveyDtoSalida.setKnowledgeCulturalActivity(
                      surveyEntity.getKnowledgeCulturalActivity());
                  surveyDtoSalida.setSurveyCulturalActivity(
                      surveyCulturalActivityMapper.convertToDto(
                          surveyEntity.getSurveyCulturalActivity()));
                  surveyDtoSalida.setOtherTypeCulturalActivity(
                      surveyEntity.getOtherTypeCulturalActivity());
                  surveyDtoSalida.setStatusChange(surveyEntity.getStatusChange());

                  // Mapear otros campos que necesites...
                  return surveyDtoSalida;
                })
            .collect(Collectors.toList());

    // Asignar la lista de encuestas a SurveyUserSurveyDto
    surveyUserDtos.setSurvey(surveyDtos);

    return Optional.of(surveyUserDtos);
  }

  @Override
  public Optional<SurveyAnnexReportDto> listSurveyAnnex(Long id) {
    SurveyAnnexReportDto surveyDtos = new SurveyAnnexReportDto();

    // Buscar al usuario por authId
    Optional<SurveyEntity> surveyEntityOpt = surveyRepository.findById(id);

    // Verificar si el usuario fue encontrado
    if (!surveyEntityOpt.isPresent()) {
      return Optional.empty();
    }

    surveyDtos.setId(surveyEntityOpt.get().getId());
    surveyDtos.setFolioHouse(surveyEntityOpt.get().getFolioHouse());
    surveyDtos.setSurveyDate(surveyEntityOpt.get().getSurveyDate());
    surveyDtos.setLatitude(surveyEntityOpt.get().getLatitude());
    surveyDtos.setDeviceId(surveyEntityOpt.get().getDeviceId());
    surveyDtos.setPathPhotoIdentificationAdverse(surveyEntityOpt.get().getPathPhotoIneAdverse());
    surveyDtos.setPathPhotoIdentificationReverse(surveyEntityOpt.get().getPathPhotoIneReverse());
    surveyDtos.setPathPhotoFacadeQr(surveyEntityOpt.get().getPathPhotoFacadeQr());
    surveyDtos.setMunicipalityId(surveyEntityOpt.get().getMunicipalityId());
    surveyDtos.setMunicipality(surveyEntityOpt.get().getMunicipality());

    surveyDtos.setLocalityId(surveyEntityOpt.get().getLocalityId());
    surveyDtos.setLocality(surveyEntityOpt.get().getLocality());
    surveyDtos.setLocalityOther(surveyEntityOpt.get().getLocalityOther());
    surveyDtos.setStreet(surveyEntityOpt.get().getStreet());
    surveyDtos.setExteriorNumber(surveyEntityOpt.get().getExteriorNumber());
    surveyDtos.setInteriorNumber(surveyEntityOpt.get().getInteriorNumber());
    surveyDtos.setPostalCode(surveyEntityOpt.get().getPostalCode());
    surveyDtos.setReference(surveyEntityOpt.get().getReference());
    surveyDtos.setFamilyNumbers(surveyEntityOpt.get().getFamilyNumbers());
    surveyDtos.setHousingOther(surveyEntityOpt.get().getHousingOther());
    surveyDtos.setSurveyHousing(
        surveyHousingMapper.convertToDto(surveyEntityOpt.get().getSurveyHousing()));
    surveyDtos.setSurveyHousingCondition(
        surveyHousingConditionMapper.convertToDto(
            surveyEntityOpt.get().getSurveyHousingCondition()));
    surveyDtos.setSurveyHousingDocument(
        surveyHousingDocumentMapper.convertToDto(surveyEntityOpt.get().getSurveyHousingDocument()));
    surveyDtos.setSurveyHousingMaterialWall(
        surveyHousingMaterialWallMapper.convertToDto(
            surveyEntityOpt.get().getSurveyHousingMaterialWall()));
    surveyDtos.setSurveyHousingMaterialRoof(
        surveyHousingMaterialRoofMapper.convertToDto(
            surveyEntityOpt.get().getSurveyHousingMaterialRoof()));
    surveyDtos.setSurveyHousingMaterialFloor(
        surveyHousingMaterialFloorMapper.convertToDto(
            surveyEntityOpt.get().getSurveyHousingMaterialFloor()));
    surveyDtos.setSurveyHousingServicesEntities(
        surveyHousingServiceMapper.convertToDtoList(
            surveyEntityOpt.get().getSurveyHousingServicesEntities()));
    surveyDtos.setCulturalActivityParticipation(
        surveyEntityOpt.get().getCulturalActivityParticipation());
    surveyDtos.setKnowledgeCulturalActivity(surveyEntityOpt.get().getKnowledgeCulturalActivity());
    surveyDtos.setSurveyCulturalActivity(
        surveyCulturalActivityMapper.convertToDto(
            surveyEntityOpt.get().getSurveyCulturalActivity()));
    surveyDtos.setOtherTypeCulturalActivity(surveyEntityOpt.get().getOtherTypeCulturalActivity());
    surveyDtos.setStatusChange(surveyEntityOpt.get().getStatusChange());

    // Buscar las encuestas asociadas al usuario
    List<SurveyAnnexEntity> surveyAnnexEntity = surveyAnnexRepository.findBySurveyId(id);

    // Convertir la lista de SurveyEntity a SurveyDto
    List<SurveyAnnexDto> annexDtos =
        surveyAnnexEntity.stream()
            .map(
                surveyEntity -> {
                  SurveyAnnexDto surveyDtoSalida = new SurveyAnnexDto();
                  surveyDtoSalida.setId(surveyEntity.getId());
                  surveyDtoSalida.setSurveyPersonId(surveyEntity.getSurveyPersonId());
                  surveyDtoSalida.setSurveyId(surveyEntity.getSurveyId());
                  surveyDtoSalida.setEthnicOrAfromexicanGroup(
                      surveyEntity.getEthnicOrAfromexicanGroup());
                  surveyDtoSalida.setWhichGroup(surveyEntity.getWhichGroup());
                  surveyDtoSalida.setSpeaksIndigenousLanguage(
                      surveyEntity.getSpeaksIndigenousLanguage());
                  surveyDtoSalida.setOtherSpeak(surveyEntity.getOtherSpeak());
                  surveyDtoSalida.setHasADisability(surveyEntity.getHasADisability());
                  surveyDtoSalida.setDesabilityOrigin(surveyEntity.getDesabilityOrigin());
                  surveyDtoSalida.setDesabililtyInformation(
                      surveyEntity.getDesabililtyInformation());
                  surveyDtoSalida.setDesabilityType(surveyEntity.getDesabilityType());
                  surveyDtoSalida.setCausesDisability(surveyEntity.getCausesDisability());
                  surveyDtoSalida.setAnotherCause(surveyEntity.getAnotherCause());
                  surveyDtoSalida.setHealthCertificate(surveyEntity.getHealthCertificate());
                  surveyDtoSalida.setWhichCertificate(surveyEntity.getWhichCertificate());
                  surveyDtoSalida.setRequiriesHelp(surveyEntity.getRequiriesHelp());
                  surveyDtoSalida.setNamesAux(surveyEntity.getNamesAux());
                  surveyDtoSalida.setLastNameAux(surveyEntity.getLastNameAux());
                  surveyDtoSalida.setSurNameAux(surveyEntity.getSurNameAux());
                  surveyDtoSalida.setFamilyRelationship(
                      surveyFamilyRelationshipMapper.convertToDto(
                          surveyEntity.getFamilyRelationship()));
                  surveyDtoSalida.setOtherRelationShip(surveyEntity.getOtherRelationShip());
                  surveyDtoSalida.setPhoneNumber(surveyEntity.getPhoneNumber());
                  surveyDtoSalida.setRequestingDocumentIne(surveyEntity.getRequestingDocumentIne());
                  surveyDtoSalida.setRequestingDocumentMinutes(
                      surveyEntity.getRequestingDocumentMinutes());
                  surveyDtoSalida.setRequestingDocumentCurp(
                      surveyEntity.getRequestingDocumentCurp());
                  surveyDtoSalida.setRequestingDocumentProofAddress(
                      surveyEntity.getRequestingDocumentProofAddress());
                  surveyDtoSalida.setRequestingAuxiliarIne(surveyEntity.getRequestingAuxiliarIne());
                  surveyDtoSalida.setRequestingAuxiliarMinutes(
                      surveyEntity.getRequestingAuxiliarMinutes());
                  surveyDtoSalida.setRequestingAuxiliarCurp(
                      surveyEntity.getRequestingAuxiliarCurp());
                  surveyDtoSalida.setRequestingAuxiliarProofAddress(
                      surveyEntity.getRequestingAuxiliarProofAddress());
                  surveyDtoSalida.setSurveyIndigenousLanguage(
                      surveyIndigenousLanguageMapper.convertToDto(
                          surveyEntity.getSurveyIndigenousLanguage()));
                  surveyDtoSalida.setSurveyOriginOfDisability(
                      surveyOriginOfDisabilityMapper.convertToDto(
                          surveyEntity.getSurveyOriginOfDisability()));
                  surveyDtoSalida.setSurveyDisabilityInformation(
                      surveyDisabilityInformationMapper.convertToDto(
                          surveyEntity.getSurveyDisabilityInformation()));
                  surveyDtoSalida.setSurveyTypeOfDisability(
                      surveyTypeOfDisabilityMapper.convertToDto(
                          surveyEntity.getSurveyTypeOfDisability()));
                  surveyDtoSalida.setSurveyCausesOfDisability(
                      surveyCausesOfDisabilityMapper.convertToDto(
                          surveyEntity.getSurveyCausesOfDisability()));
                  surveyDtoSalida.setSurveyTutorOrAssistant(
                      surveyTutorOrAssistantMapper.convertToDto(
                          surveyEntity.getSurveyTutorOrAssistant()));
                  surveyDtoSalida.setSurveyRequestingOrAuxiliaryDocument(
                      surveyRequestingOrAuxiliaryDocumentMapper.convertToDto(
                          surveyEntity.getSurveyRequestingOrAuxiliaryDocument()));
                  surveyDtoSalida.setPathFileIdentificationApplicant(
                      surveyEntity.getPathFileIdentificationApplicant());
                  surveyDtoSalida.setPathFileBirthCertificateApplicant(
                      surveyEntity.getPathFileBirthCertificateApplicant());
                  surveyDtoSalida.setPathFileCurpApplicant(surveyEntity.getPathFileCurpApplicant());
                  surveyDtoSalida.setPathFileAddressApplicant(
                      surveyEntity.getPathFileAddressApplicant());
                  surveyDtoSalida.setPathFileIdentificationAssistant(
                      surveyEntity.getPathFileIdentificationAssistant());
                  surveyDtoSalida.setPathFileBirthCertificateAssistant(
                      surveyEntity.getPathFileBirthCertificateAssistant());
                  surveyDtoSalida.setPathFileCurpAssistant(surveyEntity.getPathFileCurpAssistant());
                  surveyDtoSalida.setPathFileAddressAssistant(
                      surveyEntity.getPathFileAddressAssistant());
                  // surveyDtoSalida.setSurveyP

                  return surveyDtoSalida;
                })
            .collect(Collectors.toList());

    // Asignar la lista de encuestas a SurveyUserSurveyDto
    surveyDtos.setSurveyAnnex(annexDtos);

    return Optional.of(surveyDtos);
  }
}
