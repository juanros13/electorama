package com.electorama.msvc.visits.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class VisitedDto {

  private Long id;

  @NotNull private Long municipalityId;

  private Long seccionId;

  private String coordinateZoneId;

  @NotNull private Long localityId;

  @NotNull private Date visitDate;

  private Integer numberOfVisited;

  private Date created;

  private Date updated;

  private Date deleted;

  @NotBlank private String userCreatedId;

  private List<VisitedPeopleDto> visitedPeople;

  private String referenceAuthId;
}
