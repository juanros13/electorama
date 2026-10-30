package com.electorama.msvc.survey.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "survey_user_section")
public class SurveyUserSectionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long surveyUserId;
  private Integer sectionId;
  private Integer sectionNumber;
  private Boolean isOwner;

  public Boolean getIsOwner() {
    return isOwner != null ? isOwner : Boolean.FALSE;
  }

  public void setIsOwner(Boolean isOwner) {
    this.isOwner = isOwner;
  }
}
