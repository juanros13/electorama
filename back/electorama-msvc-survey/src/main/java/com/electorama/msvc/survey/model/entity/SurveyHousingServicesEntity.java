package com.electorama.msvc.survey.model.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "survey_housing_services")
public class SurveyHousingServicesEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ManyToMany(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
  @JoinTable(
      name = "survey_housing_service_list",
      joinColumns = @JoinColumn(name = "survey_housing_services_id"),
      inverseJoinColumns = @JoinColumn(name = "survey_id"))
  private List<SurveyEntity> surveyEntities;
}
