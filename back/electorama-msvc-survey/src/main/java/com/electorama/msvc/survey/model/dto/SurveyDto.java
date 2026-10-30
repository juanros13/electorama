package com.electorama.msvc.survey.model.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class SurveyDto {

  private Long id;

  private SurveyUserFolioDto surveyUserFolio;

  private String folioHouse;
  private Timestamp surveyDate;
  private String latitude;
  private String longitude;

  private SurveyUserDto surveyUser;

  private String deviceId;
  // private String pathPhotoFacade;
  private String pathPhotoFacadeQr;
  private String pathPhotoIneAdverse;
  private String pathPhotoIneReverse;
  private String pathPhotoFacade;

  private Integer municipalityId;
  private String municipality;
  private Integer localityId;
  private String locality;
  private String localityOther;
  private String street;
  private String exteriorNumber;
  private String interiorNumber;
  private String postalCode;
  private String reference;

  private List<SurveyPersonDto> surveyPersonEntityList = new ArrayList<>();

  private String familyNumbers;

  private String housingOther;

  private SurveyHousingDto surveyHousing;

  private SurveyHousingConditionDto surveyHousingCondition;

  private SurveyHousingDocumentDto surveyHousingDocument;

  private SurveyHousingMaterialWallDto surveyHousingMaterialWall;

  private SurveyHousingMaterialRoofDto surveyHousingMaterialRoof;

  private SurveyHousingMaterialFloorDto surveyHousingMaterialFloor;

  private List<SurveyHousingServicesDto> surveyHousingServicesEntities;

  private String culturalActivityParticipation;
  private String knowledgeCulturalActivity;
  private SurveyCulturalActivityDto surveyCulturalActivity;
  private String otherTypeCulturalActivity;

  private Date created;

  private Date updated;

  private Date deleted;

  private Integer statusChange;

  private Long sectionId;

  private Long sectionNumber;
}
