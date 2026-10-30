package com.electorama.geography.model.mapper;

import com.electorama.geography.model.dto.State;
import com.electorama.geography.model.entity.StateEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

public class StateMapper extends BaseMapper<StateEntity, State> {

  @Override
  public StateEntity convertToEntity(State dto, Object... args) {
    StateEntity calendarEntity = new StateEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public State convertToDto(StateEntity entity, Object... args) {
    State calendar = new State();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }

  public Optional<State> convertToDtoOptional(StateEntity entity, Object... args) {
    Optional<State> calendar = Optional.of(new State());
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
