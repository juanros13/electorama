package com.electorama.msvc.followers.model.rest;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * Un convencido tal como lo sirve msvc-structure, con su cadena de mando resuelta.
 *
 * <p>Los nombres de los campos son los de la respuesta: el modelo nuevo es el dueño del dato y este
 * módulo lo traduce a su DTO, no al contrario.
 */
@Getter
@Setter
public class StructureFollowerResponse {

  private Long id;
  private String names;
  private String lastName;
  private String surName;
  private String curp;
  private String ine;
  private Date birthdate;
  private String cellPhoneNumber;
  private String cellPhoneNumberOther;
  private String email;
  private String status;
  private Date created;
  private Date updated;
  private Date deleted;
  private String street;
  private String externalNumber;
  private String internalNumber;
  private String neighborhood;
  private String zipCode;
  private Integer birthEntityId;
  private Integer residenceEntityId;
  private Integer residenceMunicipalityId;
  private Long catSex;
  private Long catMaritalStatus;
  private Long catFamilyRole;
  private Boolean isAdult;
  private Boolean isInSync;
  private Integer validity;
  private String sectionNumberIne;
  private String supportWhatsapp;
  private String supportsMovement;
  private String committeeParticipation;
  private String pollinStationRepresentativeParticipation;
  private String invitingOthersToParticipate;
  private String numberOfPeopleInvited;
  private String latitude;
  private String longitude;
  private String uuid;
  private String deviceId;
  private String userCreateId;
  private String authId;
  private String promoterAuthId;
  private String promoterAltAuthId;
  private String promoterName;
  private String coordinatorZoneAuthId;
  private String coordinatorName;
  private String referentAuthId;
  private String referentName;
  private Boolean voteFlag;
  private Date voteFlagTimestamp;
  private String sectionNumber;
  private Long sectionId;
  private Long municipalityId;
  private Long federalDistrictId;
  private Long localDistrictId;
  private Long positionId;
}
