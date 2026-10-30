package com.electorama.msvc.users.model.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String authId;
  private String authName;

  private String phone;

  private Long rejuntadoId;

  private String tokenGoogleCalendar;
  private String idGoogleCalendar;

  private String authIdReference;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  private Date deleted;

  private String curp;
  private String ine;
  private String names;
  private String lastName;
  private String surName;

  private Integer sex;

  private Date birthdate;
  private String cellPhoneNumberOther;
  private String status;
  private String email;
  private Integer birthEntityId;
  private String street;
  private String neighborhood;
  private String externalNumber;
  private String internalNumber;
  private String zipCode;
  private Integer residenceEntityId;
  private Integer residenceMunicipalityId;
  private String supportWhatsapp;

  private Boolean flagAlter = false;
}
