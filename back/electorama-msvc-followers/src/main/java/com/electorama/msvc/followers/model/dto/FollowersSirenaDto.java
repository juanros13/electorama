package com.electorama.msvc.followers.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import lombok.Data;

@Data
public class FollowersSirenaDto {

  private Long id;

  private Date registerDate;
  private String registerTime;
  private String user;
  private Integer entity;
  private Integer electoralDistrict;
  private String municipalityName;
  private Integer seccionNumber;

  private String supports_movement;
  private String committee_participation;
  private String pollin_station_representative_participation;
  private String inviting_others_to_participate;
  private String number_of_people_invited;
  private String is_adult;

  @NotBlank private String names;
  @NotBlank private String lastName;
  private String surName;
  private String ine;
  @NotBlank private String curp;
  @NotBlank private String street;
  private String externalNumber;
  private String internalNumber;
  private String postalCode;
  private String neighborhoodId;
  private String phone;
  private String supportWhatsapp;
  private String cellPhoneNumberOther;
  private String email;

  private String latitude;
  private String longitude;

  private Integer seccionId;
  private Integer municipalityId;

  private Long promotersId;
  private Long followerId;
}
