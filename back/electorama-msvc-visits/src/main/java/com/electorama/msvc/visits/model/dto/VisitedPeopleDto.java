package com.electorama.msvc.visits.model.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Data;

@Data
public class VisitedPeopleDto {
  private Long id;
  private String names;

  private String lastName;

  private String surName;

  private Integer adultsInHome;

  private String phone;

  private Integer seccionNumber;

  private Boolean deliveredGazette;

  private String notes;

  private VisitedDto visitedEntity;

  private Date deleted;

  private Integer visitNumber;

  @NotNull private Boolean openTheDoor;

  @NotNull private String street;

  @NotNull private String externalNumber;

  @NotNull private String latitude;

  private String longitude;

  @NotNull private String uuid;

  private Boolean isRelated;

  private Integer ageOfThePersonWhoOpensTheDoor;

  private String sexOfTheOneWhoOpensTheDoor;
}
