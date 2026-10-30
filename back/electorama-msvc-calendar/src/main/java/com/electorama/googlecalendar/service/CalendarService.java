package com.electorama.googlecalendar.service;

import com.electorama.googlecalendar.model.dto.Calendar;
import java.util.List;
import java.util.Optional;

public interface CalendarService {

  List<Calendar> list(String authId);

  Optional<Calendar> byId(Long id, String authId);

  Calendar save(Calendar calendar, String authId);

  void delete(Long id);
}
