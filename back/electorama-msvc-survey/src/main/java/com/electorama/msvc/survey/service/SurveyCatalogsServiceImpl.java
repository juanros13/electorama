package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.*;
import com.electorama.msvc.survey.model.entity.*;
import com.electorama.msvc.survey.model.mapper.*;
import com.electorama.msvc.survey.model.repository.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class SurveyCatalogsServiceImpl implements SurveyCatalogsService {

  @Autowired SurveyHousingRepository surveyHousingRepository;

  @Autowired SurveyHousingMaterialWallRepository surveyHousingMaterialWallRepository;

  @Autowired SurveyHousingMaterialFloorRepository surveyHousingMaterialFloorRepository;

  @Autowired SurveyHousingMaterialRoofRepository surveyHousingMaterialRoofRepository;

  @Autowired SurveyHousingConditionRepository surveyHousingConditionRepository;

  @Autowired SurveyHousingDocumentRepository surveyHousingDocumentRepository;

  @Autowired SurveyCulturalActivityRepository surveyCulturalActivityRepository;

  @Autowired SurveyJobRepository surveyJobRepository;

  @Autowired SurveyProfessionRepository surveyProfessionRepository;

  @Autowired SurveyFederalProgramRepository surveyFederalProgramRepository;

  @Autowired SurveyDisabilityRepository surveyDisabilityRepository;

  @Autowired SurveyFamilyRelationshipRepository surveyFamilyRelationshipRepository;

  @Autowired private SurveyIndigenousLanguageRepository indigenousLanguageRepository;

  @Autowired private SurveyOriginOfDisabilityRepository originOfDisabilityRepository;

  @Autowired private SurveyDisabilityInformationRepository disabilityInformationRepository;

  @Autowired private SurveyTypeOfDisabilityRepository typeOfDisabilityRepository;

  @Autowired private SurveyCausesOfDisabilityRepository causesOfDisabilityRepository;

  @Autowired private SurveyTutorOrAssistantRepository tutorOrAssistantRepository;

  @Autowired
  private SurveyRequestingOrAuxiliaryDocumentRepository requestingOrAuxiliaryDocumentRepository;

  @Autowired private SurveyHousingServiceRepository surveyHousingServiceRepository;
  private SurveyIndigenousLanguageMapper indigenousLanguageMapper =
      new SurveyIndigenousLanguageMapper();

  private final SurveyOriginOfDisabilityMapper originOfDisabilityMapper =
      new SurveyOriginOfDisabilityMapper();

  private final SurveyDisabilityInformationMapper disabilityInformationMapper =
      new SurveyDisabilityInformationMapper();

  private final SurveyTypeOfDisabilityMapper typeOfDisabilityMapper =
      new SurveyTypeOfDisabilityMapper();

  private final SurveyCausesOfDisabilityMapper causesOfDisabilityMapper =
      new SurveyCausesOfDisabilityMapper();

  private final SurveyTutorOrAssistantMapper tutorOrAssistantMapper =
      new SurveyTutorOrAssistantMapper();

  private final SurveyRequestingOrAuxiliaryDocumentMapper requestingOrAuxiliaryDocumentMapper =
      new SurveyRequestingOrAuxiliaryDocumentMapper();

  private final SurveyFamilyRelationshipMapper surveyFamilyRelationshipMapper =
      new SurveyFamilyRelationshipMapper();
  private final SurveyHousingMapper surveyHousingMapper = new SurveyHousingMapper();
  private final SurveyHousingMaterialWallMapper surveyHousingMaterialWallMapper =
      new SurveyHousingMaterialWallMapper();

  private final SurveyHousingMaterialFloorMapper surveyHousingMaterialFloorMapper =
      new SurveyHousingMaterialFloorMapper();

  private final SurveyHousingMaterialRoofMapper surveyHousingMaterialRoofMapper =
      new SurveyHousingMaterialRoofMapper();

  private final SurveyHousingConditionMapper surveyHousingConditionMapper =
      new SurveyHousingConditionMapper();

  private final SurveyHousingDocumentMapper surveyHousingDocumentMapper =
      new SurveyHousingDocumentMapper();

  private final SurveyCulturalActivityMapper surveyCulturalActivityMapper =
      new SurveyCulturalActivityMapper();

  private final SurveyJobMapper surveyJobMapper = new SurveyJobMapper();

  private final SurveyProfessionMapper surveyProfessionMapper = new SurveyProfessionMapper();

  private final SurveyFederalProgramMapper surveyFederalProgramMapper =
      new SurveyFederalProgramMapper();

  private final SurveyDisabilityMapper surveyDisabilityMapper = new SurveyDisabilityMapper();

  private final SurveyHousingServiceMapper surveyHousingServiceMapper =
      new SurveyHousingServiceMapper();

  @Override
  public List<SurveyHousingDto> getHousing() {
    return surveyHousingMapper.convertToDtoList(surveyHousingRepository.findAll());
  }

  @Override
  public List<SurveyHousingMaterialWallDto> getHousingMaterialWall() {
    return surveyHousingMaterialWallMapper.convertToDtoList(
        surveyHousingMaterialWallRepository.findAll());
  }

  @Override
  public List<SurveyHousingMaterialFloorDto> getHousingMaterialFloor() {
    return surveyHousingMaterialFloorMapper.convertToDtoList(
        surveyHousingMaterialFloorRepository.findAll());
  }

  @Override
  public List<SurveyHousingMaterialRoofDto> getHousingMaterialRoof() {
    return surveyHousingMaterialRoofMapper.convertToDtoList(
        surveyHousingMaterialRoofRepository.findAll());
  }

  @Override
  public List<SurveyHousingConditionDto> getHousingCondition() {
    return surveyHousingConditionMapper.convertToDtoList(
        surveyHousingConditionRepository.findAll());
  }

  @Override
  public List<SurveyHousingDocumentDto> getHousingDocument() {
    return surveyHousingDocumentMapper.convertToDtoList(surveyHousingDocumentRepository.findAll());
  }

  @Override
  public List<SurveyCulturalActivityDto> getSurveyCulturalActivity() {
    return surveyCulturalActivityMapper.convertToDtoList(
        surveyCulturalActivityRepository.findAll());
  }

  @Override
  public List<SurveyJobDto> getSurveyJob() {
    return surveyJobMapper.convertToDtoList(surveyJobRepository.findAll());
  }

  @Override
  public List<SurveyProfessionDto> getSurveyProfession() {
    return surveyProfessionMapper.convertToDtoList(surveyProfessionRepository.findAll());
  }

  @Override
  public List<SurveyFederalProgramDto> getSurveyFederalProgram() {
    return surveyFederalProgramMapper.convertToDtoList(surveyFederalProgramRepository.findAll());
  }

  @Override
  public List<SurveyDisabilityDto> getDisability() {
    return surveyDisabilityMapper.convertToDtoList(surveyDisabilityRepository.findAll());
  }

  @Override
  public List<SurveyFamilyRelationshipDto> getRelationship() {
    return surveyFamilyRelationshipMapper.convertToDtoList(
        surveyFamilyRelationshipRepository.findAll());
  }

  @Override
  public List<SurveyIndigenousLanguageDto> getAllIndigenousLanguages() {
    return indigenousLanguageMapper.convertToDtoList(indigenousLanguageRepository.findAll());
  }

  @Override
  public List<SurveyOriginOfDisabilityDto> getAllOriginsOfDisability() {
    return originOfDisabilityMapper.convertToDtoList(originOfDisabilityRepository.findAll());
  }

  @Override
  public List<SurveyDisabilityInformationDto> getAllDisabilityInformation() {
    return disabilityInformationMapper.convertToDtoList(disabilityInformationRepository.findAll());
  }

  @Override
  public List<SurveyTypeOfDisabilityDto> getAllTypesOfDisability() {
    return typeOfDisabilityMapper.convertToDtoList(typeOfDisabilityRepository.findAll());
  }

  @Override
  public List<SurveyCausesOfDisabilityDto> getAllCausesOfDisability() {
    return causesOfDisabilityMapper.convertToDtoList(causesOfDisabilityRepository.findAll());
  }

  @Override
  public List<SurveyTutorOrAssistantDto> getAllTutorsOrAssistants() {
    return tutorOrAssistantMapper.convertToDtoList(tutorOrAssistantRepository.findAll());
  }

  @Override
  public List<SurveyRequestingOrAuxiliaryDocumentDto> getAllRequestingOrAuxiliaryDocuments() {
    return requestingOrAuxiliaryDocumentMapper.convertToDtoList(
        requestingOrAuxiliaryDocumentRepository.findAll());
  }

  @Override
  public List<SurveyHousingServicesDto> getSurveyHousingService() {
    return surveyHousingServiceMapper.convertToDtoList(surveyHousingServiceRepository.findAll());
  }
}
