package com.electorama.msvc.users.model.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users_seccions")
public class UserSeccionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userAuthId;

  private String seccionNumber;

  private Long seccionGeoId;

  private Integer meta;

  private Date deleted;

  private Long municipalityId;
}
