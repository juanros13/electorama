package com.electorama.msvc.survey.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "survey")
public class SurveyEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_user_folio_id", referencedColumnName = "id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyUserFolioEntity surveyUserFolio;

  private String folioHouse;
  private Timestamp surveyDate;
  private String latitude;
  private String longitude;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_user_id", referencedColumnName = "id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyUserEntity surveyUser;

  private String deviceId;
  private String pathPhotoIneAdverse;
  private String pathPhotoIneReverse;
  private String pathPhotoFacade;
  private String pathPhotoFacadeQr;

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

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "survey", cascade = CascadeType.ALL)
  @JsonIgnoreProperties(
      value = {"survey", "hibernateLazyInitializer", "handler"},
      allowSetters = true)
  private List<SurveyPersonEntity> surveyPersonEntityList = new ArrayList<>();

  private String familyNumbers;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_housing_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyHousingEntity surveyHousing;

  private String housingOther;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_housing_condition_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyHousingConditionEntity surveyHousingCondition;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_housing_document_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyHousingDocumentEntity surveyHousingDocument;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(
      name = "survey_housing_material_wall_id",
      referencedColumnName = "id",
      nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyHousingMaterialWallEntity surveyHousingMaterialWall;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(
      name = "survey_housing_material_roof_id",
      referencedColumnName = "id",
      nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyHousingMaterialRoofEntity surveyHousingMaterialRoof;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(
      name = "survey_housing_material_floor_id",
      referencedColumnName = "id",
      nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyHousingMaterialFloorEntity surveyHousingMaterialFloor;

  @ManyToMany(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE})
  @JoinTable(
      name = "survey_housing_service_list",
      joinColumns = @JoinColumn(name = "survey_id"),
      inverseJoinColumns = @JoinColumn(name = "survey_housing_services_id"))
  private List<SurveyHousingServicesEntity> surveyHousingServicesEntities;

  private String culturalActivityParticipation;
  private String knowledgeCulturalActivity;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "type_cultural_activity_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyCulturalActivityEntity surveyCulturalActivity;

  private String otherTypeCulturalActivity;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  @Temporal(TemporalType.TIMESTAMP)
  private Date deleted;

  private Integer statusChange;

  private Long sectionId;

  private Long sectionNumber;

  public void addSurveyPerson(SurveyPersonEntity surveyPersonEntity) {
    surveyPersonEntityList.add(surveyPersonEntity);
    surveyPersonEntity.setSurvey(this);
  }

  public void removeSurveyPerson(SurveyPersonEntity surveyPersonEntity) {
    surveyPersonEntityList.remove(surveyPersonEntity);
    surveyPersonEntity.setSurvey(null);
  }
}
