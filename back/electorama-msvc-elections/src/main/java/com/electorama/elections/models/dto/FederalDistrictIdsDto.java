package com.electorama.elections.models.dto;

import java.util.List;
import lombok.Data;

@Data
public class FederalDistrictIdsDto {
  private List<Long> federalDistrictIds;
  private List<Long> candidatesIds;
}
