package com.electorama.elections.models.dto;

import java.util.List;
import lombok.Data;

@Data
public class LocalDistrictIdsDto {
  private List<Long> localDistrictIds;
  private List<Long> candidatesIds;
}
