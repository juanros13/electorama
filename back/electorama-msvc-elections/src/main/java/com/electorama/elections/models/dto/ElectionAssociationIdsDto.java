package com.electorama.elections.models.dto;

import java.util.List;
import lombok.Data;

@Data
public class ElectionAssociationIdsDto {
  private List<Long> electionAssociationIds;
}
