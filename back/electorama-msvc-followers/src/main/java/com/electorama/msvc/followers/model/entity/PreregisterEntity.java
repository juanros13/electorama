package com.electorama.msvc.followers.model.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;

@Data
@Entity
@Table(name = "preregister")
public class PreregisterEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @Temporal(TemporalType.TIMESTAMP)
  private Date deleted;

  private Integer areaIdentificator;
  private String userName;
  private String date;
  private String userLastName;
  private String userSecondLastName;
  private String userSex;
  private String userCurp;
  private String userOccupation;
  private Double userMonthlyIncome;
  private String userMunicipality;
  private String municipality;
  private String locality;
  private String userLocality;
  private String userStreet;
  private String userOutsideNumber;
  private String userInsideNumber;
  private String userZipCode;
  private String userPhone;
  private String userContactPhone;
  private String userEmail;
  private Boolean isSynchronized;
  private String latitude;
  private String longitude;
  private String uuid;
  private String deviceId;
  private String ineFront;
  private String ineBack;

  // farming data
  private String userHasUpp;
  private Double userUppNumber;
  private String userUppBiggerThan;
  private Double userUppMeasurement;
  private String userCommunity;
  private String userSubscribed;
  private Double userCattleAmount;

  // fishing data
  private String userAllowsWaterAccess;
  private String userWaterName;
  private Double userWaterDistance;
  private String userHasCrates;
  private Double userCratesAmount;
  private String userWaterMunicipality;
  private String userWaterLocality;

  // sembrando vidas
  private String userHasProductionUnit;
  private Double userAvailableHectares;
  private String userCommunalLand;
  private String userTypeOfProperty;
  private String userCivilStatus;
  private String userIndigenousEthnicity;
}
