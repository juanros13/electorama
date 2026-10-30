package com.electorama.googlecalendar.service;

import com.electorama.googlecalendar.exception.EntityNotFoundException;
import com.electorama.googlecalendar.model.dto.Calendar;
import com.electorama.googlecalendar.model.entity.CalendarEntity;
import com.electorama.googlecalendar.model.mapper.CalendarMapper;
import com.electorama.googlecalendar.model.repository.CalendarRepository;
import com.electorama.googlecalendar.model.repository.EventRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarServiceImpl implements CalendarService {

  private final CalendarMapper calendarMapper = new CalendarMapper();

  @Autowired private CalendarRepository calendarRepository;

  @Autowired private EventRepository eventRepository;

  @Override
  public List<Calendar> list(String authId) {
    return new ArrayList<>(calendarMapper.convertToDto(calendarRepository.findByAuthId(authId)));
  }

  @Override
  public Optional<Calendar> byId(Long id, String authId) {
    return Optional.of(
        calendarMapper.convertToDto(
            calendarRepository
                .findByIdAndAuthId(id, authId)
                .orElseThrow(EntityNotFoundException::new)));
  }

  @Override
  public Calendar save(Calendar calendar, String authId) {
    CalendarEntity calendarEntity = calendarMapper.convertToEntity(calendar);
    calendarEntity.setAuthId(authId);
    return calendarMapper.convertToDto(calendarRepository.save(calendarEntity));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    eventRepository.deleteAllByCalendarId(id.intValue());
    calendarRepository.deleteById(id);
  }
}
