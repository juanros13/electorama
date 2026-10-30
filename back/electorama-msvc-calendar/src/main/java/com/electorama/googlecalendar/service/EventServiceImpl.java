package com.electorama.googlecalendar.service;

import com.electorama.googlecalendar.exception.EntityNotFoundException;
import com.electorama.googlecalendar.model.dto.Event;
import com.electorama.googlecalendar.model.entity.EventEntity;
import com.electorama.googlecalendar.model.mapper.EventMapper;
import com.electorama.googlecalendar.model.repository.EventRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

  private final EventMapper eventMapper = new EventMapper();

  @Autowired private EventRepository eventRepository;

  @Override
  public List<Event> list(String authId) {
    return new ArrayList<>(eventMapper.convertToDto(eventRepository.findByAuthId(authId)));
  }

  @Override
  public Optional<Event> byId(Long id, String authId) {
    return Optional.of(
        eventMapper.convertToDto(
            eventRepository
                .findByIdAndAuthId(id, authId)
                .orElseThrow(EntityNotFoundException::new)));
  }

  @Override
  public Event save(Event event, String authId) {
    EventEntity eventEntity = eventMapper.convertToEntity(event);
    eventEntity.setAuthId(authId);
    return eventMapper.convertToDto(eventRepository.save(eventEntity));
  }

  @Override
  public void delete(Long id) {
    eventRepository.deleteById(id);
  }
}
