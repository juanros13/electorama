package com.electorama.msvc.electionsresults.models.dto;

import java.util.List;
import lombok.Data;

@Data
public class FederalDistrictIdsDto {
  private List<Long> candidatesIds;
  private List<Long> federalDistrictIds;
}
