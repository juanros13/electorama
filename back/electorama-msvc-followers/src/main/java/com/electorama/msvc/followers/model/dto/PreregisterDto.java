package com.electorama.msvc.followers.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class PreregisterDto {
  private Long id;
  private String date;
  private Date dateCreated;
  private Integer areaIdentificator;
  private String userName;
  private String userLastName;
  private String userSecondLastName;
  private String userSex;
  private String userCurp;
  private String userOccupation;
  private Double userMonthlyIncome;
  private String userMunicipality; // where the user lives
  private String municipality;
  private String locality;
  private String userLocality; // where the user lives
  private String userStreet;
  private String userOutsideNumber;
  private String userInsideNumber;
  private String userZipCode;
  private String userColony;
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
