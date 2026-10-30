package com.electorama.geography.service;

import com.electorama.geography.exception.EntityNotFoundException;
import com.electorama.geography.model.dto.State;
import com.electorama.geography.model.mapper.StateMapper;
import com.electorama.geography.model.repository.StateRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StateServiceImpl implements StateService {

  @Autowired private StateRepository stateRepositor;

  private final StateMapper stateMapper = new StateMapper();

  @Override
  public Optional<State> byId(Long id) {
    return Optional.of(
        stateMapper.convertToDto(
            stateRepositor.findById(id).orElseThrow(EntityNotFoundException::new)));
  }

  @Override
  public List<State> findAll() {
    return (List<State>) stateMapper.convertToDto(stateRepositor.findAll());
  }
}
