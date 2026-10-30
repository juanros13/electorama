package com.electorama.msvc.visits.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class DatesRequestDto {

  private Date startDate;
  private Date endDate;
}
