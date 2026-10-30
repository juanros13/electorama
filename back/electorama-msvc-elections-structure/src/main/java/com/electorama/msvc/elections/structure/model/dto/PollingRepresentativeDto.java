package com.electorama.msvc.elections.structure.model.dto;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;

@Data
public class PollingRepresentativeDto {

  private Long id;

  private String names;

  private String lastName;

  private String surName;

  private String curp;

  private String ine;

  private CatSexDto catSex;

  private Date birthdate;

  private String cellPhoneNumber;

  private String cellPhoneNumberOther;

  private String status;

  private String email;

  private String userCreateId;

  private Date created;

  private Date updated;

  private Date deleted;

  private Integer validity;

  private String street;

  private String neighborhood;

  private String externalNumber;

  private String internalNumber;

  private String zipCode;

  private Integer residenceEntityId;

  private Integer residenceMunicipalityId;

  private String supportWhatsapp;

  private Long pollingBoothId;

  private String pollingBoothDescription;

  private String sectionId;

  private String sectionNumber;

  private String authId;

  private PollingRepresentativeTypeDto pollingRepresentativeType;
}
