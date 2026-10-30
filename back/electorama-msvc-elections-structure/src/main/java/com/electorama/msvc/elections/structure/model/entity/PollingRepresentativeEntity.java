package com.electorama.msvc.elections.structure.model.entity;

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
@Table(name = "pollings_representatives")
public class PollingRepresentativeEntity {

  private static final long serialVersionUID = 1L;

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

  private String userCreateId;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
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

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "polling_representative_type_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PollingRepresentativeTypeEntity pollingRepresentativeType;
}
