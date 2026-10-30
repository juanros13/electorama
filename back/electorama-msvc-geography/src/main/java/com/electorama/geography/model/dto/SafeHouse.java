package com.electorama.geography.model.dto;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;

@Data
public class SafeHouse {

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

  private Date created;

  private Date updated;
}
