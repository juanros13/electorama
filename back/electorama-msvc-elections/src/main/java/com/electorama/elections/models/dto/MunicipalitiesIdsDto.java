package com.electorama.elections.models.dto;

import java.util.List;
import lombok.Data;

@Data
public class MunicipalitiesIdsDto {
  private List<Long> candidatesIds;
  private List<Long> municipalitiesIds;
}
