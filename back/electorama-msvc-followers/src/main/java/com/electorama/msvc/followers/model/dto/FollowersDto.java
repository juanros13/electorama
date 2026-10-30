package com.electorama.msvc.followers.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class FollowersDto {

  private Long id;

  private String names;

  private String lastName;

  private String surName;

  private String curp;

  private String ine;

  private CatSexDto catSex;

  private Date birthdate;

  private String cellPhoneNumber;

  private String status;

  private String email;

  private CatMaritalStatusDto catMaritalStatus;

  private Integer birthEntityId;

  private CatFamilyRoleDto catFamilyRole;

  private String sectionNumber;

  private String userCreateId;
  private String authId;

  private Date deleted;
  private Date created;
  private Date updated;

  private Integer validity;

  private Boolean isAdult;

  private String cellPhoneNumberOther;

  private String sectionNumberIne;

  private Boolean isInSync;

  private String internalNumber;

  private String zipCode;

  private String neighborhood;

  private Integer residenceEntityId;

  private Integer residenceMunicipalityId;

  private String supportWhatsapp;

  private String supportsMovement;

  private String committeeParticipation;

  private String pollinStationRepresentativeParticipation;

  private String invitingOthersToParticipate;

  private String street;

  private String externalNumber;

  private String numberOfPeopleInvited;

  private String longitude;

  private String latitude;

  private String uuid;

  private String deviceId;

  private Long followerSirenaId;

  private String promoterAuthId;

  private String promoterName;

  private String referentName;
  private String referentAuthId;
  private String coordinatorZoneAuthId;
  private String coordinatorName;
  private String promoterAltAuthId;

  private Boolean voteFlag;

  private Date voteFlagTimestamp;

  private Long sectionId;

  private Long federalDistrictId;

  private Long localDistrictId;

  private String localDistrictName;

  private String federalDistrictName;

  private Long municipalityId;

  private String municipalityName;
}
