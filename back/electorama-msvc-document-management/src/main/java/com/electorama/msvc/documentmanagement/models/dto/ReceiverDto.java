package com.electorama.msvc.documentmanagement.models.dto;

import java.util.Date;
import lombok.Data;

@Data
public class ReceiverDto {

  private Long id;

  private PositionPersonDto positionPerson;

  private Date created;

  private Date updated;

  private Date deleted;
}
