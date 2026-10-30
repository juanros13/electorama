package com.electorama.msvc.visits.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "visited_people")
public class VisitedPeopleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String names;

  private String lastName;

  private String surName;

  private Integer adultsInHome;

  private String phone;

  private Boolean deliveredGazette;

  private String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "visited_id")
  @JsonIgnoreProperties({"visitedPeople", "hibernateLazyInitializer", "handler"})
  private VisitedEntity visitedEntity;

  private Date deleted;

  private Integer visitNumber;

  @NotNull private Boolean openTheDoor;

  @NotNull private String street;

  @NotNull private String externalNumber;

  @NotNull private String latitude;

  @NotNull private String longitude;

  private String uuid;

  private Boolean isRelated;

  private Integer ageOfThePersonWhoOpensTheDoor;

  private String sexOfTheOneWhoOpensTheDoor;
}
