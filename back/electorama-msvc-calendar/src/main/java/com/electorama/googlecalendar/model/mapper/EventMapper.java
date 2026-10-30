package com.electorama.googlecalendar.model.mapper;

import com.electorama.googlecalendar.model.dto.Event;
import com.electorama.googlecalendar.model.entity.EventEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

public class EventMapper extends BaseMapper<EventEntity, Event> {

  @Override
  public EventEntity convertToEntity(Event dto, Object... args) {
    EventEntity eventEntity = new EventEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, eventEntity);
    }
    return eventEntity;
  }

  @Override
  public Event convertToDto(EventEntity entity, Object... args) {
    Event event = new Event();
    if (entity != null) {
      BeanUtils.copyProperties(entity, event);
    }
    return event;
  }

  public Optional<Event> convertToDtoOptional(EventEntity entity, Object... args) {
    Optional<Event> event = Optional.of(new Event());
    if (entity != null) {
      BeanUtils.copyProperties(entity, event);
    }
    return event;
  }
}
