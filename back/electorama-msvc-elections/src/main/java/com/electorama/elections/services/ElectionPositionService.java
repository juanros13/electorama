package com.electorama.elections.services;

import com.electorama.elections.models.dto.ElectionPositionDto;
import java.util.List;

public interface ElectionPositionService {

  List<ElectionPositionDto> getAllPositions();
}
