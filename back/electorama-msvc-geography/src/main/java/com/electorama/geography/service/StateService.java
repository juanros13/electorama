package com.electorama.geography.service;

import com.electorama.geography.model.dto.State;
import java.util.List;
import java.util.Optional;

public interface StateService {

  Optional<State> byId(Long id);

  List<State> findAll();
}
