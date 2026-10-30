package com.electorama.msvc.users.model.dto;

import java.util.Date;
import lombok.Data;
import lombok.ToString;

@Data
public class User {
  private Long id;

  private String email;

  // Nunca debe aparecer en logs (este DTO se loguea via toString en varios controllers).
  @ToString.Exclude private String password;

  private String authId;

  private Boolean enabled;

  private String firstName;

  private String lastName;

  private String avatar;

  private String authIdReference;

  private Integer meta;

  private Long userSeccionId;

  private Long municipalityId;

  private Date deleted;

  private String curp;
  private String ine;
  private String names;
  private String surName;
  private Integer Sex;

  private Date birthdate;
  private String phone;
  private String cellPhoneNumberOther;
  private String status;
  private Integer birthEntityId;
  private String street;
  private String neighborhood;
  private String externalNumber;
  private String internalNumber;
  private String zipCode;
  private Integer residenceEntityId;
  private Integer residenceMunicipalityId;
  private String supportWhatsapp;
  private String authName;

  private Boolean flagAlter;
}
