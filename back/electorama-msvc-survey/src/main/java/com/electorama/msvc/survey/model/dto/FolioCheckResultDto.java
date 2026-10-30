package com.electorama.msvc.survey.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class FolioCheckResultDto {

  private List<Integer> foundFolios;
  private List<Integer> notFoundFolios;
}
