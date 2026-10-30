package com.electorama.geography.model.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "geo_safe_houses")
public class SafeHouseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String alias;

  private String reference;

  private String managerName;

  private String managerPhone;

  private Long seccionId;

  private Long municipalityId;

  private String latitude;

  private String longitude;

  private String neighborhood;

  private String street;

  private String externalNumber;

  private String internalNumber;

  private String zipCode;

  private String userCreateId;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;
}
