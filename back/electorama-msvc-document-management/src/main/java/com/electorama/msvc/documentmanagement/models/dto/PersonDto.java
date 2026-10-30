package com.electorama.msvc.documentmanagement.models.dto;

import java.util.Date;
import lombok.Data;

@Data
public class PersonDto {

  private Long id;

  private String names;

  private String lastName;

  private String surName;

  private String email;

  private String address;

  private String cellPhoneNumber;

  private Date created;

  private Date updated;

  private Date deleted;
}
