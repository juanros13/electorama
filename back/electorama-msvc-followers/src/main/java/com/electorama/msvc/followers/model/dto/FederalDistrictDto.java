package com.electorama.msvc.followers.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class FederalDistrictDto {
  private Long id;

  private Long goal;
  private Long progress;
  private Long federalDistrict;
  private List<String> municipalityNames;
}
