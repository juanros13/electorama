package com.electorama.msvc.documentmanagement.models.dto;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;

@Data
public class PositionDto {

  private Long id;
  private String name;
  private String positionKey;
  private Integer statusId;
  private DepartamentDto departament;

  private PositionTypeDto positionType;

  private Date created;
  private Date updated;
  private Date deleted;
}
