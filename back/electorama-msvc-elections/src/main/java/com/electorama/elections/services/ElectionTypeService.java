package com.electorama.elections.services;

import com.electorama.elections.models.dto.ElectionTypeDto;
import java.util.List;

public interface ElectionTypeService {

  List<ElectionTypeDto> getAllTypes();
}
