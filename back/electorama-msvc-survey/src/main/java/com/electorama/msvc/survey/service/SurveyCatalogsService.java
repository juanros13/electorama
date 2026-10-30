package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.*;
import java.util.List;

public interface SurveyCatalogsService {

  List<SurveyHousingDto> getHousing();

  List<SurveyHousingMaterialWallDto> getHousingMaterialWall();

  List<SurveyHousingMaterialFloorDto> getHousingMaterialFloor();

  List<SurveyHousingMaterialRoofDto> getHousingMaterialRoof();

  List<SurveyHousingConditionDto> getHousingCondition();

  List<SurveyHousingDocumentDto> getHousingDocument();

  List<SurveyCulturalActivityDto> getSurveyCulturalActivity();

  List<SurveyJobDto> getSurveyJob();

  List<SurveyProfessionDto> getSurveyProfession();

  List<SurveyFederalProgramDto> getSurveyFederalProgram();

  List<SurveyDisabilityDto> getDisability();

  List<SurveyFamilyRelationshipDto> getRelationship();

  List<SurveyIndigenousLanguageDto> getAllIndigenousLanguages();

  List<SurveyOriginOfDisabilityDto> getAllOriginsOfDisability();

  List<SurveyDisabilityInformationDto> getAllDisabilityInformation();

  List<SurveyTypeOfDisabilityDto> getAllTypesOfDisability();

  List<SurveyCausesOfDisabilityDto> getAllCausesOfDisability();

  List<SurveyTutorOrAssistantDto> getAllTutorsOrAssistants();

  List<SurveyRequestingOrAuxiliaryDocumentDto> getAllRequestingOrAuxiliaryDocuments();

  List<SurveyHousingServicesDto> getSurveyHousingService();
}
