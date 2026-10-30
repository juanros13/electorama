package com.electorama.msvc.followers.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class DatesRequestDto {

  private Date startDate;
  private Date endDate;
}
