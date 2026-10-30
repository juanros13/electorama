package com.electorama.googlecalendar.model.repository;

import com.electorama.googlecalendar.model.entity.EventEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

  List<EventEntity> findByAuthId(String authId);

  Optional<EventEntity> findByIdAndAuthId(Long id, String authId);

  void deleteAllByCalendarId(Integer calendarId);
}
