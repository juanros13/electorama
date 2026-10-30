package com.electorama.msvc.followers.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "followers")
public class FollowersEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String names;

  private String lastName;

  private String surName;

  private String curp;

  private String ine;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "cat_sex", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private CatSexEntity catSex;

  private Date birthdate;

  private String cellPhoneNumber;

  private String cellPhoneNumberOther;

  private String status;

  private String email;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "cat_marital_status", referencedColumnName = "id", nullable = true)
  private CatMaritalStatusEntity catMaritalStatus;

  private Integer birthEntityId;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "cat_family_role", referencedColumnName = "id", nullable = true)
  private CatFamilyRoleEntity catFamilyRole;

  private String sectionNumber;

  private String userCreateId;

  private String promoterAuthId;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  private Date deleted;

  private Integer validity;

  private Boolean isAdult;

  private Boolean isInSync;

  private String street;

  private String neighborhood;

  private String externalNumber;

  private String internalNumber;

  private String zipCode;

  private Integer residenceEntityId;

  private Integer residenceMunicipalityId;

  private String supportWhatsapp;

  private String supportsMovement;

  private String committeeParticipation;

  private String pollinStationRepresentativeParticipation;

  private String invitingOthersToParticipate;

  private String numberOfPeopleInvited;

  private String longitude;

  private String latitude;

  private String uuid;

  private String deviceId;

  private String sectionNumberIne;

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
