package com.electorama.msvc.followers.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "promoters")
public class PromotersEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String names;

  @NotBlank private String lastName;

  private String surName;

  private String email;

  @NotBlank private String cellPhoneNumber;

  @NotBlank private String street;

  private String interiorNumber;

  private String outdoorNumber;

  private String neighborhoodId;

  private String postalCode;

  private Integer municipalityId;

  @NotBlank private String curp;

  private String ine;

  private Integer vencimiento;

  private String notes;

  private String userCreateId;
  private String cordinatorZoneAuthId;
  // se cambia authId por promoter_auth_id
  private String promoterAuthId;
  private String referentAuthId;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  private Date deleted;

  private Integer seccionId;

  private Long followerId;

  private Integer followersGoal;

  @Column(nullable = false)
  private Boolean flagAlter = false;
}
