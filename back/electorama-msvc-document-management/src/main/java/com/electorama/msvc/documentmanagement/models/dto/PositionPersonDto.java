package com.electorama.msvc.documentmanagement.models.dto;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;

@Data
public class PositionPersonDto {

  private Long id;

  private PersonDto person;

  private PositionDto position;

  private Date created;
  private Date updated;
  private Date deleted;
}
