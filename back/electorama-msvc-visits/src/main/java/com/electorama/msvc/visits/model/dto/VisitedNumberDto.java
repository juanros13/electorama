package com.electorama.msvc.visits.model.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Data;

@Data
public class VisitedNumberDto {
  private Long id;

  @NotNull private Long visitedPeopleId;

  @NotNull private Integer visitedNumber;

  private Date created;
  private Date updated;
  private Date deleted;
}
