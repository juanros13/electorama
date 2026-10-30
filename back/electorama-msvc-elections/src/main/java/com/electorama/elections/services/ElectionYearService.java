package com.electorama.elections.services;

import com.electorama.elections.models.dto.ElectionYearDto;
import java.util.List;

public interface ElectionYearService {

  List<ElectionYearDto> getAllYears();
}
