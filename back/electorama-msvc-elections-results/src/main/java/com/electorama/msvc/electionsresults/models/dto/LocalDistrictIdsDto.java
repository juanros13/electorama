package com.electorama.msvc.electionsresults.models.dto;

import java.util.List;
import lombok.Data;

@Data
public class LocalDistrictIdsDto {
  private List<Long> candidatesIds;
  private List<Long> localDistrictIds;
}
