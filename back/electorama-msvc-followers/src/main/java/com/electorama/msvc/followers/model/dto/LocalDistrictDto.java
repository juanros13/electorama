package com.electorama.msvc.followers.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class LocalDistrictDto {
  private Long id;

  private Long goal;
  private Long progress;
  private Long localDistrict;
  private List<String> municipalityNames;
}
