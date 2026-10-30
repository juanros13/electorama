package com.electorama.msvc.survey.model.dto;

import lombok.Data;

@Data
public class CsvDto {

  private String password;
  private String email;
  private String firstName;
  private String lastName;
  private String deviceId;
  private String municipalityName;
  private int municipalityId;

  // Constructor con todos los campos
  public CsvDto(
      String password,
      String email,
      String firstName,
      String lastName,
      String deviceId,
      String municipalityName,
      int municipalityId) {
    this.password = password;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.deviceId = deviceId;
    this.municipalityName = municipalityName;
    this.municipalityId = municipalityId;
  }
}
