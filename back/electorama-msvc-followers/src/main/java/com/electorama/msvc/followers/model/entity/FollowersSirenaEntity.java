package com.electorama.msvc.followers.model.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "followers_sirena")
public class FollowersSirenaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Date registerDate;
  private String registerTime;

  @Column(name = "usuario")
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

  private String names;
  private String lastName;
  private String surName;
  private String ine;
  private String curp;
  private String street;
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

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  private Date deleted;

  private Long promotersId;

  private Long followerId;
}
