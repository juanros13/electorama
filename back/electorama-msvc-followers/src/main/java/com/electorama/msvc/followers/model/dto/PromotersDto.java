package com.electorama.msvc.followers.model.dto;

import com.electorama.msvc.followers.model.rest.PromoterResponse;
import java.util.Date;
import lombok.Data;

@Data
public class PromotersDto {

  private Long id;

  private String names;

  private String lastName;

  private String surName;

  private String email;

  private String cellPhoneNumber;

  private String street;

  private String interiorNumber;

  private String outdoorNumber;

  private String neighborhoodId;

  private String postalCode;

  private Integer municipalityId;

  private String curp;

  private String ine;

  private Integer vencimiento;

  private String notes;

  private Date created;

  private Date updated;

  private Date deleted;

  private PromoterResponse user;

  private Integer seccionId;

  private Long followerSirenaId;

  private Long followerId;

  private String userCreateId;
  private String cordinatorZoneAuthId;
  // se cambia authId por promoter_auth_id
  private String promoterAuthId;
  private String referentAuthId;

  private Integer followersCount;

  private Integer followersGoal;

  private Boolean flagAlter;
}
