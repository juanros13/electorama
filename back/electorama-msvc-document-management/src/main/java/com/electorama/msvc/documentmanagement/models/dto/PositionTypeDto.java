package com.electorama.msvc.documentmanagement.models.dto;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;

@Data
public class PositionTypeDto {

  private Long id;
  private String name;

  private Date created;
  private Date updated;
  private Date deleted;
}
