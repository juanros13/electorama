package com.electorama.msvc.followers.model.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "follower_mark")
public class FollowerMarkEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long followerId;
  private Integer markNumber;
  private Long politicalPartyId;
  private Long pollingBoothId;

  private Boolean markFlag;
  private Boolean isSynchronized;

  private String fullName;

  private Long municipalityId;
  private String municipality;
  private String federalDistrict;
  private String federalDistrictName;
  private String localDistrict;
  private String seccionNumber;
}
